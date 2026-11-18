package com.cet4.platform.service.impl;

import com.cet4.platform.service.AiScoringResult;
import com.cet4.platform.service.AiScoringService;
import com.cet4.platform.support.AiScoringPromptTemplates;
import com.cet4.platform.support.AiScoringResponseParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiScoringServiceImpl implements AiScoringService {

    private static final String TRANSLATION = "translation";

    private final AiScoringResponseParser responseParser;
    private RestTemplate restTemplate;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Value("${ai.deepseek.timeout:30}")
    private int timeout;

    public AiScoringServiceImpl(AiScoringResponseParser responseParser) {
        this.responseParser = responseParser;
    }

    @PostConstruct
    public void initRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMs = Math.max(timeout, 1) * 1000;
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public AiScoringResult scoreSubjectiveAnswer(String questionType, String content, String answer, int maxScore) {
        if (maxScore <= 0 || answer == null || answer.isBlank()) {
            return AiScoringResult.fail();
        }

        try {
            String prompt = buildPrompt(questionType, content, answer, maxScore);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/v1/chat/completions", requestEntity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return AiScoringResult.fail();
            }

            String responseContent = extractContent(response.getBody());
            AiScoringResult result = responseParser.parse(responseContent, maxScore);
            log.info("AI评分完成: questionType={}, maxScore={}, score={}, hasFeedback={}",
                    questionType, maxScore, result.getScore(), result.getFeedback() != null);
            return result;
        } catch (Exception ex) {
            log.warn("AI评分失败，已降级为0分。questionType={}", questionType, ex);
            return AiScoringResult.fail();
        }
    }

    private String buildPrompt(String questionType, String content, String answer, int maxScore) {
        if (TRANSLATION.equalsIgnoreCase(questionType)) {
            return AiScoringPromptTemplates.translation(content, answer, maxScore);
        }
        return AiScoringPromptTemplates.writing(content, answer, maxScore);
    }

    private String extractContent(Map responseBody) {
        Object choicesObj = responseBody.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }

        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return "";
        }

        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            return "";
        }

        Object contentObj = messageMap.get("content");
        return contentObj == null ? "" : String.valueOf(contentObj);
    }

}
