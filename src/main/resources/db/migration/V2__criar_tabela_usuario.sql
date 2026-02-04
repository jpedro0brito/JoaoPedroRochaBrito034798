CREATE TABLE usuario (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER'
);

INSERT INTO usuario (login, senha, role) 
VALUES ('admin', '$2a$10$DEUfyYyrkl.rC/HrXcn3n.8okWpLjn5PZRQiU72NN694681kr6O8C', 'ADMIN');