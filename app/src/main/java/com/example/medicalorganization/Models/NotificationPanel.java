package com.example.medicalorganization.Models;

public class NotificationPanel {

    public String patientName;

    public Event event;
    public String eventId;

    public boolean accepted;

    public String patientToken;
    public String patientId;

    public NotificationPanel(){}

    public NotificationPanel(String patientName, Event event, String eventId, boolean accepted, String patientToken, String patientID) {
        this.patientName = patientName;
        this.event = event;
        this.eventId = eventId;
        this.accepted = accepted;
        this.patientToken = patientToken;
        this.patientId = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getPatientToken() {
        return patientToken;
    }

    public void setPatientToken(String patientToken) {
        this.patientToken = patientToken;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
