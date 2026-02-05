package com.app.models;

public class Employee {
    private String name;
    private String position;
    private double salary;
    
    public Employee() {}
    
    // Getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    
    @Override
    public String toString() {
        return String.format("Employee{name='%s', position='%s', salary=%.2f}", 
                           name, position, salary);
    }
}
