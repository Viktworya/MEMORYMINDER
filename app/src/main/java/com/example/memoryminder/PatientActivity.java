package com.example.memoryminder;

public class PatientActivity {
    private String imageUrl;
    private int steps;
    private String timestamp;

    // Default constructor required for calls to DataSnapshot.getValue(PatientActivity.class)
    public PatientActivity() {
    }

    public PatientActivity(String imageUrl, int steps, String timestamp) {
        this.imageUrl = imageUrl;
        this.steps = steps;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
