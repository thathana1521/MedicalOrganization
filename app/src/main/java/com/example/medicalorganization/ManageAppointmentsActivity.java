package com.example.medicalorganization;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalorganization.Interfaces.Api;
import com.example.medicalorganization.Models.Event;
import com.example.medicalorganization.Models.NotificationPanel;
import com.example.medicalorganization.Models.Patient;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageAppointmentsActivity extends AppCompatActivity {

    private DatabaseReference doctorReference, EventsReference;
    private FirebaseAuth mAuth;
    private RecyclerView myEventsList;

    public ImageView closePopup;
    public Button yesButton, noButton;
    public Dialog dialog;
    public TextView cancelTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_appointments);

        dialog = new Dialog(this);
        mAuth=FirebaseAuth.getInstance();
        doctorReference= FirebaseDatabase.getInstance().getReference().child("Doctors")
                .child(mAuth.getCurrentUser().getUid());

        myEventsList = (RecyclerView)findViewById(R.id.appointments_list);
        myEventsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        myEventsList.setHasFixedSize(true);

        doctorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Events")){
                    EventsReference = doctorReference.child("Events");
                    final Query query = EventsReference.orderByChild("accepted").equalTo(true);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()){
                                FirebaseRecyclerOptions options =
                                        new FirebaseRecyclerOptions.Builder<Event>()
                                                .setQuery(query, Event.class)
                                                .build();


                                FirebaseRecyclerAdapter<Event, ManageAppointmentsActivity.EventViewHolder> adapter
                                        = new FirebaseRecyclerAdapter<Event, ManageAppointmentsActivity.EventViewHolder>(options) {
                                    @Override
                                    protected void onBindViewHolder(@NonNull final ManageAppointmentsActivity.EventViewHolder holder, final int position, @NonNull final Event model) {
                                        String date = model.date.getDay() + "-" + model.date.getMonth() + "-" + model.date.getYear();
                                        String startingTime = model.startTime.getHours() + ":" + model.startTime.getMinutes();
                                        String endingTime = model.endTime.getHours() + ":" + model.endTime.getMinutes();
                                        String patientName = model.patientName;
                                        final String eventId = getRef(position).getKey();


                                        holder.setDate(date);
                                        holder.setHour(startingTime + " - " + endingTime);
                                        holder.setPatientName(patientName);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                showPopup(model, eventId);
                                            }
                                        });

                                    }

                                    @NonNull
                                    @Override
                                    public ManageAppointmentsActivity.EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
                                        ManageAppointmentsActivity.EventViewHolder viewHolder = new ManageAppointmentsActivity.EventViewHolder(view);
                                        return viewHolder;
                                    }
                                };

                                myEventsList.setAdapter(adapter);
                                adapter.startListening();

                            }
                            else{
                                Toast.makeText(getApplicationContext(),"No Appointments to manage.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(),"No Appointments to manage.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Some error occured", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    private void showPopup(final Event model, final String eventId) {
        dialog.setContentView(R.layout.popup_cancel_appointment);
        closePopup =(ImageView) dialog.findViewById(R.id.closeImageView1);
        yesButton = (Button)dialog.findViewById(R.id.yes_button);
        noButton = (Button)dialog.findViewById(R.id.no_button);
        cancelTV = (TextView)dialog.findViewById(R.id.cancelTV);

        cancelTV.setText("Do you want to cancel the appointment for " +
                model.patientName);

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotificationToPatient(model);
                makeEventNotAccepted(eventId);
                dialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void makeEventNotAccepted(String eventId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Doctors")
                .child(mAuth.getCurrentUser().getUid())
                .child("Events")
                .child(eventId)
                .child("accepted");
        ref.setValue(false);
    }

    private void sendNotificationToPatient(Event event) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://medicalorganization-7b35a.firebaseapp.com/api1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);
        Call<ResponseBody> call = api.sendNotification(event.patientToken, "Appointment Canceled", "Your appointment has been canceled by the doctor " + event.doctorName +
                ". Please choose another appointment");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Toast.makeText(getApplicationContext(),"Notification about cancelling sent to the Patient", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Some error occured. The patient did not receive the notification", Toast.LENGTH_LONG).show();
            }
        });


    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView event_date, event_hour, patient_name;

        public EventViewHolder(@NonNull View itemView) {
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

        public void setPatientName(String patientName){
            patient_name = (TextView)mView.findViewById(R.id.name);
            patient_name.setText(patientName);
        }

    }
}
