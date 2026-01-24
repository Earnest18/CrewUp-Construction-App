package com.example.ConstructionApp;

public class UserModel {

    private String userId;
    private String username;
    private String email;
    private String location;

    public UserModel() {
        // Required empty constructor
    }

    // GETTERS
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getLocation() { return location; }

    // SETTERS
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setLocation(String location) { this.location = location; }
}
