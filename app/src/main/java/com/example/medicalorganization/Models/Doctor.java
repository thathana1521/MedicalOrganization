package com.example.medicalorganization.Models;

public class Doctor {

    public String Name;

    public String Surname;

    public String Age;

    public String Email;

    public Doctor(){
        // Default constructor required for calls to DataSnapshot.getValue(Doctor.class)
    }

    public Doctor(String name, String surname, String age,String email){

        this.Name = name;
        this.Surname = surname;
        this.Age = age;
        this.Email = email;
    }

}
