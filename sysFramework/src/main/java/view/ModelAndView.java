package main.java.view;

import java.util.Map;

public class ModelAndView {
    private String viewName;
    private Map<String, Object> data;

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
        this.data = new java.util.HashMap<>();
    }

    public ModelAndView(String viewName, Map<String, Object> data) {
        this.viewName = viewName;
        this.data = data;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getData() {
        return data;
    }
    public void addAttribute(String key, Object value) {
        data.put(key, value);
    }
}
