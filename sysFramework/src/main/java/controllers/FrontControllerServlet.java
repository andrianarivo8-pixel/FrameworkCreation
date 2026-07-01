package main.java.controllers;
import java.io.*;
import java.lang.reflect.Method;

import jakarta.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.util.List;
import java.util.Map;

import main.java.annotations.Controller;
import main.java.annotations.UrlMapping;
import main.java.utils.ScanAnnotation;
import main.java.utils.UtilAnalyser;
import main.java.model.HttpMethod;
import main.java.model.Mapping;
import main.java.model.UrlMethod;

import java.util.ArrayList;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {
    
    //Nouvelle structure : clé = (URL + HTTP Method)
    private Map<UrlMethod, Mapping> urlMappings = new HashMap<>();
    
    @Override
    public void init() throws ServletException {
        try {
            String scanPackage = getServletConfig().getInitParameter("scanPackage");

            if (scanPackage == null || scanPackage.isEmpty()) {
                return;
            }

            //Scanner toutes les classes
            List<Class<?>> allClasses = UtilAnalyser.findClasses(scanPackage);

            //Filtrer les classes avec @Controller
            List<Class<?>> annotatedClasses = ScanAnnotation.findAnnotatedClasses(allClasses, Controller.class);

            urlMappings.clear();

            for (Class<?> clazz : annotatedClasses) {
                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {
                   if(method.isAnnotationPresent(UrlMapping.class)) {
                    
                        UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                        
                        String url = annotation.value();
                        HttpMethod httpMethod = annotation.method();

                        UrlMethod urlMethod = new UrlMethod(url, httpMethod);
                        
                        Mapping mapping = new Mapping(
                            clazz.getName(),
                            method.getName()
                        );

                        urlMappings.put(urlMethod, mapping);
                    }
                }
            }
           
        } catch (Exception e) {
                    throw new ServletException("Erreur lors du chargement des controllers", e);
                }
            }

protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    PrintWriter out = resp.getWriter();
        
    try {
    String requestUrl = req.getRequestURI();
    String contextPath = req.getContextPath();

    // retire /TestFrameworkMVC
    String url = requestUrl.substring(contextPath.length());
    
    String httpMethodStr = req.getMethod(); // GET, POST, etc.
    // On crée un objet UrlMethod pour la recherche dans la map
    UrlMethod requestedKey = new UrlMethod(url, httpMethodStr);
    
    // pour
    Mapping mapping = urlMappings.get(requestedKey);
    if (mapping != null) {
            out.println("URL trouvée : " + url);
            out.println("Méthode HTTP : " + httpMethodStr);
            out.println("Controller : " + mapping.getClassName());
            out.println("Méthode : " + mapping.getMethodName() + "()");
        } else {
            out.println("URL/Méthode non trouvée : " + httpMethodStr + " " + url);
            out.println("Mappings disponibles :");

            for (Map.Entry<UrlMethod, Mapping> entry : urlMappings.entrySet()) {
                UrlMethod key = entry.getKey();
                Mapping m = entry.getValue();
                out.println(key + " -> " + m.getClassName() + "." + m.getMethodName() + "()");
            }
        }

    } catch (Exception e) {
        out.println("Erreur lors du traitement de la requête : " + e.getMessage());
        e.printStackTrace();
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
