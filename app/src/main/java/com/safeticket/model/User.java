package com.safeticket.model;

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String idNumber;    // ID card number
    private String phoneNumber;
    private String email;       // Standard field for auth
    private boolean isVerified; // Is the user identity verified?
    private double rating;      // Seller rating (0-5)

    // Empty constructor (Required for Firebase)
    public User() { }

    public User(String userId, String firstName, String lastName, String idNumber, String phoneNumber, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isVerified = false; // Default value
        this.rating = 0.0;       // Default value
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}