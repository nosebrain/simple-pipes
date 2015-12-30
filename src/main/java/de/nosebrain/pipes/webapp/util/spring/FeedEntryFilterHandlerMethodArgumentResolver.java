package de.nosebrain.pipes.webapp.util.spring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import de.nosebrain.pipes.FilterGrammarLexer;
import de.nosebrain.pipes.FilterGrammarParser;
import de.nosebrain.pipes.FilterGrammarParser.Bool_opContext;
import de.nosebrain.pipes.FilterGrammarParser.ExpressionContext;
import de.nosebrain.pipes.FilterGrammarParser.FactorContext;
import de.nosebrain.pipes.FilterGrammarParser.VariableContext;
import de.nosebrain.pipes.filter.AndFilter;
import de.nosebrain.pipes.filter.AuthorFilter;
import de.nosebrain.pipes.filter.BooleanFilter;
import de.nosebrain.pipes.filter.CategoryFilter;
import de.nosebrain.pipes.filter.FeedEntryFilter;
import de.nosebrain.pipes.filter.NegatingFilter;
import de.nosebrain.pipes.filter.OrFilter;
import de.nosebrain.pipes.filter.TitleContainsFilter;

public class FeedEntryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
  private static final Map<String, FeedEntryFilterBuilder> BUILDER_MAP = new HashMap<>();
  
  static {
    BUILDER_MAP.put("title", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter) {
        return new TitleContainsFilter(parameter);
      }
    });
    
    BUILDER_MAP.put("author", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter) {
        return new AuthorFilter(parameter);
      }
    });
    
    BUILDER_MAP.put("category", new FeedEntryFilterBuilder() {
      
      @Override
      public FeedEntryFilter buildFilter(final String parameter) {
        return new CategoryFilter(parameter);
      }
    });
  }
  
  static interface FeedEntryFilterBuilder {
    public FeedEntryFilter buildFilter(String parameter);
  }
  
  @Override
  public boolean supportsParameter(final MethodParameter parameter) {
    final Class<?> parameterType = parameter.getParameterType();
    return (parameterType != null) && parameterType.isAssignableFrom(FeedEntryFilter.class);
  }

  @Override
  public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
    final FilterGrammarLexer lexer = new FilterGrammarLexer(new ANTLRInputStream(webRequest.getParameter("filter")));
    
    // Get a list of matched tokens
    final CommonTokenStream tokens = new CommonTokenStream(lexer);
    
    // Pass the tokens to the parser
    final FilterGrammarParser parser = new FilterGrammarParser(tokens);
    
    final ExpressionContext expression = parser.expression();
    return convertExpression(expression);
  }
  
  private static FeedEntryFilter convertFactor(final FactorContext factorContext) {
    final VariableContext variableContext = factorContext.variable();
    if (variableContext == null) {
      return convertExpression(factorContext.expression());
    }
    
    final String feedEntryName = variableContext.getText();
    
    final FeedEntryFilterBuilder builder = BUILDER_MAP.get(feedEntryName);
    final String value = factorContext.operand().getText();
    FeedEntryFilter filter = builder.buildFilter(value);
    if (factorContext.operator().NEQ() != null) {
      filter = new NegatingFilter(filter);
    }
    
    return filter;
  }


  private static FeedEntryFilter convertExpression(final ExpressionContext expression) {
    final Iterator<FactorContext> factorIterator = expression.factor().iterator();
    final Iterator<Bool_opContext> operatorIterator = expression.bool_op().iterator();
    FeedEntryFilter filterToReturn = null;
    FeedEntryFilter previousFilter = null;
    BooleanFilter booleanFilter = null;
    
    if (factorIterator.hasNext()) {
      final FactorContext factorContext = factorIterator.next();
      final FeedEntryFilter factorFilter = convertFactor(factorContext);
      filterToReturn = factorFilter;
      previousFilter = factorFilter;
    }
    
    while (factorIterator.hasNext()) {
      final Bool_opContext operatorContext = operatorIterator.next();
      if (operatorContext.AND() != null) {
        if ((booleanFilter == null) || !(booleanFilter instanceof AndFilter)) {
          final BooleanFilter newFilter = new AndFilter();
          if (booleanFilter != null) {
            newFilter.appendFilter(booleanFilter);
          }
          
          if (previousFilter != null) {
            newFilter.appendFilter(previousFilter);
            previousFilter = null;
          }
          booleanFilter = newFilter;
          filterToReturn = booleanFilter;
        }
      } else {
        if ((booleanFilter == null) || !(booleanFilter instanceof OrFilter)) {
          final BooleanFilter newFilter = new OrFilter();
          if (booleanFilter != null) {
            newFilter.appendFilter(booleanFilter);
          }
          
          if (previousFilter != null) {
            newFilter.appendFilter(previousFilter);
            previousFilter = null;
          }
          
          booleanFilter = newFilter;
          filterToReturn = booleanFilter;
        }
      }
      
      final FactorContext factorContext = factorIterator.next();
      final FeedEntryFilter factorFilter = convertFactor(factorContext);
      
      booleanFilter.appendFilter(factorFilter);
    }
    
    return filterToReturn;
  }

}
