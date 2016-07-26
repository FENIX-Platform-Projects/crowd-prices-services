package org.fao.crowd.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@WebListener
@ApplicationScoped
public class Init implements ServletContextListener {
    private static HashMap<String,String> initProperties = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        for (String parameterName : Collections.list(context.getInitParameterNames()))
            initProperties.put(parameterName, context.getInitParameter(parameterName));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public String getProperty(String name) {
        return initProperties.get(name);
    }

    public Collection<String> getPropertiesName () {
        return initProperties.keySet();
    }

}
