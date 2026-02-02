package com.example.smartbasketapp;

public class RegisterRequest {
    private String Name;
    private String Surname;
    private String Email;
    private String password;

    public RegisterRequest(String name, String surname, String email, String password) {
        this.Name = name;
        this.Surname = surname;
        this.Email = email;
        this.password = password;
    }
}