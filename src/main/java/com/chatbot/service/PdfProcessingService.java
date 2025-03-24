package com.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingService {
    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;
    private final Semaphore ollamaSemaphore = new Semaphore(3); // Rate limit Ollama requests
    private static final int CHUNK_SIZE = 1000;
    private static final int OVERLAP_SIZE = 100;

    /**
     * Asynchronously processes a single PDF file.
     */
    @Async("pdfProcessingExecutor")
    public CompletableFuture<Void> processSinglePdf(MultipartFile file) {
        log.info("Starting to process PDF file: {}", file.getOriginalFilename());
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Extracting text from PDF: {}", file.getOriginalFilename());
                String text = extractTextFromPdf(file);
                log.debug("Extracted {} characters of text from PDF", text.length());

                log.info("Generating QA pairs for PDF: {}", file.getOriginalFilename());
                CompletableFuture<List<String>> qaPairsFuture = generateQAPairs(text);
                List<String> qaPairs = qaPairsFuture.get();
                log.info("Generated {} QA pairs from PDF: {}", qaPairs.size(), file.getOriginalFilename());
                
                // Log each QA pair for detailed tracking
                for (int i = 0; i < qaPairs.size(); i++) {
                    log.debug("QA Pair {}/{} from {}: \n{}", 
                        i + 1, qaPairs.size(), file.getOriginalFilename(), qaPairs.get(i));
                }

                log.debug("Storing {} QA pairs in vector store", qaPairs.size());
                CompletableFuture<Void> storeFuture = storeQAPairs(qaPairs);
                storeFuture.get(); // Wait for storage to complete
                log.info("Successfully processed and stored QA pairs for PDF: {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Error processing PDF file: {}. Error: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("Failed to process PDF", e);
            }
        });
    }

    /**
     * Asynchronously processes all PDF files in a directory.
     */
    @Async("pdfProcessingExecutor")
    public CompletableFuture<Void> processDirectory(String directoryPath) {
        log.info("Starting to process directory: {}", directoryPath);
        return CompletableFuture.runAsync(() -> {
            try {
                List<Path> pdfFiles = Files.walk(Path.of(directoryPath))
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .collect(Collectors.toList());
                log.info("Found {} PDF files in directory: {}", pdfFiles.size(), directoryPath);

                List<CompletableFuture<Void>> futures = pdfFiles.stream()
                    .map(path -> CompletableFuture.runAsync(() -> {
                        try {
                            log.debug("Processing PDF file: {}", path);
                            processPdfFile(path);
                            log.debug("Completed processing PDF file: {}", path);
                        } catch (IOException e) {
                            log.error("Error processing PDF file: {}. Error: {}", path, e.getMessage(), e);
                            throw new RuntimeException("Failed to process PDF", e);
                        }
                    }))
                    .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                log.info("Successfully processed all {} PDFs in directory: {}", pdfFiles.size(), directoryPath);
            } catch (IOException e) {
                log.error("Error processing directory: {}. Error: {}", directoryPath, e.getMessage(), e);
                throw new RuntimeException("Failed to process directory", e);
            }
        });
    }

    private void processPdfFile(Path path) throws IOException {
        log.debug("Processing PDF file from path: {}", path);
        try (PDDocument document = PDDocument.load(path.toFile())) {
            String text = new PDFTextStripper().getText(document);
            log.debug("Extracted {} characters of text from PDF: {}", text.length(), path);

            CompletableFuture<List<String>> qaPairsFuture = generateQAPairs(text);
            List<String> qaPairs = qaPairsFuture.get();
            log.info("Generated {} QA pairs from PDF: {}", qaPairs.size(), path);

            CompletableFuture<Void> storeFuture = storeQAPairs(qaPairs);
            storeFuture.get();
            log.debug("Successfully stored QA pairs for PDF: {}", path);
        } catch (Exception e) {
            log.error("Error in processPdfFile for path: {}. Error: {}", path, e.getMessage(), e);
            throw new IOException("Failed to process PDF file", e);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        log.debug("Starting text extraction from PDF: {}", file.getOriginalFilename());
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            String text = new PDFTextStripper().getText(document);
            log.debug("Completed text extraction. Extracted {} characters", text.length());
            return text;
        }
    }

    /**
     * Generates Q&A pairs from text using the chat model.
     */
    @Async("pdfProcessingExecutor")
    protected CompletableFuture<List<String>> generateQAPairs(String text) {
        log.debug("Starting QA pair generation for text of length: {}", text.length());
        List<String> chunks = splitTextIntoChunks(text);
        log.debug("Split text into {} chunks for processing", chunks.size());
        
        @SuppressWarnings("unchecked")
        List<CompletableFuture<List<String>>> futures = chunks.stream()
            .map(chunk -> CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Acquiring semaphore for chunk processing (length: {})", chunk.length());
                    ollamaSemaphore.acquire();
                    try {
                        String prompt = "Generate question-answer pairs from the following text. Format each pair as 'Q: [question]\nA: [answer]'\n\n" + chunk;
                        log.debug("Sending chunk to chat model for QA generation");
                        String response = chatModel.call(new Prompt(prompt))
                            .getResult()
                            .getOutput()
                            .getContent();
                        List<String> pairs = parseQAResponse(response);
                        log.debug("Generated {} QA pairs from chunk", pairs.size());
                        return pairs;
                    } finally {
                        ollamaSemaphore.release();
                        log.debug("Released semaphore after chunk processing");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted while generating Q&A pairs", e);
                    return new ArrayList<String>();
                }
            }))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<String> allPairs = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                log.info("Completed QA pair generation. Total pairs generated: {}", allPairs.size());
                return allPairs;
            });
    }

    private List<String> splitTextIntoChunks(String text) {
        log.debug("Splitting text of length {} into chunks", text.length());
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;
        
        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;
            
            if (wordCount >= CHUNK_SIZE) {
                chunks.add(currentChunk.toString());
                String[] overlapWords = currentChunk.toString().split("\\s+");
                currentChunk = new StringBuilder();
                for (int i = Math.max(0, overlapWords.length - OVERLAP_SIZE); i < overlapWords.length; i++) {
                    currentChunk.append(overlapWords[i]).append(" ");
                }
                wordCount = currentChunk.toString().split("\\s+").length;
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        log.debug("Split text into {} chunks", chunks.size());
        return chunks;
    }

    private List<String> parseQAResponse(String response) {
        log.debug("Parsing QA response of length: {}", response.length());
        List<String> qaPairs = new ArrayList<>();
        String[] lines = response.split("\n");
        StringBuilder currentPair = new StringBuilder();
        
        for (String line : lines) {
            if (line.trim().startsWith("Q:") || line.trim().startsWith("A:")) {
                currentPair.append(line).append("\n");
                if (line.trim().startsWith("A:")) {
                    qaPairs.add(currentPair.toString().trim());
                    currentPair = new StringBuilder();
                }
            }
        }
        
        log.debug("Parsed {} QA pairs from response", qaPairs.size());
        return qaPairs;
    }

    /**
     * Stores Q&A pairs in the vector store asynchronously.
     */
    @Async("vectorStoreExecutor")
    protected CompletableFuture<Void> storeQAPairs(List<String> qaPairs) {
        log.debug("Starting to store {} QA pairs in vector store", qaPairs.size());
        return CompletableFuture.runAsync(() -> {
            List<Document> documents = qaPairs.stream()
                .map(pair -> {
                    log.trace("Creating document for QA pair: {}", pair);
                    return new Document(pair);
                })
                .collect(Collectors.toList());
            vectorStore.add(documents);
            log.debug("Successfully stored {} documents in vector store", documents.size());
        });
    }
} 