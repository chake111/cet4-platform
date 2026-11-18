<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EXAM_TITLE } from '@/constants/exam'
import PageTopBar from '@/components/layout/PageTopBar.vue'
import ExamBriefInfo from '@/components/exam/brief/ExamBriefInfo.vue'
import ExamBriefFlow from '@/components/exam/brief/ExamBriefFlow.vue'
import ExamBriefRules from '@/components/exam/brief/ExamBriefRules.vue'
import { examService } from '@/services/examService'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const examInfo = ref(null)

const examTitle = computed(() => {
  if (!examInfo.value) return ''
  const e = examInfo.value
  if (e.year && e.month && e.setNo) {
    return `${e.year} 年 ${e.month} 月 · 第 ${e.setNo} 套`
  }
  return e.title || ''
})

const fetchExamInfo = async () => {
  const examId = route.params.id
  if (!examId) {
    ElMessage.error('无法打开试卷，请返回列表重新选择')
    return
  }

  loading.value = true
  try {
    const res = await examService.getExamList()
    const list = Array.isArray(res.data) ? res.data : []
    examInfo.value = list.find((exam) => String(exam.id) === String(examId)) || null
  } catch {
    examInfo.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/exam')
}

const startExam = () => {
  router.push(`/exam/${route.params.id}/start`)
}

onMounted(fetchExamInfo)
</script>

<template>
  <div class="exam-brief-page">
    <PageTopBar :title="EXAM_TITLE" />

    <div class="main-area">
      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else class="brief-content">
        <h1 class="page-title">考试说明</h1>

        <!-- 信息区 -->
        <ExamBriefInfo :exam-title="examTitle" :exam-info="examInfo" />

        <div class="divider"></div>

        <!-- 考试流程区 -->
        <ExamBriefFlow />

        <div class="divider"></div>

        <!-- 注意事项区 -->
        <ExamBriefRules />

        <!-- 底部操作区 -->
        <div class="action-bar">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="startExam">开始考试</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped src="../../styles/views/exam-brief-page.css"></style>
