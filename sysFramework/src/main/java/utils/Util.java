package main.java.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import main.java.model.MapFile;  

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
     * Convertit une String (valeur du formulaire) vers le type du paramètre.
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

        return value;
    }

    public static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == boolean.class || type == Boolean.class
                || type == float.class || type == Float.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class
                || type == char.class || type == Character.class
                || type.isPrimitive();
    }

    /**
     * Binding récursif d'un objet à partir des paramètres de la requête.
     *
     * @param clazz         la classe à instancier
     * @param prefix        préfixe des noms de champs ("" ou "adresse" ou "adresse.pays")
     * @param requestParams map des paramètres HTTP (champs texte)
     * @param request       la requête (pour les fichiers multipart)
     * @return l'objet rempli
     */
    public static Object bindObject(Class<?> clazz, String prefix,
                                    Map<String, String[]> requestParams,
                                    HttpServletRequest request) throws Exception {

        Object obj = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            String fullParamName = (prefix == null || prefix.isEmpty())
                    ? fieldName
                    : prefix + "." + fieldName;

            Class<?> fieldType = field.getType();

            // ===== MapFile (upload) =====
            if (fieldType == MapFile.class) {
                MapFile mapFile = null;
                if (request != null && isMultipart(request)) {
                    Part part = request.getPart(fullParamName);
                    mapFile = buildMapFile(part);
                }
                field.set(obj, mapFile);
                continue;
            }

            // ===== Type simple =====
            if (isSimpleType(fieldType)) {
                if (requestParams != null && requestParams.containsKey(fullParamName)) {
                    String[] values = requestParams.get(fullParamName);
                    String value = (values != null && values.length > 0) ? values[0] : null;
                    Object converted = convertValue(value, fieldType);
                    field.set(obj, converted);
                }
            }
            // ===== Objet imbriqué (récursion) =====
            else {
                Object nestedObj = bindObject(fieldType, fullParamName, requestParams, request);
                field.set(obj, nestedObj);
            }
        }

        return obj;
    }

    /** Vérifie si la requête est multipart. */
    public static boolean isMultipart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private static String uploadPath = null;

    /**
     * Récupère le chemin d'upload depuis le contexte de la servlet (lazy).
     */
    public static String getUploadPath(ServletContext context) throws Exception {
        if (uploadPath != null) {
            return uploadPath;
        }

        String path = context.getInitParameter("upload.path");
        if (path == null || path.trim().isEmpty()) {
            throw new Exception("Paramètre 'upload.path' manquant dans web.xml");
        }

        java.io.File dir = new java.io.File(path.trim());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new Exception("Impossible de créer le dossier d'upload : " + path);
        }
        if (!dir.isDirectory()) {
            throw new Exception("upload.path n'est pas un dossier : " + path);
        }

        uploadPath = dir.getAbsolutePath();
        return uploadPath;
    }

    /**
     * Nettoie un nom de fichier pour éviter le path traversal.
     */
    public static String sanitizeFileName(String original) {
        if (original == null || original.isEmpty()) {
            return "fichier_sans_nom";
        }
        String name = new java.io.File(original).getName();
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isEmpty()) {
            return "fichier_sans_nom";
        }
        return name;
    }

    /**
     * Construit un MapFile à partir d'une Part (null si aucun fichier).
     */
    public static MapFile buildMapFile(Part part) throws Exception {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isEmpty()) {
            return null;
        }

        String nomPropre = sanitizeFileName(submitted);
        String contentType = part.getContentType();

        try (java.io.InputStream is = part.getInputStream()) {
            byte[] data = is.readAllBytes();
            return new MapFile(nomPropre, contentType, data);
        }
    }
}