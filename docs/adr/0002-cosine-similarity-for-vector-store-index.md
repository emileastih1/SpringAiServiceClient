# ADR 0002 — Cosine similarity as the vector distance metric

**Date:** 2026-05-27
**Status:** Accepted

## Context

`vector_store` uses an HNSW index with `vector_cosine_ops`. The active embedding model is `mxbai-embed-large` (1024 dims), which produces normalised vectors for which cosine similarity is the correct distance function.

pgvector HNSW operator classes (`vector_cosine_ops`, `vector_l2_ops`, `vector_ip_ops`) are fixed at index creation time. Changing the metric requires dropping and recreating the index.

## Decision

Lock in `vector_cosine_ops` (cosine similarity) as the distance metric for `vector_store`. The choice is tied to `mxbai-embed-large`. If the embedding model is replaced with one that uses a different metric, a new Liquibase changeset must drop and recreate the index with the appropriate operator class.

## Alternatives considered

- **L2 (`vector_l2_ops`)** — correct for unnormalised models; not appropriate for `mxbai-embed-large`.
- **Inner product (`vector_ip_ops`)** — equivalent to cosine on normalised vectors but requires vectors to be unit-length at query time; less explicit.

## Consequences

- HNSW index parameters use pgvector defaults (`m=16`, `ef_construction=64`), sufficient for the current data volume. Revisit if the corpus grows past ~100k documents.
- Any embedding model swap must be accompanied by a migration that recreates the index with the matching operator class.
