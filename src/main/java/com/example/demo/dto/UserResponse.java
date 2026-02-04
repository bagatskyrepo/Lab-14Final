package com.example.demo.dto;

public class UserResponse {
   
    private Integer id; 
    private String username;
    private String email;

    // constructor
    public UserResponse(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // new getters
    public Integer getId() { 
        return id; 
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}