package com.example.medicalorganization;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
    }

    public void openProfileActivity(View view) {startActivity(new Intent(this,ProfileActivity.class));}

    public void openEventsActivity(View view) {startActivity(new Intent(this,CalendarActivity.class));}
}
