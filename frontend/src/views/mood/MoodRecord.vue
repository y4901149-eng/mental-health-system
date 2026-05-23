<template>
  <div class="mood-record-page">
    <section class="module-header">
      <div>
        <h1>每日情绪记录</h1>
        <p>记录当天不同时间的情绪变化，用于后续趋势分析。</p>
      </div>
      <el-button plain @click="$router.push('/mood/chart')">
        <i class="el-icon-data-line"></i>
        查看分析
      </el-button>
    </section>

    <el-card shadow="never" class="status-card" v-loading="loadingToday">
      <div class="status-grid">
        <div class="status-copy">
          <span class="section-kicker">今日状态</span>
          <h2>{{ statusTitle }}</h2>
          <p>{{ statusDescription }}</p>
        </div>
        <div class="status-metrics">
          <div class="metric-item">
            <span>今日记录</span>
            <strong>{{ todayRecordCount }} 次</strong>
          </div>
          <div class="metric-item">
            <span>今日平均分</span>
            <strong>{{ todayAvgScore }}</strong>
          </div>
          <div class="metric-item">
            <span>最近情绪</span>
            <strong>{{ latestMoodLabel }}</strong>
          </div>
          <div class="metric-item">
            <span>最近时间</span>
            <strong>{{ latestRecordTime || '--' }}</strong>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="form-card">
      <div class="card-heading">
        <div>
          <span class="section-kicker">新增记录</span>
          <h3>记录当前情绪</h3>
        </div>
        <span>同一天可连续记录多次</span>
      </div>

      <div class="section-title">
        <h4>情绪类型</h4>
        <span>选择当前最接近的状态</span>
      </div>

      <div class="mood-grid">
        <button
          v-for="item in moodOptions"
          :key="item.value"
          type="button"
          class="mood-option"
          :class="{ active: moodTag === item.value }"
          @click="moodTag = item.value">
          <i :class="item.icon"></i>
          <strong>{{ item.label }}</strong>
          <span>{{ item.description }}</span>
        </button>
      </div>

      <div class="score-panel">
        <div class="section-title compact">
          <h4>情绪评分</h4>
          <span>{{ scoreLabel }}：{{ scoreDescription }}</span>
        </div>
        <div class="score-main">
          <div class="score-number">{{ moodScore }}</div>
          <el-slider
            v-model="moodScore"
            :min="1"
            :max="10"
            :marks="marks"
            show-stops>
          </el-slider>
        </div>
      </div>

      <div class="note-panel">
        <div class="section-title compact">
          <h4>备注</h4>
          <span>可简单记录事件或状态，选填</span>
        </div>
        <el-input
          v-model="note"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="例如：考试前有点紧张，复习结束后状态稳定。">
        </el-input>
      </div>

      <div class="action-row">
        <el-button type="primary" :loading="submitting" @click="save">
          保存记录
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="records-card" v-loading="loadingToday">
      <div slot="header" class="panel-header">
        <div>
          <strong>今日记录列表</strong>
          <span>最新记录在前</span>
        </div>
        <el-tag size="small" effect="plain">{{ todayRecordCount }} 条</el-tag>
      </div>

      <div v-if="todayRecords.length" class="record-list">
        <div v-for="record in todayRecords" :key="record.id || record.createTime" class="record-item">
          <div class="record-time">{{ formatTime(record.createTime) || '--:--' }}</div>
          <div class="record-body">
            <div class="record-title">
              <strong>{{ getMoodLabel(record.moodTag) }}</strong>
              <span>{{ formatScore(record.moodScore) }} 分</span>
            </div>
            <p>{{ formatNote(record.note) }}</p>
          </div>
        </div>
      </div>

      <el-empty
        v-else
        description="今日暂无记录，可以添加一次情绪记录。"
        :image-size="96">
      </el-empty>
    </el-card>

    <div class="note-tip">持续记录可以帮助观察情绪变化，但记录本身不替代专业评估。</div>
  </div>
</template>

<script>
import { recordMood, getTodayMood } from '@/api/mood'

const moodOptions = [
  { value: 'happy', label: '开心', icon: 'el-icon-sunny', description: '愉快、积极' },
  { value: 'calm', label: '平静', icon: 'el-icon-moon', description: '稳定、放松' },
  { value: 'sad', label: '悲伤', icon: 'el-icon-heavy-rain', description: '低落、难过' },
  { value: 'anxious', label: '焦虑', icon: 'el-icon-warning-outline', description: '紧张、担心' },
  { value: 'angry', label: '愤怒', icon: 'el-icon-bell', description: '烦躁、不满' },
  { value: 'tired', label: '疲惫', icon: 'el-icon-time', description: '乏力、倦怠' }
]

export default {
  name: 'MoodRecord',
  data() {
    return {
      loadingToday: false,
      submitting: false,
      todayStats: {
        totalRecords: 0,
        avgScore: null,
        latestRecord: null,
        records: []
      },
      moodScore: 5,
      moodTag: 'calm',
      note: '',
      moodOptions,
      marks: {
        1: '较低',
        4: '一般',
        7: '良好',
        10: '很好'
      }
    }
  },
  computed: {
    todayRecords() {
      return Array.isArray(this.todayStats.records) ? this.todayStats.records : []
    },
    todayRecordCount() {
      return Number(this.todayStats.totalRecords || this.todayRecords.length || 0)
    },
    hasTodayRecord() {
      return this.todayRecordCount > 0
    },
    latestRecord() {
      return this.todayStats.latestRecord || this.todayRecords[0] || null
    },
    statusTitle() {
      return this.hasTodayRecord ? '今日已记录情绪变化' : '今日暂无记录'
    },
    statusDescription() {
      if (!this.hasTodayRecord) return '今日暂无记录，可以添加一次情绪记录。'
      return '可继续记录新的情绪，用于观察一天内的状态变化。'
    },
    todayAvgScore() {
      if (this.todayStats.avgScore === null || this.todayStats.avgScore === undefined || this.todayStats.avgScore === '') {
        return '--'
      }
      return Number(this.todayStats.avgScore).toFixed(1)
    },
    latestMoodLabel() {
      if (!this.latestRecord) return '--'
      return this.getMoodLabel(this.latestRecord.moodTag)
    },
    latestRecordTime() {
      if (!this.latestRecord) return ''
      return this.formatTime(this.latestRecord.createTime)
    },
    scoreLabel() {
      if (this.moodScore <= 3) return '较低'
      if (this.moodScore <= 5) return '一般'
      if (this.moodScore <= 7) return '平稳'
      return '良好'
    },
    scoreDescription() {
      if (this.moodScore <= 3) return '建议适当休息并关注压力来源'
      if (this.moodScore <= 5) return '存在一定波动，可继续观察'
      if (this.moodScore <= 7) return '整体处于可调节状态'
      return '当前状态较好，注意保持'
    }
  },
  created() {
    this.fetchTodayMood()
  },
  methods: {
    fetchTodayMood() {
      this.loadingToday = true
      return getTodayMood().then(res => {
        this.todayStats = this.normalizeTodayStats(res.data)
      }).catch(() => {
        this.todayStats = this.normalizeTodayStats({})
        this.$message.warning('今日记录读取失败，请稍后重试。')
      }).finally(() => {
        this.loadingToday = false
      })
    },
    normalizeTodayStats(data) {
      const source = data || {}
      const legacyRecord = source.id ? source : null
      const records = Array.isArray(source.records)
        ? source.records
        : (legacyRecord ? [legacyRecord] : [])
      return {
        totalRecords: Number(source.totalRecords !== undefined && source.totalRecords !== null ? source.totalRecords : records.length),
        avgScore: source.avgScore !== undefined ? source.avgScore : this.calculateAverage(records),
        latestRecord: source.latestRecord || records[0] || null,
        records
      }
    },
    save() {
      this.submitting = true
      recordMood({
        moodScore: this.moodScore,
        moodTag: this.moodTag,
        note: this.note
      }).then(() => {
        this.$message.success('记录已保存。')
        this.note = ''
        return this.fetchTodayMood()
      }).catch(() => {
        this.$message.error('保存失败，请检查网络后稍后重试。')
      }).finally(() => {
        this.submitting = false
      })
    },
    calculateAverage(records) {
      const scoredRecords = records.filter(item => item.moodScore !== null && item.moodScore !== undefined)
      if (!scoredRecords.length) return null
      const total = scoredRecords.reduce((sum, item) => sum + Number(item.moodScore || 0), 0)
      return total / scoredRecords.length
    },
    getMoodLabel(value) {
      const option = this.moodOptions.find(item => item.value === value)
      return option ? option.label : '未知'
    },
    formatScore(value) {
      if (value === null || value === undefined || value === '') return '--'
      return value
    },
    formatNote(value) {
      return value && String(value).trim() ? value : '未填写备注'
    },
    formatTime(value) {
      if (!value) return ''
      const normalized = typeof value === 'string' ? value.replace(' ', 'T') : value
      const date = new Date(normalized)
      if (Number.isNaN(date.getTime())) return ''
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${hour}:${minute}`
    }
  }
}
</script>

<style scoped>
.mood-record-page {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 24px 44px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}

.module-header h1 {
  margin: 0;
  color: #243447;
  font-size: 26px;
  font-weight: 700;
}

.module-header p {
  margin: 8px 0 0;
  color: #6b7787;
  font-size: 14px;
  line-height: 1.6;
}

.status-card,
.form-card,
.records-card {
  border: 1px solid #d8e0ea;
  border-radius: 8px;
  margin-bottom: 18px;
}

.status-card ::v-deep .el-card__body {
  padding: 22px;
}

.status-grid {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(360px, 1.45fr);
  gap: 18px;
  align-items: stretch;
}

.section-kicker {
  display: block;
  margin-bottom: 8px;
  color: #6d7b8d;
  font-size: 13px;
  font-weight: 600;
}

.status-copy h2 {
  margin: 0 0 8px;
  color: #243447;
  font-size: 22px;
}

.status-copy p {
  margin: 0;
  color: #5d6878;
  font-size: 14px;
  line-height: 1.7;
}

.status-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid #e4eaf1;
  border-radius: 6px;
  overflow: hidden;
  background: #fafbfc;
}

.metric-item {
  min-width: 0;
  padding: 14px 12px;
  border-right: 1px solid #e4eaf1;
}

.metric-item:last-child {
  border-right: 0;
}

.metric-item span {
  display: block;
  color: #718096;
  font-size: 12px;
  margin-bottom: 8px;
}

.metric-item strong {
  display: block;
  color: #243447;
  font-size: 18px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-card ::v-deep .el-card__body {
  padding: 24px;
}

.card-heading,
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.card-heading {
  padding-bottom: 16px;
  margin-bottom: 18px;
  border-bottom: 1px solid #edf1f5;
}

.card-heading h3 {
  margin: 0;
  color: #243447;
  font-size: 18px;
}

.card-heading > span,
.panel-header span {
  color: #7a8798;
  font-size: 13px;
  line-height: 1.6;
}

.section-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-title h4 {
  margin: 0;
  color: #243447;
  font-size: 15px;
}

.section-title span {
  color: #7a8798;
  font-size: 13px;
}

.section-title.compact {
  margin-bottom: 12px;
}

.mood-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}

.mood-option {
  min-height: 88px;
  border: 1px solid #d8e0ea;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  display: grid;
  grid-template-columns: 24px 1fr;
  column-gap: 10px;
  row-gap: 4px;
  align-items: center;
  padding: 14px;
  text-align: left;
  transition: border-color .16s ease, background-color .16s ease;
}

.mood-option:hover {
  border-color: #9fb4cc;
}

.mood-option i {
  grid-row: span 2;
  color: #7a8798;
  font-size: 20px;
}

.mood-option strong {
  color: #243447;
  font-size: 15px;
}

.mood-option span {
  color: #8792a2;
  font-size: 12px;
}

.mood-option.active {
  border-color: #2f6f9f;
  background: #f5f9fc;
}

.mood-option.active i,
.mood-option.active strong {
  color: #2f6f9f;
}

.score-panel {
  background: #f8fafc;
  border: 1px solid #e4eaf1;
  border-radius: 6px;
  padding: 18px;
  margin-bottom: 22px;
}

.score-main {
  display: grid;
  grid-template-columns: 72px 1fr;
  align-items: center;
  gap: 18px;
}

.score-number {
  width: 58px;
  height: 58px;
  border: 1px solid #d8e0ea;
  background: #fff;
  color: #243447;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
}

.note-panel {
  margin-bottom: 22px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.action-row .el-button {
  min-width: 128px;
}

.panel-header strong {
  display: block;
  color: #243447;
  font-size: 15px;
  margin-bottom: 3px;
}

.record-list {
  display: grid;
  gap: 12px;
}

.record-item {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 14px;
  padding: 14px;
  border: 1px solid #e4eaf1;
  border-radius: 6px;
  background: #fff;
}

.record-time {
  color: #2f6f9f;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.5;
}

.record-body {
  min-width: 0;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.record-title strong {
  color: #243447;
  font-size: 15px;
}

.record-title span {
  color: #5d6878;
  font-size: 13px;
  background: #f3f6f9;
  border: 1px solid #e4eaf1;
  border-radius: 4px;
  padding: 2px 8px;
}

.record-body p {
  margin: 0;
  color: #606f80;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}

.note-tip {
  color: #6b7787;
  background: #fff;
  border: 1px solid #d8e0ea;
  border-radius: 6px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 860px) {
  .status-grid {
    grid-template-columns: 1fr;
  }

  .status-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .metric-item:nth-child(2) {
    border-right: 0;
  }

  .metric-item:nth-child(-n + 2) {
    border-bottom: 1px solid #e4eaf1;
  }
}

@media (max-width: 760px) {
  .mood-record-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .card-heading,
  .section-title,
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .mood-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .score-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 500px) {
  .mood-grid,
  .status-metrics,
  .record-item {
    grid-template-columns: 1fr;
  }

  .metric-item,
  .metric-item:nth-child(2) {
    border-right: 0;
    border-bottom: 1px solid #e4eaf1;
  }

  .metric-item:last-child {
    border-bottom: 0;
  }
}
</style>
