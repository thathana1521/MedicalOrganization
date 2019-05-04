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

import com.example.medicalorganization.Models.NotificationPanel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

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
        NotifRef = mFirebaseDatabase.getInstance().getReference().child("Doctors").child(mAuth.getCurrentUser().getUid()).child("Notifications");
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        DoctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("Notifications")) {
                        FirebaseRecyclerOptions options =
                                new FirebaseRecyclerOptions.Builder<NotificationPanel>()
                                        .setQuery(NotifRef, NotificationPanel.class)
                                        .build();

                        FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder> adapter
                                = new FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotifViewHolder holder, int position, @NonNull final NotificationPanel model) {
                                String patientName = model.patientName;
                                String date = model.event.date.toString();

                                holder.setDescription(patientName + " has requested an appointment on " + date);
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ShowPopup(model);
                                    }
                                });
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
                    else{
                        Toast.makeText(getApplicationContext(), "No available Notifications", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //vres ta notifications pou exei o giatros apo firebase. isws xreiastei getextras
    }
    public static class NotifViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView description;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescription(String disc){
            description = (TextView)mView.findViewById(R.id.appointment);
            description.setText(disc);
        }
    }

    private void ShowPopup(final NotificationPanel panel){
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
                //send notification to patient that the appointment accepted
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

}
