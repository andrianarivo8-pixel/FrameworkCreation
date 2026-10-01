package main.java.utils;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class JsonUtil {

    public static String ObjectToStringJson(Object obj) {
        return toJson(obj);
    }

    private static String toJson(Object value) {

        // 1. null
        if (value == null) {
            return "null";
        }

        // 2. String / Character
        if (value instanceof String || value instanceof Character) {
            return "\"" + escapeJson(value.toString()) + "\"";
        }

        // 3. Boolean / Number
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        // 4. Tableau : int[], String[], Object[], ...
        if (value.getClass().isArray()) {
            StringBuilder json = new StringBuilder("[");
            
            int length = Array.getLength(value);

            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    json.append(", ");
                }

                Object element = Array.get(value, i);
                json.append(toJson(element));
            }

            json.append("]");
            return json.toString();
        }

        // 5. Collection : List, ArrayList, Vector, Set, ...
        if (value instanceof Collection<?> collection) {
            StringBuilder json = new StringBuilder("[");

            boolean first = true;

            for (Object element : collection) {
                if (!first) {
                    json.append(", ");
                }

                json.append(toJson(element));
                first = false;
            }

            json.append("]");
            return json.toString();
        }

        // 6. Map : HashMap, LinkedHashMap, ...
        if (value instanceof Map<?, ?> map) {
            StringBuilder json = new StringBuilder("{");

            boolean first = true;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    json.append(", ");
                }

                json.append("\"")
                    .append(escapeJson(String.valueOf(entry.getKey())))
                    .append("\": ");

                json.append(toJson(entry.getValue()));

                first = false;
            }

            json.append("}");
            return json.toString();
        }

        // 7. Objet Java classique
        return objectToJson(value);
    }

    private static String objectToJson(Object obj) {

        StringBuilder json = new StringBuilder("{");
        boolean firstField = true;

        Method[] methods = obj.getClass().getDeclaredMethods();

        for (Method method : methods) {

            // On prend uniquement les getters sans paramètre
            // et on ignore getClass()
            if (method.getName().startsWith("get")
                    && method.getParameterCount() == 0
                    && !method.getName().equals("getClass")) {

                try {
                    Object fieldValue = method.invoke(obj);

                    String fieldName = method.getName().substring(3);

                    if (fieldName.isEmpty()) {
                        continue;
                    }

                    fieldName = Character.toLowerCase(fieldName.charAt(0))
                            + fieldName.substring(1);

                    if (!firstField) {
                        json.append(", ");
                    }

                    json.append("\"")
                        .append(escapeJson(fieldName))
                        .append("\": ");

                    // Conversion récursive
                    json.append(toJson(fieldValue));

                    firstField = false;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        json.append("}");

        return json.toString();
    }

    // Évite les problèmes avec les caractères spéciaux JSON
    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}