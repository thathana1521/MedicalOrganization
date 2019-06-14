package com.example.medicalorganization.Holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.medicalorganization.R;

public class DoctorHolder extends RecyclerView.ViewHolder {

    View mView;
    TextView doctorName;
    public DoctorHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }
    public void setDoctorName(String name){
        doctorName = (TextView) mView.findViewById(R.id.doctorName);
        doctorName.setText(name);
    }
}
