package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    
    //Secure Logging (SLF4J)
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService userDetailsService,
                          RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.createUser(request);
        // logging creation (without password)
        logger.info("New user registered: {}", created.getEmail());
        
        UserResponse response = new UserResponse(created.getId(), created.getUsername(), created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/hello")
    public String hello() {
        return "Hello, user!";
    }

    // but login with logging
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // generate access token
            String accessToken = jwtUtil.generateToken(userDetails);

            // 2. generate refresh token & save to DB
            Integer userId = ((CustomUserDetailsService.CustomUserDetails) userDetails).getId();
            refreshTokenService.deleteByUserId(userId);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());

            // log successful login without logging
            logger.info("Successful login for user: {}", request.getEmail());

            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken()));

        } catch (Exception e) {
            // (failed login attempt)
            // Ми логуємо хто хотів увійти ане не пишемо пароль, який він ввів
            logger.warn("Failed login attempt for user: {}", request.getEmail());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // (Refresh & Rotate)
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    //rotate refresh token
                    refreshTokenService.deleteByUserId(user.getId());
                    
                    String newAccessToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());
                    
                    logger.debug("Token refreshed successfully for user: {}", user.getEmail());
                    
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    //logout
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = ((CustomUserDetailsService.CustomUserDetails) userDetails).getId();
        
        // DELETE REFRESH TOKEN FROM DB
        refreshTokenService.deleteByUserId(userId);
        
        // log logout event
        logger.info("User ID: {} logged out successfully", userId);
        
        return ResponseEntity.ok("Log out successful!");
    }
    
    // get user by id with logging
    
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id); 
            UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) { 
        logger.info("Admin deleted user with ID: {}", id);
        return ResponseEntity.ok("User deleted (Simulation)");
    }
}