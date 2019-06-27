package com.example.medicalorganization;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.medicalorganization.Models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SetEventActivity extends AppCompatActivity implements TimePickerFragment.OnTimePickedListener, DatePickerDialog.OnDateSetListener {

    private Button btnSetDate, btnSetStHour, btnSetEndHour;

    public Date dateTime = null, date;
    public Date startTime = null, endTime = null;

    private FirebaseDatabase mFireDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mEventsDatabaseReference;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_event);

        btnSetDate = (Button)findViewById(R.id.btnSetDate);
        btnSetStHour = (Button)findViewById(R.id.btnSetStHour);
        btnSetEndHour = (Button)findViewById(R.id.btnSetEndHour);

        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        mFireDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String currentDoctorId = mAuth.getCurrentUser().getUid();
        mEventsDatabaseReference = mFireDatabase.getReference().child("Doctors").child(currentDoctorId).child("Events");
    }

    public void setDate(View view) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DatePickerDialog dialog = new DatePickerDialog(this, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public void setStartingHour(View view) {
        showTimePickerDialog(view);
    }

    public void setEndingHour(View view) {
        showTimePickerDialog(view);
    }

    public void createEvent(View view) {
        String doctorName = mAuth.getCurrentUser().getDisplayName();

        if (startTime == null || endTime == null) {
            Toast.makeText(getApplicationContext(), "Start Time or End Time is not set!", Toast.LENGTH_LONG).show();
        } else {
            Event event = new Event(date, startTime, endTime, doctorName, "No Patient choosed this event",null, null, mAuth.getCurrentUser().getUid(), false );
            if (event==null){
                Toast.makeText(getApplicationContext(),"Something went wrong with the creation of event.", Toast.LENGTH_LONG).show();
            }
            else {
                addEventToDatabase(event);
            }
        }

    }

    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    /*#toDo check better the views. This only works when setting first the start time and then the end time*/
    @Override
    public void onTimePicked(int hour, int minute) {
        Toast.makeText(getApplicationContext(), "Hour = " + hour + " minute = " + minute, Toast.LENGTH_LONG).show();
        dateTime = new Date(0, 0, 0, hour, minute);
        if(startTime == null){
            startTime = dateTime;
        }
        else {
            endTime = dateTime;
        }
    }
    public void addEventToDatabase(Event event){
        progressBar.setVisibility(View.VISIBLE);
        mEventsDatabaseReference.push().setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Event Created Successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new Date(year, month, dayOfMonth);
    }
}
