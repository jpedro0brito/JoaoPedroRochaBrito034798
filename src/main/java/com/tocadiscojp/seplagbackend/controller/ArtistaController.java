package com.tocadiscojp.seplagbackend.controller;

import com.tocadiscojp.seplagbackend.dto.ArtistaRequest;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.service.ArtistaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "2. Artistas", description = "Endpoints para gerenciar o catálogo de artistas")
@RestController
@RequestMapping("/v1/artistas")
public class ArtistaController {

    private final ArtistaService service;

    public ArtistaController(ArtistaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar artistas", description = "Lista todos os artistas cadastrados no sistema.")
    @GetMapping
    public ResponseEntity<List<ArtistaResponse>> listar(
            @Parameter(description = "Tipo de artista (SOLO/BANDA)") @RequestParam(required = false) TipoArtista tipo,
            @Parameter(description = "Nome do artista") @RequestParam(required = false) String nome,
            @Parameter(description = "Ordem de ordenação (asc/desc)") @RequestParam(defaultValue = "asc") String ordem) {
        return ResponseEntity.ok(service.listar(tipo, nome, ordem));
    }

    @Operation(summary = "Cadastrar artista", description = "Salvar um novo artista no catálogo.")
    @PostMapping
    public ResponseEntity<ArtistaResponse> salvar(
            @Parameter(description = "Dados do artista a ser cadastrado") @RequestBody @Valid ArtistaRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }

    @Operation(summary = "Atualizar artista", description = "Atualizar um artista existente no catálogo")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistaResponse> alterar(
            @PathVariable UUID id,
            @Parameter(description = "Dados do artista a ser atualizado") @RequestBody @Valid ArtistaRequest request) {
        return ResponseEntity.ok(service.alterar(id, request));
    }
}