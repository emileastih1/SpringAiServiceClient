DROP TABLE IF EXISTS vector_store;

CREATE TABLE vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1024)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
