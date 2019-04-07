package com.example.medicalorganization.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DoctorsFragment extends Fragment {

    private View doctorsView;
    private RecyclerView myDoctorsList;

    private DatabaseReference DoctorsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public DoctorsFragment() {
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        doctorsView = inflater.inflate(R.layout.fragment_doctors, container, false);

        myDoctorsList = (RecyclerView) doctorsView.findViewById(R.id.doctors_list);

        DoctorsRef = FirebaseDatabase.getInstance().getReference().child("Doctors");


        myDoctorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return doctorsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(DoctorsRef, Doctor.class)
                        .build();

        FirebaseRecyclerAdapter<Doctor, DoctorsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Doctor, DoctorsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final DoctorsViewHolder holder, int position, @NonNull Doctor model) {

                holder.setDoctorName(model.Name + " " + model.Surname);

            }

            @NonNull
            @Override
            public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_doctor, viewGroup, false);
                DoctorsViewHolder viewHolder = new DoctorsViewHolder(view);
                return viewHolder;
            }
        };

        myDoctorsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class DoctorsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView doctorName;
        public DoctorsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDoctorName(String name){
            doctorName = (TextView) mView.findViewById(R.id.doctorName);
            doctorName.setText(name);
        }
    }
}
