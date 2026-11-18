<script setup>
import { normalizeAnswer, UNANSWERED_TEXT, getScoreText } from '@/domain/exam'

defineProps({
  session: {
    type: Object,
    default: () => ({}),
  },
})
</script>

<template>
  <div class="session-card">
    <div v-if="session.sessionTitle" class="session-header">
      <span class="session-title">{{ session.sessionTitle }}</span>
      <span class="session-meta">{{ session.questions.length }} 题</span>
    </div>

    <div class="session-question-list">
      <div class="session-question-header">
        <span class="sq-col sq-col-no">题号</span>
        <span class="sq-col sq-col-my">我的答案</span>
        <span class="sq-col sq-col-correct">正确答案</span>
        <span class="sq-col sq-col-score">得分</span>
        <span class="sq-col sq-col-status">状态</span>
      </div>

      <div
        v-for="question in session.questions"
        :key="question.questionId"
        class="session-question-row"
        :class="{
          'row-wrong': question.correct === false,
          'row-unanswered': normalizeAnswer(question.userAnswer) === UNANSWERED_TEXT,
        }"
      >
        <span class="sq-col sq-col-no">Q{{ question.questionNo }}</span>
        <span
          class="sq-col sq-col-my"
          :class="{
            'text-danger': question.correct === false,
            'text-tertiary': normalizeAnswer(question.userAnswer) === UNANSWERED_TEXT,
          }"
        >
          {{ normalizeAnswer(question.userAnswer) }}
        </span>
        <span class="sq-col sq-col-correct">{{ normalizeAnswer(question.correctAnswer) }}</span>
        <span class="sq-col sq-col-score">{{ getScoreText(question) }}</span>
        <span class="sq-col sq-col-status">
          <span v-if="question.correct === true" class="status-tag tag-correct">正确</span>
          <span v-else-if="question.correct === false" class="status-tag tag-wrong">错误</span>
          <span v-else class="status-tag tag-unanswered">未作答</span>
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped src="../../../styles/components/session-review-card.css"></style>
