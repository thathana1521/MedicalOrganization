package com.example.medicalorganization;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.medicalorganization.Models.Doctor;

import com.example.medicalorganization.Models.Patient;
import com.example.medicalorganization.Models.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFireDatabase;
    private DatabaseReference mDoctorsReference, mPatientReference;
    private TextView name, identity, mail, verifiedEmail, appointments, ratingTextView, ratingTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = (TextView) findViewById(R.id.nameTextView);
        identity = (TextView) findViewById(R.id.identityTextView);
        mail = (TextView) findViewById(R.id.mailTextView);
        verifiedEmail = (TextView) findViewById(R.id.verifiedEmail);
        appointments = (TextView)findViewById(R.id.rantevouTextView);
        ratingTextView = (TextView)findViewById(R.id.ratingTextView);
        ratingTitle = (TextView)findViewById(R.id.ratingTitle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mFireDatabase = FirebaseDatabase.getInstance();
        mDoctorsReference = mFireDatabase.getReference().child("Doctors");
        mPatientReference = mFireDatabase.getReference().child("Patients");
        loadUserInformation();
    }

    private void loadUserInformation() {

        final String email = mAuth.getCurrentUser().getEmail();
        Log.e("CHECK",email);
        mDoctorsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Doctor doctor = data.getValue(Doctor.class);

                    if (doctor.Email.equals(email)) {
                        name.setText(doctor.Name + " " + doctor.Surname);
                        identity.setText("Doctor");
                        mail.setText(doctor.Email);

                        setDoctorsTextViews(data.getKey().toString());
                    }

                    //Check if user is verified email address, if not click to verify
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        verifiedEmail.setText("Email Verified");
                    } else {
                        verifiedEmail.setText("Email not Verified. Click to send Verification Email");
                        verifiedEmail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProfileActivity.this, "Verification email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mPatientReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Patient patient = data.getValue(Patient.class);
                    if (patient.Email.equals(email)) {
                        ratingTextView.setVisibility(View.INVISIBLE);
                        ratingTitle.setVisibility(View.INVISIBLE);
                        name.setText(patient.Name + " " + patient.Surname);
                        identity.setText("Patient");
                        mail.setText(patient.Email);
                        appointments.setText(String.valueOf(patient.Appointments));
                    }

                    //Check if user is verified email address, if not click to verify
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        verifiedEmail.setText("Email Verified");
                    } else {
                        verifiedEmail.setText("Email not Verified. Click to send Verification Email");
                        verifiedEmail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProfileActivity.this, "Verification email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

    private void setDoctorsTextViews(final String id) {

        ratingTextView.setVisibility(View.VISIBLE);
        ratingTitle.setVisibility(View.VISIBLE);
        ratingTitle.setText("Rating");
        mDoctorsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals(id)) {
                        Doctor doctor = data.getValue(Doctor.class);
                        //Toast.makeText(getApplicationContext(),"Doctors appointment "+ doctor.Appointments,Toast.LENGTH_LONG).show();
                        appointments.setText(String.valueOf(doctor.Appointments));
                        averageRating(id);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void averageRating(final String id) {
        mDoctorsReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Ratings")){
                    mDoctorsReference.child(id).child("Ratings").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Integer> ratings = new ArrayList<>();
                            for(DataSnapshot data: dataSnapshot.getChildren()){
                                Rating rating = data.getValue(Rating.class);
                                ratings.add(rating.rating);
                            }
                            int sum = 0;
                            int items = ratings.size();
                            for(Integer integer : ratings){
                                sum = sum + integer;
                            }
                            double average = sum/items;
                            ratingTextView.setText(String.valueOf(average));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    ratingTextView.setText(String.valueOf(0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if the user is not logged in
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        final String email = mAuth.getCurrentUser().getEmail();
        mDoctorsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Doctor doctor = data.getValue(Doctor.class);
                    if (doctor.Email.equals(email)) {
                        startActivity(new Intent(getApplicationContext(), DoctorActivity.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPatientReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Patient patient = data.getValue(Patient.class);
                    if (patient.Email.equals(email)) {
                        startActivity(new Intent(getApplicationContext(), PatientActivity.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

