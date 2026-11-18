<script setup>
import { computed } from 'vue'
import { useExamSessionStore } from '@/stores/examSession'
import { useExamAnswerStore } from '@/stores/examAnswer'
import { EXAM_TITLE, STAGE_LIST, STAGE_ORDER } from '@/constants/exam'
import { useExamTimer } from '@/composables/useExamTimer'

const sessionStore = useExamSessionStore()
const answerStore = useExamAnswerStore()

const isStageDone = (idx) => {
  const currentIdx = STAGE_ORDER.indexOf(sessionStore.currentStage)
  return idx < currentIdx
}

const isStageActive = (stageKey) => stageKey === sessionStore.currentStage

// #4: 计算当前阶段答题进度
const answeredProgress = computed(() => {
  const questions = sessionStore.currentQuestions
  const answers = answerStore.currentAnswers
  if (!questions || questions.length === 0) return { answered: 0, total: 0 }

  const answered = questions.filter((q) => {
    const answer = answers[q.id]
    return answer !== undefined && answer !== null && answer !== ''
  }).length

  return { answered, total: questions.length }
})

const { timeText, isDanger } = useExamTimer(sessionStore)
</script>

<template>
  <header class="exam-header">
    <div class="header-left">
      <span class="brand">{{ EXAM_TITLE }}</span>
    </div>

    <!-- #5: 步骤进度条 -->
    <div class="header-center">
      <div class="step-progress">
        <template v-for="(stage, idx) in STAGE_LIST" :key="stage.key">
          <div
            class="step-item"
            :class="{
              active: isStageActive(stage.key),
              done: isStageDone(idx),
            }"
          >
            <div class="step-circle">
              <span v-if="isStageDone(idx)" class="step-check">✓</span>
              <span v-else class="step-number">{{ idx + 1 }}</span>
            </div>
            <span class="step-label">{{ stage.label }}</span>
          </div>
          <div
            v-if="idx < STAGE_LIST.length - 1"
            class="step-connector"
            :class="{ done: isStageDone(idx) }"
          ></div>
        </template>
      </div>
    </div>

    <div class="header-right">
      <!-- #4: 答题进度 -->
      <span v-if="answeredProgress.total > 0" class="progress-badge">
        已答 {{ answeredProgress.answered }}/{{ answeredProgress.total }}
      </span>

      <!-- #11: 倒计时 + 危险提示 -->
      <span class="timer" :class="{ danger: isDanger }">{{ timeText }}</span>
      <span v-if="isDanger" class="danger-warning">⚠ 剩余不足5分钟</span>
    </div>
  </header>
</template>

<style scoped src="../../styles/components/exam-header.css"></style>
