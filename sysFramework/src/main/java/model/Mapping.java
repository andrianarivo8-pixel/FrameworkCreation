package main.java.model;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> className;
    private Method methodName;
    
    public Class<?> getClassName() {
        return className;
    }
    public void setClassName(Class<?> className) {
        this.className = className;
    }
    public Method getMethodName() {
        return methodName;
    }
    public void setMethodName(Method methodName) {
        this.methodName = methodName;
    }
    public Mapping(Class<?> className, Method methodName) {
        this.className = className;
        this.methodName = methodName;
    }



}
