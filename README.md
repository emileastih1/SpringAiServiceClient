# dms

> AI / RAG microservice for the Intelligent Document Management system.

## System Overview

```
         API clients (curl / Postman / Swagger UI)
               │  Bearer JWT
               ▼
 ┌──────────────────────────┐   HTTP   ┌──────────────────────────┐
 │  intelligent-content-    │ ───────▶ │          dms             │
 │      management          │          │  (AI / RAG — this svc)   │
 │  port 8085  /idm         │          │   port 8086              │
 └───┬─────────┬─────┬──────┘          └───────┬──────────────────┘
     │         │     │                         │
     ▼         ▼     ▼                         ▼
 PostgreSQL  Elastic Keycloak       PostgreSQL + pgvector
  (docs)    (search)  (IdP)          (vector store)
                                   + Ollama (local LLM)
```

This service owns document ingestion and question-answering. It is called over HTTP by [intelligent-content-management](https://github.com/emileastih1/Intelligent_Content_Management); API clients never call it directly.

## What This Service Does

Ingests documents by generating vector embeddings with `mxbai-embed-large` and storing them in a pgvector-backed PostgreSQL table. Answers natural-language queries via a RAG pipeline: similarity search retrieves relevant document chunks, which are passed as context to `gemma3:4b` running locally in Ollama. Both models run entirely on-device — no external AI API calls.

## Architecture Decisions

**ADR 0001 — pgvector extension installed in `public` schema**
The `vector`, `hstore`, and `uuid-ossp` extensions must be installed in `public`, not in the `vectorcontent` application schema. Spring AI's `PgVectorStore` does not schema-qualify the `vector` type; if the extension lives in a non-default schema, every connection that lacks `vectorcontent` in its `search_path` fails with `PSQLException: Unknown type vector`. The application tables remain in `vectorcontent`. See [`docs/adr/0001-pgvector-extension-in-public-schema.md`](docs/adr/0001-pgvector-extension-in-public-schema.md).

**ADR 0002 — Cosine similarity as the vector distance metric**
The HNSW index on `vector_store` uses `vector_cosine_ops` to match `mxbai-embed-large`, which produces normalised vectors. The operator class is fixed at index creation time — swapping the embedding model requires a Liquibase migration that drops and recreates the index with the correct operator class. See [`docs/adr/0002-cosine-similarity-for-vector-store-index.md`](docs/adr/0002-cosine-similarity-for-vector-store-index.md).

**ADR 0003 — `jsonb` for the `metadata` column**
Spring AI metadata filters use the `@>` (contains) operator, which is only available on `jsonb`, not `json`. Defining `metadata` as `json` silently prevented all metadata-filtered similarity searches. See [`docs/adr/0003-metadata-column-jsonb.md`](docs/adr/0003-metadata-column-jsonb.md).

**ADR 0004 — Modify existing changesets pre-production**
Foundational mistakes in `release1` and `release2` (wrong extension schema, wrong metadata column type) were corrected in-place rather than via an additive `release3` changeset. This is valid because the project has no production database — all dev databases are ephemeral Testcontainers instances. If your local DB predates this change, run `./mvnw liquibase:clearCheckSums` before the next `./mvnw spring-boot:run`. See [`docs/adr/0004-modify-existing-changesets-pre-production.md`](docs/adr/0004-modify-existing-changesets-pre-production.md).

## Tech Stack

| Technology | Version | Role |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 4.0.5 | Application framework |
| Spring AI | 2.0.0-M7 | RAG pipeline orchestration |
| Ollama | local | LLM runtime |
| gemma3:4b | — | Chat / generation model |
| mxbai-embed-large | — | Embedding model (1024 dims, cosine) |
| PostgreSQL + pgvector | 16 | Vector store (`vectorcontent` schema) |
| Liquibase | — | Schema migrations |
| Testcontainers | 1.21.0 | Integration test infrastructure |
| Docker (pgvector/pgvector:pg16) | — | Local PostgreSQL orchestration |

## Getting Started

**Prerequisites:** Java 21, Docker Desktop, [Ollama](https://ollama.com) installed and running.

```bash
# 1. Pull the required models (one-time, ~2–5 GB each)
ollama pull gemma3:4b
ollama pull mxbai-embed-large

# 2. Start PostgreSQL with pgvector (port 5434, database doc_management_db)
docker run -d --name dms-postgres \
  -e POSTGRES_PASSWORD=toor \
  -e POSTGRES_DB=doc_management_db \
  -p 5434:5432 \
  pgvector/pgvector:pg16

# 3. Run the application (ollama profile is active by default)
./mvnw spring-boot:run

# 4. Explore the API
# http://localhost:8086/AiServiceClient/swagger-ui/index.html

# 5. Verify the service is up
# curl http://localhost:8086/AiServiceClient/actuator/health
```

This service is a backend dependency of `intelligent-content-management`. See the [intelligent-content-management README](https://github.com/emileastih1/Intelligent_Content_Management) for the full system setup.

## API

| | |
|---|---|
| Swagger UI | http://localhost:8086/AiServiceClient/swagger-ui/index.html |
| Context path | `/AiServiceClient` |
| Auth | None — called service-to-service from `intelligent-content-management` |

## Running Tests

```bash
./mvnw test -Pwindows-docker-desktop
```

**Docker Desktop 29.x** — `~/.testcontainers.properties` must contain:

```
docker.host=npipe:////./pipe/docker_cli
```

Testcontainers is pinned at **1.21.0** for Docker Desktop 29.x compatibility (`src/test/resources/docker-java.properties` sets `api.version=1.44` — do not remove this file).

## Support

[Open an issue](https://github.com/emileastih1/Intelligent_Content_Management/issues) · emileastih1@gmail.com
