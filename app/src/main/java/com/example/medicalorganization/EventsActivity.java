package com.example.medicalorganization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.medicalorganization.Models.Event;
import com.example.medicalorganization.TimePickerFragment;

import java.util.Date;

public class EventsActivity extends AppCompatActivity implements TimePickerFragment.OnTimePickedListener {

    public Date dateTime = null;
    public Date startTime = null, endTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
    }


    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void createEvent(View view) {
        Intent intent = getIntent();
        String year = intent.getStringExtra("year");
        String month = intent.getStringExtra("month");
        String day = intent.getStringExtra("dayOfMonth");

        Date date = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));



        if (startTime != null && endTime != null) {
            Toast.makeText(getApplicationContext(), "start hour: " + startTime.getHours() + startTime.getMinutes(), Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "end hour: " + endTime.getHours()+ endTime.getMinutes(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Start Time or End Time is not set!", Toast.LENGTH_LONG).show();
        }
        Event event = new Event(date, startTime, endTime, false );
        if (event==null){
            Toast.makeText(getApplicationContext(),"Something went wrong with the creation of event.", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Event Created with date:"+event.date.toString()+" startTime: "+event.startTime.toString()+" endTime: "+event.endTime.toString(), Toast.LENGTH_LONG).show();

        }
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
}
