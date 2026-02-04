package com.example.demo.repository;

import com.example.demo.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // method JPA (generate SQL automatically)
    List<Note> findByUserEmail(String email); // *

    // Raw SQL Query 
     
    @Query(value = "SELECT COUNT(*) FROM notes n JOIN users u ON n.user_id = u.id WHERE u.email = :email", nativeQuery = true)
    int countNotesByUserEmail(@Param("email") String email);
}