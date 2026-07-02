package com.partha.document_analyzer.dto.ai;

import com.partha.document_analyzer.enums.Sentiment;

import java.util.List;

public record DocumentAnalysisResult(String summary,
                                     List<String> keyTopics,
                                     String documentType,
                                     Sentiment sentiment) {
}
