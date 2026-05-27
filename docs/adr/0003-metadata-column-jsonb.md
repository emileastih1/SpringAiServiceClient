# ADR 0003 — Use `jsonb` for the `metadata` column in `vector_store`

**Date:** 2026-05-27
**Status:** Accepted

## Context

`vector_store.metadata` was defined as `json`. Spring AI's `PgVectorStore` generates metadata-filter queries using the `@>` (contains) operator. `@>` is only available on `jsonb`, not `json`. Using `json` silently prevents all metadata-filtered similarity searches.

## Decision

Define `metadata` as `jsonb`. Applied via the `release2` changeset (which already drops and recreates the table).

## Alternatives considered

- **`json`** — stores raw text, no operator support, no GIN indexing. Rejected because Spring AI metadata filters require `@>`.
- **Separate filter table** — normalise metadata into a relational table. Rejected: over-engineering for the current query patterns.

## Consequences

- Spring AI `similaritySearch` with metadata filters works out of the box.
- GIN indexes on `metadata` are possible if query patterns demand it later.
- No data-migration cost: `release2` already recreates the table from scratch.
