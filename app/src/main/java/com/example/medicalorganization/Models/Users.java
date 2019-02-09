package com.example.medicalorganization.Models;

public class Users {

    public String Name;

    public String Surname;

    public String Age;

    public String Email;

    public String Identity;

    public Users(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String name, String surname, String age,String email, String identity ){

        this.Name = name;
        this.Surname = surname;
        this.Age = age;
        this.Email = email;
        this.Identity = identity;
    }
}
