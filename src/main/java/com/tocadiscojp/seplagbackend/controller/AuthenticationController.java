package com.tocadiscojp.seplagbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tocadiscojp.seplagbackend.dto.LoginRequest;
import com.tocadiscojp.seplagbackend.dto.RefreshTokenRequest;
import com.tocadiscojp.seplagbackend.dto.TokenResponse;
import com.tocadiscojp.seplagbackend.model.Usuario;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import com.tocadiscojp.seplagbackend.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "4. Autenticação", description = "Endpoints para autenticação de usuários")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AuthenticationController(
            AuthenticationManager manager,
            TokenService tokenService,
            UsuarioRepository usuarioRepository) {
        this.manager = manager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Efetuar login", description = "Ação de login do usuário")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> efetuarLogin(
            @Parameter(description = "Dados de login do usuário") @RequestBody @Valid LoginRequest dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);
        Usuario usuario = (Usuario) authentication.getPrincipal();

        String accessToken = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @Operation(summary = "Refresh token", description = "Atualiza o token de acesso")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Dados do refresh token") @RequestBody RefreshTokenRequest request) {
        String login = tokenService.getSubject(request.refreshToken());

        if (login == null) {
            return ResponseEntity.status(403).build();
        }

        UserDetails userDetails = usuarioRepository.findByLogin(login);
        if (userDetails == null) {
            return ResponseEntity.status(403).build();
        }

        Usuario usuario = (Usuario) userDetails;

        String novoAccessToken = tokenService.gerarToken(usuario);

        String novoRefreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new TokenResponse(novoAccessToken, novoRefreshToken));
    }
}
