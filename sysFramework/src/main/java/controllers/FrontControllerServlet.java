package main.java.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import main.java.model.Mapping;
import main.java.model.UrlMethod;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        PrintWriter out = resp.getWriter();

        try {
            ServletContext context = req.getServletContext();
            @SuppressWarnings("unchecked")
            Map<UrlMethod, Mapping> mappings = (Map<UrlMethod, Mapping>) context.getAttribute("urlMappings");

            if (mappings == null) {
                throw new ServletException("Aucun mapping trouvé dans le contexte du servlet. Vérifiez le Listener.");
            }

            String requestUrl = req.getRequestURI();
            String contextPath = req.getContextPath();
            String url = requestUrl.substring(contextPath.length());
            
            String httpMethodStr = req.getMethod();

            UrlMethod requestedKey = new UrlMethod(url, httpMethodStr);
            Mapping mapping = mappings.get(requestedKey);

            if (mapping != null) {
                // Exécution de la méthode
                Class<?> controllerClass = mapping.getClassName();
                Method methodToInvoke = mapping.getMethodName();
                
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                
                methodToInvoke.invoke(controllerInstance);

                // Affichage
                out.println("✅ Méthode exécutée avec succès !");
                out.println("URL : " + url);
                out.println("HTTP Method : " + httpMethodStr);
                out.println("Controller : " + controllerClass.getSimpleName());
                out.println("Méthode : " + methodToInvoke.getName() + "()");

            } else {
                out.println("❌ URL/Méthode non trouvée : " + httpMethodStr + " " + url);
                out.println("\nMappings disponibles :");

                for (Map.Entry<UrlMethod, Mapping> entry : mappings.entrySet()) {
                    UrlMethod key = entry.getKey();
                    Mapping m = entry.getValue();
                    out.println(key + " -> " + m.getClassName().getSimpleName() + "." 
                                + m.getMethodName().getName() + "()");
                }
            }

        } catch (Exception e) {
            out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace(out);
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