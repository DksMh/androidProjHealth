package com.example.test_dot;

public class LoginUser {
    private String emailId;     // 이메일 아이디
    private String name;        // 이름
    private String age;         // 나이
    private String gender;      // 성별

    public LoginUser(){
        // Default constructor required for calls to DataSnapshot.getValue(LoginUser.class)
    }

    public LoginUser(String emailId, String name, String age, String gender) {
        this.emailId = emailId;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
