<script setup>
import SessionReviewCard from './SessionReviewCard.vue'
import SubjectiveReviewItem from './SubjectiveReviewItem.vue'

defineProps({
  groups: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <div class="detail-list">
    <section v-for="group in groups" :key="group.stage" class="detail-section">
      <h3 class="detail-section-title">{{ group.label }}</h3>

      <SessionReviewCard
        v-for="session in group.sessions"
        :key="session.sessionId"
        :session="session"
      />

      <div v-if="group.subjectiveQuestions.length > 0" class="subjective-list">
        <SubjectiveReviewItem
          v-for="question in group.subjectiveQuestions"
          :key="question.questionId"
          :question="question"
        />
      </div>
    </section>
  </div>
</template>

<style scoped src="../../../styles/components/answer-review-section.css"></style>
