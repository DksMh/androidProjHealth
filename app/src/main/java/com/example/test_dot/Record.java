package com.example.test_dot;

import java.io.Serializable;

public class Record implements Serializable {
    private int no;
    private String emailId;  //아이디
    private int type;       // 런닝, 자전거
    private String num;     // 거리
    private String date;    // 날짜
    private String kcal;    // 칼로리

    public Record() {
        // Default constructor required for calls to DataSnapshot.getValue(Record.class)
        this(0,"",0,"","","");
    }

    public Record(int no, String emailId, int type, String num, String date, String kcal) {
        this.no = no;
        this.emailId = emailId;
        this.type = type;
        this.num = num;
        this.date = date;
        this.kcal = kcal;
    }

    public int getNo() { return no; }

    public void setNo(int no) { this.no = no; }

    public String getEmailId() {return emailId;}

    public void setEmailId(String emailId) {this.emailId = emailId;}

    public int getType() {return type;}

    public void setType(int type) {this.type = type;}

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKcal() {return kcal;}

    public void setKcal(String kcal) { this.kcal = kcal; }


    @Override
    public String toString() {
        return "Record{" +
                "no=" + no +
                ", emailId='" + emailId + '\'' +
                ", type=" + type +
                ", num='" + num + '\'' +
                ", date='" + date + '\'' +

                ", kcal=" + kcal +
                '}';
    }

}
