package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) //* 
    private User user;

    public Note() {
    }

    public Note(String content, User user) {
        this.content = content;
        this.user = user;
    }

   
    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}