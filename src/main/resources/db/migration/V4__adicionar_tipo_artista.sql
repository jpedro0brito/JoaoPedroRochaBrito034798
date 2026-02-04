ALTER TABLE artista ADD COLUMN tipo VARCHAR(20);

UPDATE artista SET tipo = 'BANDA' WHERE nome = 'Guns N'' Roses';

UPDATE artista SET tipo = 'SOLO' WHERE tipo IS NULL;

ALTER TABLE artista ALTER COLUMN tipo SET NOT NULL;