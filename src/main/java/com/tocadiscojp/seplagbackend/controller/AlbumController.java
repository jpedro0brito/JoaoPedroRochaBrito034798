package com.tocadiscojp.seplagbackend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.service.AlbumService;

@RestController
@RequestMapping("/v1/albuns")
public class AlbumController {

    private final AlbumService service;

    public AlbumController(AlbumService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<AlbumResponse>> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "titulo", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> cadastrar(@RequestBody AlbumRequest request) {
        AlbumResponse novoAlbum = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAlbum);
    }
}
