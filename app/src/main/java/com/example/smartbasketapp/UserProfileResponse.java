package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("user")
    private UserData user;
    public static class UserData {
        @SerializedName("Name")
        private String name;
        @SerializedName("Surname")
        private String surname;
        @SerializedName("Email")
        private String email;
        @SerializedName("bankCard")
        private String bankCard;

        public String getName() { return name; }
        public String getSurname() { return surname; }
        public String getEmail() { return email; }
        public String getBankCard() { return bankCard; }
    }

    public String getStatus() { return status; }
    public UserData getUser() { return user; }
}