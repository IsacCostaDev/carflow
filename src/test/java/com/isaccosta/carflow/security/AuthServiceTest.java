package com.isaccosta.carflow.security;

import com.isaccosta.carflow.security.dto.AuthResponse;
import com.isaccosta.carflow.security.dto.LoginRequest;
import com.isaccosta.carflow.tenant.Role;
import com.isaccosta.carflow.tenant.Usuario;
import com.isaccosta.carflow.tenant.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        // Injeta o valor da propriedade de expiração que normalmente viria do
        // application.yml
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 900000L);

        usuarioValido = Usuario.builder()
                .id(UUID.randomUUID())
                .lojaId(UUID.randomUUID())
                .nome("Isac Costa")
                .email("isac@carflow.com")
                .senhaHash("$2a$10$hashSimuladoDoBcrypt")
                .role(Role.DONO)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais forem válidas")
    void deveAutenticarComSucesso() {
        // 1. Cenário (Given)
        LoginRequest request = new LoginRequest("isac@carflow.com", "SenhaCorreta@123");

        when(usuarioRepository.findByEmailIgnoringTenant("isac@carflow.com"))
                .thenReturn(Optional.of(usuarioValido));

        when(passwordEncoder.matches("SenhaCorreta@123", usuarioValido.getSenhaHash()))
                .thenReturn(true);

        when(jwtService.generateAccessToken(usuarioValido))
                .thenReturn("access-token-simulado");

        when(jwtService.generateRefreshToken(usuarioValido))
                .thenReturn("refresh-token-simulado");

        // 2. Ação (When)
        AuthResponse response = authService.login(request);

        // 3. Validação (Then)
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-simulado");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-simulado");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando a senha estiver incorreta")
    void deveFalharQuandoSenhaIncorreta() {
        // 1. Cenário (Given)
        LoginRequest request = new LoginRequest("isac@carflow.com", "SenhaErrada");

        when(usuarioRepository.findByEmailIgnoringTenant("isac@carflow.com"))
                .thenReturn(Optional.of(usuarioValido));

        when(passwordEncoder.matches("SenhaErrada", usuarioValido.getSenhaHash()))
                .thenReturn(false);

        // 2 e 3. Ação e Validação (When & Then)
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando o usuário não existir")
    void deveFalharQuandoUsuarioNaoExistir() {
        // 1. Cenário (Given)
        LoginRequest request = new LoginRequest("inexistente@carflow.com", "123456");

        when(usuarioRepository.findByEmailIgnoringTenant(anyString()))
                .thenReturn(Optional.empty());

        // 2 e 3. Ação e Validação (When & Then)
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando o usuário estiver inativo")
    void deveFalharQuandoUsuarioInativo() {
        // 1. Cenário (Given)
        usuarioValido.setAtivo(false);
        LoginRequest request = new LoginRequest("isac@carflow.com", "SenhaCorreta@123");

        when(usuarioRepository.findByEmailIgnoringTenant("isac@carflow.com"))
                .thenReturn(Optional.of(usuarioValido));

        // 2 e 3. Ação e Validação (When & Then)
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }
}
