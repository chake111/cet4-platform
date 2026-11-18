package com.cet4.platform.support;

import com.cet4.platform.common.BusinessException;
import com.cet4.platform.domain.QuestionTypes;
import com.cet4.platform.dto.AnswerItemDTO;
import com.cet4.platform.entity.ExamRecord;
import com.cet4.platform.entity.Question;
import com.cet4.platform.entity.User;
import com.cet4.platform.entity.UserAnswer;
import com.cet4.platform.mapper.QuestionMapper;
import com.cet4.platform.mapper.UserAnswerMapper;
import com.cet4.platform.support.ExamScoringSupport.ObjectiveGrade;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 考试记录答案写入器：负责按题判分并落库 user_answer。
 */
@Component
@RequiredArgsConstructor
public class ExamRecordAnswerWriter {

    private final QuestionMapper questionMapper;
    private final UserAnswerMapper userAnswerMapper;

    public int saveRecordAnswers(Long recordId, User user, ExamRecord examRecord, List<AnswerItemDTO> answerItems) {
        Map<Long, Question> questionMap = loadQuestions(answerItems);

        int totalObjectiveScore = 0;
        for (AnswerItemDTO answerItem : answerItems) {
            totalObjectiveScore += saveOneAnswer(recordId, user, examRecord, answerItem, questionMap);
        }
        return totalObjectiveScore;
    }

    private Map<Long, Question> loadQuestions(List<AnswerItemDTO> answerItems) {
        List<Long> questionIds = answerItems.stream()
                .map(AnswerItemDTO::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
    }

    private int saveOneAnswer(Long recordId,
                              User user,
                              ExamRecord examRecord,
                              AnswerItemDTO answerItem,
                              Map<Long, Question> questionMap) {
        Long questionId = answerItem.getQuestionId();
        if (questionId == null) {
            throw new BusinessException("questionId 不能为空");
        }

        Question question = questionMap.get(questionId);
        if (question == null || !Objects.equals(question.getExamId(), examRecord.getExamId())) {
            throw new BusinessException("题目不存在或不属于当前考试");
        }

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setExamRecordId(recordId);
        userAnswer.setUserId(user.getId());
        userAnswer.setQuestionId(questionId);
        userAnswer.setUserAnswer(answerItem.getAnswer());

        int score = 0;
        if (QuestionTypes.isObjective(question.getQuestionType())) {
            // 考试记录提交沿用原有语义：标准答案缺失时仍参与比较
            ObjectiveGrade grade = ExamScoringSupport.gradeObjective(question, answerItem.getAnswer(), false);
            score = grade.getScore();
            userAnswer.setIsCorrect(grade.isCorrect() ? 1 : 0);
            userAnswer.setScore(score);
        }

        userAnswerMapper.insert(userAnswer);
        return score;
    }
}