package com.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingService {
    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public CompletableFuture<Void> processSinglePdf(MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            try {
                String text = extractTextFromPdf(file);
                List<String> qaPairs = generateQAPairs(text);
                storeQAPairs(qaPairs);
            } catch (IOException e) {
                log.error("Error processing PDF file: " + file.getOriginalFilename(), e);
            }
        }, executorService);
    }

    public CompletableFuture<Void> processDirectory(String directoryPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<Path> pdfFiles = Files.walk(Path.of(directoryPath))
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .collect(Collectors.toList());

                List<CompletableFuture<Void>> futures = pdfFiles.stream()
                    .map(path -> CompletableFuture.runAsync(() -> {
                        try {
                            processPdfFile(path);
                        } catch (IOException e) {
                            log.error("Error processing PDF file: " + path, e);
                        }
                    }, executorService))
                    .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (IOException e) {
                log.error("Error processing directory: " + directoryPath, e);
            }
        }, executorService);
    }

    private void processPdfFile(Path path) throws IOException {
        try (PDDocument document = PDDocument.load(path.toFile())) {
            String text = new PDFTextStripper().getText(document);
            List<String> qaPairs = generateQAPairs(text);
            storeQAPairs(qaPairs);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private List<String> generateQAPairs(String text) {
        List<String> chunks = splitTextIntoChunks(text);
        
        // Process chunks in parallel with rate limiting
        List<CompletableFuture<List<String>>> futures = chunks.stream()
            .map(chunk -> CompletableFuture.supplyAsync(() -> {
                String prompt = "Generate question-answer pairs from the following text. Generate as many relevant questions as possible:\n\n" + chunk;
                Message userMessage = new UserMessage(prompt);
                
                // Process Ollama request asynchronously
                return CompletableFuture.supplyAsync(() -> {
                    String response = chatModel.call(new Prompt(List.of(userMessage)))
                        .getResult()
                        .getOutput()
                        .getContent();
                    return parseQAResponse(response);
                }, executorService).join();
            }, executorService))
            .collect(Collectors.toList());

        // Collect all results
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();
        
        for (String word : words) {
            currentChunk.append(word).append(" ");
            if (currentChunk.toString().split("\\s+").length >= 1000) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }

    private List<String> parseQAResponse(String response) {
        List<String> qaPairs = new ArrayList<>();
        String[] lines = response.split("\n");
        StringBuilder currentPair = new StringBuilder();
        
        for (String line : lines) {
            if (line.trim().startsWith("Q:") || line.trim().startsWith("A:")) {
                currentPair.append(line).append("\n");
            } else if (currentPair.length() > 0) {
                qaPairs.add(currentPair.toString().trim());
                currentPair = new StringBuilder();
            }
        }
        
        if (currentPair.length() > 0) {
            qaPairs.add(currentPair.toString().trim());
        }
        
        return qaPairs;
    }

    private void storeQAPairs(List<String> qaPairs) {
        List<Document> documents = qaPairs.stream()
            .map(Document::new)
            .collect(Collectors.toList());
        vectorStore.add(documents);
    }
} 