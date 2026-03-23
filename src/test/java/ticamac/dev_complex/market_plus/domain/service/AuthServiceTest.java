package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.auth.AuthResponse;
import ticamac.dev_complex.market_plus.application.dto.auth.LoginRequest;
import ticamac.dev_complex.market_plus.application.dto.auth.RegisterRequest;
import ticamac.dev_complex.market_plus.domain.model.User;
import ticamac.dev_complex.market_plus.domain.model.UserRole;
import ticamac.dev_complex.market_plus.domain.port.out.UserRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — tests unitaires")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // ─── Données de test ───────────────────────────────────

    private User buildUser(String email, boolean active) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(active);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("john@test.com");
        req.setPassword("password123");
        req.setFirstName("John");
        req.setLastName("Doe");
        return req;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ─── Tests Register ────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("doit créer un compte et retourner un token")
        void register_success() {
            RegisterRequest request = buildRegisterRequest();
            User savedUser = buildUser("john@test.com", true);

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getEmail()).isEqualTo("john@test.com");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");

            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("doit rejeter si l'email existe déjà")
        void register_emailAlreadyExists_throwsException() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(buildRegisterRequest()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("existe déjà");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit normaliser l'email en minuscules")
        void register_normalizesEmail() {
            RegisterRequest request = buildRegisterRequest();
            request.setEmail("JOHN@TEST.COM");

            User savedUser = buildUser("john@test.com", true);
            when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any())).thenReturn(savedUser);
            when(jwtService.generateToken(any())).thenReturn("token");

            authService.register(request);

            verify(userRepository).existsByEmail("john@test.com");
        }

        @Test
        @DisplayName("ne doit jamais stocker le mot de passe en clair")
        void register_passwordIsHashed() {
            RegisterRequest request = buildRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                // Vérifie que le mot de passe a été hashé avant la sauvegarde
                assertThat(u.getPasswordHash()).isEqualTo("$2a$12$hashed");
                assertThat(u.getPasswordHash()).doesNotContain("password123");
                return buildUser("john@test.com", true);
            });
            when(jwtService.generateToken(any())).thenReturn("token");

            authService.register(request);
        }

        @Test
        @DisplayName("doit assigner le rôle CUSTOMER par défaut")
        void register_assignsCustomerRole() {
            RegisterRequest request = buildRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                assertThat(u.getRole()).isEqualTo(UserRole.CUSTOMER);
                return buildUser("john@test.com", true);
            });
            when(jwtService.generateToken(any())).thenReturn("token");

            authService.register(request);
        }
    }

    // ─── Tests Login ───────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("doit retourner un token si les identifiants sont corrects")
        void login_success() {
            User user = buildUser("john@test.com", true);
            when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("jwt.token");

            AuthResponse response = authService.login(buildLoginRequest("john@test.com", "password123"));

            assertThat(response.getToken()).isEqualTo("jwt.token");
            assertThat(response.getEmail()).isEqualTo("john@test.com");
        }

        @Test
        @DisplayName("doit rejeter si l'email est introuvable")
        void login_emailNotFound_throwsException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(buildLoginRequest("unknown@test.com", "pass")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email ou mot de passe incorrect");
        }

        @Test
        @DisplayName("doit rejeter si le mot de passe est incorrect")
        void login_wrongPassword_throwsException() {
            User user = buildUser("john@test.com", true);
            when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(buildLoginRequest("john@test.com", "wrongpass")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email ou mot de passe incorrect");

            // Le token ne doit JAMAIS être généré si le mot de passe est mauvais
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("doit rejeter un compte suspendu")
        void login_suspendedAccount_throwsException() {
            User suspended = buildUser("john@test.com", false);
            when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(suspended));

            assertThatThrownBy(() -> authService.login(buildLoginRequest("john@test.com", "password123")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("suspendu");

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("doit normaliser l'email avant la recherche")
        void login_normalizesEmail() {
            User user = buildUser("john@test.com", true);
            when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("token");

            authService.login(buildLoginRequest("JOHN@TEST.COM", "password123"));

            verify(userRepository).findByEmail("john@test.com");
        }
    }
}