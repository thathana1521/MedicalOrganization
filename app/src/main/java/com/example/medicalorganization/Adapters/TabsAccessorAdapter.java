package com.example.medicalorganization.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.medicalorganization.Fragments.AppointmentsFragment;
import com.example.medicalorganization.Fragments.DoctorsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        //analogws se poio fragment apo ta tabs eimaste dialexe. an 0 tote doctors an 1 tote rantevou
        switch (i){
            case 0:
                DoctorsFragment doctorsFragment = new DoctorsFragment();
                return doctorsFragment;
            case 1:
                AppointmentsFragment appointmentsFragment = new AppointmentsFragment();
                return appointmentsFragment;
            default:
                return null;
        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Available Doctors";
            case 1:
                return "My Appointments";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
