package com.example.test_dot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    // 데이터베이스 - 파이어베이스 연동
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = database.getReference("DB");
    // 엑티비티 메인에 있는 것들
    Button commit;
    TextView user_name, user_gender, user_age;
    EditText user_height, user_weight;
    // 키 몸무게 받는 변수
    String edit_height;
    String edit_weight;

    // Userinfo
    Userinfo user;
    // 운동하기 메인페이지에 정보보내기
    Intent intent;
    // 걷기에도 정보를 보내주자 왜냐하면 걷기에 운동하기 메인으로 돌아오면 user정보가 null된다.
    // 왜냐하면 user정보는 run에는 id와 email만 있으니까 그래서 오류가 발생한다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그인하는 유저 테이블을 먼저 만들어 놓는다.
        DatabaseReference ref = rootRef.child("LoginUser").child("1");
        LoginUser loginuser = new LoginUser("user01@naver.com", "홍길자", "26","여성");
        ref.setValue(loginuser);

        // user.java 는 userinfo 하고 user를 하나 더 만들어서 정보를 만들어 놓는다.
        // 유저 이름, 성별, 나이
        user_name = findViewById(R.id.user_name);
        user_gender = findViewById(R.id.user_gender);
        user_age = findViewById(R.id.user_age);

        // avtivity_main에 있는 EditText 키와 몸무게
        commit = findViewById(R.id.commit); // 키, 몸무게 저장
        user_height = findViewById(R.id.user_height); // 유저 키 적는 곳
        user_weight = findViewById(R.id.user_weight); //유저 몸무게 적는 곳
        
        // 기초
        DatabaseReference ref2 = rootRef.child("UserAcount").child("1");
        user = new Userinfo(1, loginuser.getEmailId(), loginuser.getName(), loginuser.getAge(), loginuser.getGender(), "null", "null");
        ref2.setValue(user);

        // 유저 키와 몸무게 모두 null인 상황
        if(user.getHeight().equals("null") && user.getWeight().equals("null")){
            System.out.println("유저의 키와 몸무게가 null 입니다.");
            //버튼 누르면 값을 저장
            commit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference ref2 = rootRef.child("UserAcount").child("1");
                    // 운동에 들어가는 유저의 기본 정보는 LoginUser에서 받아오고 키와 몸무게만 추가적으로 저장
                    edit_height = user_height.getText().toString();
                    edit_weight = user_weight.getText().toString();
                    user = new Userinfo(1, loginuser.getEmailId(), loginuser.getName(), loginuser.getAge(), loginuser.getGender(), edit_height, edit_weight);
                    ref2.setValue(user);
                    // 운동하기 메인페이지에 사용
                    intent = new Intent(MainActivity.this, HealthMain.class);
                    // Userinfo 객체로 전송
                    intent.putExtra("put-user",user);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            System.out.println("유저의 키와 몸무게가 모두 있습니다.");
            // 운동하기 메인페이지에 사용
            intent = new Intent(MainActivity.this, HealthMain.class);
            user = new Userinfo(1, loginuser.getEmailId(), loginuser.getName(), loginuser.getAge(), loginuser.getGender(), edit_height, edit_weight);
            ref2.setValue(user);
            // 걷기 페이지도 넘겨준다.
            // Userinfo 객체로 전송
            intent.putExtra("put-user",user);
            startActivity(intent);
            finish();
        }

        rootRef.child("UserAcount").child("1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 엑티비티_메인.xml에서 userinfo 보여주기
                Userinfo userinfo = snapshot.getValue(Userinfo.class);
                user_name.setText(userinfo.getName());
                user_gender.setText(userinfo.getGender());
                user_age.setText(userinfo.getAge());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}

