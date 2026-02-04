# API de Gerenciamento Musical - Seplag Backend

---

## Decisões de Arquitetura e Tecnologias

A escolha das tecnologias baseou-se no padrão de mercado para sistemas resilientes:

* **Java 17 & Spring Boot 3.5:** Utilização de *Records* para DTOs (imutabilidade) e o novo *RestClient* para integrações externas.
* **PostgreSQL & Flyway:** Banco de dados relacional robusto com versionamento de schema para garantir que o ambiente de produção seja idêntico ao de desenvolvimento.
* **MinIO (S3 API):** Escolha estratégica para simular armazenamento em nuvem (AWS S3) localmente, garantindo que a aplicação seja *Cloud-Ready*.
* **JWT + Refresh Token:** Implementação de segurança em duas camadas. O *Access Token* curto (5 min) minimiza riscos, enquanto o *Refresh Token* garante uma boa experiência ao usuário.
* **Bucket4j (Rate Limit):** Implementado para prevenir abusos de API e ataques de negação de serviço (DoS).

---

## Desafios Técnicos e Soluções

Durante o desenvolvimento, enfrentamos e resolvemos desafios complexos de engenharia:

### 1. Sincronização de Regionais

**O Desafio:** Consumir uma API externa e sincronizar com o banco local com regras de inativação e criação de novos registros para manter histórico.
**A Solução:** Implementamos uma lógica de comparação em memória utilizando *Maps* para garantir complexidade , evitando múltiplas consultas desnecessárias ao banco de dados durante o loop de sincronização.

### 2. Busca Avançada N:N com Paginação

**O Desafio:** Filtrar álbuns por tipo de artista (SOLO/BANDA) ou nome, mantendo a paginação do Spring Data correta.
**A Solução:** O uso de `DISTINCT` em relacionamentos Many-to-Many causa problemas no cálculo de páginas do Hibernate. Resolvemos isso utilizando `LEFT JOIN` e definindo um `countQuery` customizado no repositório, garantindo que o contador de registros seja preciso mesmo com filtros complexos.

### 3. Observabilidade Real (Health Checks)

**O Desafio:** O Health Check padrão do Spring diz que a app está "UP" mesmo se o storage (MinIO) cair.
**A Solução:** Criamos um `MinioHealthIndicator` customizado. Agora, o endpoint `/actuator/health` valida se o banco **e** o storage estão respondendo, permitindo que orquestradores (Kubernetes/Docker) tomem decisões reais de Liveness e Readiness.

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