package com.example.medicalorganization.Models;

import java.util.List;

public class Doctor {

    public String Name;

    public String Surname;

    public String Age;

    public String Email;

    public String Device_Token;

    public int Appointments;

    public Doctor(){
        // Default constructor required for calls to DataSnapshot.getValue(Doctor.class)
    }

    public Doctor(String name, String surname, String age,String email, String device_token, int appointments){

        this.Name = name;
        this.Surname = surname;
        this.Age = age;
        this.Email = email;
        this.Device_Token = device_token;
        this.Appointments = appointments;

    }

}
