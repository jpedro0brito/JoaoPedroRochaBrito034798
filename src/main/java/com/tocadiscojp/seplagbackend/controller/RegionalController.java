package com.tocadiscojp.seplagbackend.controller;

import com.tocadiscojp.seplagbackend.model.Regional;
import com.tocadiscojp.seplagbackend.service.RegionalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/regionais")
public class RegionalController {

    private final RegionalService service;

    public RegionalController(RegionalService service) {
        this.service = service;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar() {
        String resultado = service.sincronizarRegionais();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<List<Regional>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }
}