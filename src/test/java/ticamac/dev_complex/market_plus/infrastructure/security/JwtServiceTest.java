package ticamac.dev_complex.market_plus.infrastructure.security;

import ticamac.dev_complex.market_plus.domain.model.User;
import ticamac.dev_complex.market_plus.domain.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService — tests unitaires")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Instanciation directe — pas besoin de Spring
        jwtService = new JwtService(
                "test-secret-key-minimum-32-characters-long-for-hmac",
                86400000L);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("john@test.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setActive(true);
        testUser.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("doit générer un token non nul")
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("doit pouvoir extraire l'userId depuis le token")
    void extractUserId_returnsCorrectId() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractUserId(token)).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("doit pouvoir extraire le rôle depuis le token")
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("doit pouvoir extraire l'email depuis le token")
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractEmail(token)).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("doit valider un token valide")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("doit rejeter un token falsifié")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken(testUser);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("doit rejeter un token expiré")
    void isTokenValid_expiredToken_returnsFalse() {
        // Token avec expiration à -1ms (déjà expiré)
        JwtService shortLivedService = new JwtService(
                "test-secret-key-minimum-32-characters-long-for-hmac",
                -1L);
        String token = shortLivedService.generateToken(testUser);
        assertThat(shortLivedService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("deux utilisateurs doivent avoir des tokens différents")
    void generateToken_differentUsersHaveDifferentTokens() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("jane@test.com");
        anotherUser.setRole(UserRole.ADMIN);

        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(anotherUser);

        assertThat(token1).isNotEqualTo(token2);
    }
}