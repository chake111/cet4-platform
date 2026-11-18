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
  wordBank: {
    type: Array,
    default: () => [],
  },
  passage: {
    type: String,
    default: '',
  },
})

defineEmits(['update-answer'])

/** 将文章中的 {26} 占位符替换为带编号的空格标记 */
const renderBlankArticle = (article) => {
  if (!article) return ''
  return article.replace(/\{(\d+)\}/g, '____($1)')
}
</script>

<template>
  <!-- 选词填空词库：sticky 定位 -->
  <div v-if="wordBank.length" class="word-bank">
    <span class="word-bank-label">词库：</span>
    <span v-for="(word, wi) in wordBank" :key="wi" class="word-chip">
      {{ word }}
    </span>
  </div>
  <PassageBlock v-if="passage" :passage="renderBlankArticle(passage)" />
  <div class="question-list">
    <div v-for="question in questions" :key="question.id" class="question-block">
      <div class="question-no">Q{{ question.questionNo }}</div>
      <div class="blank-input-row">
        <span class="blank-label">第 {{ question.questionNo }} 空：</span>
        <el-input
          :model-value="answers[question.id] || ''"
          placeholder="输入所选单词"
          clearable
          @update:model-value="$emit('update-answer', question.id, $event)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped src="../../../styles/components/question-list.css"></style>

<style scoped>
.word-bank {
  position: sticky;
  top: 64px;
  z-index: 10;
  margin-bottom: 12px;
  padding: 10px 16px;
  border-radius: var(--r-input);
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-height: 160px;
  overflow-y: auto;
}

.word-bank-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-accent);
}

.word-chip {
  display: inline-block;
  margin: 4px 6px 4px 0;
  padding: 2px 10px;
  border-radius: var(--r-button);
  font-size: 13px;
  color: var(--c-text-primary);
  transition: background 0.15s;
}

.word-chip:hover {
  background: var(--c-bg-hover);
}

.blank-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.blank-label {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--c-text-secondary);
  white-space: nowrap;
}

.blank-input-row .el-input {
  max-width: 240px;
}

@media (max-width: 720px) {
  .word-bank {
    top: 60px;
  }
}

@media (max-width: 480px) {
  .word-bank {
    top: 48px;
    padding: 8px 12px;
    max-height: 120px;
  }

  .word-chip {
    font-size: 12px;
    padding: 2px 8px;
  }

  .blank-input-row {
    flex-wrap: wrap;
  }

  .blank-input-row .el-input {
    max-width: 100%;
  }
}
</style>
