package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // База очищається після кожного тесту
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void accessProtectedResource_WithoutToken_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullAuthFlow_RegisterLoginAndAccessProtectedResource() throws Exception {
        // 1. Register
        CreateUserRequest signup = new CreateUserRequest();
        signup.setUsername("IntegrationUser");
        signup.setEmail("integration@test.com");
        signup.setPassword("securePass123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // 2. Login
        LoginRequest login = new LoginRequest();
        login.setEmail("integration@test.com");
        login.setPassword("securePass123");

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        // Get Token
        String responseJson = loginResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseJson);
        String accessToken = jsonNode.get("accessToken").asText();

        // 3. Access Secured Resource
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void login_WithWrongPassword_ShouldFail() throws Exception {
        CreateUserRequest signup = new CreateUserRequest();
        signup.setUsername("WrongPassUser");
        signup.setEmail("wrong@test.com");
        signup.setPassword("correctPass");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest();
        login.setEmail("wrong@test.com");
        login.setPassword("WRONG_PASS");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // 👇 ОСЬ ЦЕЙ ТЕСТ ВИПРАВЛЕНО 👇
    @Test
    void accessControl_UserCannotAccessOtherUserData() throws Exception {
        // 1. Реєструємо "Жертву" (User A)
        CreateUserRequest userA = new CreateUserRequest();
        userA.setUsername("VictimUser");
        userA.setEmail("victim@test.com");
        userA.setPassword("pass123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userA)))
                .andExpect(status().isCreated());

        // 2. Логінимось за Жертву, щоб отримати її токен
        LoginRequest loginA = new LoginRequest();
        loginA.setEmail("victim@test.com");
        loginA.setPassword("pass123");

        MvcResult resultA = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn();
        
        String tokenA = objectMapper.readTree(resultA.getResponse().getContentAsString())
                .get("accessToken").asText();

        // 3. СТВОРЮЄМО НОТАТКУ від імені Жертви (ВАЖЛИВО: тепер нотатка ID=1 існує в базі)
        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Secret Note of Victim\"}")) // JSON тіло запиту
                .andExpect(status().isOk()); // або isCreated() залежно від контролера

        // 4. Реєструємо "Атакувальника" (User B)
        CreateUserRequest userB = new CreateUserRequest();
        userB.setUsername("AttackerUser");
        userB.setEmail("attacker@test.com");
        userB.setPassword("pass123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userB)))
                .andExpect(status().isCreated());

        // 5. Логінимо Атакувальника
        LoginRequest loginB = new LoginRequest();
        loginB.setEmail("attacker@test.com");
        loginB.setPassword("pass123");

        MvcResult resultB = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn();

        String tokenB = objectMapper.readTree(resultB.getResponse().getContentAsString())
                .get("accessToken").asText();

        // 6. АТАКА: Атакувальник намагається отримати доступ до нотатки ID 1
        // Тепер нотатка існує, але вона чужа -> тому має бути 403 Forbidden!
        mockMvc.perform(get("/api/notes/1")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }
}