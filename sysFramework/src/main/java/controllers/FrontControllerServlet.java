package main.java.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.java.annotations.WebApiRest;
import main.java.model.Mapping;
import main.java.model.UrlMethod;
import main.java.view.ModelAndView;

import java.util.Map;

import org.springframework.web.context.WebApplicationContext;

import main.java.utils.JsonUtil;
import main.java.utils.Util;

public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        PrintWriter out = resp.getWriter();

        try {
            ServletContext context = req.getServletContext();
            WebApplicationContext springContext = (WebApplicationContext) context.getAttribute("springContext");
            @SuppressWarnings("unchecked")
            Map<UrlMethod, Mapping> mappings = (Map<UrlMethod, Mapping>) context.getAttribute("urlMappings");

            if (mappings == null) {
                throw new ServletException("Aucun mapping trouvé dans le contexte du servlet. Verifiez le Listener.");
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

                Class<?> returnType = methodToInvoke.getReturnType();

                if (returnType != ModelAndView.class &&  !(methodToInvoke.isAnnotationPresent(WebApiRest.class))) {
                    throw new ServletException("La méthode " + methodToInvoke.getName() + " du controller "
                            + controllerClass.getSimpleName() + " doit retourner un objet ModelAndView.");
                }
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                // 1. Récupérer les paramètres de la méthode
                java.lang.reflect.Parameter[] methodParams = methodToInvoke.getParameters();

                // 2. Préparer le tableau d'arguments
                Object[] args = new Object[methodParams.length];

                // 3. Récupérer les paramètres de la requête
                Map<String, String[]> requestParams = req.getParameterMap();

                boolean hasRequestParams = requestParams != null && !requestParams.isEmpty();

                for(int i = 0; i < methodParams.length; i++) {
                    java.lang.reflect.Parameter param = methodParams[i];
                    String paramName = param.getName(); //nom du paramètre de la méthode
                    Class<?> paramType = param.getType(); //type du paramètre de la méthode 

                    // Cas spécial déjà existant : WebApplicationContext
                if (paramType.getName().equals("org.springframework.web.context.WebApplicationContext")) {
                    if (springContext == null) {
                        throw new ServletException(
                            "Aucun springContext disponible : vérifiez que ContextLoaderListener est bien dans web.xml");
                    }
                    args[i] = springContext;
                    continue;
                }

                // Si la requête a des paramètres → on essaie de matcher
                if (hasRequestParams && requestParams.containsKey(paramName)) {
                    String[] values = requestParams.get(paramName);
                    String value = (values != null && values.length > 0) ? values[0] : null;

                    // Conversion simple String → type du paramètre
                    args[i] = Util.convertValue(value, paramType);
                } else {
                    // Pas de paramètre correspondant → on met null (ou valeur par défaut)
                    args[i] = null;
                }
            }

            
            
        
               // 4. Invoke avec les arguments construits
                Object result = methodToInvoke.invoke(controllerInstance, args);
                
                // if (Util.haveParameter(methodToInvoke, "org.springframework.web.context.WebApplicationContext")) {
                //     if (springContext == null) {
                //         throw new ServletException(
                //                 "Aucun springContext dispo: verifez que ContexteLoaderListener est bien dans web.xml");
                //     }
                //     result = (Object) methodToInvoke.invoke(controllerInstance, springContext);
                // } else {
                //     result = (Object) methodToInvoke.invoke(controllerInstance);
                // }


                if (result instanceof ModelAndView) {
                    addArgToRequest(req, ((ModelAndView) result).getData());

                    String viewPath = ((ModelAndView) result).getViewName();
                    String prefix = context.getInitParameter("viewprefix");
                    String suffix = context.getInitParameter("viewsuffix");
                    String fullViewPath = "/" + prefix + "/" + viewPath + suffix;
                    req.getRequestDispatcher((fullViewPath)).forward(req, resp);
                } else {
                    if (result instanceof String) {
                        // out.println((String) result);
                        resp.setContentType("application/json");
                        resp.getWriter().write((String) result);
                    } else {
                        // out.println(result);
                        resp.setContentType("application/json");
                        resp.getWriter().write(JsonUtil.ObjectToStringJson(result));
                    }
                }

            }

            else {
                out.println("❌ URL/Méthode non trouvée : " + httpMethodStr + " " + url);
                out.println("\nMappings disponibles :");

                for (Map.Entry<UrlMethod, Mapping> entry : mappings.entrySet()) {
                    UrlMethod key = entry.getKey();
                    Mapping m = entry.getValue();
                    out.println(key + " -> " + m.getClassName().getSimpleName() + "."
                            + m.getMethodName().getName() + "()");
                }
            }

            // // Affichage
            // out.println("✅ Méthode exécutée avec succès !");
            // out.println("URL : " + url);
            // out.println("HTTP Method : " + httpMethodStr);
            // out.println("Controller : " + controllerClass.getSimpleName());
            // out.println("Méthode : " + methodToInvoke.getName() + "()");

        } catch (Exception e) {
            out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace(out);
        }
    }

    protected void addArgToRequest(HttpServletRequest req, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
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