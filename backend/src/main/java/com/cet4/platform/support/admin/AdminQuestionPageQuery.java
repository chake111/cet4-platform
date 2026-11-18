package com.cet4.platform.support.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cet4.platform.entity.Exam;
import com.cet4.platform.entity.Question;
import com.cet4.platform.mapper.ExamMapper;
import com.cet4.platform.mapper.QuestionMapper;
import com.cet4.platform.vo.admin.AdminQuestionVO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 后台题目分页查询：负责分页条件、考试信息批量加载与视图组装。
 */
@Component
@RequiredArgsConstructor
public class AdminQuestionPageQuery {

    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final AdminQuestionViewAssembler viewAssembler;

    public Map<String, Object> page(Integer current, Integer size, String type, Integer year, Integer month) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Question::getId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Question::getQuestionType, type);
        }
        if (year != null || month != null) {
            List<Long> examIds = listExamIds(year, month);
            if (examIds.isEmpty()) {
                return emptyPage(current, size);
            }
            wrapper.in(Question::getExamId, examIds);
        }

        IPage<Question> resultPage = questionMapper.selectPage(new Page<>(current, size), wrapper);
        Map<Long, Exam> examsById = listExamsById(resultPage.getRecords());
        List<AdminQuestionVO> records = resultPage.getRecords().stream()
                .map(question -> viewAssembler.toVO(question, examsById.get(question.getExamId())))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", resultPage.getCurrent());
        result.put("size", resultPage.getSize());
        result.put("total", resultPage.getTotal());
        result.put("records", records);
        return result;
    }

    private List<Long> listExamIds(Integer year, Integer month) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(Exam::getYear, year);
        }
        if (month != null) {
            wrapper.eq(Exam::getMonth, month);
        }
        return examMapper.selectList(wrapper).stream().map(Exam::getId).toList();
    }

    private Map<Long, Exam> listExamsById(List<Question> questions) {
        List<Long> examIds = questions.stream()
                .map(Question::getExamId)
                .distinct()
                .toList();
        if (examIds.isEmpty()) {
            return Map.of();
        }
        return examMapper.selectBatchIds(examIds).stream()
                .collect(Collectors.toMap(Exam::getId, Function.identity()));
    }

    private Map<String, Object> emptyPage(Integer current, Integer size) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", current);
        result.put("size", size);
        result.put("total", 0L);
        result.put("records", List.of());
        return result;
    }
}