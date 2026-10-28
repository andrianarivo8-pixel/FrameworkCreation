package main.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indique que la méthode du controller nécessite une authentification.
 * L'utilisateur doit être connecté (attribut de session configuré).
 */
@Retention(RetentionPolicy.RUNTIME)  // visible à l'exécution (réflexion)
@Target(ElementType.METHOD)          // utilisable sur les méthodes
public @interface Auth {
    String[] roles() default {}; //liste de roles autorisés
}