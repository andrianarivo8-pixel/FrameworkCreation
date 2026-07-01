package main.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import main.java.model.HttpMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {

    String value();  // l' URL (ex:"/list")
    HttpMethod method() default HttpMethod.GET;  // la méthode HTTP (GET, POST, etc.)

}