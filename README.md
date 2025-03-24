# AI-Powered PDF Chatbot

A Spring Boot application that processes PDF documents and provides an AI-powered chat interface to answer questions about the content of those documents.

## Features

- PDF document processing and text extraction
- Question-Answer pair generation using AI
- Vector store for efficient document retrieval
- Asynchronous processing for better performance
- REST API endpoints for document upload and chat

## Prerequisites

- Java 17 or later
- Gradle 8.0 or later
- Ollama (for AI processing)
- Spring Boot 3.2.3

## Setup

1. Clone the repository:
```bash
git clone <your-repository-url>
cd <repository-name>
```

2. Configure Ollama:
   - Install Ollama from [https://ollama.ai/](https://ollama.ai/)
   - Pull the required model:
   ```bash
   ollama pull llama2
   ```

3. Configure the application:
   - Create `application.properties` or `application.yml` with your settings
   - Set the Ollama model name and other configurations

4. Build the project:
```bash
./gradlew build
```

5. Run the application:
```bash
./gradlew bootRun
```

## API Endpoints

### PDF Processing
- `POST /api/pdf/upload`: Upload and process a single PDF file
- `POST /api/pdf/process-directory`: Process all PDFs in a directory

### Chat
- `POST /api/chat/ask`: Ask questions about the processed documents

## Architecture

The application uses:
- Spring Boot for the web framework
- Spring AI for AI processing
- PDFBox for PDF text extraction
- Vector store for document retrieval
- Asynchronous processing with CompletableFuture

## Performance Optimizations

- Asynchronous processing of PDFs and chat requests
- Rate limiting for AI operations
- Efficient text chunking with overlap
- Caching of frequently asked questions
- Parallel processing of multiple PDFs

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 