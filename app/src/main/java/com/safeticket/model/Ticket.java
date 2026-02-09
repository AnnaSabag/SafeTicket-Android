package com.safeticket.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Ticket {
    @DocumentId
    private String ticketId;
    private String sellerId;
    private String eventName;
    private String category;
    private String location;
    private String country;
    private String eventDate; // שונה ל-String לצורך DatePicker
    private String eventTime;
    private double originalPrice;
    private double askingPrice;
    private String ticketImage;
    private String sellerName;
    private String sellerPhone;
    private boolean isActive;

    public Ticket() {}

    public Ticket(String ticketId, String sellerId, String eventName, String location, String country,
                  String eventDate, double originalPrice, double askingPrice, String category,
                  String ticketImage, String sellerName, String sellerPhone) {
        this.ticketId = ticketId;
        this.sellerId = sellerId;
        this.eventName = eventName;
        this.location = location;
        this.country = country;
        this.eventDate = eventDate;
        this.originalPrice = originalPrice;
        this.askingPrice = askingPrice;
        this.category = category;
        this.ticketImage = ticketImage;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.isActive = true;
    }

    // Getters & Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public double getAskingPrice() { return askingPrice; }
    public void setAskingPrice(double askingPrice) { this.askingPrice = askingPrice; }
    public String getTicketImage() { return ticketImage; }
    public void setTicketImage(String ticketImage) { this.ticketImage = ticketImage; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    @PropertyName("isActive")
    public boolean getIsActive() { return isActive; }
    @PropertyName("isActive")
    public void setIsActive(boolean active) { isActive = active; }
}