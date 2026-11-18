package com.cet4.platform.support;

import com.cet4.platform.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamSubmitLockTest {

    @Mock
    private ExamCacheStore examCacheStore;

    private ExamSubmitLock submitLock;

    @BeforeEach
    void setUp() {
        submitLock = new ExamSubmitLock(examCacheStore);
    }

    @Test
    void acquireReturnsRedisToken() {
        when(examCacheStore.acquireSubmitLock(1L, 2L)).thenReturn("lock-token");

        assertEquals("lock-token", submitLock.acquire(1L, 2L));
    }

    @Test
    void acquireThrowsWhenLockAlreadyHeld() {
        when(examCacheStore.acquireSubmitLock(1L, 2L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> submitLock.acquire(1L, 2L));
    }

    @Test
    void releaseDelegatesToCacheStore() {
        submitLock.release(1L, 2L, "lock-token");

        verify(examCacheStore).releaseSubmitLock(1L, 2L, "lock-token");
    }
}