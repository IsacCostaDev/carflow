package com.isaccosta.carflow.security;

import com.isaccosta.carflow.security.dto.AuthResponse;
import com.isaccosta.carflow.security.dto.LoginRequest;
import com.isaccosta.carflow.security.dto.RefreshTokenRequest;
import com.isaccosta.carflow.tenant.Usuario;
import com.isaccosta.carflow.tenant.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${security.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoringTenant(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpirationMs / 1000);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isTokenValid(token)) {
            throw new BadCredentialsException("Refresh token inválido ou expirado");
        }

        String email = jwtService.extractEmail(token);

        Usuario usuario = usuarioRepository.findByEmailIgnoringTenant(email)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        String newAccessToken = jwtService.generateAccessToken(usuario);
        String newRefreshToken = jwtService.generateRefreshToken(usuario);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                accessTokenExpirationMs / 1000);
    }
}
