package com.partha.document_analyzer.services;


import com.partha.document_analyzer.dto.ai.DocumentAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final ChatClient chatClient;

    @Value("classpath:prompts/summarize-document.st")
    private Resource summarizePromptResource;

    @Value("classpath:prompts/answer-question.st")
    private Resource answerQuestionPromptResource;

    public DocumentAnalysisResult summarizeDocument(String title, String content) {

        log.info("sending document {} to claude for summarization", title);

        try
        {
            BeanOutputConverter<DocumentAnalysisResult> outputConverter = new BeanOutputConverter<>(DocumentAnalysisResult.class);

            String templateText = summarizePromptResource.getContentAsString(StandardCharsets.UTF_8);
            PromptTemplate promptTemplate = new PromptTemplate(templateText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title,
                    "content", content,
                    "format", outputConverter.getFormat()));

            String response = chatClient.prompt(prompt).call().content();

            return outputConverter.convert(response);
        } catch (IOException e) {
            log.error("Failed to load summarize prompt template", e);
            throw new RuntimeException("Failed to load prompt template", e);
        }

    }

    public String answerQuestion(String title, String content, String question) {
        log.info("Answering question about document {}", title);

        try {

            String templateText = answerQuestionPromptResource.getContentAsString(StandardCharsets.UTF_8);
            PromptTemplate promptTemplate = new PromptTemplate(templateText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title,
                    "content", content,
                    "question", question));

            return chatClient.prompt(prompt).call().content();
        } catch (IOException e) {
            log.error("Failed to load summaranswer question prompt template", e);
            throw new RuntimeException("Failed to load prompt template", e);
        }

    }

}
