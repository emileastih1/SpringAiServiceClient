# ADR 0001 — Install pgvector (and companion extensions) in the `public` schema

**Date:** 2026-05-27
**Status:** Accepted

## Context

The `vector_store` table lives in the `vectorcontent` schema. The original migration set `SET SEARCH_PATH TO vectorcontent` before running `CREATE EXTENSION vector`, which caused the extension's types, operator classes, and support functions to be installed in `vectorcontent` rather than `public`.

PostgreSQL extension symbols (types, casts, functions) are resolved through the session `search_path`. Any connection whose `search_path` does not include `vectorcontent` early enough will fail to resolve the `vector` type, producing `PSQLException: Unknown type vector`. Spring AI's `PgVectorStore` does not schema-qualify the `vector` type.

The same problem applies to `hstore` and `uuid-ossp`, installed in the same changeset.

## Decision

Install `vector`, `hstore`, and `uuid-ossp` into the `public` schema. The `CREATE EXTENSION` statements must run without `SET SEARCH_PATH TO vectorcontent` in effect — either before the SET or with the search_path reset to `public` first.

The `vector_store` table and all application tables remain in `vectorcontent`.

## Alternatives considered

Keep extensions in `vectorcontent` and guarantee `vectorcontent` is always first in every role's `search_path`. Rejected: fragile across new roles, Testcontainers-managed users, and tool connections that don't inherit the role default.

## Consequences

- Extensions are database-wide and visible from any schema context.
- The `vectorcontent` schema owns only application tables.
- Existing installs require `DROP EXTENSION` + `CREATE EXTENSION` with the correct search_path; a new Liquibase changeset handles this (do not modify the original changeset).
