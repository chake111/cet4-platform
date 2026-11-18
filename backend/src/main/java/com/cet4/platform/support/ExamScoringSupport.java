package com.cet4.platform.support;

import com.cet4.platform.domain.QuestionTypes;
import com.cet4.platform.entity.Question;
import com.cet4.platform.service.AiScoringResult;
import com.cet4.platform.service.AiScoringService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamScoringSupport {

    private final AiScoringService aiScoringService;

    public ScoringResult score(Map<String, String> answers, Iterable<Question> questions) {
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question question : questions) {
            questionMap.put(question.getId(), question);
        }

        ScoringResult result = new ScoringResult();
        for (Question question : questionMap.values()) {
            int maxScore = maxScoreOf(question);
            result.total += maxScore;
            if (QuestionTypes.isSubjective(question.getQuestionType())) {
                continue;
            }
            // 整卷提交沿用原有语义：标准答案为空时不参与客观题判分
            ObjectiveGrade grade = gradeObjective(
                    question, answers.get(String.valueOf(question.getId())), true);
            if (!grade.isScored()) {
                continue;
            }
            result.score += grade.getScore();
            result.questionScoreMap.put(question.getId(), grade.getScore());
            result.questionCorrectMap.put(question.getId(), grade.isCorrect());
        }

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Long questionId = parseQuestionId(entry.getKey());
            if (questionId == null) {
                continue;
            }
            Question question = questionMap.get(questionId);
            if (question == null || !QuestionTypes.isSubjective(question.getQuestionType())) {
                continue;
            }
            AiScoringResult aiResult = aiScoringService.scoreSubjectiveAnswer(
                    question.getQuestionType(),
                    question.getContent(),
                    entry.getValue(),
                    maxScoreOf(question));
            int aiScore = aiResult.getScore();
            result.score += aiScore;
            result.questionScoreMap.put(questionId, aiScore);
            result.questionCorrectMap.put(questionId, null);
            if (aiResult.getFeedback() != null) {
                result.questionFeedbackMap.put(questionId, aiResult.getFeedback());
            }
        }

        return result;
    }

    /**
     * 客观题统一判分入口，整卷提交与考试记录提交共用同一套比较逻辑。
     *
     * @param skipBlankCorrectAnswer 标准答案缺失时是否跳过判分（整卷提交沿用该历史语义）
     */
    public static ObjectiveGrade gradeObjective(Question question,
                                                String userAnswer,
                                                boolean skipBlankCorrectAnswer) {
        String normalizedCorrectAnswer = normalizeAnswer(question.getCorrectAnswer());
        if (skipBlankCorrectAnswer && isBlank(normalizedCorrectAnswer)) {
            return ObjectiveGrade.unscored();
        }
        boolean correct = Objects.equals(normalizeAnswer(userAnswer), normalizedCorrectAnswer);
        return ObjectiveGrade.of(correct, correct ? maxScoreOf(question) : 0);
    }

    public static String normalizeAnswer(String answer) {
        return answer == null ? null : answer.trim().toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static int maxScoreOf(Question question) {
        return question.getScore() == null ? 0 : question.getScore();
    }

    private Long parseQuestionId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Getter
    public static class ScoringResult {
        private int score;
        private int total;
        private final Map<Long, Integer> questionScoreMap = new HashMap<>();
        private final Map<Long, Boolean> questionCorrectMap = new HashMap<>();
        private final Map<Long, String> questionFeedbackMap = new HashMap<>();
    }

    @Getter
    public static final class ObjectiveGrade {
        private final boolean scored;
        private final boolean correct;
        private final int score;

        private ObjectiveGrade(boolean scored, boolean correct, int score) {
            this.scored = scored;
            this.correct = correct;
            this.score = score;
        }

        private static ObjectiveGrade unscored() {
            return new ObjectiveGrade(false, false, 0);
        }

        private static ObjectiveGrade of(boolean correct, int score) {
            return new ObjectiveGrade(true, correct, score);
        }
    }
}
