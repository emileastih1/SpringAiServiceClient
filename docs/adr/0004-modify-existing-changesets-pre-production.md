# ADR 0004 — Modify existing changesets pre-production rather than adding corrective ones

**Date:** 2026-05-27
**Status:** Accepted

## Context

The grill-with-docs session (2026-05-27) identified several foundational mistakes in `release1` and `release2`: extensions installed in the wrong schema, unused extensions (`hstore`, `uuid-ossp`), `metadata json` instead of `jsonb`, missing per-changeset `SET SEARCH_PATH`, and a stale `documentcontent` role in the search-path changeset.

The standard Liquibase practice is to never modify applied changesets — doing so causes a checksum mismatch failure. The alternatives were: (A) modify in place + run `mvn liquibase:clearCheckSums`, or (B) add a `release3` with corrective SQL.

## Decision

Modify the existing `release1` and `release2` changesets directly and run `mvn liquibase:clearCheckSums` to resync. `test-init.sql` is updated to match.

This decision is valid because:
- The project is pre-production. No production database exists.
- The mistakes are foundational (wrong schema for extensions, wrong column type). Carrying them as permanent history would mislead future readers.
- All developers work against ephemeral Docker/Testcontainers databases that are recreated from scratch on each test run.

## Consequences

- Any existing dev database must have checksums cleared (`mvn liquibase:clearCheckSums`) or be recreated before the next `mvn spring-boot:run` or `mvn test`.
- This exception does not apply once a production deployment exists. From that point, corrective changesets must be used.
