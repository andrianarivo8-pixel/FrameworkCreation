package main.java.model;

public enum HttpMethod {
    GET,
    POST;

    //pour convertir une String en Enum de facon sécurisée
    public static HttpMethod fromString(String method) {
        if(method == null) return GET; // valeur par défaut
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET; // valeur par défaut
        }
    }

}
