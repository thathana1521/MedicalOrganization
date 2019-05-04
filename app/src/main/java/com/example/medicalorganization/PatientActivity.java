package com.example.medicalorganization;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFireDatabase;
    private DatabaseReference mPatientsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mFireDatabase = FirebaseDatabase.getInstance();
        mPatientsReference = mFireDatabase.getReference().child("Patients");
    }

    public void openProfileActivity(View view) {startActivity(new Intent(this,ProfileActivity.class));}

    public void openAppointmentsActivity(View view) {
        final String email = mAuth.getCurrentUser().getEmail();
        mPatientsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Patient patient = data.getValue(Patient.class);
                    if(patient.Email.equals(email)){
                        startActivity(new Intent(getApplicationContext(), AppointmentPatientActivity.class));
                    }
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

        switch (item.getItemId()){
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        return true;
    }
}
