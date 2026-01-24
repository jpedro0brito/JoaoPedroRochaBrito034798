-- 1. Criação das Tabelas
CREATE TABLE artista (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    genero_musical VARCHAR(100) NOT NULL
);

CREATE TABLE album (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    ano_lancamento INTEGER NOT NULL,
    capa_url VARCHAR(500)
);

CREATE TABLE artista_album (
    artista_id UUID NOT NULL,
    album_id UUID NOT NULL,
    PRIMARY KEY (artista_id, album_id),
    CONSTRAINT fk_artista_assoc FOREIGN KEY (artista_id) REFERENCES artista(id),
    CONSTRAINT fk_album_assoc FOREIGN KEY (album_id) REFERENCES album(id)
);

INSERT INTO artista (nome, genero_musical) VALUES 
('Serj Tankian', 'Rock Alternativo'),
('Mike Shinoda', 'Hip Hop / Rock'),
('Michel Teló', 'Sertanejo'),
('Guns N'' Roses', 'Hard Rock');

INSERT INTO album (titulo, ano_lancamento) VALUES
('Harakiri', 2012),
('Black Blooms', 2019),
('The Rough Dog', 2021),
('The Rising Tied', 2005),
('Post Traumatic', 2018),
('Post Traumatic EP', 2018),
('Where''d You Go', 2006),
('Bem Sertanejo', 2014),
('Bem Sertanejo - O Show (Ao Vivo)', 2017),
('Bem Sertanejo - (1ª Temporada) - EP', 2014),
('Use Your Illusion I', 1991),
('Use Your Illusion II', 1991),
('Greatest Hits', 2004);

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al WHERE a.nome = 'Serj Tankian' AND al.titulo IN ('Harakiri', 'Black Blooms', 'The Rough Dog');

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al WHERE a.nome = 'Mike Shinoda' AND al.titulo IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go');

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al WHERE a.nome = 'Michel Teló' AND al.titulo LIKE 'Bem Sertanejo%';

INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al WHERE a.nome = 'Guns N'' Roses' AND al.titulo IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits');