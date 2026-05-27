# Document Management Service (DMS)

A RAG service that ingests source documents, stores their vector embeddings, and answers queries by retrieving semantically relevant content and passing it to a language model.

## Language

**Document**:
A unit of source content ingested into the system. The system of record — embeddings are derived from Documents and can always be regenerated from them.
_Avoid_: File, resource, content

**Embedding**:
A fixed-length vector that encodes the semantic meaning of a Document, produced by an embedding model. Stored alongside the Document content for similarity retrieval.
_Avoid_: Vector, encoding, representation

**Vector Store**:
The retrieval component that holds Embeddings and answers similarity queries. Backed by the `vector_store` table in the `vectorcontent` schema.
_Avoid_: Embedding store, vector database, vector table

**Similarity Search**:
A query that finds Documents whose Embeddings are closest to a query Embedding, optionally filtered by metadata. The primary read operation in the system.
_Avoid_: Semantic search, nearest-neighbour query, vector search

**Ingestion**:
The process of taking a Document, generating its Embedding, and writing both to the Vector Store. Embeddings are derived — re-running Ingestion on the same Document is always safe.
_Avoid_: Indexing, uploading, storing

## Example dialogue

> **Dev:** Do we need to back up the vector store before running the migration?
>
> **Domain expert:** No — the Vector Store is derived. If we lose it, we re-run Ingestion over the Documents and it rebuilds itself.
>
> **Dev:** And if the embedding model changes?
>
> **Domain expert:** Then the old Embeddings are invalid — different models produce incompatible vector spaces. We'd need to re-ingest all Documents with the new model.
