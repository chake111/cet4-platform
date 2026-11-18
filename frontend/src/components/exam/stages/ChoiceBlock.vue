<script setup>
import PassageBlock from './PassageBlock.vue'
import QuestionOptionList from './QuestionOptionList.vue'

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
      <p v-if="question.content?.stem" class="question-stem">{{ question.content.stem }}</p>
      <QuestionOptionList
        :model-value="answers[question.id] || ''"
        :options="question.content?.options || []"
        @update:model-value="$emit('update-answer', question.id, $event)"
      />
    </div>
  </div>
</template>

<style scoped src="../../../styles/components/question-list.css"></style>

<style scoped>
.question-stem {
  margin: 0 0 14px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--c-text-primary);
  white-space: pre-wrap;
}

@media (max-width: 480px) {
  .question-stem {
    font-size: 13px;
    line-height: 1.6;
  }
}
</style>
