package ticamac.dev_complex.market_plus.infrastructure.web;

import ticamac.dev_complex.market_plus.application.dto.auth.AuthResponse;
import ticamac.dev_complex.market_plus.application.dto.auth.LoginRequest;
import ticamac.dev_complex.market_plus.application.dto.auth.RegisterRequest;
import ticamac.dev_complex.market_plus.domain.port.in.AuthUseCase;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authUseCase.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }
}