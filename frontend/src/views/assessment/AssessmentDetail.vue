<!-- 评估答题页面 -->
<!-- 用户逐题作答并提交 -->

<template>
  <div class="page-container" style="max-width: 700px; margin: 0 auto;">
    <el-card v-if="assessment">
      <div slot="header">
        <h2>{{ assessment.title }}</h2>
        <span style="color: #909399; font-size: 13px;">共 {{ assessment.questions.length }} 题</span>
      </div>

      <!-- 进度条 -->
      <el-progress :percentage="progress" style="margin-bottom: 20px;"></el-progress>

      <!-- 题目列表 -->
      <div v-for="(q, index) in assessment.questions" :key="q.id" v-show="index === currentIndex">
        <h3 style="margin-bottom: 20px;">
          {{ index + 1 }}. {{ q.questionText }}
        </h3>

        <el-radio-group v-model="answers[q.id]" style="display: block;">
          <el-radio :label="q.scoreA" style="display: block; margin-bottom: 15px;">
            A. {{ q.optionA }}
          </el-radio>
          <el-radio :label="q.scoreB" style="display: block; margin-bottom: 15px;">
            B. {{ q.optionB }}
          </el-radio>
          <el-radio :label="q.scoreC" style="display: block; margin-bottom: 15px;">
            C. {{ q.optionC }}
          </el-radio>
          <el-radio :label="q.scoreD" style="display: block;">
            D. {{ q.optionD }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 按钮 -->
      <div style="margin-top: 30px; display: flex; justify-content: space-between;">
        <el-button @click="prev" :disabled="currentIndex === 0">上一题</el-button>
        <span style="color: #909399;">{{ currentIndex + 1 }} / {{ assessment.questions.length }}</span>
        <el-button v-if="currentIndex < assessment.questions.length - 1" type="primary" @click="next">
          下一题
        </el-button>
        <el-button v-else type="success" @click="submit" :loading="submitting">
          提交评估
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAssessmentDetail, submitAssessment } from '@/api/assessment'

export default {
  name: 'AssessmentDetail',
  data() {
    return {
      assessment: null,
      currentIndex: 0,
      answers: {},
      submitting: false
    }
  },
  computed: {
    progress() {
      const total = this.assessment ? this.assessment.questions.length : 1
      return Math.round((this.currentIndex / total) * 100)
    }
  },
  created() {
    const id = this.$route.params.id
    getAssessmentDetail(id).then(res => {
      this.assessment = res.data
    })
  },
  methods: {
    next() {
      if (!this.answers[this.assessment.questions[this.currentIndex].id]) {
        this.$message.warning('请先选择答案')
        return
      }
      this.currentIndex++
    },
    prev() {
      if (this.currentIndex > 0) this.currentIndex--
    },
    submit() {
      // 检查是否所有题目都答了
      const unanswered = this.assessment.questions.find(q => !this.answers[q.id])
      if (unanswered) {
        this.$message.warning('请完成所有题目再提交')
        return
      }

      this.submitting = true
      submitAssessment({
        assessmentId: this.assessment.id,
        answers: this.answers
      }).then(res => {
        this.$message.success('评估完成！')
        this.$router.push('/assessment/result/' + res.data.id)
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>
