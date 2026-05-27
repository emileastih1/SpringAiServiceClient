SET SEARCH_PATH TO vectorcontent, public;

DROP TABLE IF EXISTS vector_store;

CREATE TABLE vector_store (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content text,
    metadata jsonb,
    embedding vector(1024)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
