package main.java.controllers;

import main.java.annotations.Controller;
import main.java.annotations.UrlMapping;

@Controller
public class EmpController {

    @UrlMapping("/essai")
    public void list(){
        System.out.println("list of employees");
    }
     @UrlMapping("/emp/new")
    public void create(){
        System.out.println("Create employees");
    }

}
