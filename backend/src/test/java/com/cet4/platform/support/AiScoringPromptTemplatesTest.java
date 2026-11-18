package com.cet4.platform.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiScoringPromptTemplatesTest {

    @Test
    void buildsTranslationPromptWithContentAnswerAndMaxScore() {
        String prompt = AiScoringPromptTemplates.translation("汉译英题目", "student translation", 15);

        assertTrue(prompt.contains("汉译英翻译"));
        assertTrue(prompt.contains("题目要求：\n汉译英题目\n\n学生答案：\nstudent translation"));
        assertTrue(prompt.contains("满分为 15 分。"));
        assertTrue(prompt.contains("\"accuracy\""));
        assertFalse(prompt.contains("__CONTENT__"));
        assertFalse(prompt.contains("__ANSWER__"));
        assertFalse(prompt.contains("__MAX_SCORE__"));
    }

    @Test
    void buildsWritingPromptWithEssaySpecificRules() {
        String prompt = AiScoringPromptTemplates.writing("作文题目", "student essay", 20);

        assertTrue(prompt.contains("英语作文"));
        assertTrue(prompt.contains("如果答案少于 20 个英文单词，最高给 20 分"));
        assertTrue(prompt.contains("\"strengths\""));
        assertTrue(prompt.contains("满分为 20 分。"));
    }

    @Test
    void rendersMissingContentAsEmptyString() {
        String prompt = AiScoringPromptTemplates.translation(null, "answer", 10);

        assertTrue(prompt.contains("题目要求：\n\n\n学生答案：\nanswer"));
        assertFalse(prompt.contains("null"));
    }
}