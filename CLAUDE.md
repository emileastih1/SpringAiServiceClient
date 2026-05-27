## Project

Spring Boot 3.4.5 + Spring AI 1.0.0 RAG service (DMS — Document Management Service).
Issue tracker: `github.com/emileastih1/Intelligent_Content_Management`.

## Stack

- Spring Boot 3.4.5, Spring AI 1.0.0, Java 17
- Ollama (local) with `gemma3:4b` (chat) + `mxbai-embed-large` (embeddings, 1024 dims)
- PostgreSQL + pgvector via `pgvector/pgvector:pg16`
- Liquibase for schema migrations (schema: `vectorcontent`, table: `vector_store`)
- preliquibase 1.5.1 for pre-migration schema creation (incompatible with Spring Boot 3.4.5 — see below)

## Running tests

Always activate the `windows-docker-desktop` Maven profile:

```
mvn test -P windows-docker-desktop
```

The profile sets `DOCKER_HOST=tcp://localhost:2375` for the forked surefire JVM.

## Docker Desktop 29.x — required setup

Docker Desktop 29.x rejects Docker API requests at version ≤1.24 with HTTP 400.
The shaded docker-java inside Testcontainers defaults to v1.24 and ignores `DOCKER_API_VERSION` env var.

**Fix in place:** `src/test/resources/docker-java.properties` sets `api.version=1.44`.
Do not remove this file — tests will fail with `BadRequestException (Status 400)` without it.

`~/.testcontainers.properties` on the dev machine must contain:
```
docker.host=npipe:////./pipe/docker_cli
```
(the global Testcontainers config; Docker Desktop 29.x advertises `docker_cli` as its canonical pipe)

Testcontainers is pinned at **1.21.0** (`<testcontainers.version>1.21.0</testcontainers.version>` in `pom.xml`) to ensure compatibility with Docker Desktop 29.x.

## preliquibase incompatibility with Spring Boot 3.4.5

preliquibase 1.5.1 uses reflection to locate `LiquibaseAutoConfiguration$LiquibaseConfiguration.liquibase()`. Spring Boot 3.4.5 changed that method's signature, causing instantiation failure.

**Workaround for tests:**
- `src/test/resources/application-ollama.yml` excludes `PreLiquibaseAutoConfiguration` via `spring.autoconfigure.exclude`
- `src/test/resources/test-init.sql` creates the `vectorcontent` schema (replaces preliquibase's role in the test container)

The main application (`src/main/resources/preliquibase/postgresql.sql`) still handles schema creation at runtime — do not remove it.

## Active profiles

- `ollama` — wires `OllamaRagClientConfig` (chat) + `OllamaRagEmbeddingClientConfig` (embeddings). Used by all integration tests.
- `ollama-chat` — alternative chat-only profile; **not** active during tests.

`OllamaRagEmbeddingClientConfig` injects `OllamaApi` by type — safe because `ollama-chat` is not active during tests (no ambiguity).

## Liquibase migration order

1. `sprint1/release1` — sets search path, creates `vector` extension, creates `vector_store` (1536 dims), grants privileges
2. `sprint1/release2` — drops and recreates `vector_store` with **1024 dims** (matches `mxbai-embed-large`)

`VectorStoreConfig` is configured with `dimensions=1024`, `initializeSchema=false` (Liquibase owns the schema).
