package de.nosebrain.pipes.webapp.util.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import de.nosebrain.pipes.filter.CategoryFilter;
import de.nosebrain.pipes.filter.FeedEntryFilter;
import de.nosebrain.pipes.filter.NegatingFilter;
import de.nosebrain.pipes.filter.TitleFilter;

public class FeedEntryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
  private static final String NEGATE_CHAR = "!";
  
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
    final String[] categoriesArray = webRequest.getParameterValues("filters.categories");
    final String categoryParameter = conversionService.convert(categoriesArray, String.class);
    final FeedEntryFilter categoryFilter = getCategoryFilter(conversionService, categoryParameter);
    if (categoryFilter != null) {
      filters.add(categoryFilter);
    }
    
    final String titleParameter = webRequest.getParameter("filters.title");
    final FeedEntryFilter titleFilter = getTitleFilter(titleParameter);
    if (titleFilter != null) {
      filters.add(titleFilter);
    }
    
    return filters;
  }
  
  private static FeedEntryFilter getTitleFilter(String titleParameter) {
    if ((titleParameter != null) && !titleParameter.isEmpty()) {
      if (titleParameter.startsWith(NEGATE_CHAR)) {
        titleParameter = titleParameter.substring(1);
        return new NegatingFilter(buildTitleFilter(titleParameter));
      }
      return buildTitleFilter(titleParameter);
    }
    
    return null;
  }
  
  private static FeedEntryFilter buildTitleFilter(final String titleParameter) {
    return new TitleFilter(titleParameter);
  }

  private static FeedEntryFilter getCategoryFilter(final ConversionService conversionService, String categoryParameter) {
    if ((categoryParameter != null) && !categoryParameter.isEmpty()) {
      if (categoryParameter.startsWith(NEGATE_CHAR)) {
        categoryParameter = categoryParameter.substring(1);
        return buildCategoryFilter(conversionService, categoryParameter);
      }
      return buildCategoryFilter(conversionService, categoryParameter);
    }
    
    return null;
  }
  
  private static FeedEntryFilter buildCategoryFilter(final ConversionService conversionService, final String categoryParameter) {
    @SuppressWarnings("unchecked") // ok
    final Set<String> categories = conversionService.convert(categoryParameter, Set.class);
    final CategoryFilter categoryFilter = new CategoryFilter(categories);
    return categoryFilter;
  }

}
