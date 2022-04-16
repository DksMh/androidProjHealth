package com.example.test_dot;

public class HealthUser {
    private String userName;
    private String gender;
    private double height;
    private double weight;

    public HealthUser(){
    }

    public HealthUser(String userName, String gender, double height, double weight){
        this.userName = userName;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
