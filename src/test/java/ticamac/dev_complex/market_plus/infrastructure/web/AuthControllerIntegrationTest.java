package ticamac.dev_complex.market_plus.infrastructure.web;

import ticamac.dev_complex.market_plus.AbstractIntegrationTest;
import ticamac.dev_complex.market_plus.application.dto.auth.LoginRequest;
import ticamac.dev_complex.market_plus.application.dto.auth.RegisterRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("AuthController — tests d'intégration")
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private RegisterRequest validRegisterRequest;

        @BeforeEach
        void setUp() {
                validRegisterRequest = new RegisterRequest();
                validRegisterRequest.setFirstName("John");
                validRegisterRequest.setLastName("Doe");
                validRegisterRequest.setEmail("john.integration@test.com");
                validRegisterRequest.setPassword("password123");
        }

        @Nested
        @DisplayName("POST /api/auth/register")
        class RegisterIntegrationTests {

                @Test
                @DisplayName("doit retourner 201 avec un token JWT")
                void register_returns201WithToken() throws Exception {
                        MvcResult result = mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.token").isNotEmpty())
                                        .andExpect(jsonPath("$.type").value("Bearer"))
                                        .andExpect(jsonPath("$.email").value("john.integration@test.com"))
                                        .andExpect(jsonPath("$.role").value("CUSTOMER"))
                                        .andReturn();

                        String token = objectMapper.readTree(
                                        result.getResponse().getContentAsString()).get("token").asText();

                        assertThat(token).isNotBlank();
                        assertThat(token.split("\\.")).hasSize(3);
                }

                @Test
                @DisplayName("doit retourner 400 si l'email est déjà utilisé")
                void register_duplicateEmail_returns400() throws Exception {
                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isCreated());

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value(
                                                        org.hamcrest.Matchers.containsString("existe déjà")));
                }

                @Test
                @DisplayName("doit retourner 400 si le mot de passe est trop court")
                void register_shortPassword_returns400() throws Exception {
                        validRegisterRequest.setPassword("123");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.fields.password").exists());
                }

                @Test
                @DisplayName("doit retourner 400 si l'email est invalide")
                void register_invalidEmail_returns400() throws Exception {
                        validRegisterRequest.setEmail("pas-un-email");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.fields.email").exists());
                }
        }

        @Nested
        @DisplayName("POST /api/auth/login")
        class LoginIntegrationTests {

                @BeforeEach
                void registerUser() throws Exception {
                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)));
                }

                @Test
                @DisplayName("doit retourner 200 avec un token valide")
                void login_validCredentials_returns200() throws Exception {
                        LoginRequest loginRequest = new LoginRequest();
                        loginRequest.setEmail("john.integration@test.com");
                        loginRequest.setPassword("password123");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.token").isNotEmpty())
                                        .andExpect(jsonPath("$.role").value("CUSTOMER"));
                }

                @Test
                @DisplayName("doit retourner 400 si le mot de passe est incorrect")
                void login_wrongPassword_returns400() throws Exception {
                        LoginRequest loginRequest = new LoginRequest();
                        loginRequest.setEmail("john.integration@test.com");
                        loginRequest.setPassword("wrongpassword");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("endpoint protégé sans token doit retourner 403")
                void protectedEndpoint_withoutToken_returns403() throws Exception {
                        mockMvc.perform(post("/api/products/admin")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("endpoint public avec token valide doit retourner 200")
                void publicEndpoint_withValidToken_returns200() throws Exception {
                        LoginRequest loginRequest = new LoginRequest();
                        loginRequest.setEmail("john.integration@test.com");
                        loginRequest.setPassword("password123");

                        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                                        .andReturn();

                        String token = objectMapper.readTree(
                                        loginResult.getResponse().getContentAsString()).get("token").asText();

                        mockMvc.perform(get("/api/products")
                                        .header("Authorization", "Bearer " + token))
                                        .andExpect(status().isOk());
                }
        }
}