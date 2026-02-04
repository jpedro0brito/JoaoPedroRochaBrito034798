# Music Management API - Seplag Backend

* **Java 17 & Spring Boot 3.5:** Uso das versões mais estáveis e modernas para aproveitar *Records* (imutabilidade nos DTOs) e *RestClient* (fluidez em chamadas externas).
* **Security com JWT & Refresh Token:** Implementação de um fluxo seguro de renovação de sessão, respeitando a expiração de 5 minutos sem prejudicar a experiência do usuário.
* **Armazenamento Cloud-Native (MinIO/S3):** Em vez de salvar arquivos no sistema local, utilizamos a API S3. Isso permite que a aplicação seja escalada horizontalmente sem perda de arquivos.
* **Flyway Migrations:** Gestão rigorosa do banco de dados, permitindo a evolução do schema (como a adição do campo `ativo` e `tipo`) de forma versionada.

---

## Desafios Técnicos e Soluções

### 1. Sincronização de Regionais

**Problema:** Consumir uma API externa e refletir as mudanças no banco local sem gerar duplicidade ou perda de dados.
**Solução:** Implementada uma lógica de sincronização que identifica novos registros, inativa registros ausentes e trata atualizações de atributos (inativando o antigo e criando o novo para preservar o histórico).

### 2. Busca N:N Paginada com Integridade

**Problema:** O uso de `JOIN` em relacionamentos Muitos-para-Muitos costuma causar erros na contagem de páginas e duplicidade de registros.
**Solução:** Refinamos o `AlbumRepository` com `LEFT JOIN` e `countQuery` customizados. Isso garantiu que o filtro por nome do artista ou tipo (SOLO/BANDA) funcione perfeitamente com a paginação nativa do Spring.

### 3. Estratégia de Soft Delete

**Problema:** Remover um artista que possui álbuns vinculados causaria inconsistência.
**Solução:** Adotamos o *Soft Delete*.

* **Álbuns:** Apenas desativados.
* **Artistas:** Se não houver vínculos, remoção física. Se houver álbuns, o artista e todos os seus álbuns são desativados em cascata, preservando a integridade referencial.

---

## Funcionalidades Implementadas

### Gerais

* **Segurança:** CORS dinâmico via variáveis de ambiente e proteção de rotas.
* **Filtros Avançados:** Busca por nome do artista, tipo de artista e ordenação alfabética dinâmica.
* **Imagens:** Upload sanitizado para MinIO com geração de links pré-assinados (30 min).

### Sênior

* **WebSocket:** Notificações em tempo real para o front-end via protocolo STOMP.
* **Rate Limit:** Limite estrito de 10 req/min por usuário usando algoritmo de *Intervally Refill*.
* **Testes:** Cobertura de testes unitários para regras de negócio críticas usando Mockito.

---

## Como Executar

### 1. Ambiente Docker (API + DB + MinIO)

O projeto está totalmente containerizado. Para subir o ecossistema completo:

```bash
docker-compose up --build

```

### 2. Acesso à API

* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
* **Health Check:** `http://localhost:8080/actuator/health`
* **WebSocket Monitor:** `http://localhost:8080/index.html` (Página de teste inclusa)

### 3. Credenciais de Teste

* **Usuário:** `admin` | **Senha:** `123456`
* **MinIO Console:** `minioadmin` | `minioadmin` (Porta 9001)

---

## Executando Testes

Para validar as regras de negócio:

```bash
./mvnw test

```