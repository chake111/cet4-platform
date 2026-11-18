<script setup>
import PassageBlock from './PassageBlock.vue'

defineProps({
  questions: {
    type: Array,
    default: () => [],
  },
  answers: {
    type: Object,
    default: () => ({}),
  },
  passage: {
    type: String,
    default: '',
  },
})

defineEmits(['update-answer'])
</script>

<template>
  <PassageBlock v-if="passage" :passage="passage" />
  <div class="question-list">
    <div v-for="question in questions" :key="question.id" class="question-block">
      <div class="question-no">Q{{ question.questionNo }}</div>
      <div class="matching-select-row">
        <span class="matching-label">匹配段落：</span>
        <el-input
          :model-value="answers[question.id] || ''"
          placeholder="输入段落字母（如 A、B、C...）"
          clearable
          @update:model-value="$emit('update-answer', question.id, $event)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped src="../../../styles/components/question-list.css"></style>

<style scoped>
.matching-select-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.matching-label {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--c-text-secondary);
  white-space: nowrap;
}

.matching-select-row .el-input {
  max-width: 240px;
}

@media (max-width: 480px) {
  .matching-select-row {
    flex-wrap: wrap;
  }

  .matching-select-row .el-input {
    max-width: 100%;
  }
}
</style>
