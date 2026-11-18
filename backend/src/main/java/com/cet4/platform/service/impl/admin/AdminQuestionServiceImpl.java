package com.cet4.platform.service.impl.admin;

import com.cet4.platform.common.BusinessException;
import com.cet4.platform.dto.admin.AdminQuestionUpsertRequest;
import com.cet4.platform.entity.Exam;
import com.cet4.platform.entity.Question;
import com.cet4.platform.mapper.ExamMapper;
import com.cet4.platform.mapper.QuestionMapper;
import com.cet4.platform.service.admin.AdminQuestionService;
import com.cet4.platform.support.admin.AdminQuestionContentSupport;
import com.cet4.platform.support.admin.AdminQuestionContentSupport.ContentParsedResult;
import com.cet4.platform.support.admin.AdminQuestionPageQuery;
import com.cet4.platform.support.admin.AdminQuestionViewAssembler;
import com.cet4.platform.vo.admin.AdminQuestionVO;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminQuestionServiceImpl implements AdminQuestionService {

    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final AdminQuestionContentSupport contentSupport;
    private final AdminQuestionViewAssembler viewAssembler;
    private final AdminQuestionPageQuery pageQuery;

    @Override
    public AdminQuestionVO create(AdminQuestionUpsertRequest request) {
        Exam exam = getExam(request.getExamId());
        Question question = buildEntity(null, request);
        questionMapper.insert(question);
        return viewAssembler.toVO(question, exam);
    }

    @Override
    public AdminQuestionVO update(Long id, AdminQuestionUpsertRequest request) {
        Question existing = getById(id);
        Exam exam = getExam(request.getExamId());
        Question question = buildEntity(id, request);
        question.setCreatedAt(existing.getCreatedAt());
        questionMapper.updateById(question);
        return viewAssembler.toVO(question, exam);
    }

    @Override
    public void delete(Long id) {
        Question existing = getById(id);
        questionMapper.deleteById(existing.getId());
    }

    @Override
    public Map<String, Object> page(Integer current, Integer size, String type, Integer year, Integer month) {
        return pageQuery.page(current, size, type, year, month);
    }

    private Question getById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    private Exam getExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("exam 不存在");
        }
        return exam;
    }

    private Question buildEntity(Long id, AdminQuestionUpsertRequest request) {
        String normalizedType = request.getType().trim().toLowerCase(Locale.ROOT);
        ContentParsedResult content = contentSupport.validateAndSerializeContent(normalizedType, request.getContent());

        Question question = new Question();
        question.setId(id);
        question.setExamId(request.getExamId());
        question.setPart(request.getPart());
        question.setQuestionNo(request.getQuestionNo());
        question.setQuestionType(normalizedType);
        question.setContent(content.contentJson());
        question.setPassage(request.getPassage());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setScore(request.getScore());
        question.setAudioUrl(request.getAudioUrl());
        question.setSortOrder(request.getSortOrder());
        question.setOptionA(content.optionA());
        question.setOptionB(content.optionB());
        question.setOptionC(content.optionC());
        question.setOptionD(content.optionD());
        question.setDeleted(0);
        return question;
    }
}