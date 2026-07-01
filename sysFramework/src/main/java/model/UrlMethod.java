package main.java.model;

public class UrlMethod {
    
    private String url;
    private HttpMethod httpMethod;

    public UrlMethod(String url, HttpMethod httpMethod) {
        this.url = url;
        this.httpMethod = (httpMethod != null) ? httpMethod : HttpMethod.GET;
    }

    // la conversion String → Enum.
    public UrlMethod(String url, String httpMethodStr) {
        this.url = url;
        this.httpMethod = HttpMethod.fromString(httpMethodStr);
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlMethod)) return false;
        
        UrlMethod other = (UrlMethod) obj;
        return this.url.equals(other.url) && 
               this.httpMethod == other.httpMethod;
    }

    @Override
    public int hashCode() {
        return url.hashCode() * 31 + httpMethod.hashCode();
    }

    @Override
    public String toString() {
        return httpMethod + " " + url;
    }
}