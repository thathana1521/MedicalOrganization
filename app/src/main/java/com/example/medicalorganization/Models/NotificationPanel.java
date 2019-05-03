package com.example.medicalorganization.Models;

public class NotificationPanel {

    public String patientName;

    public Event event;

    public boolean accepted;

    public String patientToken;

    public NotificationPanel(){}

    public NotificationPanel(String patientName, Event event, boolean accepted, String patientToken) {
        this.patientName = patientName;
        this.event = event;
        this.accepted = accepted;
        this.patientToken = patientToken;
    }
}
