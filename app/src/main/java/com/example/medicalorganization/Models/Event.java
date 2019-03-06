package com.example.medicalorganization.Models;

import java.util.Date;

public class Event {


    public java.util.Date startTime;
    public Date endTime;
    public java.util.Date date;
    public String doctorName;
    public String patientName;
    public boolean accepted;

    public Event() {}

    public Event(java.util.Date date, java.util.Date startTime, java.util.Date endTime, String doctorName, String patientName, boolean accepted) {

        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.accepted = accepted;
    }
}
