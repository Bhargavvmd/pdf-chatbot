-- Enable the vector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the vector_store table if it doesn't exist
CREATE TABLE IF NOT EXISTS vector_store (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(1536)
); 