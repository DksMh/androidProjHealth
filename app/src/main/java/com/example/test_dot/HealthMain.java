package com.example.test_dot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HealthMain extends AppCompatActivity {
    // 유저 정보와 기록 정보
    Userinfo user;
    Record record;
    // record 어댑터

    // health_main 화면에 나오는 TextView
    TextView user_name; // 이름
    TextView user_gender; // 성별
    TextView user_age; // 나이
    TextView user_height; // 키
    TextView user_weight; // 몸무게
    TextView avg_weight; // 표준 몸무게
    TextView user_bmi; // bmi

    // 메인엑티비티.java에서 보낸 파이어베이스에 저장된 정보가져오기
    Intent intent;
    // HealthMain에서 Run으로 보낼 정보
    Intent hm_intent;
    // run 정보 다시 받기
    Intent run_intent;
    // Record로 정보 넘기기
    Intent goToRecord;


    // health_main 화면에 나오는 Button 런닝과 기록확인
    //private LinearLayout mRecordLayout;
    //private TextView userRunData;
    private Button walkBtn;
    private Button recordBtn;
    //int weightcal;

    // 걷기에서도 사용되는 몸무게(칼로리 계산을 위해서 몸무게 정보가 넘어가야한다)
    String usingRunweight;
    String fire_usingRunweight;

    // 파이어베이스에 저장된 내용 가져오기
    // main으로 넘어올때
    private String fireMain_id;
    private String fireMain_name;
    private String fireMain_age;
    private String fireMain_gender;
    private String fireMain_height;
    private String fireMain_weight;

    // run에서 다시 넘어올 때
    private String firebase_id2;
    private String firebase_name;
    private String firebase_age;
    private String firebase_gender;
    private String firebase_height;
    private String firebase_weight;

    private String firebase_distance; // 거리
    private String firebase_date; // 날짜
    private String firebase_kcal; // 칼로리
    //
    // 표준체중 계산
    double weightcal;
    double run_user_Hm;
    double user_Hm;

    // BMI 계산
    String height1;
    String firebase_height1;
    double bmical;
    String bmiresult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_main);
        // 메인엑티비티.java에서 보낸 파이어베이스에 저장된 정보가져오기
        intent = getIntent();
        // Run에서 보낸 정보 가져오기
        run_intent = getIntent();
        // Userinfo, Record 객체 받기
        user = (Userinfo) intent.getSerializableExtra("put-user");
        //record = (Record) run_intent.getSerializableExtra("put-record");
        System.out.println("user : "+user); // com.example.test_dot.Userinfo@f559521
        //System.out.println("record : "+record);
        
        // user 정보가 main에서 받아와진 경우 -> 처음 운동하기로 들어왔을 경우
        if(!(user==null)) {
            fireMain_id = user.getEmailId();
            fireMain_name = user.getName();
            fireMain_age = user.getAge();
            fireMain_gender = user.getGender();
            fireMain_height = user.getHeight();
            fireMain_weight = user.getWeight();
            System.out.println("fireMain_id : " + fireMain_id);
            System.out.println("fireMain_name : " + fireMain_name);
            System.out.println("fireMain_age : " + fireMain_age);
            System.out.println("fireMain_gender : " + fireMain_gender);
            System.out.println("fireMain_height : " + fireMain_height);
            System.out.println("fireMain_weight : " + fireMain_weight);
        }
        // run에서 기록 정보가 받아와지지 않은 경우 -> 운동안했을 때.
//        if(!(record==null)) {
//            firebase_num = record.getNum();
//            firebase_date = record.getDate();
//            firebase_kcal = record.getKcal();
//            System.out.println("firebase_num : "+firebase_num);
//            System.out.println("firebase_date : "+firebase_date);
//            System.out.println("firebase_kcal : "+firebase_kcal);
//        }
        // Run에서 user 정보 다시 받음
        System.out.println("run_intent : "+run_intent); // cmp=com.example.test_dot/.HealthMain (has extras)
        firebase_id2 = run_intent.getStringExtra("throw-id");
        firebase_name = run_intent.getStringExtra("throw-name");
        firebase_age = run_intent.getStringExtra("throw-age");
        firebase_gender = run_intent.getStringExtra("throw-gender");
        firebase_height = run_intent.getStringExtra("throw-height");
        firebase_weight = run_intent.getStringExtra("throw-weight");
        firebase_date = run_intent.getStringExtra("throw-date");
        firebase_distance = run_intent.getStringExtra("throw-distance");
        firebase_kcal = run_intent.getStringExtra("throw-kcal");

        System.out.println("firebase_id2 : "+firebase_id2);
        System.out.println("firebase_name : "+firebase_name);
        System.out.println("firebase_age : "+firebase_age);
        System.out.println("firebase_gender : "+firebase_gender);
        System.out.println("firebase_height : "+firebase_height);
        System.out.println("firebase_width : "+firebase_weight);


        System.out.println("firebase_date!!! : "+firebase_date);
        System.out.println("firebase_distance!!! : "+firebase_distance);
        System.out.println("firebase_kcal!!! : "+firebase_kcal);

        // 아이디
        // 이름
        user_name = (TextView) findViewById(R.id.user_name);
        // 나이
        user_age = (TextView) findViewById(R.id.user_age);
        // 성별
        user_gender = (TextView) findViewById(R.id.user_gender);
        // 키
        user_height = (TextView) findViewById(R.id.user_height);
        // 몸무게
        user_weight = (TextView) findViewById(R.id.user_weight);
        //표준몸무게
        // 체중 대비 백분율 (PIBW, Percent of Ideal Body Weight) : 체질량지수를 이용
        // 여자 : 신장(m)×신장(m)×21
        // 남자 : 신장(m)×신장(m)×22
        avg_weight = (TextView) findViewById(R.id.avg_weight);
        // BMI
        // 신체질량지수(BMI) = 체중(kg) / (신장(m))2 --> 제곱
        user_bmi = (TextView) findViewById(R.id.user_bmi);

        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
            if(!((fireMain_id==null)&& (fireMain_name==null)&&(fireMain_age==null)&&(fireMain_gender==null)&&(fireMain_height==null)&&(fireMain_weight==null))){
                System.out.println("fireMain_id : "+fireMain_id); // fireMain_id : user01@naver.com

                user_name.setText(user.getName() + " 님의 정보");
                System.out.println("user.getName() : " + user.getName()); // user.getName() : 홍길자

                user_age.setText(user.getAge() + " 세");
                System.out.println("user.getAge() : " + user.getAge()); // user.getAge() : 26

                user_gender.setText(user.getGender());
                System.out.println("user.getGender() : " + user.getGender()); // user.getGender() : 여성

                user_height.setText(user.getHeight() + " cm"); // user.getHeight() : 160
                System.out.println("user.getHeight() : " + user.getHeight()); // user.getHeight() : 160
                height1 = user.getHeight(); // 참조하여 생성한 객체 user_height 으로 값을 읽어 들여와 String 형태의 변수 height1에 저장한다
                double user_Hcm = Double.parseDouble(height1);
                // cm를 m로 바꾸기
                user_Hm = user_Hcm / 100;
                System.out.println("user_Hm : " + user_Hm); // user_Hm : 1.6

                usingRunweight = user.getWeight();
                user_weight.setText(usingRunweight + " kg");
                System.out.println("user.getWeight() : " + user.getWeight()); // user.getWeight() : 50

                // bmi
                bmical = Double.parseDouble(usingRunweight) / ((Double.parseDouble(height1) / 100) * (Double.parseDouble(height1) / 100));
                if (bmical < 20) {
                    bmiresult = "저체중";
                } else if (bmical <= 24 && bmical > 20) {
                    bmiresult = "정상";
                } else if (bmical <= 30 && bmical > 24) {
                    bmiresult = "과체중";
                } else {
                    bmiresult = "비만";
                }
                String BMI = String.format("%.2f", bmical);
                user_bmi.setText(BMI + " ( " + bmiresult + " )");

            }
        } else {
            // user email로 판별하니까 계속 가지고 가야한다.
            System.out.println(firebase_id2);
            user_name.setText(firebase_name+"님의 정보");
            user_age.setText(firebase_age+" 세");
            user_gender.setText(firebase_gender);

            user_height.setText(firebase_height+" cm");
            firebase_height1 = firebase_height; // 참조하여 생성한 객체 user_height 으로 값을 읽어 들여와 String 형태의 변수 height1에 저장한다
            double user_Hcm = Double.parseDouble(firebase_height1);
            // cm를 m로 바꾸기
            run_user_Hm = user_Hcm/100;
            System.out.println("firebase_height_user_Hm : "+run_user_Hm);

            fire_usingRunweight = firebase_weight;
            user_weight.setText(firebase_weight+" kg");

            System.out.println("fire_usingRunweight"+fire_usingRunweight); // null 값임
            System.out.println("firebase_height1"+firebase_height1); // -> 160 잘뜸
            bmical = Double.parseDouble(fire_usingRunweight) / ((Double.parseDouble(firebase_height1) / 100) * (Double.parseDouble(firebase_height1) / 100));
            //String bmiresult;
            if (bmical < 20) {
                bmiresult = "저체중";
            } else if (bmical <= 24 && bmical > 20) {
                bmiresult = "정상";
            } else if (bmical <= 30 && bmical > 24) {
                bmiresult = "과체중";
            } else {
                bmiresult = "비만";
            }
            String BMI = String.format("%.2f", bmical);
            user_bmi.setText(BMI + " ( " + bmiresult + " )");

        }
//
//        // 이름
//        user_name = (TextView) findViewById(R.id.user_name);
//        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
//            if(!((user.getEmailId()==null)&& (user.getName()==null)&&(user.getAge()==null)&&(user.getGender()==null)&&(user.getHeight()==null)&&(user.getWeight()==null))) {
//                user_name.setText(user.getName() + " 님의 정보");
//                System.out.println("user.getName() : " + user.getName()); // user.getName() : 홍길자
//            }
//        } else {
//            user_name.setText(firebase_name+"님의 정보");
//        }
//
//
//        // 나이
//        user_age = (TextView) findViewById(R.id.user_age);
//        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
//            if(!((user.getEmailId()==null)&& (user.getName()==null)&&(user.getAge()==null)&&(user.getGender()==null)&&(user.getHeight()==null)&&(user.getWeight()==null))) {
//                user_age.setText(user.getAge() + " 세");
//                System.out.println("user.getAge() : " + user.getAge()); // user.getAge() : 26
//            }
//        } else {
//            user_age.setText(firebase_age+" 세");
//        }
//
//        // 성별
//        user_gender = (TextView) findViewById(R.id.user_gender);
//        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
//            if(!((user.getEmailId()==null)&& (user.getName()==null)&&(user.getAge()==null)&&(user.getGender()==null)&&(user.getHeight()==null)&&(user.getWeight()==null))) {
//                user_gender.setText(user.getGender());
//                System.out.println("user.getGender() : " + user.getGender()); // user.getGender() : 여성
//            }
//        } else {
//            user_gender.setText(firebase_gender);
//        }
//
//        // 키
//        user_height = (TextView) findViewById(R.id.user_height);
//        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
//            if(!((user.getEmailId()==null)&& (user.getName()==null)&&(user.getAge()==null)&&(user.getGender()==null)&&(user.getHeight()==null)&&(user.getWeight()==null))) {
//                user_height.setText(user.getHeight() + " cm"); // user.getHeight() : 160
//                System.out.println("user.getHeight() : " + user.getHeight()); // user.getHeight() : 160
//                height1 = user.getHeight(); // 참조하여 생성한 객체 user_height 으로 값을 읽어 들여와 String 형태의 변수 height1에 저장한다
//                double user_Hcm = Double.parseDouble(height1);
//                // cm를 m로 바꾸기
//                user_Hm = user_Hcm / 100;
//                System.out.println("user_Hm : " + user_Hm); // user_Hm : 1.6
//            }
//        } else {
//            user_height.setText(firebase_height+" cm");
//            firebase_height1 = firebase_height; // 참조하여 생성한 객체 user_height 으로 값을 읽어 들여와 String 형태의 변수 height1에 저장한다
//            double user_Hcm = Double.parseDouble(firebase_height1);
//            // cm를 m로 바꾸기
//            run_user_Hm = user_Hcm/100;
//            System.out.println("firebase_height_user_Hm : "+run_user_Hm);
//        }
//
//
//
//        // 몸무게
//        user_weight = (TextView) findViewById(R.id.user_weight);
//        if ((firebase_id2==null)&&(firebase_name==null)&&(firebase_age==null)&&(firebase_gender==null)&&(firebase_height==null)&&(firebase_weight==null)) {
//            if(!((user.getEmailId()==null)&& (user.getName()==null)&&(user.getAge()==null)&&(user.getGender()==null)&&(user.getHeight()==null)&&(user.getWeight()==null))) {
//                usingRunweight = user.getWeight();
//                user_weight.setText(usingRunweight + " kg");
//                System.out.println("user.getWeight() : " + user.getWeight()); // user.getWeight() : 50
//            }
//        } else {
//            fire_usingRunweight = firebase_weight;
//            user_weight.setText(firebase_weight+" kg");
//        }

        //표준몸무게
        // 체중 대비 백분율 (PIBW, Percent of Ideal Body Weight) : 체질량지수를 이용
        // 여자 : 신장(m)×신장(m)×21
        // 남자 : 신장(m)×신장(m)×22
        // 표준 체중
        //avg_weight = (TextView) findViewById(R.id.avg_weight);
        if (firebase_gender==null) {
            if(user.getGender().equals("여성")){ // -> 여성
                weightcal = user_Hm*user_Hm*21;
            }else{ // -> 남성
                weightcal = user_Hm*user_Hm*22;
            }
        }else{
            if(firebase_gender.equals("여성")){ // -> 여성
                weightcal = run_user_Hm*run_user_Hm*21;
            }else{ // -> 남성
                weightcal = run_user_Hm*run_user_Hm*22;
            }
        }

        String avgweightcal = String.format("%.2f", weightcal);
        System.out.println("avgweightcal : "+avgweightcal); // avgweightcal : 53.76
        avg_weight.setText(avgweightcal);

        // 걷기
        walkBtn = (Button) findViewById(R.id.walk);
        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // HealthMain에서 걷기 페이지로 정보를 보낸다.
                hm_intent = new Intent(HealthMain.this, RunBicycle.class);
                // main으로 돌아올 떄 다 지워지므로 user정보를 다 넘긴다.
                hm_intent.putExtra("put-id",user.getEmailId()); // firebase_id = user.getEmailId();
                hm_intent.putExtra("put-name",user.getName());
                hm_intent.putExtra("put-age",user.getAge());
                hm_intent.putExtra("put-gender",user.getGender());
                hm_intent.putExtra("put-height",user.getHeight());
                hm_intent.putExtra("put-weight",usingRunweight); // usingRunweight = user.getWeight()
                startActivity(hm_intent);
                finish();
            }
        });

        // 기록확인
        recordBtn = (Button) findViewById(R.id.record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            private Object RecordCheck;
            @Override
            public void onClick(View view) {
                goToRecord = new Intent(HealthMain.this, RecordCheck.class);
                // 여기서는 걷기 정보를 볼 수 있도록 걷고 난 기록을 보낸다. user정보도 다 넘긴다.
                if(!(user==null)) {
                    goToRecord.putExtra("put-id", user.getEmailId()); // firebase_id = user.getEmailId();
                    goToRecord.putExtra("put-weight", usingRunweight); // usingRunweight = user.getWeight()
                }else{
                    goToRecord.putExtra("put-id", firebase_id2); // firebase_id = user.getEmailId();
                    goToRecord.putExtra("put-date", firebase_date);
                    goToRecord.putExtra("put-dis", firebase_distance);
                    goToRecord.putExtra("put-kcal", firebase_kcal);
                }
                startActivity(goToRecord);
            }
        });

    }
}

