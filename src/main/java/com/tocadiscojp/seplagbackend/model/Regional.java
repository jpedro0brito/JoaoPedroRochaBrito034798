package com.tocadiscojp.seplagbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "regional")
public class Regional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_externo")
    private Integer idExterno;

    private String nome;
    private boolean ativo;
    private LocalDateTime dataSincronizacao;

    public Regional() {
    }

    public Regional(Integer idExterno, String nome) {
        this.idExterno = idExterno;
        this.nome = nome;
        this.ativo = true;
        this.dataSincronizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(Integer idExterno) {
        this.idExterno = idExterno;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataSincronizacao() {
        return dataSincronizacao;
    }
}