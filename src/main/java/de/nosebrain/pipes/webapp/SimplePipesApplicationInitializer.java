package de.nosebrain.pipes.webapp;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import de.nosebrain.pipes.webapp.config.SimplePipesConfig;

public class SimplePipesApplicationInitializer implements WebApplicationInitializer {

  public static final String SERVLET_NAME = "simple-pipes";

  @Override
  public void onStartup(final ServletContext servletContext) {
    final WebApplicationContext context = createContext();
    
    // the character encoding filter
    final CharacterEncodingFilter filter = new CharacterEncodingFilter();
    filter.setEncoding("UTF-8");
    servletContext.addFilter("encodingFilter", filter);
    
    servletContext.addListener(new ContextLoaderListener(context));
    final ServletRegistration.Dynamic dispatcher = servletContext.addServlet(SERVLET_NAME, new DispatcherServlet(context));
    dispatcher.setLoadOnStartup(1);
    dispatcher.setAsyncSupported(true);
    dispatcher.addMapping("/*");
  }
  
  private static AnnotationConfigWebApplicationContext createContext() {
    final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    context.setConfigLocation(SimplePipesConfig.class.getPackage().getName());
    return context;
  }

}