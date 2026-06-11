package main.java.controller;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {
   
        protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
            
           PrintWriter out = resp.getWriter();
           String url=req.getRequestURI();
        out.println("URL: " + url); 

        //    if(url.equals("/") || url.contains("page1")) {
        //        req.setAttribute("url",url);
        //        req.getRequestDispatcher("/index.jsp").forward(req, resp);
        //    } else if(url.contains("page2")) {
        //        req.setAttribute("url",url);
        //        req.getRequestDispatcher("/Accueil.jsp").forward(req, resp);
        //    }
        //    out.println("URL: " + url); 
        //    else {
        //        out.println("Unknown URL");  
        //     }
           
    }

      @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
}
