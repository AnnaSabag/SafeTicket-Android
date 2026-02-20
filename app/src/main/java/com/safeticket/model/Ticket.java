package com.safeticket.model;

import com.google.firebase.firestore.DocumentId; // Annotation to specify the document ID
import com.google.firebase.firestore.IgnoreExtraProperties; // If there is no data of one of the fields, it will be ignored
import com.google.firebase.firestore.PropertyName; // Annotation to specify the property name

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Ticket {
    @DocumentId
    private String ticketId;
    private String sellerId;
    private String eventName;
    private String category;
    private String location;
    private String exactAddress; // Exact location
    private String eventDate;
    private String eventTime;
    private double originalPrice;
    private double askingPrice;
    private int quantity; // Number of tickets
    private String ticketImage; // Base64 image
    private String sellerName;
    private String sellerPhone;
    private boolean isActive; // If the ticket is active or not
    private boolean isSold; // If the ticket is sold or not
    private int viewCount; // Number of times the ticket has been viewed
    private List<String> viewedBy = new ArrayList<>(); // List of users who viewed the ticket
    public Ticket() {} // Empty constructor, required for Firebase
    public Ticket(String ticketId, String sellerId, String eventName, String location, String exactAddress,
                  String eventDate, double originalPrice, double askingPrice, int quantity, String category,
                  String ticketImage, String sellerName, String sellerPhone) { // Constructor
        this.ticketId = ticketId;
        this.sellerId = sellerId;
        this.eventName = eventName;
        this.location = location;
        this.exactAddress = exactAddress;
        this.eventDate = eventDate;
        this.originalPrice = originalPrice;
        this.askingPrice = askingPrice;
        this.quantity = quantity;
        this.category = category;
        this.ticketImage = ticketImage;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.isActive = true; // By default, the ticket is active
        this.isSold = false; // By default, the ticket is not sold
        this.viewCount = 0; // By default, the ticket has not been viewed
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
    public String getExactAddress() { return exactAddress; }
    public void setExactAddress(String exactAddress) { this.exactAddress = exactAddress; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public double getAskingPrice() { return askingPrice; }
    public void setAskingPrice(double askingPrice) { this.askingPrice = askingPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTicketImage() { return ticketImage; }
    public void setTicketImage(String ticketImage) { this.ticketImage = ticketImage; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public List<String> getViewedBy() { // Getter for viewedBy
        return viewedBy != null ? viewedBy : new ArrayList<>(); // Return an empty list if viewedBy is null
    }
    public void setViewedBy(List<String> viewedBy) { this.viewedBy = viewedBy; }
    @PropertyName("isSold") // Annotation to specify the property name
    public boolean isSold() { return isSold; }
    @PropertyName("isSold") // Annotation to specify the property name
    public void setSold(boolean sold) { isSold = sold; }

    @PropertyName("isActive") // Annotation to specify the property name
    public boolean getIsActive() { return isActive; }
    @PropertyName("isActive") // Annotation to specify the property name
    public void setIsActive(boolean active) { isActive = active; }
}