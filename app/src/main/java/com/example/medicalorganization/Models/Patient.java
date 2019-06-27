package com.example.medicalorganization.Models;

public class Patient {

    public String Name;

    public String Surname;

    public String Age;

    public String Email;

    public String Device_Token;

    public String Phone;

    public int Appointments;

    public Patient(){
        // Default constructor required for calls to DataSnapshot.getValue(Doctor.class)
    }

    public Patient(String name, String surname, String age, String email, String phone, String device_Token, int appointments){

        this.Name = name;
        this.Surname = surname;
        this.Age = age;
        this.Email = email;
        this.Phone = phone;
        this.Device_Token = device_Token;
        this.Appointments = appointments;
    }

}