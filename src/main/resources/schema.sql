-- Enable the vector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Drop existing table if it exists
DROP TABLE IF EXISTS vector_store;

-- Create the vector_store table
CREATE TABLE vector_store (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(768)
); 