package com.example.urbanroots.domain.models;
public class Crop {
    private String cropId;
    private String cropName;
    private double price;
    private String description;
    private String status;
    private String soilType;
    private String sunlightHours;
    private String wateringInterval;
    private String harvestTime;
    private Long timestamp; // Add this field

    // Default constructor for Firestore
    public Crop() {}

    // Getters and setters
    public String getCropId() { return cropId; }
    public void setCropId(String cropId) { this.cropId = cropId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    public String getSunlightHours() { return sunlightHours; }
    public void setSunlightHours(String sunlightHours) { this.sunlightHours = sunlightHours; }
    public String getWateringInterval() { return wateringInterval; }
    public void setWateringInterval(String wateringInterval) { this.wateringInterval = wateringInterval; }
    public String getHarvestTime() { return harvestTime; }
    public void setHarvestTime(String harvestTime) { this.harvestTime = harvestTime; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}