package com.example.test_dot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Queue;

public class RecordCheck extends AppCompatActivity {
    LinearLayout mRecordLayout;
    // 파이어베이스 실시간 db 관리 객체 얻어오기
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    // 저장시킬 노드 참조 객체 가져오기
    //DatabaseReference rootRef = database.getReference("DB"); // () 안에 아무 것도 안쓰면 최상위 노드
    DatabaseReference rootRef;
    Intent run_intent;
    Intent goToRecord;
    ListView userRunData;

    LocalDate date = LocalDate.now();
    // 파이어베이스에 저장된 내용 가져오기
    private String firebase_id;
    private String firebase_rundate;
    private String firebase_runnum;
    private String firebase_runkcal;
    //
    //
    ArrayList<String> midList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);

        // listview
        userRunData = (ListView) findViewById(R.id.listview);
        // initializing our array list
        //final ArrayList<String> midList = new ArrayList<String>();
        midList = new ArrayList<String>();

        // calling a method to get data from
        // Firebase and set data to list view
        initializeListView();



    }
//        Query myQ = rootRef.child("Record").orderByChild("type").equalTo(1);
//        myQ.addValueEventListener(new ValueEventListener() {
//              @Override
//              public void onDataChange(@NonNull DataSnapshot snapshot) {
//                  StringBuffer buffer = new StringBuffer();
//                  for (DataSnapshot ds : snapshot.getChildren()) {
//                      midList.clear();
//
//                      Record record = ds.getValue(Record.class);
//                      String date = record.getDate();
//                      String num = record.getNum();
//                      String kal = record.getKcal();
//
//                      buffer.append(date + " , " + num + " , "+ kal + "\n");
//
//                      midList.add(buffer.toString());
//                  }
//                  userRunData.setAdapter(adapter);
//                  System.out.println(buffer);
//              }
//
//              @Override
//              public void onCancelled(@NonNull DatabaseError error) {
//
//              }
//        });

    private void initializeListView() {
        // creating a new array adapter for our list view
        // 기존에 있는 layout말고 customlayout으로 바꿈
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, midList);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.records_layout, midList);

        // below line is used for getting reference
        // of our Firebase Database.
        rootRef = FirebaseDatabase.getInstance().getReference("DB");

        // in below line we are calling method for add child event
        // listener to get the child of our database.
        rootRef.child("Record").orderByChild("type").equalTo(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when new child is added to
                // our data base and after adding new child
                // we are adding that item inside our array list and
                // notifying our adapter that the data in adapter is changed.
                Record record = snapshot.getValue(Record.class);
                midList.add("\t\t"+record.getDate()+"\t\t\t\t\t\t\t"+record.getNum()+"m\t\t\t\t\t\t\t"+record.getKcal()+"Kcal");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when the new child is added.
                // when the new child is added to our list we will be
                // notifying our adapter that data has changed.
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // below method is called when we remove a child from our database.
                // inside this method we are removing the child from our array list
                // by comparing with it's value.
                // after removing the data we are notifying our adapter that the
                // data has been changed.
                midList.remove(snapshot.getValue(Record.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when we move our
                // child in our database.
                // in our code we are note moving any child.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // this method is called when we get any
                // error from Firebase with error.
            }
        });
        // below line is used for setting
        // an adapter to our list view.
        userRunData.setAdapter(adapter);
    }


}
