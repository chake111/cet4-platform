package com.cet4.platform.support;

import com.cet4.platform.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 考试提交锁：基于 Redis 的提交互斥，防止同一用户对同一考试重复提交。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmitLock {

    private final ExamCacheStore examCacheStore;

    /**
     * 获取提交锁；获取失败说明存在重复提交，抛出业务异常。
     *
     * @return 用于失败释放的锁令牌
     */
    public String acquire(Long userId, Long examId) {
        String token = examCacheStore.acquireSubmitLock(userId, examId);
        if (token == null) {
            log.warn("重复提交被拦截: userId={}, examId={}", userId, examId);
            throw new BusinessException("请勿重复提交，请稍后再试");
        }
        return token;
    }

    /**
     * 提交失败时释放锁；提交成功则由 TTL 自然过期，保持原有语义。
     */
    public void release(Long userId, Long examId, String token) {
        examCacheStore.releaseSubmitLock(userId, examId, token);
    }
}