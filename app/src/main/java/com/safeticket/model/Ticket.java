package com.safeticket.model;

public class Ticket {
    // --- Backend Fields (Matching Firestore) ---
    private String ticketId;
    private String sellerId;
    private String eventName;
    private String location;
    private long eventDate;       // Timestamp in milliseconds
    private double askingPrice;   // Changed from String to double
    private double originalPrice; // Changed from String to double
    private String category;      // e.g., "Concert", "Sport"
    private boolean isActive;     // Is the ticket available?

    // --- UI Fields (Local usage / Future implementation) ---
    private String country;
    private int eventImage;       // Resource ID (for dummy data)
    private String sellerName;    // Temp: To display name until we link Users
    private String sellerPhone;   // Temp: To display phone until we link Users

    // Empty constructor (Required for Firebase)
    public Ticket() { }

    // Full Constructor
    public Ticket(String ticketId, String sellerId, String eventName, String location, String country, long eventDate, double askingPrice, double originalPrice, String category, int eventImage, String sellerName, String sellerPhone) {
        this.ticketId = ticketId;
        this.sellerId = sellerId;
        this.eventName = eventName;
        this.location = location;
        this.country = country;
        this.eventDate = eventDate;
        this.askingPrice = askingPrice;
        this.originalPrice = originalPrice;
        this.category = category;
        this.eventImage = eventImage;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.isActive = true; // Default to true
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { this.eventDate = eventDate; }

    public double getAskingPrice() { return askingPrice; }
    public void setAskingPrice(double askingPrice) { this.askingPrice = askingPrice; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getEventImage() { return eventImage; }
    public void setEventImage(int eventImage) { this.eventImage = eventImage; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
}