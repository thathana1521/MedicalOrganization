package com.example.medicalorganization;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.example.medicalorganization.Adapters.TabsAccessorAdapter;
import com.example.medicalorganization.Adapters.ViewPagerAdapter;
import com.example.medicalorganization.Fragments.AppointmentsFragment;
import com.example.medicalorganization.Fragments.DoctorsFragment;
import com.example.medicalorganization.Fragments.RateFragment;

public class AppointmentPatientActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_patient);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Medical Organization");

        tabLayout = (TabLayout)findViewById(R.id.tablayout_id);
        viewPager = (ViewPager)findViewById(R.id.viewpager_id);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //Add fragment Here
        adapter.AddFragment(new DoctorsFragment(), "Doctors");
        adapter.AddFragment(new AppointmentsFragment(), "My Appointments");
        adapter.AddFragment(new RateFragment(), "Rate");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_doctor);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_appointment);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_star);



        //Remove shadow from action bar
        AppCompatActivity activity;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);


    }
}
