<script setup>
import { computed } from 'vue'
import { EXAM_TOTAL_SCORE, STAGE_LIST } from '@/constants/exam'

defineProps({
  examTitle: {
    type: String,
    default: '',
  },
  examInfo: {
    type: Object,
    default: () => null,
  },
})

const totalDuration = computed(() => STAGE_LIST.reduce((sum, s) => sum + s.duration, 0))
</script>

<template>
  <div class="info-section">
    <div class="info-row">
      <span class="info-label">考试名称</span>
      <span class="info-value">{{ examTitle }}</span>
    </div>
    <div class="info-row">
      <span class="info-label">总时长</span>
      <span class="info-value">{{ totalDuration }} 分钟</span>
    </div>
    <div v-if="examInfo" class="info-row">
      <span class="info-label">总分</span>
      <span class="info-value">{{ examInfo.totalScore || EXAM_TOTAL_SCORE }}</span>
    </div>
  </div>
</template>

<style scoped src="../../../styles/views/exam-brief-info.css"></style>
