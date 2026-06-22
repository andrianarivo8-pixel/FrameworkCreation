package main.java.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ScanAnnotation {

    public static List<Class<?>> findAnnotatedClasses(List<Class<?>> classes,
        Class<? extends Annotation> annotationClass) {
        List<Class<?>> annotatedClasses = new ArrayList<>();

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                annotatedClasses.add(clazz);
            }
        }

        return annotatedClasses;
    }
}
