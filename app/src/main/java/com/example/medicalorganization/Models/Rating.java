package com.example.medicalorganization.Models;

public class Rating {
    public int rating;
    public String PatientName;

    public Rating(){}

    public Rating(int rating, String patientName) {
        this.rating = rating;
        PatientName = patientName;
    }

}
