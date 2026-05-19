<template>
  <div class="assessment-detail-page">
    <div v-if="loading" class="state-panel">
      <i class="el-icon-loading"></i>
      <span>正在加载测评内容...</span>
    </div>

    <el-empty
      v-else-if="!assessment"
      description="未找到测评内容"
      class="state-panel">
      <el-button @click="$router.push('/assessment')">返回列表</el-button>
    </el-empty>

    <template v-else>
      <section class="detail-header">
        <div>
          <h1>{{ assessment.title }}</h1>
          <p>{{ assessment.description }}</p>
        </div>
        <el-button plain @click="$router.push('/assessment')">返回列表</el-button>
      </section>

      <el-card shadow="never" class="question-card">
        <div class="progress-section">
          <div class="progress-meta">
            <span>答题进度</span>
            <strong>已答 {{ answeredCount }} / {{ totalQuestions }} 题</strong>
          </div>
          <el-progress :percentage="progress" :stroke-width="8"></el-progress>
        </div>

        <div class="question-body">
          <div class="question-index">第 {{ currentIndex + 1 }} 题 / 共 {{ totalQuestions }} 题</div>
          <h2>{{ currentQuestion.questionText }}</h2>

          <el-radio-group
            :value="answers[currentQuestion.id]"
            class="option-list"
            @input="setAnswer(currentQuestion.id, $event)">
            <el-radio
              v-for="option in currentOptions"
              :key="option.key"
              :label="option.score"
              class="option-item">
              <span class="option-key">{{ option.key }}</span>
              <span>{{ option.text }}</span>
            </el-radio>
          </el-radio-group>
        </div>

        <div class="action-row">
          <el-button @click="prev" :disabled="currentIndex === 0">上一题</el-button>
          <div class="question-jump">
            <button
              v-for="(q, index) in assessment.questions"
              :key="q.id"
              type="button"
              :class="{ active: index === currentIndex, answered: answers[q.id] !== undefined }"
              @click="currentIndex = index">
              {{ index + 1 }}
            </button>
          </div>
          <el-button
            v-if="currentIndex < totalQuestions - 1"
            type="primary"
            @click="next">
            下一题
          </el-button>
          <el-button
            v-else
            type="primary"
            :loading="submitting"
            @click="submit">
            提交测评
          </el-button>
        </div>
      </el-card>

      <p class="notice">测评结果仅供自我了解和学习参考，不作为医学诊断。</p>
    </template>
  </div>
</template>

<script>
import { getAssessmentDetail, submitAssessment } from '@/api/assessment'

export default {
  name: 'AssessmentDetail',
  data() {
    return {
      loading: false,
      assessment: null,
      currentIndex: 0,
      answers: {},
      submitting: false
    }
  },
  computed: {
    totalQuestions() {
      return this.assessment && this.assessment.questions ? this.assessment.questions.length : 0
    },
    currentQuestion() {
      return this.assessment.questions[this.currentIndex]
    },
    currentOptions() {
      const q = this.currentQuestion
      return [
        { key: 'A', text: q.optionA, score: q.scoreA },
        { key: 'B', text: q.optionB, score: q.scoreB },
        { key: 'C', text: q.optionC, score: q.scoreC },
        { key: 'D', text: q.optionD, score: q.scoreD }
      ]
    },
    answeredCount() {
      if (!this.assessment) return 0
      return this.assessment.questions.filter(q => this.answers[q.id] !== undefined).length
    },
    progress() {
      if (!this.totalQuestions) return 0
      return Math.round((this.answeredCount / this.totalQuestions) * 100)
    }
  },
  created() {
    this.fetchDetail()
  },
  methods: {
    fetchDetail() {
      this.loading = true
      getAssessmentDetail(this.$route.params.id).then(res => {
        this.assessment = res.data
      }).catch(() => {
        this.$message.error('测评内容加载失败，请稍后重试。')
      }).finally(() => {
        this.loading = false
      })
    },
    next() {
      if (this.answers[this.currentQuestion.id] === undefined) {
        this.$message.warning('请先完成当前题目。')
        return
      }
      this.currentIndex++
    },
    prev() {
      if (this.currentIndex > 0) this.currentIndex--
    },
    setAnswer(questionId, value) {
      this.$set(this.answers, questionId, value)
    },
    submit() {
      const unansweredIndex = this.assessment.questions.findIndex(q => this.answers[q.id] === undefined)
      if (unansweredIndex !== -1) {
        this.currentIndex = unansweredIndex
        this.$message.warning(`还有第 ${unansweredIndex + 1} 题未完成，请完成后再提交。`)
        return
      }

      this.submitting = true
      submitAssessment({
        assessmentId: this.assessment.id,
        answers: this.answers
      }).then(res => {
        this.$message.success('测评已提交。')
        this.$router.push('/assessment/result/' + res.data.id)
      }).catch(() => {
        this.$message.error('提交失败，请稍后重试。')
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>

<style scoped>
.assessment-detail-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 28px 24px 44px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}

.detail-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  line-height: 1.4;
}

.detail-header p {
  margin: 8px 0 0;
  color: #6f7d8f;
  font-size: 14px;
  line-height: 1.7;
}

.question-card,
.state-panel {
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  background: #fff;
}

.question-card ::v-deep .el-card__body {
  padding: 24px;
}

.progress-section {
  padding-bottom: 20px;
  border-bottom: 1px solid #edf1f7;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.progress-meta strong {
  color: #2c3e50;
}

.question-body {
  padding: 28px 0 18px;
}

.question-index {
  color: #7a8798;
  font-size: 13px;
  margin-bottom: 10px;
}

.question-body h2 {
  margin: 0 0 22px;
  color: #2c3e50;
  font-size: 20px;
  line-height: 1.6;
}

.option-list {
  width: 100%;
}

.option-item {
  display: flex !important;
  align-items: center;
  width: 100%;
  min-height: 48px;
  margin: 0 0 10px !important;
  padding: 12px 14px;
  border: 1px solid #dfe7f1;
  border-radius: 4px;
  background: #fff;
}

.option-item.is-checked {
  border-color: #409eff;
  background: #f6faff;
}

.option-key {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin-right: 10px;
  border: 1px solid #dfe7f1;
  color: #606266;
  font-size: 12px;
}

.action-row {
  display: grid;
  grid-template-columns: 96px 1fr 112px;
  align-items: center;
  gap: 16px;
  padding-top: 18px;
  border-top: 1px solid #edf1f7;
}

.question-jump {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
}

.question-jump button {
  width: 28px;
  height: 28px;
  border: 1px solid #dfe7f1;
  background: #fff;
  color: #606266;
  cursor: pointer;
}

.question-jump button.answered {
  border-color: #8bbcf3;
  color: #409eff;
}

.question-jump button.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.notice {
  margin: 14px 0 0;
  color: #909399;
  font-size: 13px;
}

.state-panel {
  padding: 48px 20px;
  text-align: center;
  color: #7a8798;
}

.state-panel i {
  margin-right: 8px;
  color: #409eff;
}

@media (max-width: 760px) {
  .assessment-detail-page {
    padding: 20px 14px 34px;
  }

  .detail-header {
    flex-direction: column;
  }

  .action-row {
    grid-template-columns: 1fr;
  }
}
</style>
