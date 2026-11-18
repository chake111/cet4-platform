<script setup>
const RULES = [
  '考试开始后不可暂停，请确保有充足的时间',
  '听力阶段音频仅播放一次，提前调整音量',
  '各阶段时间独立计时，时间到自动进入下一阶段',
  '写作和翻译答案需手动保存，建议边写边存',
  '交卷后不可修改答案',
]
</script>

<template>
  <div class="rules-section">
    <h2 class="section-title">注意事项</h2>
    <div class="rules-list">
      <div v-for="(rule, index) in RULES" :key="index" class="rule-item">
        <span class="rule-index">{{ index + 1 }}</span>
        <span class="rule-text">{{ rule }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped src="../../../styles/views/exam-brief-rules.css"></style>
