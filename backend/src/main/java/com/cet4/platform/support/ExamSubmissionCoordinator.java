package com.cet4.platform.support;

import com.cet4.platform.common.BusinessException;
import com.cet4.platform.domain.ExamStatus;
import com.cet4.platform.dto.AnswerItemDTO;
import com.cet4.platform.dto.ExamSubmitRequest;
import com.cet4.platform.dto.SubmitAnswerDTO;
import com.cet4.platform.entity.ExamRecord;
import com.cet4.platform.entity.Question;
import com.cet4.platform.entity.User;
import com.cet4.platform.mapper.ExamRecordMapper;
import com.cet4.platform.support.ExamScoringSupport.ScoringResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考试提交编排器：负责整卷提交与考试记录提交的流程编排，
 * 具体的提交锁、判分与持久化分别委托给协作组件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmissionCoordinator {

    private final ExamPaperResolver examPaperResolver;
    private final ExamRecordAccessSupport examRecordAccessSupport;
    private final ExamQuerySupport examQuerySupport;
    private final ExamCacheStore examCacheStore;
    private final ExamSubmitLock examSubmitLock;
    private final ExamScoringSupport examScoringSupport;
    private final ExamSubmissionWriter examSubmissionWriter;
    private final ExamRecordAnswerWriter examRecordAnswerWriter;
    private final ExamRecordMapper examRecordMapper;

    public Map<String, Object> submitExamSession(ExamSubmitRequest request, User user) {
        Long examId = examPaperResolver.resolveExamIdByPaperId(request.getPaperId());
        String submitLockToken = examSubmitLock.acquire(user.getId(), examId);

        boolean success = false;
        try {
            List<Question> questions = examQuerySupport.listQuestions(examId);
            if (questions.isEmpty()) {
                throw new BusinessException("试卷未配置题目");
            }

            Map<String, String> answers = request.getAnswers() == null ? Map.of() : request.getAnswers();
            ScoringResult scoringResult = examScoringSupport.score(answers, questions);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = readStartedAt(user, now);
            ExamRecord examRecord = examSubmissionWriter.saveSubmittedSession(
                    user,
                    examId,
                    request.getPaperId(),
                    startTime,
                    now,
                    answers,
                    questions,
                    scoringResult);

            examCacheStore.clearSession(user.getId(), request.getPaperId());
            success = true;
            log.info("考试提交成功: userId={}, examId={}, recordId={}, score={}, total={}",
                    user.getId(), examId, examRecord.getId(), scoringResult.getScore(), scoringResult.getTotal());

            Map<String, Object> result = new HashMap<>();
            result.put("recordId", examRecord.getId());
            result.put("score", scoringResult.getScore());
            result.put("total", scoringResult.getTotal());
            return result;
        } finally {
            if (!success) {
                examSubmitLock.release(user.getId(), examId, submitLockToken);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitExamRecord(Long recordId, User user, SubmitAnswerDTO submitAnswerDTO) {
        ExamRecord examRecord = examRecordAccessSupport.getAndValidateExamRecord(recordId, user.getId());
        String submitLockToken = examSubmitLock.acquire(user.getId(), examRecord.getExamId());

        boolean success = false;
        try {
            if (!ExamStatus.IN_PROGRESS.equals(examRecord.getStatus())) {
                throw new BusinessException("考试记录状态不是 in_progress，无法提交");
            }

            List<AnswerItemDTO> answerItems = submitAnswerDTO == null || submitAnswerDTO.getAnswers() == null
                    ? List.of()
                    : submitAnswerDTO.getAnswers();

            int totalObjectiveScore = examRecordAnswerWriter.saveRecordAnswers(recordId, user, examRecord, answerItems);
            examRecord.setTotalScore(totalObjectiveScore);
            examRecord.setStatus(ExamStatus.SUBMITTED);
            examRecord.setSubmitTime(LocalDateTime.now());
            examRecordMapper.updateById(examRecord);

            success = true;
            Map<String, Object> result = new HashMap<>();
            result.put("totalScore", totalObjectiveScore);
            result.put("examRecordId", recordId);
            return result;
        } finally {
            if (!success) {
                examSubmitLock.release(user.getId(), examRecord.getExamId(), submitLockToken);
            }
        }
    }

    private LocalDateTime readStartedAt(User user, LocalDateTime fallback) {
        try {
            return examCacheStore.readStartedAt(user.getId(), fallback);
        } catch (Exception e) {
            log.warn("解析 Redis session startedAt 失败，使用当前时间作为 startTime", e);
            return fallback;
        }
    }
}