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

    private List<String> notificationsIdList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference DoctorsRef, NotifRef;

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
                    //save the keys of notifications on list
                    NotifRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()){

                                notificationsIdList.add(data.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    FirebaseRecyclerOptions options =
                            new FirebaseRecyclerOptions.Builder<NotificationPanel>()
                                    .setQuery(notifQuery, NotificationPanel.class)
                                    .build();

                    FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder> adapter
                            = new FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final NotifViewHolder holder, final int position, @NonNull final NotificationPanel model) {
                            //ean to notification den exei ginei apodekto tote mpainei sto recyclerview
                            if(!model.accepted) {
                                String patientName = model.patientName;
                                String date = model.event.date.toString();

                                holder.setDescription(patientName + " has requested an appointment on " + date);
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String id = notificationsIdList.get(position);
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

    private void ShowPopup(final NotificationPanel panel, final String id) {
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
                sendNotificationToPatient(panel, id);
                dialog.dismiss();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send notification to patient that the appointment rejected
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public void sendNotificationToPatient(final NotificationPanel panel, final String id) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://medicalorganization-7b35a.firebaseapp.com/api1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        Call<ResponseBody> call = api.sendNotification(panel.patientToken, "Your Request has been accepted", "");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Toast.makeText(getApplicationContext(), "Notification sent to the Patient", Toast.LENGTH_LONG).show();

                FirebaseDatabase.getInstance().getReference().child("Patients")
                        .child(mAuth.getCurrentUser().getUid())
                        .child("Notifications")
                        .child(id)
                        .child("accepted")
                        .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Event successfully accepted", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Some error occured. The doctor did not receive the notification", Toast.LENGTH_LONG).show();
            }
        });

    }
}
