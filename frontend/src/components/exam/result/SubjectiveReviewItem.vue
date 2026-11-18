<script setup>
import { normalizeAnswer } from '@/domain/exam'
import AiFeedbackPanel from './AiFeedbackPanel.vue'

defineProps({
  question: {
    type: Object,
    default: () => ({}),
  },
})
</script>

<template>
  <div class="question-item">
    <div class="question-header">
      <span class="question-no">Q{{ question.questionNo }}</span>
    </div>

    <div class="question-row subjective-row">
      <span class="row-label">我的答案</span>
      <div class="subjective-content">{{ normalizeAnswer(question.userAnswer) }}</div>
    </div>

    <AiFeedbackPanel :feedback="question.aiFeedback" />
  </div>
</template>

<style scoped src="../../../styles/components/subjective-review-item.css"></style>
