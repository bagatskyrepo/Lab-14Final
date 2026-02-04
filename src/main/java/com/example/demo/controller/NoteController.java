package com.example.demo.controller;

import com.example.demo.model.Note;
import com.example.demo.model.User;
import com.example.demo.repository.NoteRepository;
import com.example.demo.repository.UserRepository;
// 1. Додані імпорти для логування
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    // 2. Створення логера
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // 1. create note
    @PostMapping
    public Note createNote(@RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String content = body.get("content");

        Note note = new Note(content, currentUser);
        
        // Зберігаємо змінну, щоб залогувати ID
        Note savedNote = noteRepository.save(note);
        
        // LOG INFO: Успішне створення
        logger.info("User '{}' created a new note with ID: {}", email, savedNote.getId());
        
        return savedNote;
    }

    // all notes
    @GetMapping
    public List<Note> getMyNotes() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return noteRepository.findByUserEmail(email);
    }

    // count my notes
    @GetMapping("/count")
    public int getMyNotesCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // call SQL метод
        return noteRepository.countNotesByUserEmail(email);
    }

    // 3. updute note (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().getEmail().equals(email)) {
            // LOG WARN: Спроба несанкціонованої зміни
            logger.warn("SECURITY ALERT: User '{}' tried to MODIFY Note ID {} owned by '{}'", 
                email, id, note.getUser().getEmail());
                
            return ResponseEntity.status(403).body("Access denied: You do not own this note");
        }

        note.setContent(body.get("content"));
        noteRepository.save(note);
        
        // LOG INFO: Успішне оновлення
        logger.info("User '{}' updated Note ID {}", email, id);
        
        return ResponseEntity.ok(note);
    }

    // 4. note (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName(); 
        //I get the current user's email from the SecurityContext authenticated via JWT**

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
                // fetch the note by its id**

        if (!note.getUser().getEmail().equals(email)) { // check if the note belongs to the current user**
            
            // LOG WARN: Спроба несанкціонованого видалення
            logger.warn("SECURITY ALERT: User '{}' tried to DELETE Note ID {} owned by '{}'", 
                email, id, note.getUser().getEmail());
            
            return ResponseEntity.status(403).body("Access denied: You do not own this note");
        }

        noteRepository.delete(note);
        
        // LOG INFO: Успішне видалення
        logger.info("User '{}' deleted Note ID {}", email, id);
        
        return ResponseEntity.ok("Note deleted successfully");
    }
    // 5. GET SINGLE NOTE 
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Шукаємо нотатку
        var noteOptional = noteRepository.findById(id);

        if (noteOptional.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 якщо нотатки взагалі немає
        }

        Note note = noteOptional.get();

        // 2. ПЕРЕВІРКА ВЛАСНИКА 
        if (!note.getUser().getEmail().equals(email)) {
            
            // LOG WARN: Спроба несанкціонованого доступу (читання)
            logger.warn("SECURITY ALERT: User '{}' tried to ACCESS Note ID {} owned by '{}'", 
                email, id, note.getUser().getEmail());
            
            //  403 Forbidden
            return ResponseEntity.status(403).body("Access denied: You do not own this note");
        }

        return ResponseEntity.ok(note);
    }
}