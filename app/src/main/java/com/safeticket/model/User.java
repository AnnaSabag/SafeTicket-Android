package com.safeticket.model;

public class User {
    private String userId, firstName, lastName, idNumber, phoneNumber, email;
    private boolean isVerified;
    private String profileImageBase64, idCardBase64, selfieImageBase64;
    public User() {} // Empty constructor, required for Firebase
    public User(String userId, String firstName, String lastName, String idNumber, String phoneNumber, String email) { // Constructor
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isVerified = false;
    }
    // Getters & Setters
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
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public String getIdCardBase64() { return idCardBase64; }
    public void setIdCardBase64(String idCardBase64) { this.idCardBase64 = idCardBase64; }
    public String getSelfieImageBase64() { return selfieImageBase64; }
    public void setSelfieImageBase64(String selfieImageBase64) { this.selfieImageBase64 = selfieImageBase64; }
}