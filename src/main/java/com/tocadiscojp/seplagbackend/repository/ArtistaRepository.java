package com.tocadiscojp.seplagbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Artista;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, UUID> {
        Page<Artista> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

        @Query("SELECT a FROM Artista a WHERE " +
                        "a.ativo = TRUE AND " +
                        "(:tipo IS NULL OR a.tipo = :tipo) AND " +
                        "(:nome IS NULL OR :nome = '' OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')))")
        List<Artista> buscarComFiltros(
                        @Param("tipo") TipoArtista tipo,
                        @Param("nome") String nome,
                        Sort sort);
}