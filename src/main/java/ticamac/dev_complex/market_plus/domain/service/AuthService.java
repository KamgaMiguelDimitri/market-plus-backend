package ticamac.dev_complex.market_plus.domain.service;

import ticamac.dev_complex.market_plus.application.dto.auth.AuthResponse;
import ticamac.dev_complex.market_plus.application.dto.auth.LoginRequest;
import ticamac.dev_complex.market_plus.application.dto.auth.RegisterRequest;
import ticamac.dev_complex.market_plus.domain.model.User;
import ticamac.dev_complex.market_plus.domain.model.UserRole;
import ticamac.dev_complex.market_plus.domain.port.in.AuthUseCase;
import ticamac.dev_complex.market_plus.domain.port.out.UserRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    
    @Override
    public AuthResponse register(RegisterRequest request) {
        // ✅ Normaliser EN PREMIER avant tout appel
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }

        User user = new User();
        user.setEmail(email); // ← utiliser la variable normalisée
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getRole().name());
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        // ✅ Normaliser EN PREMIER
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Ce compte est suspendu.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name());
    }
}