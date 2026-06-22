package main.java.controllers;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.util.List;

import main.java.annotations.Controller;
import main.java.utils.ScanAnnotation;
import main.java.utils.UtilAnalyser;
import java.util.ArrayList;

public class FrontControllerServlet extends HttpServlet {
    
    private List<String> listControllers;
    
    @Override
    public void init() throws ServletException {
        try {
            String scanPackage = getServletConfig().getInitParameter("scanPackage");

            if (scanPackage == null || scanPackage.isEmpty()) {
                listControllers = new ArrayList<>();
                return;
            }

            List<Class<?>> allClasses = UtilAnalyser.findClasses(scanPackage);
            List<Class<?>> annotatedClasses = ScanAnnotation.findAnnotatedClasses(allClasses, Controller.class);

            listControllers = new ArrayList<>();
            for (Class<?> clazz : annotatedClasses) {
                listControllers.add(clazz.getName());
            }

        } catch (Exception e) {
            throw new ServletException("Erreur lors du chargement des controllers", e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        PrintWriter out = resp.getWriter();
        // String url = req.getRequestURI();

        // out.println("URL: " + url);
        out.println("Controllers trouvés :");

        if (listControllers != null && !listControllers.isEmpty()) {
            for (String controller : listControllers) {
                out.println(controller);
            }
        } else {
            out.println("Aucun controller trouvé");
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
