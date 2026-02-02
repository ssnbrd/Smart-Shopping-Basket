package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;
public class LoginResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("token")
    private String token;

    public class User {


        @SerializedName("ID_user")
        private int idUser;

        @SerializedName("Name")
        private String name;

        @SerializedName("Surname")
        private String surname;

        @SerializedName("Email")
        private String email;

        @SerializedName("bankCard")
        private String bankCard;

        public int getIdUser() {
            return idUser;
        }

        public String getName() {
            return name;
        }

        public String getSurname() {
            return surname;
        }

        public String getEmail() {
            return email;
        }

        public String getBankCard() {
            return bankCard;
        }
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public User getUser() { return user; }
    public String getToken() { return token; }
}