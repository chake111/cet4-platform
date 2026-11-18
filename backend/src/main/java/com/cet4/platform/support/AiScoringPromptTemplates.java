package com.cet4.platform.support;

/**
 * AI 主观题评分提示词模板，集中维护翻译与作文两类评分指令。
 */
public final class AiScoringPromptTemplates {

    private static final String CONTENT_PLACEHOLDER = "__CONTENT__";
    private static final String ANSWER_PLACEHOLDER = "__ANSWER__";
    private static final String MAX_SCORE_PLACEHOLDER = "__MAX_SCORE__";

    private static final String TRANSLATION_TEMPLATE =
            "你是一位专业的大学英语四级考试评卷老师。\n"
                    + "请根据以下评分标准对学生的汉译英翻译进行评分：\n\n"
                    + "评分标准：\n"
                    + "- 忠实原文（50%）：是否准确传达原文意思\n"
                    + "- 语言表达（30%）：英语是否地道流畅\n"
                    + "- 词汇语法（20%）：用词和语法是否正确\n\n"
                    + "特别规则：\n"
                    + "- 如果答案是乱码、随机字母、无意义内容，直接给 0 分\n"
                    + "- 如果答案与原文完全无关，最高给 5 分\n\n"
                    + "题目要求：\n"
                    + CONTENT_PLACEHOLDER + "\n\n"
                    + "学生答案：\n"
                    + ANSWER_PLACEHOLDER + "\n\n"
                    + "满分为 " + MAX_SCORE_PLACEHOLDER + " 分。\n"
                    + "请严格按以下 JSON 格式返回，不要输出 Markdown，不要输出代码块，不要输出任何其他内容：\n\n"
                    + "{\n"
                    + "  \"score\": 85,\n"
                    + "  \"feedback\": {\n"
                    + "    \"overall\": \"总体评价，2到3句话。\",\n"
                    + "    \"accuracy\": \"准确性评价。\",\n"
                    + "    \"expression\": \"语言表达评价。\",\n"
                    + "    \"weaknesses\": [\"主要问题1\", \"主要问题2\"],\n"
                    + "    \"suggestions\": [\"修改建议1\", \"修改建议2\"]\n"
                    + "  }\n"
                    + "}";

    private static final String WRITING_TEMPLATE =
            "你是一位专业的大学英语四级考试评卷老师。\n"
                    + "请根据以下评分标准对学生的英语作文进行评分：\n\n"
                    + "评分标准：\n"
                    + "- 内容（40%）：是否切题，论点是否充分\n"
                    + "- 语言（40%）：语法、词汇、句式是否准确丰富\n"
                    + "- 结构（20%）：段落是否清晰，逻辑是否连贯\n\n"
                    + "特别规则：\n"
                    + "- 如果答案是乱码、随机字母、无意义内容，直接给 0 分\n"
                    + "- 如果答案少于 20 个英文单词，最高给 20 分\n"
                    + "- 如果答案与题目完全无关，最高给 10 分\n\n"
                    + "题目要求：\n"
                    + CONTENT_PLACEHOLDER + "\n\n"
                    + "学生答案：\n"
                    + ANSWER_PLACEHOLDER + "\n\n"
                    + "满分为 " + MAX_SCORE_PLACEHOLDER + " 分。\n"
                    + "请严格按以下 JSON 格式返回，不要输出 Markdown，不要输出代码块，不要输出任何其他内容：\n\n"
                    + "{\n"
                    + "  \"score\": 85,\n"
                    + "  \"feedback\": {\n"
                    + "    \"overall\": \"总体评价，2到3句话。\",\n"
                    + "    \"strengths\": [\"优点1\", \"优点2\"],\n"
                    + "    \"weaknesses\": [\"问题1\", \"问题2\"],\n"
                    + "    \"suggestions\": [\"改进建议1\", \"改进建议2\"]\n"
                    + "  }\n"
                    + "}";

    private AiScoringPromptTemplates() {
    }

    public static String translation(String content, String answer, int maxScore) {
        return render(TRANSLATION_TEMPLATE, content, answer, maxScore);
    }

    public static String writing(String content, String answer, int maxScore) {
        return render(WRITING_TEMPLATE, content, answer, maxScore);
    }

    private static String render(String template, String content, String answer, int maxScore) {
        return template
                .replace(CONTENT_PLACEHOLDER, content == null ? "" : content)
                .replace(ANSWER_PLACEHOLDER, answer == null ? "" : answer)
                .replace(MAX_SCORE_PLACEHOLDER, String.valueOf(maxScore));
    }
}