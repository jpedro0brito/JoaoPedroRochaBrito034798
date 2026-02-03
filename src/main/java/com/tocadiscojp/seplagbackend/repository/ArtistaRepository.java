package com.tocadiscojp.seplagbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tocadiscojp.seplagbackend.model.Artista;
import java.util.UUID;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, UUID> {
    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}