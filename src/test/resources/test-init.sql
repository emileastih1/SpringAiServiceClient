CREATE ROLE doc_management_user WITH LOGIN PASSWORD 'toor';
CREATE SCHEMA IF NOT EXISTS vectorcontent;

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS vectorcontent.vector_store (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content text,
    metadata jsonb,
    embedding vector(1024)
);

CREATE INDEX ON vectorcontent.vector_store USING HNSW (embedding vector_cosine_ops);
