package com.example.medicalorganization;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<NotificationPanel> list = new ArrayList<>();
    private RecyclerView.Adapter adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference DoctorsRef, NotifRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Toast.makeText(getApplicationContext(), mAuth.getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
        DoctorsRef = mFirebaseDatabase.getInstance().getReference().child("Doctors").child(mAuth.getCurrentUser().getUid());
        NotifRef = mFirebaseDatabase.getInstance().getReference().child("Doctors").child(mAuth.getCurrentUser().getUid()).child("Notifications");
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        DoctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Toast.makeText(getApplicationContext(), mAuth.getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
                    if (dataSnapshot.hasChild("Notifications")) {
                        FirebaseRecyclerOptions options =
                                new FirebaseRecyclerOptions.Builder<NotificationPanel>()
                                        .setQuery(NotifRef, NotificationPanel.class)
                                        .build();

                        FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder> adapter
                                = new FirebaseRecyclerAdapter<NotificationPanel, NotifViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotifViewHolder holder, int position, @NonNull NotificationPanel model) {
                                String patientName = model.patientName;
                                String date = model.event.date.toString();

                                holder.setDescription(patientName + " has requested an appointment on " + date);
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

}
