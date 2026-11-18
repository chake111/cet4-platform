<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import BackTopButton from '@/components/layout/BackTopButton.vue'
import ExamRecordTable from '@/components/exam/records/ExamRecordTable.vue'
import { examService } from '@/services/examService'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const records = ref([])

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await examService.getExamRecords()
    records.value = Array.isArray(res.data) ? res.data : []
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

const viewReport = (recordId) => {
  router.push(`/exam/record/${recordId}/result`)
}

const handleLogout = async () => {
  userStore.logout()
  await router.push('/login')
}

onMounted(() => {
  fetchRecords()
})
</script>

<template>
  <div v-loading="loading" class="exam-record-list">
    <header class="page-header">
      <div class="header-left">
        <span class="back-link" @click="$router.push('/exam')"
          ><el-icon><ArrowLeft /></el-icon> 返回</span
        >
        <div class="header-titles">
          <h1 class="page-title">考试记录</h1>
          <p class="page-subtitle">查看已完成的 CET-4 模拟考试</p>
        </div>
      </div>
      <span class="logout-link" @click="handleLogout">退出登录</span>
    </header>

    <el-empty
      v-if="!loading && records.length === 0"
      description="暂无考试记录"
      class="empty-state"
    />

    <ExamRecordTable v-else :records="records" @view-report="viewReport" />

    <!-- 返回顶部 -->
    <BackTopButton />
  </div>
</template>

<style scoped src="../../styles/views/exam-record-list.css"></style>
