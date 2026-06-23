package main.java.model;

public class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }
    //getters 
    public String getClassName() {
        return className;
}

    public String getMethodName() {
        return methodName;
    }
}
