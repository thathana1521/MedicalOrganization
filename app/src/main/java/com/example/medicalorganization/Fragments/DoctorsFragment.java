package com.example.medicalorganization.Fragments;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.medicalorganization.AvailableEvents;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DoctorsFragment extends Fragment {

    private View doctorsView;
    private RecyclerView myDoctorsList;

    private DatabaseReference DoctorsRef;

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
            protected void onBindViewHolder(@NonNull final DoctorsViewHolder holder, final int position, @NonNull final Doctor model) {

                holder.setDoctorName(model.Name + " " + model.Surname);

                //setting up the onclick listener for recyclerview
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //jump to available events of doctor. passing doctorId to the AvailableEventsActivity
                        Intent intent = new Intent(getActivity(), AvailableEvents.class);
                        String doctorId = getRef(position).getKey();
                        intent.putExtra("doctorToken", model.Device_Token);
                        intent.putExtra("doctorId", doctorId);
                        startActivity(intent);
                    }
                });
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
