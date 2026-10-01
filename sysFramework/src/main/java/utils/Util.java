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
}
