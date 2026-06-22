package main.java.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class UtilAnalyser {

    public static List<Class<?>> findClasses(String packageName)
            throws IOException, ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                String directoryPath = URLDecoder.decode(
                        resource.getPath(),
                        StandardCharsets.UTF_8
                );
                File directory = new File(directoryPath);
                classes.addAll(findClassesInDirectory(directory, packageName));
            }
        }

        return classes;
    }

    private static List<Class<?>> findClassesInDirectory(File directory, String packageName)
            throws ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(packageName + "." + className));
            }
        }

        return classes;
    }
}
