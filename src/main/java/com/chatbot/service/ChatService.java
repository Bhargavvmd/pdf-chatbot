package com.chatbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final VectorStore vectorStore;
    private final OllamaChatModel chatModel;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public CompletableFuture<String> getAnswer(String question) {
        return CompletableFuture.supplyAsync(() -> {
            // Find relevant Q&A pairs from the vector store asynchronously
            CompletableFuture<List<Document>> docsFuture = CompletableFuture.supplyAsync(() -> 
                vectorStore.similaritySearch(question), executorService);

            // Wait for documents to be retrieved
            List<Document> relevantDocs = docsFuture.join();
            
            // Combine relevant context with the question using stream
            String context = relevantDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n", 
                    "Based on the following context, please answer the question:\n\n",
                    "\n\nQuestion: " + question));
            
            // Generate answer using chat model asynchronously
            return CompletableFuture.supplyAsync(() -> {
                Message userMessage = new UserMessage(context);
                return chatModel.call(new Prompt(List.of(userMessage)))
                    .getResult()
                    .getOutput()
                    .getContent();
            }, executorService).join();
        }, executorService);
    }
} 