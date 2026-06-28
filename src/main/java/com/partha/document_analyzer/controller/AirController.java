package com.partha.document_analyzer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AirController {

    private final ChatClient chatClient;

    @GetMapping("/test")
    public String test() {
        return chatClient.prompt().user("Say hello in one sentence").call().content();
    }

}
