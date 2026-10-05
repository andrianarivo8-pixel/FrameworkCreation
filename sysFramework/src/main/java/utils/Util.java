package main.java.utils;

import java.lang.reflect.Method;

public class Util {

    public static boolean haveParameter(Method method, String parameterTypeName) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> paramType : parameterTypes) {
            if (paramType.getName().equals(parameterTypeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convertit une String (valeur du formulaire) vers le type du paramètre de la méthode.
     * Gère les types de base pour le moment.
     */
    public static Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }

        // Pour les autres types on renvoie la String brute
        return value;
    }
}