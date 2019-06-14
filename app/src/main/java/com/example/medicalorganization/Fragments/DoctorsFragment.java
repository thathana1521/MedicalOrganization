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
import android.widget.EditText;
import android.support.v7.widget.SearchView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalorganization.Adapters.DoctorAdapter;
import com.example.medicalorganization.AvailableEvents;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class DoctorsFragment extends Fragment {

    private View doctorsView;
    private RecyclerView myDoctorsList;
    private SearchView searchBar;
    private ArrayList<Doctor> doctorList;

    private DatabaseReference DoctorsRef;

    public DoctorsFragment() {
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        doctorsView = inflater.inflate(R.layout.fragment_doctors, container, false);

        searchBar = (SearchView) doctorsView.findViewById(R.id.searchBar);
        myDoctorsList = (RecyclerView) doctorsView.findViewById(R.id.doctors_list);

        DoctorsRef = FirebaseDatabase.getInstance().getReference().child("Doctors");

        myDoctorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return doctorsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseSearch("");
        if (searchBar != null) {
            searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    firebaseSearch(newText);
                    return true;
                }
            });
        }
    }

    private void firebaseSearch(String search) {
        Query query;
        if(!(search.isEmpty())) {
            query = DoctorsRef.orderByChild("Surname").startAt(search).endAt(search + "\uf8ff");
        }
        else{
            query = DoctorsRef;
        }
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(query, Doctor.class)
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

