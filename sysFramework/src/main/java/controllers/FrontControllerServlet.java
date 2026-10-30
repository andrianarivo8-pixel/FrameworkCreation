package main.java.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.java.annotations.*;
import main.java.model.Mapping;
import main.java.model.UrlMethod;
import main.java.view.ModelAndView;
import main.java.model.MaSession;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

import org.springframework.web.context.WebApplicationContext;

import main.java.utils.JsonUtil;
import main.java.utils.Util;
import jakarta.servlet.annotation.MultipartConfig;
import main.java.model.MapFile;
import jakarta.servlet.http.Part;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, // 10 Mo par fichier
        maxRequestSize = 20 * 1024 * 1024, // 20 Mo pour toute la requête
        fileSizeThreshold = 1024 * 1024 // seuil avant écriture temporaire
)
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

                // ========== SPRINT 10 : AUTH ==========
                if (methodToInvoke.isAnnotationPresent(main.java.annotations.Auth.class)) {

                    Auth authAnnotation = methodToInvoke.getAnnotation(Auth.class);

                    String authKey = (String) context.getAttribute("auth.session.key");
                    String roleKey = (String) context.getAttribute("auth.role.key");
                    String loginUrl = (String) context.getAttribute("auth.login.url");

                    if (loginUrl == null || loginUrl.isEmpty()) {
                        loginUrl = "/login-form";
                    }

                    HttpSession httpSession = req.getSession(false); // false = ne crée pas de session
                    Object userAttr = (httpSession != null) ? httpSession.getAttribute(authKey) : null;

                    // 1.doit etre connecté
                    if (userAttr == null) {
                        // Non connecté → redirection vers le login
                        String redirectTo = req.getContextPath() + loginUrl;
                        resp.sendRedirect(redirectTo);
                        return; // important : on arrête ici, pas d'invoke
                    }
                    // 2.Verifier les roles si precisés
                    String[] requiredRoles = authAnnotation.roles();
                    if (requiredRoles != null && requiredRoles.length > 0) {
                        Object roleAttr = httpSession.getAttribute(roleKey);
                        String userRole = (roleAttr != null) ? roleAttr.toString() : null;

                        boolean authorized = false;
                        if (userRole != null) {
                            for (String required : requiredRoles) {
                                if (userRole.equals(required)) {
                                    authorized = true;
                                    break;
                                }
                            }
                        }
                        if (!authorized) {
                            // Connecté mais mauvais rôle
                            resp.setContentType("text/html;charset=UTF-8");
                            resp.getWriter().println("❌ Accès refusé : rôle insuffisant (rôle actuel : "
                                    + userRole + ")");
                            return;
                        }
                    }
                } // fin AUTH

                Class<?> returnType = methodToInvoke.getReturnType();

                if (returnType != ModelAndView.class && !(methodToInvoke.isAnnotationPresent(WebApiRest.class))) {
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

                // 4. Parcourir les paramètres de la méthode
                for (int i = 0; i < methodParams.length; i++) {
                    java.lang.reflect.Parameter param = methodParams[i];
                    String paramName = param.getName(); // nom du paramètre de la méthode
                    Class<?> paramType = param.getType(); // type du paramètre de la méthode

                    // Cas spécial déjà existant : WebApplicationContext
                    if (paramType.getName().equals("org.springframework.web.context.WebApplicationContext")) {
                        if (springContext == null) {
                            throw new ServletException(
                                    "Aucun springContext disponible : vérifiez que ContextLoaderListener est bien dans web.xml");
                        }
                        args[i] = springContext;
                        continue;
                    }

                    // 2. Cas MapFile (paramètre direct de la méthode)
                    if (paramType == MapFile.class) {
                        Part part = null;
                        if (Util.isMultipart(req)) {
                            part = req.getPart(paramName);
                        }
                        args[i] = Util.buildMapFile(part);
                        continue;
                    }

                    // 2. Cas spécial : HttpServletRequest ← ICI
                    if (paramType == HttpServletRequest.class) {
                        args[i] = req;
                        continue;
                    }
                    // Cas MaSession
                    if (paramType == MaSession.class) {
                        args[i] = MaSession.from(req.getSession(true));
                        continue;
                    }
                    if (Util.isSimpleType(paramType)) {
                        // CAS 1 : Type simple (String, Integer, int...)
                        if (hasRequestParams && requestParams.containsKey(paramName)) {
                            String[] values = requestParams.get(paramName);
                            String value = (values != null && values.length > 0) ? values[0] : null;

                            // Conversion simple String → type du paramètre
                            args[i] = Util.convertValue(value, paramType);
                        } else {
                            args[i] = null;
                        }
                    }

                    else {
                        // CAS 2 : Type complexe (objet)-binding récursif
                        // On utilise la méthode bindObject pour créer l'objet à partir des paramètres
                        // de la requete
                        // prefix = "" car on est au niveau du paramètre de la méthode
                        args[i] = Util.bindObject(paramType, "", requestParams, req);

                    }

                }

                // 4. Invoke avec les arguments construits
                Object result = methodToInvoke.invoke(controllerInstance, args);

                // ===== Flush de la session =====
                for (int i = 0; i < methodParams.length; i++) {
                    if (methodParams[i].getType() == MaSession.class && args[i] instanceof MaSession) {
                        // true : on s'assure d'avoir une session à écrire
                        HttpSession httpSession = req.getSession(true);
                        ((MaSession) args[i]).flushTo(httpSession);
                    }
                }

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