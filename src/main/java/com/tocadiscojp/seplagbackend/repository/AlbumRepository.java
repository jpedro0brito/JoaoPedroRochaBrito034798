package com.tocadiscojp.seplagbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Album;

import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {
        @Query(value = "SELECT DISTINCT a FROM Album a " +
                        "LEFT JOIN a.artistas ar " +
                        "WHERE a.ativo = TRUE " +
                        "AND (:tipo IS NULL OR ar.tipo = :tipo) " +
                        "AND (:nomeArtista IS NULL OR :nomeArtista = '' OR LOWER(ar.nome) LIKE LOWER(:nomeArtista))",

                        countQuery = "SELECT count(DISTINCT a) FROM Album a " +
                                        "LEFT JOIN a.artistas ar " +
                                        "WHERE a.ativo = TRUE " +
                                        "AND (:tipo IS NULL OR ar.tipo = :tipo) " +
                                        "AND (:nomeArtista IS NULL OR :nomeArtista = '' OR LOWER(ar.nome) LIKE LOWER(:nomeArtista))")
        Page<Album> buscarComFiltros(
                        @Param("tipo") TipoArtista tipo,
                        @Param("nomeArtista") String nomeArtista,
                        Pageable pageable);
}