CREATE ROLE doc_management_user WITH LOGIN PASSWORD 'toor';
CREATE SCHEMA IF NOT EXISTS vectorcontent;

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vectorcontent.vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1024)
);

CREATE INDEX ON vectorcontent.vector_store USING HNSW (embedding vector_cosine_ops);
