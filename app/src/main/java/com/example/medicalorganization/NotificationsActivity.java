package com.example.medicalorganization;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalorganization.Interfaces.Api;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Event;
import com.example.medicalorganization.Models.NotificationPanel;
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

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference DoctorsRef, NotifRef, EventsRef;

    public Dialog dialog;
    public Button acceptButton, rejectButton;
    public ImageView closePopup;
    public TextView descriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dialog = new Dialog(this);

        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        DoctorsRef = mFirebaseDatabase.getInstance().getReference().child("Doctors").child(mAuth.getCurrentUser().getUid());
        NotifRef = (DatabaseReference) mFirebaseDatabase.getInstance().getReference().child("Doctors").child(mAuth.getCurrentUser().getUid()).child("Notifications");
        final Query notifQuery = NotifRef.orderByChild("accepted").equalTo(false);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        DoctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Notifications")) {

                    notifQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()){
                                displayNotifications(notifQuery);
                            }else {
                                Toast.makeText(getApplicationContext(), "No available Notifications", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "No available Notifications", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void displayNotifications(Query notifQuery) {
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<NotificationPanel>()
                        .setQuery(notifQuery, NotificationPanel.class)
                        .build();

        FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder> adapter
                = new FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotifViewHolder holder, final int position, @NonNull final NotificationPanel model) {
                //ean to notification den exei ginei apodekto tote mpainei sto recyclerview
                if (!model.accepted) {
                    String patientName = model.patientName;
                    String date = model.event.date.toString();

                    holder.setDescription(patientName + " has requested an appointment on " + date);
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String id = getRef(position).getKey();
                            ShowPopup(model, id);
                        }
                    });
                }
            }

            @NonNull
            @Override
            public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
                NotifViewHolder viewHolder = new NotifViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView description;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescription(String disc) {
            description = (TextView) mView.findViewById(R.id.appointment);
            description.setText(disc);
        }
    }

    private void ShowPopup(final NotificationPanel panel, final String notificationId) {
        dialog.setContentView(R.layout.popup_appointment);
        closePopup = (ImageView) dialog.findViewById(R.id.closeImageView);
        acceptButton = (Button) dialog.findViewById(R.id.accept_button);
        rejectButton = (Button) dialog.findViewById(R.id.reject_button);
        descriptionTextView = (TextView) dialog.findViewById(R.id.patient_description);

        descriptionTextView.setText(panel.patientName + " requested the Appointment");

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotificationToPatient(panel, true);
                addEventOnPatient(panel.patientId, panel.event);
                addDoctorOnPatient(panel.patientId);
                makeNotificationAccepted(notificationId);
                addAppointmentOnDoctor();
                addAppointmentOnPatient(panel.patientId);
                makeEventAccepted(panel.eventId);
                dialog.dismiss();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotificationToPatient(panel, false);
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void addAppointmentOnPatient(final String patientId) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Patients");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){

                    Patient patient = data.getValue(Patient.class);
                    if(patient.Email.equals(mAuth.getCurrentUser().getEmail())){
                        int appointments = patient.Appointments;
                        appointments= appointments+1;

                        DatabaseReference appRef = ref.child(patientId).child("Appointments");
                        appRef.setValue(appointments);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeEventAccepted(String eventId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Doctors")
                .child(mAuth.getCurrentUser().getUid())
                .child("Events")
                .child(eventId)
                .child("accepted");
        ref.setValue(true);
    }

    private void addAppointmentOnDoctor() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Doctors");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){

                    Doctor doctor = data.getValue(Doctor.class);
                    if(doctor.Email.equals(mAuth.getCurrentUser().getEmail())){
                        int appointments = doctor.Appointments;
                        appointments= appointments+1;

                        DatabaseReference appRef = ref.child(mAuth.getCurrentUser().getUid()).child("Appointments");
                        appRef.setValue(appointments);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void makeNotificationAccepted(String notificationId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Doctors")
                .child(mAuth.getCurrentUser().getUid())
                .child("Notifications")
                .child(notificationId)
                .child("accepted");
                ref.setValue(true);
    }

    private void addDoctorOnPatient(final String patientId) {
        DatabaseReference docRef = FirebaseDatabase.getInstance().getReference().child("Doctors");
        final Boolean[] found = {true};
        docRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    final Doctor doctor = data.getValue(Doctor.class);
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Patients").child(patientId).child("Accepted Doctors");

                    if(mAuth.getCurrentUser().getEmail().equals(doctor.Email)){
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    for(DataSnapshot data : dataSnapshot.getChildren()){
                                        final Doctor doc = data.getValue(Doctor.class);
                                        if(mAuth.getCurrentUser().getEmail().equals(doc.Email)){
                                            found[0] = false;
                                        }
                                    }
                                    if(found[0]) {
                                        myRef.push().setValue(doctor);
                                    }
                                }
                                else{
                                    myRef.push().setValue(doctor);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addEventOnPatient(String patientID, Event event) {
        EventsRef = mFirebaseDatabase.getReference().child("Patients").child(patientID).child("Events");
        EventsRef.push().setValue(event);
    }

    public void sendNotificationToPatient(final NotificationPanel panel, boolean accepted) {
        String response;
        if(accepted){
            response="Your request has been accepted!";
        }
        else{
            response="Your request has been rejected. Please search for another appointment.";
        }
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://medicalorganization-7b35a.firebaseapp.com/api1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        Call<ResponseBody> call = api.sendNotification(panel.patientToken, response, "");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Toast.makeText(getApplicationContext(), "Notification sent to the Patient", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Some error occured. The patient did not receive the notification", Toast.LENGTH_LONG).show();
            }
        });

    }
}
