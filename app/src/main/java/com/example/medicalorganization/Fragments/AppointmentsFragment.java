package com.example.medicalorganization.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalorganization.AvailableEvents;
import com.example.medicalorganization.Models.Appointment;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Event;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentsFragment extends Fragment {


    private View v;
    private RecyclerView appointmentsList;

    private DatabaseReference AppointmentsRef;
    private FirebaseAuth mAuth;

    public AppointmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_appointments,container,false);
        appointmentsList = (RecyclerView)v.findViewById(R.id.appoinments_list);

        mAuth=FirebaseAuth.getInstance();
        AppointmentsRef = FirebaseDatabase.getInstance().getReference().child("Patients").child(mAuth.getCurrentUser().getUid());

        appointmentsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        AppointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Events")){
                    firebaseShowEvents();
                }
                else{
                    Toast.makeText(getContext(), "You have not booked any appointments, or the doctor have not accept one yet.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void firebaseShowEvents() {

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(AppointmentsRef.child("Events"), Event.class)
                        .build();

        FirebaseRecyclerAdapter<Event, AppointmentsFragment.EventsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Event, AppointmentsFragment.EventsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AppointmentsFragment.EventsViewHolder holder, final int position, @NonNull final Event model) {

                String startingTime = model.startTime.getHours() + ":" + model.startTime.getMinutes();
                String endingTime = model.endTime.getHours() + ":" + model.endTime.getMinutes();

                holder.setDoctorName(model.doctorName);
                holder.setDate(model.date.getDay() + "-" + model.date.getMonth() + "-" + model.date.getYear());
                holder.setHour(startingTime + " - " + endingTime);

            }

            @NonNull
            @Override
            public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
                AppointmentsFragment.EventsViewHolder viewHolder = new AppointmentsFragment.EventsViewHolder(view);
                return viewHolder;
            }
        };

        appointmentsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView doctorName;
        TextView dateTv;
        TextView hourTv;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date){
            dateTv = (TextView) mView.findViewById(R.id.event_date);
            dateTv.setText(date);
        }

        public void setHour(String hour){
            hourTv = (TextView) mView.findViewById(R.id.event_hour);
            hourTv.setText(hour);
        }

        public void setDoctorName(String name) {
            doctorName = (TextView) mView.findViewById(R.id.name);
            doctorName.setText(name);
        }
    }
}
