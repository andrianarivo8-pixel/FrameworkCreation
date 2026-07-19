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
    }eb
}
