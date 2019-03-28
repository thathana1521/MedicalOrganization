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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.medicalorganization.Models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFireDatabase;
    private DatabaseReference mReference;
    private TextView name, identity, rantevou, mail, verifiedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = (TextView)findViewById(R.id.nameTextView);
        identity = (TextView)findViewById(R.id.identityTextView);
        mail = (TextView)findViewById(R.id.mailTextView);
        verifiedEmail = (TextView) findViewById(R.id.verifiedEmail);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        mFireDatabase = FirebaseDatabase.getInstance();
        mReference = mFireDatabase.getReference().child("Users");

        loadUserInformation();
    }

    private void loadUserInformation() {

        final String email = mAuth.getCurrentUser().getEmail();
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Users user = data.getValue(Users.class);
                    if(user.Email.equals(email)){
                        name.setText(user.Name + " " + user.Surname);
                        identity.setText(user.Identity);
                        mail.setText(user.Email);
                    }

                    //Check if user is verified email address, if not click to verify
                    if(mAuth.getCurrentUser().isEmailVerified()){
                        verifiedEmail.setText("Email Verified");
                    }
                    else {
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

    public void createEvent(View view){
        final String email = mAuth.getCurrentUser().getEmail();
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Users user = data.getValue(Users.class);
                    //Toast.makeText(getApplicationContext(),"Identity = "+user.Identity,Toast.LENGTH_LONG).show();
                    //Ean o xrhsths uparxei sti vasi kai einai giatros tote anoixe to calendar view gia na ftiaxei diathesimo rantevou
                    if (user.Email.equals(email) && user.Identity.equals("Doctor")) {
                        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                        String doctorName = user.Name + " " + user.Surname;
                        intent.putExtra("doctorName", doctorName);
                        startActivity(intent);
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

    @Override
    protected void onStart() {
        super.onStart();
        //check if the user is not logged in
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,UserActivity.class));
    }
}
