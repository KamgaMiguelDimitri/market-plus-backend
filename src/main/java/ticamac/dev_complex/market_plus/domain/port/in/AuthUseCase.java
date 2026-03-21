package ticamac.dev_complex.market_plus.domain.port.in;

import ticamac.dev_complex.market_plus.application.dto.auth.AuthResponse;
import ticamac.dev_complex.market_plus.application.dto.auth.LoginRequest;
import ticamac.dev_complex.market_plus.application.dto.auth.RegisterRequest;

public interface AuthUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}