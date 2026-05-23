<template>
  <div class="assessment-result-page">
    <div v-if="loading" class="state-panel">
      <i class="el-icon-loading"></i>
      <span>正在加载测评结果...</span>
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
      <section class="result-header">
        <div>
          <h1>测评结果</h1>
          <p>{{ assessmentTitle }}</p>
        </div>
        <div class="submit-time">提交时间：{{ formatTime(record.submitTime) }}</div>
      </section>

      <section class="overview-panel">
        <div class="score-block">
          <span class="score-number">{{ displayScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="level-block">
          <span class="level-label">结果等级</span>
          <strong :class="'level-' + normalizedLevel">{{ levelText }}</strong>
          <p>{{ levelDescription }}</p>
        </div>
      </section>

      <el-row :gutter="18">
        <el-col :xs="24" :md="10">
          <el-card shadow="never" class="info-card">
            <div slot="header" class="card-title">结果信息</div>
            <div class="info-row">
              <span>测评名称</span>
              <strong>{{ assessmentTitle }}</strong>
            </div>
            <div class="info-row">
              <span>测评分数</span>
              <strong>{{ displayScore }} 分</strong>
            </div>
            <div class="info-row">
              <span>结果等级</span>
              <strong>{{ levelText }}</strong>
            </div>
            <div class="info-row">
              <span>记录编号</span>
              <strong>#{{ record.id }}</strong>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="14">
          <el-card shadow="never" class="info-card">
            <div slot="header" class="card-title">建议</div>
            <p class="suggestion-text">{{ normalizedSuggestion }}</p>
            <el-alert
              title="本结果仅供自我了解和学习参考，不作为医学诊断。若困扰持续或影响学习生活，建议向辅导员、校心理中心或专业人员寻求帮助。"
              type="info"
              :closable="false"
              show-icon>
            </el-alert>
          </el-card>
        </el-col>
      </el-row>

      <div class="action-row">
        <el-button @click="goList">返回测评列表</el-button>
        <el-button type="primary" @click="retakeAssessment">重新测评</el-button>
      </div>
    </template>
  </div>
</template>

<script>
<<<<<<< HEAD
import { getAssessmentDetail, getAssessmentRecord } from '@/api/assessment'
=======
import { getAssessmentRecord } from '@/api/assessment'

const LEVEL_MAP = {
  normal: { icon: '😊', type: 'success', text: '正常' },
  mild: { icon: '🙂', type: 'warning', text: '轻度困扰' },
  moderate: { icon: '😐', type: 'warning', text: '中度困扰' },
  severe: { icon: '😔', type: 'danger', text: '重度困扰' }
}
>>>>>>> feature-risk-admin

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
<<<<<<< HEAD
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
    levelText() {
      const map = {
        normal: '状态平稳',
        mild: '轻度关注',
        moderate: '需要关注',
        severe: '重点关注'
      }
      return map[this.normalizedLevel] || '未分级'
    },
    levelDescription() {
      const map = {
        normal: '当前结果整体处于较平稳范围。建议继续保持规律作息，并持续关注情绪变化。',
        mild: '结果提示近期可能有一定压力或情绪波动。建议调整节奏，留意睡眠、学习负荷和人际支持。',
        moderate: '结果提示当前困扰较明显。建议主动与老师、家人或校心理中心沟通，必要时预约咨询。',
        severe: '结果提示需要重点关注当前状态。建议尽快联系校心理中心或专业人员获得支持。'
      }
      return map[this.normalizedLevel] || '请结合建议内容和近期实际情况进行参考。'
    },
    normalizedSuggestion() {
      return this.record.suggestion || '建议保持规律作息、适当运动，持续记录情绪变化；如困扰持续，请及时寻求老师或专业人员帮助。'
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
        this.error = '缺少测评结果编号。'
        return
      }

      this.loading = true
      this.error = ''
      getAssessmentRecord(id).then(res => {
        this.record = res.data || null
        if (this.record && this.record.assessmentId) {
          return getAssessmentDetail(this.record.assessmentId).then(detailRes => {
            this.assessment = detailRes.data || null
          }).catch(() => {
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
=======
    levelInfo() {
      return LEVEL_MAP[this.record.level] || LEVEL_MAP.normal
    },
    levelIcon() { return this.levelInfo.icon },
    levelType() { return this.levelInfo.type },
    levelText() { return this.levelInfo.text }
  },
  created() {
    const id = this.$route.params.id
    if (id) {
      getAssessmentRecord(id).then(res => {
        if (res.data) this.record = res.data
      }).catch(() => {
        this.$message.error('获取评估结果失败')
      })
>>>>>>> feature-risk-admin
    }
  }
}
</script>

<style scoped>
.assessment-result-page {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 24px 44px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}

.result-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 26px;
}

.result-header p,
.submit-time {
  margin: 8px 0 0;
  color: #6f7d8f;
  font-size: 14px;
}

.overview-panel {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 22px;
  align-items: center;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  padding: 24px;
  margin-bottom: 18px;
}

.score-block {
  border-right: 1px solid #edf1f7;
  text-align: center;
}

.score-number {
  color: #2c3e50;
  font-size: 54px;
  line-height: 1;
  font-weight: 800;
}

.score-unit {
  color: #7a8798;
  margin-left: 4px;
}

.level-label {
  display: block;
  color: #7a8798;
  font-size: 13px;
  margin-bottom: 6px;
}

.level-block strong {
  display: block;
  font-size: 22px;
  margin-bottom: 8px;
}

.level-normal {
  color: #3d8b40;
}

.level-mild,
.level-moderate {
  color: #b7791f;
}

.level-severe {
  color: #c0392b;
}

.level-block p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}

.info-card,
.state-panel,
.result-state {
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  background: #fff;
}

.info-card {
  margin-bottom: 18px;
}

.card-title {
  color: #2c3e50;
  font-size: 15px;
  font-weight: 700;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #edf1f7;
  color: #7a8798;
  font-size: 14px;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row strong {
  color: #2c3e50;
  text-align: right;
}

.suggestion-text {
  margin: 0 0 16px;
  color: #606266;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 4px;
}

.state-panel {
  padding: 48px 20px;
  text-align: center;
  color: #7a8798;
}

.state-panel i {
  color: #409eff;
  margin-right: 8px;
}

.result-state {
  padding: 36px 20px;
}

@media (max-width: 760px) {
  .assessment-result-page {
    padding: 20px 14px 34px;
  }

  .result-header,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .overview-panel {
    grid-template-columns: 1fr;
  }

  .score-block {
    border-right: none;
    border-bottom: 1px solid #edf1f7;
    padding-bottom: 18px;
  }
}
</style>
