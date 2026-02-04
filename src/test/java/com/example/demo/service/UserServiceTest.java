package com.example.demo.service;


import com.example.demo.dto.CreateUserRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Включаем Mockito
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService; // Сюда вольются моки (repo и encoder)

    @Test
    void createUser_ShouldSaveUser_WhenEmailIsUnique() {
        // Arrange (Подготовка)
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("TestUser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Настраиваем поведение моков
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        
        // Имитируем сохранение: возвращаем юзера с ID
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1); 
            return user;
        });

        // Act (Действие)
        User createdUser = userService.createUser(request);

        // Assert (Проверка)
        assertNotNull(createdUser);
        assertEquals(1, createdUser.getId());
        assertEquals("encoded_pass", createdUser.getPassword()); // Пароль должен быть захеширован!
        
        // Проверяем, что save был вызван ровно 1 раз
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("exist@example.com");

        // Имитируем, что юзер уже есть в базе
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.createUser(request));
        
        // Убеждаемся, что save НИКОГДА не вызывался
        verify(userRepository, never()).save(any(User.class));
    }
}