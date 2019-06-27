package com.example.medicalorganization;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
import android.widget.Toast;

import com.example.medicalorganization.Fragments.DoctorsFragment;
import com.example.medicalorganization.Interfaces.Api;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Event;
import com.example.medicalorganization.Models.NotificationPanel;
import com.example.medicalorganization.Models.Notifications;
import com.example.medicalorganization.Models.Patient;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;

public class AvailableEvents extends AppCompatActivity {


    private RecyclerView myEventsList;
    private FirebaseAuth mAuth;

    //Notification Channel
    public static final String CHANNEL_ID = "MedOrganization";
    private static final String CHANNEL_NAME = "MedOrganization Channel Name";
    private static final String CHANNEL_DESC = "MedOrganization Notifications";


    private String received_user_id, doctorToken;
    private DatabaseReference EventReference, checkReferenceForEvents, mPatientReference;
    private FirebaseDatabase mFireDatabase;
    private String patientName, patientToken, patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_events);

        myEventsList = (RecyclerView) findViewById(R.id.events_list);
        received_user_id = getIntent().getExtras().get("doctorId").toString();
        doctorToken = getIntent().getExtras().get("doctorToken").toString();
        mAuth = FirebaseAuth.getInstance();
        mFireDatabase = FirebaseDatabase.getInstance();
        mPatientReference = mFireDatabase.getReference().child("Patients");
        patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        EventReference = FirebaseDatabase.getInstance().getReference().child("Doctors").child(received_user_id).child("Events");
        checkReferenceForEvents = FirebaseDatabase.getInstance().getReference().child("Doctors").child(received_user_id);

        myEventsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Notification Channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        checkReferenceForEvents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //ean o giatros exei prosthesei kapoia events
                if (dataSnapshot.hasChild("Events")){
                    final Query query = EventReference.orderByChild("accepted").equalTo(false);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()){
                                FirebaseRecyclerOptions options =
                                        new FirebaseRecyclerOptions.Builder<Event>()
                                                .setQuery(query, Event.class)
                                                .build();

                                FirebaseRecyclerAdapter<Event, EventsViewHolder> adapter
                                        = new FirebaseRecyclerAdapter<Event, AvailableEvents.EventsViewHolder>(options) {
                                    @Override
                                    protected void onBindViewHolder(@NonNull final AvailableEvents.EventsViewHolder holder, final int position, @NonNull final Event model) {

                                        String date = model.date.getDay() + "-" + model.date.getMonth() + "-" + model.date.getYear();
                                        String startingTime = model.startTime.getHours() + ":" + model.startTime.getMinutes();
                                        String endingTime = model.endTime.getHours() + ":" + model.endTime.getMinutes();
                                        final String eventId = getRef(position).getKey();
                                        holder.setDate(date);
                                        holder.setHour(startingTime + " - " + endingTime);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //otan o asthenis kanei click se ena apo ta events na stelnei notification
                                                final Retrofit retrofit = new Retrofit.Builder()
                                                        .baseUrl("https://medicalorganization-7b35a.firebaseapp.com/api1/")
                                                        .addConverterFactory(GsonConverterFactory.create())
                                                        .build();
                                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                mPatientReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot data: dataSnapshot.getChildren()){
                                                            final Patient patient = data.getValue(Patient.class);
                                                            if(patient.Email.equals(user.getEmail())){
                                                                patientToken = patient.Device_Token;
                                                                setPatientName(patient.Name + " " + patient.Surname);
                                                            }
                                                            Api api = retrofit.create(Api.class);
                                                            Call<ResponseBody> call = api.sendNotification(doctorToken, "New appointment request", patientName + " has requested an appointment for " + model.date.getDate());

                                                            call.enqueue(new Callback<ResponseBody>() {
                                                                @Override
                                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                    setNotification(patientName, model, eventId, received_user_id, patientToken, patientId);
                                                                    setPatientCredentialsOnEvent(received_user_id, eventId, patient.Name + " " + patient.Surname, patientToken);
                                                                    Toast.makeText(getApplicationContext(),"Notification sent to the Doctor", Toast.LENGTH_LONG).show();

                                                                }

                                                                @Override
                                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                    Toast.makeText(getApplicationContext(),"Some error occured. The doctor did not receive the notification", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        });

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
                            else{
                                Toast.makeText(getApplicationContext(),"No available events for this Doctor", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                //ean den exei events o sugkekrimenos giatros totet vgale mhnuma kai termatise tin activity
                else {
                    Toast.makeText(getApplicationContext(), "No available Events for this doctor.\n Please choose another Doctor.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPatientCredentialsOnEvent(String doctorId, String eventId, String patientName, String patientToken) {
        FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorId)
                .child("Events")
                .child(eventId)
                .child("patientName")
                .setValue(patientName);

        FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorId)
                .child("Events")
                .child(eventId)
                .child("patientToken")
                .setValue(patientToken);

    }

    private void setNotification(String name, Event event, String eventId, String doctorID, String patientToken, String patientId) {
        NotificationPanel notificationPanel = new NotificationPanel(name, event, eventId, false, patientToken, patientId);
        FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorID)
                .child("Notifications")
                .push()
                .setValue(notificationPanel);
    }

    public void setPatientName(String name){
        patientName = name;
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
