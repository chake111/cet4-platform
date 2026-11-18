package com.cet4.platform.support.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cet4.platform.entity.Exam;
import com.cet4.platform.entity.Question;
import com.cet4.platform.mapper.ExamMapper;
import com.cet4.platform.mapper.QuestionMapper;
import com.cet4.platform.vo.admin.AdminQuestionVO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionPageQueryTest {

    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private ExamMapper examMapper;
    @Mock
    private AdminQuestionViewAssembler viewAssembler;

    private AdminQuestionPageQuery pageQuery;

    @BeforeEach
    void setUp() {
        pageQuery = new AdminQuestionPageQuery(questionMapper, examMapper, viewAssembler);
    }

    @Test
    void assemblesPageRecordsWithExamInfo() {
        Question question = new Question();
        question.setId(5L);
        question.setExamId(2L);
        Exam exam = new Exam();
        exam.setId(2L);
        AdminQuestionVO vo = new AdminQuestionVO();

        Page<Question> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of(question));
        resultPage.setTotal(1);
        when(questionMapper.selectPage(any(), any())).thenReturn(resultPage);
        when(examMapper.selectBatchIds(List.of(2L))).thenReturn(List.of(exam));
        when(viewAssembler.toVO(question, exam)).thenReturn(vo);

        Map<String, Object> result = pageQuery.page(1, 10, null, null, null);

        assertEquals(1L, result.get("current"));
        assertEquals(10L, result.get("size"));
        assertEquals(1L, result.get("total"));
        assertEquals(List.of(vo), result.get("records"));
    }

    @Test
    void returnsEmptyPageWhenNoExamMatchesYearFilter() {
        when(examMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> result = pageQuery.page(2, 20, null, 2024, null);

        assertEquals(2, result.get("current"));
        assertEquals(20, result.get("size"));
        assertEquals(0L, result.get("total"));
        assertTrue(((List<?>) result.get("records")).isEmpty());
        verifyNoInteractions(questionMapper);
    }
}