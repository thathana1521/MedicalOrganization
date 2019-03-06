package com.example.medicalorganization;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    Button button;
    TextView myDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = (CalendarView)findViewById(R.id.calendarView);
        myDate = (TextView)findViewById(R.id.myDate);
        button = (Button)findViewById(R.id.button);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Toast.makeText(CalendarActivity.this,dayOfMonth + "-" + (month+1) + "-" +year, Toast.LENGTH_LONG ).show();
                Intent intent = new Intent(getApplicationContext(), EventsActivity.class);

                intent.putExtra("year", String.valueOf(year));
                intent.putExtra("month", String.valueOf(month));
                intent.putExtra("dayOfMonth", String.valueOf(dayOfMonth));

                Intent profileIntent = getIntent();
                String doctorName = profileIntent.getStringExtra("doctorName");

                intent.putExtra("doctorName", doctorName);

                startActivity(intent);
            }
        });
    }
}
