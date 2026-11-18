<script setup>
import { STAGE_LIST } from '@/constants/exam'
</script>

<template>
  <div class="flow-section">
    <h2 class="section-title">考试流程</h2>
    <div class="flow-steps">
      <template v-for="(stage, index) in STAGE_LIST" :key="stage.key">
        <div class="flow-step">
          <span class="step-num">{{ index + 1 }}</span>
          <div class="step-info">
            <span class="step-name">{{ stage.label }}</span>
            <span class="step-duration">{{ stage.duration }} 分钟</span>
          </div>
        </div>
        <span v-if="index < STAGE_LIST.length - 1" class="step-arrow">&rarr;</span>
      </template>
    </div>
  </div>
</template>

<style scoped src="../../../styles/views/exam-brief-flow.css"></style>
