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
                List<Document> qaPairs = extractQAPairs(text);
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
            return stripper.getText(document);
        }
    }

    private List<Document> extractQAPairs(String text) {
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
        String response = chatModel.call(prompt).getResult().getOutput().getContent();
        
        return parseQAPairs(response);
    }

    private List<Document> parseQAPairs(String response) {
        List<Document> qaPairs = new ArrayList<>();
        String[] pairs = response.split("\n\n");
        
        for (String pair : pairs) {
            if (pair.trim().isEmpty()) continue;
            
            String[] lines = pair.split("\n");
            if (lines.length < 2) continue;
            
            String question = lines[0].substring(2).trim();
            String answer = String.join("\n", 
                java.util.Arrays.copyOfRange(lines, 1, lines.length))
                .substring(2).trim();
            
            if (!question.isEmpty() && !answer.isEmpty()) {
                Document doc = new Document(answer);
                doc.getMetadata().put("question", question);
                doc.getMetadata().put("answer", answer);
                qaPairs.add(doc);
            }
        }
        
        return qaPairs;
    }

    private void storeQAPairs(List<Document> qaPairs) {
        vectorStore.add(qaPairs);
    }
} 