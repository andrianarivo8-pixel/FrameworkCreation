package main.java.utils;

import java.lang.reflect.Method;

public class Util {
    public static boolean haveParameter(Method method, Class<?> parameterType) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> type : parameterTypes) {
            if (type.equals(parameterType)) {
                return true;
            }
        }
        return false;
    }
    public static String ObjectToStringJson(Object obj) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        Method[] methods = obj.getClass().getDeclaredMethods();
        boolean firstField = true;

        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                try {
                    Object value = method.invoke(obj);
                    String fieldName = method.getName().substring(3); // Remove "get" prefix
                    fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1); // Convert first letter to lowercase

                    if (!firstField) {
                        jsonBuilder.append(", ");
                    }
                    jsonBuilder.append("\"").append(fieldName).append("\": ");
                    if (value instanceof String) {
                        jsonBuilder.append("\"").append(value).append("\"");
                    } else {
                        jsonBuilder.append(value);
                    }
                    firstField = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}
