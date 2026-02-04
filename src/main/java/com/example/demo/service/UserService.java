package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; 

    // dependency injection through constructor
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        // dublicate check email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        // calling encode password before saving**
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User(request.getUsername(), request.getEmail(), encodedPassword);
        return userRepository.save(user);
    }
    
    
}