package com.example.medicalorganization.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalorganization.AvailableEvents;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RateFragment extends Fragment {

    private View doctorsView;
    private RecyclerView myDoctorsList;
    private FirebaseAuth mAuth;
    public Dialog dialog;
    private RatingBar mRatingBar;
    private TextView mRatingTV;
    private Button sendFeedbackBtn;
    public ImageView closePopup;
    final String[] doctorId = {null};



    private DatabaseReference DoctorsRef;

    public RateFragment() {
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        doctorsView = inflater.inflate(R.layout.fragment_rate, container, false);

        mAuth = FirebaseAuth.getInstance();
        myDoctorsList = (RecyclerView) doctorsView.findViewById(R.id.doctors_list);

        DoctorsRef = FirebaseDatabase.getInstance().getReference().child("Doctors");

        myDoctorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        dialog = new Dialog(getContext());

        return doctorsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseSearch();

    }

    private void firebaseSearch() {
        final Query[] query = new Query[1];
        final DatabaseReference patientsRef = FirebaseDatabase.getInstance().getReference().child("Patients").child(mAuth.getCurrentUser().getUid());
        patientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Accepted Doctors")){
                    query[0] = patientsRef.child("Accepted Doctors");
                    displayAcceptedDoctors(query[0]);
                }
                else{
                    Toast.makeText(getContext(),"You have not made any appointments or you have already rated all the Doctors",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayAcceptedDoctors(Query query) {
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(query, Doctor.class)
                        .build();

        FirebaseRecyclerAdapter<Doctor, DoctorsFragment.DoctorsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Doctor, DoctorsFragment.DoctorsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final DoctorsFragment.DoctorsViewHolder holder, final int position, @NonNull final Doctor model) {

                holder.setDoctorName(model.Name + " " + model.Surname);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String doctorId = findDoctorId(model);
                        ShowPopup(doctorId);
                    }
                });
            }

            @NonNull
            @Override
            public DoctorsFragment.DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_doctor, viewGroup, false);
                DoctorsFragment.DoctorsViewHolder viewHolder = new DoctorsFragment.DoctorsViewHolder(view);
                return viewHolder;
            }
        };

        myDoctorsList.setAdapter(adapter);
        adapter.startListening();
    }

    private String findDoctorId(final Doctor model) {
        DatabaseReference docRef = FirebaseDatabase.getInstance().getReference().child("Doctors");

        docRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Doctor doctor = dataSnapshot1.getValue(Doctor.class);
                    if(doctor.Email.equals(model.Email)){
                        doctorId[0] = dataSnapshot1.getKey();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Toast.makeText(getContext(), doctorId[0], Toast.LENGTH_SHORT).show();
        return doctorId[0];
    }

    private void ShowPopup(final String doctorId) {
        dialog.setContentView(R.layout.popup_rate);
        mRatingBar = (RatingBar) dialog.findViewById(R.id.ratingBar);
        mRatingTV = (TextView)dialog.findViewById(R.id.tvRatingScale);
        sendFeedbackBtn = (Button)dialog.findViewById(R.id.btnSubmit);
        closePopup = (ImageView) dialog.findViewById(R.id.closeImageView);
        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                mRatingTV.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        mRatingTV.setText("Very bad");
                        break;
                    case 2:
                        mRatingTV.setText("Need some improvement");
                        break;
                    case 3:
                        mRatingTV.setText("Good");
                        break;
                    case 4:
                        mRatingTV.setText("Great");
                        break;
                    case 5:
                        mRatingTV.setText("Awesome. I love it");
                        break;
                    default:
                        mRatingTV.setText("");
                }
            }
        });

        sendFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRatingToDoctor(doctorId);
                //Toast.makeText(getContext(), "Thank you for sharing your feedback", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void addRatingToDoctor(String doctorId) {

    }


    public static class DoctorsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView doctorName;

        public DoctorsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDoctorName(String name) {
            doctorName = (TextView) mView.findViewById(R.id.doctorName);
            doctorName.setText(name);
        }
    }
}
