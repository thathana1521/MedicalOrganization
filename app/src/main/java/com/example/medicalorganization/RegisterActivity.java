package com.example.medicalorganization;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.Models.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText name, surname, age, password, email;
    private String str_identity; //declared within on register method
    private ProgressBar progressBar;
    private CheckBox identity_doctor, identity_patient;
    public int identityId;

    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = (EditText)findViewById(R.id.editText_name);
        surname = (EditText)findViewById(R.id.editText_surname);
        age = (EditText)findViewById(R.id.editText_age);
        email =(EditText)findViewById(R.id.editText_email);
        password = (EditText)findViewById(R.id.editText_password);
        identity_doctor = (CheckBox)findViewById(R.id.checkBox_doctor);
        identity_patient = (CheckBox)findViewById(R.id.checkBox_patient);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //check if the current user has already logged in
        if(mAuth.getCurrentUser() != null){
            //handle the already login user
        }

    }

    public void doctorClick(View view) {
        identityId=1;
        //identity_doctor.setChecked(false);
    }

    public void patientClick(View view) {
        identityId=0;
        //identity_patient.setChecked(false);
    }

    public void OnRegister(View view) {
        progressBar.setVisibility(View.VISIBLE);
        final String str_name = name.getText().toString().trim();
        final String str_surname = surname.getText().toString().trim();
        final String str_age = age.getText().toString();
        final String str_email =email.getText().toString().trim();
        final String str_password = password.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(str_email, str_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //we will store the additional fields in firebase database
                            if(identityId == 1){
                                //if Doctor
                                Doctor doctor = new Doctor(str_name, str_surname, str_age, str_email);
                                FirebaseDatabase.getInstance().getReference("Doctors")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(doctor).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if(task.isSuccessful()){
                                            finish();
                                            Toast.makeText(RegisterActivity.this, "Doctor registered Successfully", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                            //clear all activities on the top of the stack, when back button is pressed
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                        else {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                Toast.makeText(RegisterActivity.this, "You are already registered", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                            }
                            else {
                                //if Patient
                                Patient patient = new Patient(str_name, str_surname, str_age, str_email);
                                FirebaseDatabase.getInstance().getReference("Patients")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if(task.isSuccessful()){
                                            finish();
                                            Toast.makeText(RegisterActivity.this, "Patient registered Successfully", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                            //clear all activities on the top of the stack, when back button is pressed
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                        else {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                Toast.makeText(RegisterActivity.this, "You are already registered", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                            }


                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
