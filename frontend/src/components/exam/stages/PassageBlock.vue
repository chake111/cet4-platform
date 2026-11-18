<script setup>
defineProps({
  passage: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div v-if="passage" class="passage-box">
    <p class="passage-text">{{ passage }}</p>
  </div>
</template>

<style scoped src="../../../styles/components/passage-block.css"></style>
