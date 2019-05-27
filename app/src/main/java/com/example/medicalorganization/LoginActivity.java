package com.example.medicalorganization;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDoctorsDatabaseReference, mPatientsDatabaseReference;
    private FirebaseAuth mAuth;

    private EditText email, password;
    private ProgressBar progressBar;
    public int identityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDoctorsDatabaseReference = mFirebaseDatabase.getReference().child("Doctors");
        mPatientsDatabaseReference = mFirebaseDatabase.getReference().child("Patients");
        mAuth = FirebaseAuth.getInstance();
        email =(EditText)findViewById(R.id.editText_email);
        password = (EditText)findViewById(R.id.editText_password);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

    }

    public void OnLogin(View view) {

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    //you have successfully logged
                    final String currentUserId = mAuth.getCurrentUser().getUid();
                    final String[] token = new String[1];
                    //GET REGISTRATION TOKEN
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if(task.isSuccessful()){
                                        //get registration token
                                        token[0] = task.getResult().getToken();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Token not generated" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    //set the device token on database if doctor
                    mDoctorsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserId)){
                                mDoctorsDatabaseReference.child(currentUserId).child("Device_Token")
                                        .setValue(token[0])
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    finish();
                                                    Intent intent = new Intent(getApplicationContext(), DoctorActivity.class);
                                                    //clear all activities on the top of the stack, when back button is pressed
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //set the device token on database if patient
                    mPatientsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserId)){
                                mPatientsDatabaseReference.child(currentUserId).child("Device_Token")
                                        .setValue(token[0])
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    finish();
                                                    Intent intent = new Intent(getApplicationContext(), PatientActivity.class);
                                                    //clear all activities on the top of the stack, when back button is pressed
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(),Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if the user is already logged in
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
        }

    }

    public void OpenRegister(View view) {
        finish();
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void doctorClick(View view) {
        identityId=1;
        //identity_doctor.setChecked(false);
    }

    public void patientClick(View view) {
        identityId=0;
        //identity_patient.setChecked(false);
    }
}
