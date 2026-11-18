<script setup>
import { formatDateTime, formatDurationText } from '@/utils/date'

defineProps({
  records: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['view-report'])
</script>

<template>
  <el-table :data="records" class="record-table">
    <el-table-column label="试卷" min-width="240">
      <template #default="{ row }">
        <div class="record-title">{{ row.title }}</div>
        <div class="record-time">{{ formatDateTime(row.submitTime) }}</div>
      </template>
    </el-table-column>
    <el-table-column label="得分" width="140">
      <template #default="{ row }">
        <span class="score-value">{{ row.totalScore }}</span>
        <span class="score-full">/ {{ row.fullScore }}</span>
      </template>
    </el-table-column>
    <el-table-column label="用时" width="120">
      <template #default="{ row }">
        {{ formatDurationText(row.startTime, row.submitTime) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="120" fixed="right">
      <template #default="{ row }">
        <span class="report-btn" @click="$emit('view-report', row.recordId)">查看报告</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped src="../../../styles/components/exam-record-table.css"></style>
