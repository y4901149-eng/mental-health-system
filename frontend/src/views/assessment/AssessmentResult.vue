<template>
  <div class="assessment-result-page">
    <div v-if="loading" class="state-card">
      <i class="el-icon-loading state-icon"></i>
      <p>正在生成测评结果报告...</p>
    </div>

    <el-result
      v-else-if="error"
      icon="warning"
      title="结果加载失败"
      :sub-title="error"
      class="result-state">
      <template slot="extra">
        <el-button type="primary" @click="fetchRecord">重新加载</el-button>
        <el-button @click="goList">返回测评列表</el-button>
      </template>
    </el-result>

    <el-empty
      v-else-if="!record"
      description="暂未找到测评结果"
      class="result-state">
      <el-button type="primary" @click="goList">返回测评列表</el-button>
    </el-empty>

    <template v-else>
      <section class="overview-card">
        <div class="overview-left">
          <div class="level-icon" :class="'level-' + normalizedLevel">
            <i :class="levelIcon"></i>
          </div>
          <div>
            <p class="eyebrow">心理测评结果报告</p>
            <h1>{{ assessmentTitle }}</h1>
            <p class="submit-time">提交时间：{{ formatTime(record.submitTime) }}</p>
          </div>
        </div>
        <div class="score-panel">
          <span class="score-number">{{ displayScore }}</span>
          <span class="score-unit">分</span>
          <el-tag :type="levelType" effect="dark" class="level-tag">{{ levelText }}</el-tag>
        </div>
      </section>

      <el-row :gutter="18" class="content-row">
        <el-col :xs="24" :sm="24" :md="10">
          <el-card shadow="never" class="report-card">
            <div slot="header" class="card-title">
              <i class="el-icon-data-analysis"></i>
              <span>结果解释</span>
            </div>
            <div class="level-summary">
              <div class="level-name">{{ levelText }}</div>
              <p>{{ levelDescription }}</p>
            </div>
            <div class="metric-list">
              <div class="metric-item">
                <span>测评分数</span>
                <strong>{{ displayScore }} 分</strong>
              </div>
              <div class="metric-item">
                <span>结果等级</span>
                <strong>{{ levelText }}</strong>
              </div>
              <div class="metric-item">
                <span>测评编号</span>
                <strong>#{{ record.id }}</strong>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="14">
          <el-card shadow="never" class="report-card advice-card">
            <div slot="header" class="card-title">
              <i class="el-icon-guide"></i>
              <span>个性化建议</span>
            </div>
            <p class="advice-text">{{ record.suggestion || fallbackSuggestion }}</p>
            <el-alert
              title="测评结果仅供自我了解和情绪管理参考，不能替代专业诊断。若持续感到痛苦或出现安全风险，请及时寻求专业帮助。"
              type="info"
              :closable="false"
              show-icon>
            </el-alert>
          </el-card>
        </el-col>
      </el-row>

      <div class="action-bar">
        <el-button @click="goList">返回测评列表</el-button>
        <el-button type="primary" @click="retakeAssessment">重新测评</el-button>
      </div>
    </template>
  </div>
</template>

<script>
import { getAssessmentDetail, getAssessmentRecord } from '@/api/assessment'

export default {
  name: 'AssessmentResult',
  data() {
    return {
      loading: false,
      error: '',
      record: null,
      assessment: null
    }
  },
  computed: {
    normalizedLevel() {
      return this.record && this.record.level ? this.record.level : 'unknown'
    },
    assessmentTitle() {
      if (this.assessment && this.assessment.title) return this.assessment.title
      if (this.record && this.record.assessmentTitle) return this.record.assessmentTitle
      return '心理测评'
    },
    displayScore() {
      return this.record && this.record.totalScore != null ? this.record.totalScore : '--'
    },
    levelIcon() {
      const map = {
        normal: 'el-icon-success',
        mild: 'el-icon-warning-outline',
        moderate: 'el-icon-warning',
        severe: 'el-icon-error'
      }
      return map[this.normalizedLevel] || 'el-icon-info'
    },
    levelType() {
      const map = {
        normal: 'success',
        mild: 'warning',
        moderate: 'warning',
        severe: 'danger'
      }
      return map[this.normalizedLevel] || 'info'
    },
    levelText() {
      const map = {
        normal: '状态良好',
        mild: '轻度困扰',
        moderate: '中度困扰',
        severe: '重度困扰'
      }
      return map[this.normalizedLevel] || '未知结果'
    },
    levelDescription() {
      const map = {
        normal: '当前测评结果整体较平稳，可以继续保持规律作息、适量运动和稳定的情绪记录习惯。',
        mild: '结果提示近期可能存在轻度情绪压力，建议关注睡眠、压力来源和自我调节节奏。',
        moderate: '结果提示情绪困扰较明显，建议主动寻求家人、朋友或专业咨询支持。',
        severe: '结果提示需要高度关注当前状态，建议尽快联系专业心理咨询或医疗机构获得帮助。'
      }
      return map[this.normalizedLevel] || '暂无明确解释，请结合测评建议继续关注自己的状态。'
    },
    fallbackSuggestion() {
      return '建议持续记录情绪变化，保持规律作息，并在需要时寻求可信任的人或专业人员支持。'
    }
  },
  created() {
    this.fetchRecord()
  },
  methods: {
    fetchRecord() {
      const id = this.$route.params.id
      if (!id) {
        this.record = null
        this.error = '缺少测评结果编号'
        return
      }

      this.loading = true
      this.error = ''
      getAssessmentRecord(id).then(res => {
        this.record = res.data || null
        if (!this.record) return
        if (this.record.assessmentId) {
          return getAssessmentDetail(this.record.assessmentId)
            .then(detailRes => {
              this.assessment = detailRes.data || null
            })
            .catch(() => {
              this.assessment = null
            })
        }
      }).catch(() => {
        this.record = null
        this.error = '无法获取测评结果，请稍后重试。'
      }).finally(() => {
        this.loading = false
      })
    },
    formatTime(value) {
      if (!value) return '暂无'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const h = String(date.getHours()).padStart(2, '0')
      const min = String(date.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}`
    },
    goList() {
      this.$router.push('/assessment')
    },
    retakeAssessment() {
      if (this.record && this.record.assessmentId) {
        this.$router.push('/assessment/' + this.record.assessmentId)
      } else {
        this.goList()
      }
    }
  }
}
</script>

<style scoped>
.assessment-result-page {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 24px 42px;
}

.state-card {
  min-height: 280px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #606266;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.08);
}

.state-icon {
  font-size: 34px;
  color: #409eff;
  margin-bottom: 14px;
}

.result-state {
  background: #fff;
  border-radius: 10px;
  padding: 40px 20px;
}

.overview-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f4f9ff 100%);
  border: 1px solid #e8f0fb;
  border-radius: 12px;
  padding: 26px;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.1);
  margin-bottom: 18px;
}

.overview-left {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.level-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  flex-shrink: 0;
}

.level-normal {
  color: #67c23a;
  background: #edf8e9;
}

.level-mild,
.level-moderate {
  color: #e6a23c;
  background: #fff7e8;
}

.level-severe {
  color: #f56c6c;
  background: #fff0f0;
}

.level-unknown {
  color: #909399;
  background: #f4f4f5;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 13px;
  color: #409eff;
  font-weight: 600;
}

.overview-card h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  line-height: 1.35;
}

.submit-time {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.score-panel {
  width: 160px;
  min-height: 124px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e8f0fb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.score-number {
  color: #409eff;
  font-size: 42px;
  line-height: 1;
  font-weight: 800;
}

.score-unit {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.level-tag {
  margin-top: 12px;
}

.content-row {
  margin-bottom: 18px;
}

.report-card {
  height: 100%;
  border-radius: 10px;
  border-color: #e8f0fb;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-weight: 700;
}

.card-title i {
  color: #409eff;
}

.level-summary {
  background: #f8fbff;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
}

.level-name {
  font-size: 20px;
  color: #2c3e50;
  font-weight: 800;
  margin-bottom: 8px;
}

.level-summary p,
.advice-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}

.metric-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metric-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f5ff;
  color: #909399;
  font-size: 13px;
}

.metric-item:last-child {
  border-bottom: none;
}

.metric-item strong {
  color: #2c3e50;
  font-size: 14px;
}

.advice-card {
  background: #fff;
}

.advice-text {
  white-space: pre-wrap;
  margin-bottom: 18px;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 18px 0 0;
}

@media (max-width: 720px) {
  .assessment-result-page {
    padding: 18px 14px 32px;
  }

  .overview-card {
    flex-direction: column;
    align-items: stretch;
  }

  .overview-left {
    align-items: flex-start;
  }

  .score-panel {
    width: 100%;
  }

  .action-bar {
    flex-direction: column;
  }
}
</style>
