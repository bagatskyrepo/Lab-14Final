package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // search user by email from db
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. return 
        return new CustomUserDetails(user);
    }

    // inside class
    // realization of UserDetails interface but with ID field
    public static class CustomUserDetails implements UserDetails {
        
        private Integer id; 
        private String username;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;

        public CustomUserDetails(User user) {
            this.id = user.getId();
            this.username = user.getEmail();
            this.password = user.getPassword();
            
            // logic to assign roles based on email
            String role = user.getEmail().contains("admin") ? "ROLE_ADMIN" : "ROLE_USER";
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        }

        // getter for id 
        public Integer getId() {
            return id;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        // standard implementations
        @Override
        public boolean isAccountNonExpired() { return true; }

        @Override
        public boolean isAccountNonLocked() { return true; }

        @Override
        public boolean isCredentialsNonExpired() { return true; }

        @Override
        public boolean isEnabled() { return true; }
    }
}