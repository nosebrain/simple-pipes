package de.nosebrain.pipes.webapp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import de.nosebrain.pipes.webapp.controller.GeneralController;
import de.nosebrain.pipes.webapp.util.spring.FeedEntryFilterHandlerMethodArgumentResolver;

@Configuration
@ComponentScan(basePackageClasses = { GeneralController.class })
@PropertySource({"classpath:simple-pipe.properties", "file:${catalina.home}/conf/simple-pipes/simple-pipes.properties"})
public class SimplePipesConfig extends WebMvcConfigurationSupport {
  
  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
    super.addArgumentResolvers(argumentResolvers);
    argumentResolvers.add(new FeedEntryFilterHandlerMethodArgumentResolver());
  }
  
  @Bean
  @Override
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    final RequestMappingHandlerMapping requestMappingHandlerMapping = super.requestMappingHandlerMapping();
    requestMappingHandlerMapping.getUrlPathHelper().setUrlDecode(false);
    requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
    return requestMappingHandlerMapping;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
