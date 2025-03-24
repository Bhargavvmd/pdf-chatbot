package com.chatbot.service;

import com.chatbot.util.AnswerDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private static final int MAX_RELEVANT_DOCS = 3;
    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that answers questions based on the provided context.
            If the context doesn't contain enough information to answer the question, say so.
            Keep your answers concise and focused on the question asked.
            """;

    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;

    public String getAnswer(String question) {
        log.info("Searching for relevant documents");
        List<Document> relevantDocs = vectorStore.similaritySearch(question);
        
        if (relevantDocs.size() > MAX_RELEVANT_DOCS) {
            relevantDocs = relevantDocs.subList(0, MAX_RELEVANT_DOCS);
        }
        
        String context = buildContext(relevantDocs);
        
        PromptTemplate promptTemplate = new PromptTemplate("""
                {system}
                
                Context:
                {context}
                
                Question: {question}
                
                Answer:""");
        
        Map<String, Object> model = Map.of(
                "system", SYSTEM_PROMPT,
                "context", context,
                "question", question
        );
        
        Prompt prompt = promptTemplate.create(model);
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    private String buildContext(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    String question = doc.getMetadata().get("question").toString();
                    String answer = doc.getMetadata().get("answer").toString();
                    return String.format("Q: %s\nA: %s", question, answer);
                })
                .collect(Collectors.joining("\n\n"));
    }
} 