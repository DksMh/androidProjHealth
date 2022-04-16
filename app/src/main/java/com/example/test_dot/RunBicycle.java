package com.example.test_dot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.Random;

public class RunBicycle extends AppCompatActivity implements OnMapReadyCallback {
    // 현재위치
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // 데이터베이스 - 파이어베이스
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = database.getReference("DB");

    // HealthMain.java에서 보낸 파이어베이스에 저장된 정보가져오기
    Intent hm_intent;
    private String firebase_id2;
    private String firebase_name;
    private String firebase_age;
    private String firebase_gender;
    private String firebase_height;
    private String firebase_weight;
    // Record
    Record record;
    Intent run_intent;

    // 폴리라인 색상(빨강, 검정)
    private static final int COLOR_RED_ARGB = 0xffff0000; // -> 시작시 색상
    private static final int COLOR_BLACK_ARGB = 0xff000800; // -> 종료시 색상

    // 처음 위도, 경도
    double firstx, firsty;
    // 마지막 위도, 경도
    double lastx, lasty;

    // 선그리기
    PolylineOptions poloptions;
    // 시작, 중지, 종료 버튼
    Button start, pause, stop;
    // 스레드
    Thread thread;
    boolean isThread = false;
    boolean clearLo = true;

    // 위도, 경도 확인하기
    TextView tv1;

    public void setStop(boolean flag) {
        isThread = flag;
    }
    public void clearLoc(boolean flag) {
        clearLo = flag;
    }

    // 시작 버튼 클릭 수
    private int startCount = 0;

    // 구글맵
    private GoogleMap gMap;

    // 위도, 경도 변경해보기
    double lati, longi;

    // 날짜 초기화
    LocalDate date = LocalDate.now();

    // 위도 경도로 거리구하기
    String num; // -> 거리
    //double totalkcal; // -> 칼로리
    public void changeLo(double lati, double longi) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati, longi), 15));
    }

    // 시작 종료 시간 구하기
    long startTime;
    long endTime;
    long pasueTime;
    long pasueStart; // -> 시작하고 멈추었을 때의 시간

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walk_main);
        // HealthMain.java에서 보낸 파이어베이스에 저장된 정보가져오기
        hm_intent = getIntent();

        // user_id로 판별하니까 계속 가지고 가야한다.
        firebase_id2 = hm_intent.getStringExtra("put-id");
        firebase_name = hm_intent.getStringExtra("put-name");
        firebase_age = hm_intent.getStringExtra("put-age");
        firebase_gender = hm_intent.getStringExtra("put-gender");
        firebase_height = hm_intent.getStringExtra("put-height");
        firebase_weight = hm_intent.getStringExtra("put-weight");

        System.out.println("firebase_id2 : "+firebase_id2);
        System.out.println("firebase_name : "+firebase_name);
        System.out.println("firebase_age : "+firebase_age);
        System.out.println("firebase_gender : "+firebase_gender);
        System.out.println("firebase_height : "+firebase_height);
        System.out.println("firebase_width : "+firebase_weight);

        // 현재위치 확인하기
        if (checkLocationServicesStatus()) {checkRunTimePermission();}
        else { showDialogForLocationServiceSetting();}

        gpsTracker = new GpsTracker(RunBicycle.this);

        double curlatitude = gpsTracker.getLatitude();
        double curlongitude = gpsTracker.getLongitude();

        // 선을 그릴 수 있게 한번더 현재 위치와 경도, 위도를 다른 변수에 담아줌
        lati = curlatitude;
        longi = curlongitude;
        lastx = lati;
        lasty = longi;

        // SupportMapFragment을 통해 레이아웃에 만든 fragment의 ID를 참조하고 구글맵을 호출한다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 시작버튼
        start = (Button) findViewById(R.id.start);
        // 위도, 경도 보여주기
        //tv1 = (TextView) findViewById(R.id.tv1);

        // 시작버튼 클릭
        start.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                // startCount가 1이면 멈추기 stop버튼에 startCount를 넣어주었다.
                if (startCount == 1) {return;}
                // 초 돌이기 시작 -> 칼로리 계산을 위하여 넣었다.
                startTime = System.currentTimeMillis();
                System.out.println("startTime : "+startTime);
                // 선그리기 시작 위치
                poloptions = new PolylineOptions();
                poloptions.add(new LatLng(lastx, lasty));
                // 시작버튼 클릭회수 확인하고 지도 지우기
                // 스레드 시작
                isThread = true;
                thread = new Thread() {
                    public void run() {
                        while (isThread) {
                            try {
                                if (isThread == false) {
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        System.out.println("스레드가 돕니다.");
                                        // 경도, 위도 더해주기
                                        lati += 0.0001;
                                        longi += 0.0001;
                                        changeLo(lati, longi);
                                        // 계속 add
                                        poloptions.add(new LatLng(lati, longi));
                                        // 선색 넣어주기
                                        Polyline polyline1 = gMap.addPolyline(poloptions.clickable(true));
                                        polyline1.setColor(COLOR_RED_ARGB);
                                        // 위도, 경도 변하는 거 보여주기
                                        //tv1.setText("위도, 경도 변화 / 시작 클릭횟수 : " + String.format("%.4f", lati) + ", " + String.format("%.4f", longi) + "/ " + startCount);
                                    }
                                });
                                // 1초 걸림
                                sleep(1000);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }

                    }
                };
                thread.start();
            }
        });

        // 스레드 중지와 종료
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasueTime = System.currentTimeMillis();
                pasueStart = pasueTime - startTime;
                System.out.println("pasueStart : "+pasueStart);
                Toast.makeText(getApplicationContext(), "잠시 멈추겠습니다.", Toast.LENGTH_SHORT).show();
                isThread = false;

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 초 돌이기 종료 -> 칼로리 계산을 위하여 넣었다.
                endTime = System.currentTimeMillis();
                System.out.println("endTime : "+endTime);
                isThread = false;
                gMap.clear();
                
                firstx = lati;
                firsty = longi;
                // 선 그리기
                poloptions = new PolylineOptions();
                poloptions.add(new LatLng(lati, longi));
                poloptions.add(new LatLng(lastx, lasty));
                Polyline polyline1 = gMap.addPolyline(poloptions.clickable(true));
                polyline1.setColor(COLOR_BLACK_ARGB);

                startCount = 1;

                // 대화상자
                //String getDistance;
                AlertDialog.Builder dlg = new AlertDialog.Builder(RunBicycle.this);
                dlg.setTitle("걸은 거리와 소모한 칼로리는?");
                dlg.setMessage("걸은 거리는 "+ getDistance(firstx, firsty, lastx, lasty) + "m 이고," + "\n" + "소모한 칼로리는 " + String.format("%.2f", kcal(endTime, startTime)) + "Kcal 입니다.");
                //dlg.setIcon(R.mipmap.ic_launcher); // -> 아이콘 보여주는 것

                // 대화상자 확인을 누르면 record 가 저장되어 파이어베이스로 넘어간다.
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 여기에서 확인버튼 누르고 거리와 날짜 저장되게 만들기
                        String emailId = firebase_id2;  // -> user.getEmailId();
                        System.out.println("emailId : " + emailId); // user01@naver.com 여기까지는 잘들어옵니다.
                         //record 하나 만들어주는 코드 그래야 Sequence가 하나씩 늘어가면서 누적을 보여줄 수 있다.
//                        long seq = 1;
//                        DatabaseReference ref = rootRef.child("Record").child(seq+"");
//                        Record record = new Record((int)seq, firebase_id2, 1, num, String.valueOf(date), String.format("%.2f", kcal(endTime, startTime)));
//                        ref.setValue(record);
//                        rootRef.child("Record").child("Sequence").setValue(seq +1);
                        rootRef.child("Record").child("Sequence").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { // rootRef.child("Record").child("Sequence").get() -> task
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) { // task 는 record 안에 있는 거
                                if(!task.isSuccessful()){
                                    System.out.println("실패");
                                }else{
                                    long seq = (Long)task.getResult().getValue();
                                    System.out.println("(Long)task.getResult().getValue()~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+(long)seq);
                                    //Record record = new Record((int)seq, emailId, 1, num, String.valueOf(date), String.format("%.2f", kcal(endTime, startTime)));
                                    record = new Record((int)seq, firebase_id2, 1, num, String.valueOf(date), String.format("%.2f", kcal(endTime, startTime)));
                                    rootRef.child("Record").child(seq+"").setValue(record);
                                    rootRef.child("Record").child("Sequence").setValue(seq +1);
                                    System.out.println("record emailId : "+record.getEmailId());
                                }
                            }
                        });
                        // 토스트

                        Toast.makeText(RunBicycle.this, "확인을 눌렀습니다", Toast.LENGTH_SHORT).show();
                        // -> 확인을 누르면 기록을 바로 볼 수 있도록 만듬
                        run_intent = new Intent(RunBicycle.this, HealthMain.class);
                        // Record 객체로 전송
                        //run_intent.putExtra("put-record",record);
                        // 유저 정보 다시 던지기
                        run_intent.putExtra("throw-id",firebase_id2);
                        run_intent.putExtra("throw-name",firebase_name);
                        run_intent.putExtra("throw-age",firebase_age);
                        run_intent.putExtra("throw-gender",firebase_gender);
                        run_intent.putExtra("throw-height",firebase_height);
                        run_intent.putExtra("throw-weight",firebase_weight);
                        run_intent.putExtra("throw-date", String.valueOf(date));
                        run_intent.putExtra("throw-distance",num);
                        run_intent.putExtra("throw-kcal",String.format("%.2f", kcal(endTime, startTime)));
                        startActivity(run_intent);
                        finish();
                    }
                });
                dlg.show();
            }
        });


    }
    // 칼로리 구하기
    public double kcal(long endTime, long startTime){
        // 걷기는 3.5 mets 이다.
        double runmets;
        // 3.5mets = 12.25ml/kg/min;
        long centerTime = pasueStart/1000;
        long whatTime = (endTime - startTime)/1000;
        System.out.println("centerTime : "+centerTime);
        System.out.println("whatTime : "+whatTime );
        // 정보 끌고 오기
        //Intent intent = getIntent();
        // 걷기 칼로리 계산하기 ->> firebase에 있는 유저 몸무게 가져와서 계산
        firebase_weight = hm_intent.getStringExtra("put-weight"); // -> null 발생함
        System.out.println("firebase_weight : "+firebase_weight);
        double userWeight = Double.parseDouble(firebase_weight); // -> null
        //일반적인 걷기 => 3.5mets = 12.25ml/kg/min;
        runmets = 12.25 * userWeight * (centerTime+whatTime); // 4초면 2450.0
        double onekcal = runmets * 0.005; // 1칼로리는 12.25
        double totalkcal = onekcal * (centerTime+whatTime); // 4초뛰면 49 칼로리
        System.out.println("runmets : "+runmets);
        System.out.println("onekcal : "+onekcal);
        System.out.println("totalkcal : "+totalkcal);
        return totalkcal;
    }

    // 지도 보여주기
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        if (ContextCompat.checkSelfPermission(RunBicycle.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            checkLocationPermissionWithRationale();
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치정보")
                        .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(RunBicycle.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    // 경도, 위도로 거리 구하기
    public String getDistance(double firstx, double firsty, double lastx, double lasty) {
        double distance;
        Location locationA = new Location("point A");
        locationA.setLatitude(firstx);
        locationA.setLongitude(firsty);
        Location locationB = new Location("point B");
        locationB.setLatitude(lastx);
        locationB.setLongitude(lasty);
        distance = locationA.distanceTo(locationB);
        if (distance > 1000) {
            distance = 500;
        } else if (distance == 0) {
            Random randomGenerator = new Random();
            int start = 1;
            int end = 20;
            double range = end - start + 1;
            int randomInt5to10 = (int) (randomGenerator.nextDouble() * range + start);
            distance = randomInt5to10 + randomGenerator.nextDouble();
        }
        if (distance == 500) {
            num = String.format("%.0f", distance) + "+";
            return num;
        } else {
            num = String.format("%.1f", distance);
            return num;
        }
    }

    // 시작 버튼 더블클릭 방지
    public abstract class OnSingleClickListener implements View.OnClickListener {
        //중복클릭시간차이
        private static final long MIN_CLICK_INTERVAL = 1000;

        //마지막으로 클릭한 시간
        private long mLastClickTime;

        public abstract void onSingleClick(View v);

        @Override
        public final void onClick(View v) {
            //현재 클릭한 시간
            long currentClickTime = SystemClock.uptimeMillis();
            //이전에 클릭한 시간과 현재시간의 차이
            long elapsedTime = currentClickTime - mLastClickTime;
            //마지막클릭시간 업데이트
            mLastClickTime = currentClickTime;

            //내가 정한 중복클릭시간 차이를 안넘었으면 클릭이벤트 발생못하게 return
            if (elapsedTime <= MIN_CLICK_INTERVAL)
                return;
            //중복클릭시간 아니면 이벤트 발생
            onSingleClick(v);
        }
    }

    /* ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다. */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                //위치 값을 가져올 수 있음 ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(RunBicycle.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RunBicycle.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(RunBicycle.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(RunBicycle.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(RunBicycle.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(RunBicycle.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RunBicycle.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RunBicycle.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }
    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RunBicycle.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
