package main.java.controllers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import main.java.annotations.Controller;
import main.java.annotations.UrlMapping;
import main.java.model.HttpMethod;
import main.java.model.Mapping;
import main.java.model.UrlMethod;
import main.java.utils.ScanAnnotation;
import main.java.utils.UtilAnalyser;

public class FrontControllerListener implements ServletContextListener {
        
    

@Override
public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    
    System.out.println("=== FrontControllerListener démarré ===");
    
    try {
        String scanPackage = context.getInitParameter("scanPackage");
        System.out.println("scanPackage lu = " + scanPackage);

        if (scanPackage == null || scanPackage.isEmpty()) {
            System.out.println("ERREUR: scanPackage est vide ou null");
            return;
        }

        List<Class<?>> allClasses = UtilAnalyser.findClasses(scanPackage);
        System.out.println("Nombre de classes trouvées : " + allClasses.size());

        List<Class<?>> annotatedClasses = ScanAnnotation.findAnnotatedClasses(allClasses, Controller.class);
        System.out.println("Nombre de @Controller trouvés : " + annotatedClasses.size());

        Map<UrlMethod, Mapping> urlMappings = new HashMap<>();

        for (Class<?> clazz : annotatedClasses) {
            System.out.println("Scanning class: " + clazz.getName());
            
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    
                    String url = annotation.value();
                    HttpMethod httpMethod = annotation.method();

                    UrlMethod urlMethod = new UrlMethod(url, httpMethod);
                    Mapping mapping = new Mapping(clazz, method);

                    urlMappings.put(urlMethod, mapping);
                    System.out.println("  → Mapping ajouté : " + urlMethod);
                }
            }
        }

        context.setAttribute("urlMappings", urlMappings);
        System.out.println("SUCCESS : " + urlMappings.size() + " mappings enregistrés dans le contexte !");

    } catch (Exception e) {
        System.err.println(" ERREUR dans Listener :");
        e.printStackTrace();
    }
}

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Code à exécuter lors de la destruction du contexte (si nécessaire)
    }
    
}
