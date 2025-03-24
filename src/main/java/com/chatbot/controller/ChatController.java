package com.chatbot.controller;

import com.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/ask")
    public CompletableFuture<String> getAnswer(@RequestBody String question) {
        return CompletableFuture.completedFuture(chatService.getAnswer(question));
    }
} 