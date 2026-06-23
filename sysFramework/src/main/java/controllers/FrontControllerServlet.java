package main.java.controllers;
import java.io.*;
import java.lang.reflect.Method;

import jakarta.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.util.List;

import main.java.annotations.Controller;
import main.java.annotations.UrlMapping;
import main.java.utils.ScanAnnotation;
import main.java.utils.UtilAnalyser;
import main.java.model.Mapping;
import java.util.ArrayList;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {
    
    HashMap<String, Mapping> urlMappings;
    
    @Override
    public void init() throws ServletException {
        try {
            String scanPackage = getServletConfig().getInitParameter("scanPackage");

            if (scanPackage == null || scanPackage.isEmpty()) {
                urlMappings = new HashMap<>();
                return;
            }

            List<Class<?>> allClasses = UtilAnalyser.findClasses(scanPackage);
            List<Class<?>> annotatedClasses = ScanAnnotation.findAnnotatedClasses(allClasses, Controller.class);

            urlMappings = new HashMap<>();

           for(Class<?> clazz : annotatedClasses){

            Method[] methods = clazz.getDeclaredMethods();

            for(Method method : methods){
            
                if(method.isAnnotationPresent(UrlMapping.class)){
                
                    UrlMapping annotation =
                        method.getAnnotation(UrlMapping.class);
                
                    String url = annotation.value();
                
                    Mapping mapping =
                        new Mapping(
                            clazz.getName(),
                            method.getName()
                        );
                    
                    urlMappings.put(url, mapping);
        }
    } }
} catch (Exception e) {
            throw new ServletException("Erreur lors du chargement des controllers", e);
        }
    }

protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    PrintWriter out = resp.getWriter();

    String requestUrl = req.getRequestURI();
    String contextPath = req.getContextPath();

    // retire /TestFrameworkMVC
    String url = requestUrl.substring(contextPath.length());

    Mapping mapping = urlMappings.get(url);

    if (mapping != null) {

        out.println("URL : " + url);
        out.println("Controller : " + mapping.getClassName());
        out.println("Methode : " + mapping.getMethodName() + "()");

    } else {

        out.println("URL non trouvee");
        out.println("Mappings disponibles :");

        for (String mappingUrl : urlMappings.keySet()) {

            Mapping m = urlMappings.get(mappingUrl);

            out.println(
                mappingUrl
                + " -> "
                + m.getClassName()
                + "."
                + m.getMethodName()
            );
        }
    }
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        processRequest(req, resp);
    }
}
