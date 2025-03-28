package com.example.memoryminder;

public class Patient {
    private String firstName;
    private String lastName;
    private int age;
    private String stage;
    private String username;
    private String password;

    // Empty constructor required for Firebase
    public Patient() {
    }

    public Patient(String firstName, String lastName, int age, String stage, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.stage = stage;
        this.username = username;
        this.password = password;
    }

    // Getters and setters for each field

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
