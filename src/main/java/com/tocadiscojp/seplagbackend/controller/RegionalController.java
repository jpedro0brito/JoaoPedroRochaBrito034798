package com.tocadiscojp.seplagbackend.controller;

import com.tocadiscojp.seplagbackend.model.Regional;
import com.tocadiscojp.seplagbackend.service.RegionalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. Regionais", description = "Sincronização de dados de novas regionais")
@RestController
@RequestMapping("/v1/regionais")
public class RegionalController {

    private final RegionalService service;

    public RegionalController(RegionalService service) {
        this.service = service;
    }

    @Operation(summary = "Sincronizar dados", description = "Aciona o processo de integração para buscar novas regionais cadastradas no governo.")
    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar() {
        String resultado = service.sincronizarRegionais();
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Listar regionais", description = "Lista todas as regionais cadastradas no sistema.")
    @GetMapping
    public ResponseEntity<List<Regional>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }
}