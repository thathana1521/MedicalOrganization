package com.example.medicalorganization;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.medicalorganization.Fragments.DoctorsFragment;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Event;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AvailableEvents extends AppCompatActivity {


    private RecyclerView myEventsList;

    private String received_user_id;
    private DatabaseReference EventReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_events);

        myEventsList = (RecyclerView) findViewById(R.id.events_list);
        received_user_id = getIntent().getExtras().get("doctorId").toString();
        //BOOM an den exei orisei events o giatros
        EventReference = FirebaseDatabase.getInstance().getReference().child("Doctors").child(received_user_id).child("Events");

        myEventsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(EventReference, Event.class)
                        .build();

        FirebaseRecyclerAdapter<Event, EventsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Event, AvailableEvents.EventsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AvailableEvents.EventsViewHolder holder, final int position, @NonNull final Event model) {

                String date = model.date.getDay() + "-" + model.date.getMonth() + "-" + model.date.getYear();
                String startingTime = model.startTime.getHours() + ":" + model.startTime.getMinutes();
                String endingTime = model.endTime.getHours() + ":" + model.endTime.getMinutes();

                holder.setDate(date);
                holder.setHour(startingTime + " - " + endingTime);

            }

            @NonNull
            @Override
            public AvailableEvents.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
                AvailableEvents.EventsViewHolder viewHolder = new AvailableEvents.EventsViewHolder(view);
                return viewHolder;
            }
        };

        myEventsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView event_date, event_hour;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            event_date = (TextView)mView.findViewById(R.id.event_date);
            event_date.setText(date);
        }

        public void setHour(String hour){
            event_hour = (TextView)mView.findViewById(R.id.event_hour);
            event_hour.setText(hour);
        }

    }
}
