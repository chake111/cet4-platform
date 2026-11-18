<script setup>
defineProps({
  isPlaying: Boolean,
  hasPlayed: Boolean,
  autoPlayFailed: Boolean,
})

defineEmits(['retry'])
</script>

<template>
  <div
    class="audio-status-banner"
    :class="{
      'status-playing': isPlaying,
      'status-ended': hasPlayed,
      'status-failed': autoPlayFailed && !isPlaying && !hasPlayed,
    }"
  >
    <div class="banner-main">
      <span class="banner-icon">
        <template v-if="isPlaying">🔊</template>
        <template v-else-if="hasPlayed">✓</template>
        <template v-else-if="autoPlayFailed">⚠</template>
        <template v-else>🔊</template>
      </span>
      <span class="banner-text">
        <template v-if="isPlaying">音频播放中...</template>
        <template v-else-if="hasPlayed">音频已播放完毕</template>
        <template v-else-if="autoPlayFailed">音频无法自动播放，请点击播放按钮</template>
        <template v-else>准备播放音频...</template>
      </span>
      <span class="banner-once-rule">音频仅可播放一次</span>
    </div>
    <button
      v-if="autoPlayFailed && !isPlaying && !hasPlayed"
      class="banner-play-btn"
      @click="$emit('retry')"
    >
      播放
    </button>
  </div>
</template>

<style scoped src="../../../styles/components/audio-status-banner.css"></style>
