package com.cet4.platform.support;

import com.cet4.platform.common.BusinessException;
import com.cet4.platform.dto.AnswerItemDTO;
import com.cet4.platform.entity.ExamRecord;
import com.cet4.platform.entity.Question;
import com.cet4.platform.entity.User;
import com.cet4.platform.entity.UserAnswer;
import com.cet4.platform.mapper.QuestionMapper;
import com.cet4.platform.mapper.UserAnswerMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamRecordAnswerWriterTest {

    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private UserAnswerMapper userAnswerMapper;

    private ExamRecordAnswerWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ExamRecordAnswerWriter(questionMapper, userAnswerMapper);
    }

    @Test
    void savesObjectiveAnswerWithNormalizedScoreAndCorrectness() {
        Question question = question(10L, 2L, "single_choice", "A", 5);
        when(questionMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(question));

        int total = writer.saveRecordAnswers(55L, user(), examRecord(2L), List.of(answer(10L, " a ")));

        assertEquals(5, total);
        UserAnswer saved = captureAnswer();
        assertEquals(55L, saved.getExamRecordId());
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getQuestionId());
        assertEquals(" a ", saved.getUserAnswer());
        assertEquals(1, saved.getIsCorrect());
        assertEquals(5, saved.getScore());
    }

    @Test
    void marksWrongObjectiveAnswerWithZeroScore() {
        Question question = question(10L, 2L, "single_choice", "A", 5);
        when(questionMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(question));

        int total = writer.saveRecordAnswers(55L, user(), examRecord(2L), List.of(answer(10L, "B")));

        assertEquals(0, total);
        UserAnswer saved = captureAnswer();
        assertEquals(0, saved.getIsCorrect());
        assertEquals(0, saved.getScore());
    }

    @Test
    void subjectiveAnswerIsStoredWithoutObjectiveScore() {
        Question question = question(20L, 2L, "writing", null, 15);
        when(questionMapper.selectBatchIds(List.of(20L))).thenReturn(List.of(question));

        int total = writer.saveRecordAnswers(55L, user(), examRecord(2L), List.of(answer(20L, "essay")));

        assertEquals(0, total);
        UserAnswer saved = captureAnswer();
        assertNull(saved.getIsCorrect());
        assertNull(saved.getScore());
    }

    @Test
    void rejectsMissingQuestionId() {
        assertThrows(BusinessException.class,
                () -> writer.saveRecordAnswers(55L, user(), examRecord(2L), List.of(answer(null, "A"))));

        verify(userAnswerMapper, never()).insert(any(UserAnswer.class));
    }

    @Test
    void rejectsQuestionFromAnotherExam() {
        Question question = question(10L, 3L, "single_choice", "A", 5);
        when(questionMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(question));

        assertThrows(BusinessException.class,
                () -> writer.saveRecordAnswers(55L, user(), examRecord(2L), List.of(answer(10L, "A"))));

        verify(userAnswerMapper, never()).insert(any(UserAnswer.class));
    }

    private UserAnswer captureAnswer() {
        ArgumentCaptor<UserAnswer> captor = ArgumentCaptor.forClass(UserAnswer.class);
        verify(userAnswerMapper).insert(captor.capture());
        return captor.getValue();
    }

    private Question question(Long id, Long examId, String type, String correctAnswer, Integer score) {
        Question question = new Question();
        question.setId(id);
        question.setExamId(examId);
        question.setQuestionType(type);
        question.setCorrectAnswer(correctAnswer);
        question.setScore(score);
        return question;
    }

    private AnswerItemDTO answer(Long questionId, String answer) {
        AnswerItemDTO item = new AnswerItemDTO();
        item.setQuestionId(questionId);
        item.setAnswer(answer);
        return item;
    }

    private ExamRecord examRecord(Long examId) {
        ExamRecord examRecord = new ExamRecord();
        examRecord.setExamId(examId);
        return examRecord;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        return user;
    }
}