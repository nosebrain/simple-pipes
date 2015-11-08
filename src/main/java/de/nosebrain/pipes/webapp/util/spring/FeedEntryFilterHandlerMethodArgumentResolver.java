package de.nosebrain.pipes.webapp.util.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import de.nosebrain.pipes.filter.AuthorFilter;
import de.nosebrain.pipes.filter.CategoryFilter;
import de.nosebrain.pipes.filter.FeedEntryFilter;
import de.nosebrain.pipes.filter.NegatingFilter;
import de.nosebrain.pipes.filter.TitleFilter;

public class FeedEntryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
  private static final String NEGATE_CHAR = "!";
  
  private static final Map<String, FeedEntryFilterBuilder> BUILDER_MAP = new HashMap<>();
  
  static {
    BUILDER_MAP.put("filters.title", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter, final ConversionService conversionService) {
        return new TitleFilter(parameter);
      }
    });
    
    BUILDER_MAP.put("filters.author", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter, final ConversionService conversionService) {
        return new AuthorFilter(parameter);
      }
    });
    
    BUILDER_MAP.put("filters.categories", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter, final ConversionService conversionService) {
        @SuppressWarnings("unchecked") // ok
        final Set<String> categories = conversionService.convert(parameter, Set.class);
        return new CategoryFilter(categories);
      }
    });
  }
  
  static interface FeedEntryFilterBuilder {
    public FeedEntryFilter buildFilter(String parameter, ConversionService conversionService);
  }
  
  @Override
  public boolean supportsParameter(final MethodParameter parameter) {
    final Class<?> parameterType = parameter.getParameterType();
    
    if ((parameterType != null) && Collection.class.isAssignableFrom(parameterType)) {
      final Type genericParameterType = parameter.getGenericParameterType();
      if (genericParameterType instanceof ParameterizedType) {
        final ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if ((actualTypeArguments != null) && (actualTypeArguments.length > 0)) {
          return actualTypeArguments[0].equals(FeedEntryFilter.class);
        }
      }
    }
    return false;
  }

  @Override
  public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
    final WebDataBinder binder = binderFactory.createBinder(webRequest, null, null);
    final ConversionService conversionService = binder.getConversionService();
    
    final Set<FeedEntryFilter> filters = new HashSet<>();
    
    for (final Entry<String, FeedEntryFilterBuilder> feedEntryFilter : BUILDER_MAP.entrySet()) {
      final FeedEntryFilterBuilder builder = feedEntryFilter.getValue();
      final String parameterKey = feedEntryFilter.getKey();
      
      final FeedEntryFilter filter = buildFilter(webRequest, conversionService, parameterKey, builder);
      if (filter != null) {
        filters.add(filter);
      }
    }
    
    return filters;
  }
  
  private static FeedEntryFilter buildFilter(final NativeWebRequest webRequest, final ConversionService conversionService, final String key, final FeedEntryFilterBuilder feedEntryFilterBuilder) {
    final String[] categoriesArray = webRequest.getParameterValues(key);
    String parameter = conversionService.convert(categoriesArray, String.class);
    if ((parameter != null) && !parameter.isEmpty()) {
      if (parameter.startsWith(NEGATE_CHAR)) {
        parameter = parameter.substring(1);
        return new NegatingFilter(feedEntryFilterBuilder.buildFilter(parameter, conversionService));
      }
      
      return feedEntryFilterBuilder.buildFilter(parameter, conversionService);
    }
    
    return null;
  }

}
