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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> efetuarLogin(@RequestBody LoginRequest dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);
        Usuario usuario = (Usuario) authentication.getPrincipal();

        String accessToken = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
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
