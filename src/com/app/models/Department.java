package com.app.models;

public class Department {
    private String name;
    private String code;
    
    public Department() {}
    
    // Getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    @Override
    public String toString() {
        return String.format("Department{name='%s', code='%s'}", name, code);
    }
}