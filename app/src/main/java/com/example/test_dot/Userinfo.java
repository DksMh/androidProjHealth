package com.example.test_dot;

import java.io.Serializable;

public class Userinfo implements Serializable {
    private int no;
    private String emailId;     // 이메일 아이디
    private String name;        // 이름
    private String age;         // 나이
    private String gender;      // 성별
    private String height;      // 키
    private String weight;      // 몸무게

    public Userinfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Userinfo(int no, String emailId, String name, String age, String gender, String height, String weight){
        this.no = no;
        this.emailId = emailId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

}

