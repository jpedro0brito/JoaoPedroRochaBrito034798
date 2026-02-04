package com.tocadiscojp.seplagbackend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tocadiscojp.seplagbackend.enums.TipoArtista;

@Entity
@Table(name = "artista")
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "genero_musical", nullable = false)
    private String generoMusical;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "artista_album", joinColumns = @JoinColumn(name = "artista_id"), inverseJoinColumns = @JoinColumn(name = "album_id"))
    private List<Album> albuns = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoArtista tipo;

    public Artista() {
    }

    public Artista(String nome, String generoMusical, TipoArtista tipo) {
        this.nome = nome;
        this.generoMusical = generoMusical;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getGeneroMusical() {
        return generoMusical;
    }

    public void setGeneroMusical(String generoMusical) {
        this.generoMusical = generoMusical;
    }

    public List<Album> getAlbuns() {
        return albuns;
    }

    public TipoArtista getTipo() {
        return tipo;
    }

    public void setTipo(TipoArtista tipo) {
        this.tipo = tipo;
    }

    public void setAlbuns(List<Album> albuns) {
        this.albuns = albuns;
    }

    public void adicionarAlbum(Album album) {
        this.albuns.add(album);
        album.getArtistas().add(this);
    }
}
