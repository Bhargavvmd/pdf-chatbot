package com.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;
    private static final int MAX_CONTEXT_DOCS = 3;
    private static final int MAX_CONTEXT_LENGTH = 2000;

    /**
     * Asynchronously processes a question and returns an AI-generated answer.
     * Uses vector store for relevant document retrieval and chat model for answer generation.
     *
     * @param question The user's question
     * @return CompletableFuture containing the AI-generated answer
     */
    @Async("chatExecutor")
    @Cacheable(value = "chatResponses", key = "#question")
    public CompletableFuture<String> getAnswer(String question) {
        log.info("Processing question: {}", question);
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find relevant documents asynchronously
                log.debug("Finding relevant documents for question: {}", question);
                CompletableFuture<List<Document>> relevantDocsFuture = findRelevantDocuments(question);
                List<Document> relevantDocs = relevantDocsFuture.get();
                log.info("Found {} relevant documents", relevantDocs.size());

                // Build context from relevant documents
                StringBuilder context = new StringBuilder();
                for (Document doc : relevantDocs) {
                    if (context.length() < MAX_CONTEXT_LENGTH) {
                        context.append(doc.getContent()).append("\n\n");
                        log.debug("Added document to context: {}", doc.getId());
                    } else {
                        log.debug("Context length limit reached. Skipping remaining documents.");
                        break;
                    }
                }

                // Generate answer using chat model
                log.debug("Generating answer with context length: {}", context.length());
                String prompt = String.format("Based on the following context, answer the question: %s\n\nContext: %s", 
                    question, context.toString());
                String answer = chatModel.call(new Prompt(prompt))
                    .getResult()
                    .getOutput()
                    .getContent();
                log.info("Generated answer for question: '{}', Answer length: {}", question, answer.length());
                log.debug("Generated answer: {}", answer);
                return answer;
            } catch (Exception e) {
                log.error("Error processing question: '{}'. Error: {}", question, e.getMessage(), e);
                return "I apologize, but I encountered an error while processing your question. Please try again.";
            }
        });
    }

    /**
     * Asynchronously finds relevant documents from the vector store.
     *
     * @param question The user's question
     * @return CompletableFuture containing a list of relevant documents
     */
    @Async("vectorStoreExecutor")
    private CompletableFuture<List<Document>> findRelevantDocuments(String question) {
        log.debug("Searching vector store for question: {}", question);
        return CompletableFuture.supplyAsync(() -> {
            List<Document> docs = vectorStore.similaritySearch(question);
            log.debug("Vector store returned {} documents", docs.size());
            return docs.subList(0, Math.min(docs.size(), MAX_CONTEXT_DOCS));
        });
    }
} 