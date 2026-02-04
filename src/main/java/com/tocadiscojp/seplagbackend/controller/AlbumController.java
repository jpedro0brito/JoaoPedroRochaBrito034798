package com.tocadiscojp.seplagbackend.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.service.AlbumService;

@Tag(name = "1. Álbuns", description = "Endpoints para gerenciar o catálogo de álbuns musicais")
@RestController
@RequestMapping("/v1/albuns")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Álbum ou Artista não encontrado"),
        @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta ação"),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
})
public class AlbumController {

    private final AlbumService service;

    public AlbumController(AlbumService service) {
        this.service = service;
    }

    @Operation(summary = "Listar álbuns com paginação e filtros", description = "Permite filtrar por tipo de artista e nome, com suporte a ordenação dinâmica.")
    @GetMapping
    public ResponseEntity<Page<AlbumResponse>> listar(
            @ParameterObject Pageable pageable,
            @Parameter(description = "Filtro por categoria (SOLO/BANDA)") @RequestParam(required = false) TipoArtista tipoArtista,
            @Parameter(description = "Busca parcial pelo nome do artista") @RequestParam(required = false) String nomeArtista) {
        return ResponseEntity.ok(service.listarTodos(pageable, tipoArtista, nomeArtista));
    }

    @Operation(summary = "Cadastrar álbum", description = "Salvar um novo álbum no catálogo.")
    @PostMapping
    public ResponseEntity<AlbumResponse> cadastrar(
            @Parameter(description = "Dados do álbum a ser cadastrado") @RequestBody @Valid AlbumRequest request) {
        AlbumResponse novoAlbum = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAlbum);
    }

    @Operation(summary = "Fazer upload da capa", description = "Envia um arquivo de imagem (PNG/JPG) para ser associado ao álbum.")
    @PostMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> uploadCapa(
            @Parameter(description = " Identificando o único álbum a ser atualizado") @PathVariable UUID id,
            @Parameter(description = "Tipo de arquivo a ser enviado") @RequestParam("file") MultipartFile file) {
        AlbumResponse albumAtualizado = service.atualizarCapa(id, file);
        return ResponseEntity.ok(albumAtualizado);
    }

    @Operation(summary = "Atualizar álbum", description = "Atualizar um álbum existente no catálogo")
    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> atualizar(
            @Parameter(description = " Identificando o único álbum a ser atualizado") @PathVariable UUID id,
            @Parameter(description = " Dados do álbum a ser atualizado") @RequestBody AlbumRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }
}
