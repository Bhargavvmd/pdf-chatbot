package com.chatbot.service;

import com.chatbot.util.AnswerDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingService {
    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that extracts question-answer pairs from text.
            For each question-answer pair you find, format it as:
            Q: [question]
            A: [answer]
            
            The answer can be multiple lines if needed.
            Only include clear question-answer pairs, not general statements or descriptions.
            """;

    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;

    @Async("pdfProcessingExecutor")
    public CompletableFuture<Void> processPdf(MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Processing PDF file: {}", file.getOriginalFilename());
                String text = extractTextFromPdf(file);
                log.debug("Extracted text length: {} characters", text.length());
                
                List<Document> qaPairs = extractQAPairs(text);
                log.info("Extracted {} Q&A pairs from PDF", qaPairs.size());
                
                storeQAPairs(qaPairs);
                log.info("Successfully processed PDF file: {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Error processing PDF file: {}. Error: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("Failed to process PDF", e);
            }
        });
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Successfully extracted {} pages of text from PDF", document.getNumberOfPages());
            return text;
        }
    }

    private List<Document> extractQAPairs(String text) {
        log.info("Starting Q&A pair extraction from text");
        PromptTemplate promptTemplate = new PromptTemplate("""
                {system}
                
                Text:
                {text}
                
                Extract all question-answer pairs from the text above.
                Format each pair as:
                Q: [question]
                A: [answer]
                
                Pairs:""");
        
        Map<String, Object> model = Map.of(
                "system", SYSTEM_PROMPT,
                "text", text
        );
        
        Prompt prompt = promptTemplate.create(model);
        log.debug("Sending prompt to Ollama model");
        String response = chatModel.call(prompt).getResult().getOutput().getContent();
        log.debug("Received response from Ollama model, length: {}", response.length());
        
        return parseQAPairs(response);
    }

    private List<Document> parseQAPairs(String response) {
        List<Document> qaPairs = new ArrayList<>();
        String[] pairs = response.split("\n\n");
        
        log.info("Starting to parse {} potential Q&A pairs", pairs.length);
        
        for (String pair : pairs) {
            if (pair.trim().isEmpty()) {
                log.debug("Skipping empty pair");
                continue;
            }
            
            String[] lines = pair.split("\n");
            if (lines.length < 2) {
                log.debug("Skipping invalid pair - insufficient lines: {}", pair);
                continue;
            }
            
            String question = lines[0].substring(2).trim();
            String answer = String.join("\n", 
                java.util.Arrays.copyOfRange(lines, 1, lines.length))
                .substring(2).trim();
            
            if (!question.isEmpty() && !answer.isEmpty()) {
                Document doc = new Document(answer);
                doc.getMetadata().put("question", question);
                doc.getMetadata().put("answer", answer);
                qaPairs.add(doc);
                log.info("Generated Q&A Pair:");
                log.info("Question: {}", question);
                log.info("Answer: {}", answer);
            } else {
                log.debug("Skipping pair with empty question or answer");
            }
        }
        
        log.info("Successfully parsed {} valid Q&A pairs", qaPairs.size());
        return qaPairs;
    }

    private void storeQAPairs(List<Document> qaPairs) {
        try {
            log.info("Attempting to store {} Q&A pairs in vector store", qaPairs.size());
            for (Document doc : qaPairs) {
                if (doc.getEmbedding() != null) {
                    log.debug("Document embedding dimensions: {}", doc.getEmbedding().length);
                } else {
                    log.warn("Document has no embedding");
                }
            }
            vectorStore.add(qaPairs);
            log.info("Successfully stored Q&A pairs in vector store");
        } catch (Exception e) {
            log.error("Error storing Q&A pairs in vector store: {}", e.getMessage(), e);
            throw e;
        }
    }
} 