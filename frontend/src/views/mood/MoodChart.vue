<template>
  <div class="mood-chart-page">
    <section class="module-header">
      <div>
        <h1>情绪分析中心</h1>
        <p>基于情绪记录生成趋势和分布，仅供自我观察。</p>
      </div>
      <el-button type="primary" @click="$router.push('/mood')">
        <i class="el-icon-edit"></i>
        <span>记录情绪</span>
      </el-button>
    </section>

    <section class="toolbar">
      <span>统计周期</span>
      <el-radio-group v-model="activeDays" size="small" @change="fetchData">
        <el-radio-button :label="7">7 天</el-radio-button>
        <el-radio-button :label="14">14 天</el-radio-button>
        <el-radio-button :label="30">30 天</el-radio-button>
      </el-radio-group>
    </section>

    <el-alert
      v-if="error"
      :title="error"
      type="warning"
      :closable="false"
      show-icon
      class="error-alert">
    </el-alert>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in summaryCards" :key="item.label">
        <el-card shadow="never" class="summary-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" class="chart-row">
      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <div slot="header" class="panel-header">
            <div>
              <strong>情绪趋势</strong>
              <span>同一天多条记录按平均分显示</span>
            </div>
          </div>
          <div class="chart-shell">
            <div ref="lineChart" class="chart-box"></div>
            <el-empty
              v-if="!loading && !error && trendData.length === 0"
              description="暂无趋势数据"
              :image-size="90"
              class="chart-empty">
              <el-button type="primary" plain size="small" @click="$router.push('/mood')">去记录</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <div slot="header" class="panel-header">
            <div>
              <strong>情绪分布</strong>
              <span>统计范围内所有记录</span>
            </div>
          </div>
          <div class="chart-shell">
            <div ref="pieChart" class="chart-box"></div>
            <el-empty
              v-if="!loading && !error && distributionData.length === 0"
              description="暂无情绪分布数据"
              :image-size="90"
              class="chart-empty">
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="records-card" v-loading="recordsLoading">
      <div slot="header" class="panel-header">
        <div>
          <strong>最近 30 天情绪记录与备注</strong>
          <span>按日期分组，最新记录在前</span>
        </div>
        <el-button plain size="small" @click="$router.push('/mood')">
          记录情绪
        </el-button>
      </div>

      <div v-if="recordGroups.length" class="record-groups">
        <section v-for="group in recordGroups" :key="group.date" class="day-group">
          <div class="day-title">
            <strong>{{ group.date }}</strong>
            <span>{{ group.records.length }} 条</span>
          </div>
          <div class="record-list">
            <div v-for="record in group.records" :key="record.id || record.createTime" class="record-item">
              <div class="record-time">{{ record.time || '--:--' }}</div>
              <div class="record-body">
                <div class="record-meta">
                  <strong>{{ getMoodLabel(record.moodTag) }}</strong>
                  <span>{{ formatScore(record.moodScore) }} 分</span>
                </div>
                <p>{{ formatNote(record.note) }}</p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <el-empty
        v-else
        description="最近 30 天暂无情绪记录，可以先完成一次情绪记录。"
        :image-size="96">
      </el-empty>
    </el-card>

    <el-card shadow="never" class="insight-card">
      <div class="insight-title">情绪观察</div>
      <p>{{ insightText }}</p>
    </el-card>
  </div>
</template>

<script>
import { getMoodTrend, getMoodDistribution, getMoodSummary, getMoodRecords } from '@/api/mood'
import * as echarts from 'echarts'

const moodTagMap = {
  happy: { label: '开心', color: '#4C78A8' },
  calm: { label: '平静', color: '#59A14F' },
  sad: { label: '悲伤', color: '#E15759' },
  anxious: { label: '焦虑', color: '#F28E2B' },
  angry: { label: '愤怒', color: '#B07AA1' },
  tired: { label: '疲惫', color: '#79706E' }
}

export default {
  name: 'MoodChart',
  data() {
    return {
      activeDays: 30,
      loading: false,
      recordsLoading: false,
      error: '',
      recordsError: '',
      summary: {},
      trendData: [],
      distributionData: [],
      recentRecords: [],
      lineChart: null,
      pieChart: null
    }
  },
  computed: {
    summaryCards() {
      return [
        { label: '总记录次数', value: this.formatValue(this.summary.totalRecords, '0'), note: `近 ${this.activeDays} 天` },
        { label: '平均情绪分', value: this.formatAverage(this.summary.avgScore), note: '基于全部记录' },
        { label: '最高 / 最低', value: `${this.formatValue(this.summary.maxScore, '--')} / ${this.formatValue(this.summary.minScore, '--')}`, note: '评分波动范围' },
        { label: '主要情绪', value: this.getMoodLabel(this.summary.topMood) || '--', note: `记录天数 ${this.formatValue(this.summary.activeDays, '0')} 天` }
      ]
    },
    hasRecords() {
      return Number(this.summary.totalRecords || 0) > 0
    },
    recordGroups() {
      const groupMap = {}
      this.recentRecords.forEach(record => {
        const date = record.recordDate || '未知日期'
        if (!groupMap[date]) {
          groupMap[date] = []
        }
        groupMap[date].push(record)
      })
      return Object.keys(groupMap)
        .sort((a, b) => b.localeCompare(a))
        .map(date => ({
          date,
          records: groupMap[date]
        }))
    },
    insightText() {
      if (this.loading) return '正在整理近期数据。'
      if (this.error) return '当前数据加载失败，请稍后再试。'
      if (!this.hasRecords) return '暂无情绪记录。完成情绪记录后，可在此查看趋势变化。'

      const totalRecords = Number(this.summary.totalRecords || 0)
      const avgScore = Number(this.summary.avgScore || 0)
      if (totalRecords < 3) return '近期记录次数较少，建议持续记录后再观察变化。'
      if (avgScore >= 7) return '近期平均分较高，整体状态较稳定。建议继续保持规律作息。'
      if (avgScore >= 5) return '近期情绪整体处于中间水平，可继续观察压力来源和睡眠情况。'
      return '近期平均分偏低，建议适当休息，并考虑向老师、同学或专业人员寻求支持。'
    }
  },
  mounted() {
    this.initCharts()
    this.fetchData()
    this.fetchRecentRecords()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.lineChart) this.lineChart.dispose()
    if (this.pieChart) this.pieChart.dispose()
  },
  methods: {
    fetchData() {
      this.loading = true
      this.error = ''
      const days = this.activeDays
      Promise.all([
        getMoodTrend(days),
        getMoodDistribution(days),
        getMoodSummary(days)
      ]).then(([trendRes, distributionRes, summaryRes]) => {
        this.trendData = this.normalizeTrend(trendRes.data || [])
        this.distributionData = this.normalizeDistribution(distributionRes.data || [])
        this.summary = summaryRes.data || {}
        this.$nextTick(() => {
          this.renderLineChart()
          this.renderPieChart()
        })
      }).catch(() => {
        this.trendData = []
        this.distributionData = []
        this.summary = {}
        this.error = '情绪分析数据加载失败，请稍后重试。'
        this.$nextTick(() => {
          this.renderLineChart()
          this.renderPieChart()
        })
      }).finally(() => {
        this.loading = false
      })
    },
    fetchRecentRecords() {
      this.recordsLoading = true
      this.recordsError = ''
      getMoodRecords(30).then(res => {
        this.recentRecords = this.normalizeRecords(res.data || [])
      }).catch(() => {
        this.recentRecords = []
        this.recordsError = '最近记录加载失败，请稍后重试。'
        this.$message.warning(this.recordsError)
      }).finally(() => {
        this.recordsLoading = false
      })
    },
    initCharts() {
      if (this.$refs.lineChart) this.lineChart = echarts.init(this.$refs.lineChart)
      if (this.$refs.pieChart) this.pieChart = echarts.init(this.$refs.pieChart)
    },
    normalizeTrend(list) {
      return list.map(item => {
        const rawDate = item.record_date || item.recordDate || item.date || ''
        const score = item.avgScore !== undefined && item.avgScore !== null ? item.avgScore : item.avg_score
        return {
          date: rawDate ? String(rawDate) : '',
          label: rawDate ? String(rawDate).slice(5) : '',
          score: this.roundScore(score)
        }
      }).filter(item => item.date)
    },
    normalizeDistribution(list) {
      return list.map(item => {
        const key = item.name ? String(item.name).toLowerCase() : ''
        const meta = moodTagMap[key]
        return {
          name: meta ? meta.label : (item.name || '未知'),
          value: Number(item.value || 0),
          color: meta ? meta.color : '#A0AEC0'
        }
      }).filter(item => item.value > 0)
    },
    normalizeRecords(list) {
      return list.map(item => {
        const recordDate = item.recordDate || item.record_date || this.formatDate(item.createTime || item.create_time)
        const createTime = item.createTime || item.create_time || ''
        return {
          id: item.id,
          recordDate,
          createTime,
          time: this.formatTime(createTime),
          moodTag: item.moodTag || item.mood_tag,
          moodScore: item.moodScore !== undefined ? item.moodScore : item.mood_score,
          note: item.note,
          timestamp: this.toTimestamp(createTime, recordDate)
        }
      }).filter(item => item.recordDate)
        .sort((a, b) => b.timestamp - a.timestamp)
    },
    renderLineChart() {
      if (!this.lineChart) return
      this.lineChart.clear()
      this.lineChart.setOption({
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const item = params && params[0] ? params[0] : null
            const record = item ? this.trendData[item.dataIndex] : null
            return record ? `${record.date}<br/>平均分：${item.value}` : ''
          }
        },
        grid: { left: 44, right: 18, top: 26, bottom: 36 },
        xAxis: {
          type: 'category',
          data: this.trendData.map(item => item.label),
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#d8e0ea' } },
          axisLabel: { color: '#718096' }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 10,
          splitNumber: 5,
          axisLabel: { color: '#718096' },
          splitLine: { lineStyle: { color: '#edf1f5' } }
        },
        series: [{
          type: 'line',
          data: this.trendData.map(item => item.score),
          smooth: true,
          symbolSize: 6,
          lineStyle: { color: '#4C78A8', width: 2 },
          itemStyle: { color: '#4C78A8' },
          areaStyle: { color: 'rgba(76, 120, 168, 0.08)' }
        }]
      })
      this.lineChart.resize()
    },
    renderPieChart() {
      if (!this.pieChart) return
      this.pieChart.clear()
      this.pieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
        series: [{
          type: 'pie',
          radius: ['46%', '72%'],
          center: ['50%', '52%'],
          itemStyle: { borderColor: '#fff', borderWidth: 2 },
          label: { color: '#606266', formatter: '{b}' },
          data: this.distributionData.map(item => ({
            name: item.name,
            value: item.value,
            itemStyle: { color: item.color }
          }))
        }]
      })
      this.pieChart.resize()
    },
    getMoodLabel(value) {
      if (!value) return ''
      const meta = moodTagMap[String(value).toLowerCase()]
      return meta ? meta.label : value
    },
    formatValue(value, fallback) {
      if (value === null || value === undefined || value === '') return fallback
      return value
    },
    formatAverage(value) {
      if (value === null || value === undefined || value === '') return '--'
      return Number(value).toFixed(1)
    },
    formatScore(value) {
      if (value === null || value === undefined || value === '') return '--'
      return value
    },
    formatNote(value) {
      return value && String(value).trim() ? value : '未填写备注'
    },
    roundScore(value) {
      const number = Number(value || 0)
      return Number(number.toFixed(1))
    },
    formatDate(value) {
      if (!value) return ''
      if (typeof value === 'string' && value.length >= 10) return value.slice(0, 10)
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    formatTime(value) {
      if (!value) return ''
      const date = this.parseDate(value)
      if (!date) return ''
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${hour}:${minute}`
    },
    toTimestamp(createTime, recordDate) {
      const date = this.parseDate(createTime) || this.parseDate(recordDate)
      return date ? date.getTime() : 0
    },
    parseDate(value) {
      if (!value) return null
      const normalized = typeof value === 'string' ? value.replace(' ', 'T') : value
      const date = new Date(normalized)
      return Number.isNaN(date.getTime()) ? null : date
    },
    handleResize() {
      if (this.lineChart) this.lineChart.resize()
      if (this.pieChart) this.pieChart.resize()
    }
  }
}
</script>

<style scoped>
.mood-chart-page {
  max-width: 1160px;
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

.module-header .el-button span {
  display: inline-block;
}

.toolbar,
.summary-card,
.panel-card,
.records-card,
.insight-card {
  background: #fff;
  border: 1px solid #d8e0ea;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.toolbar > span {
  color: #243447;
  font-size: 14px;
  font-weight: 600;
}

.toolbar ::v-deep .el-radio-button__inner {
  border-radius: 0 !important;
}

.error-alert,
.summary-row,
.chart-row,
.records-card {
  margin-bottom: 18px;
}

.summary-card {
  margin-bottom: 16px;
}

.summary-card ::v-deep .el-card__body {
  padding: 18px;
}

.summary-card span,
.summary-card small {
  display: block;
  color: #718096;
  font-size: 13px;
}

.summary-card strong {
  display: block;
  min-height: 30px;
  margin: 8px 0;
  color: #243447;
  font-size: 24px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-card {
  margin-bottom: 18px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-header strong {
  display: block;
  color: #243447;
  font-size: 15px;
  margin-bottom: 3px;
}

.panel-header span {
  color: #7a8798;
  font-size: 12px;
}

.chart-shell {
  position: relative;
  min-height: 320px;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
}

.record-groups {
  display: grid;
  gap: 18px;
}

.day-group {
  border: 1px solid #e4eaf1;
  border-radius: 6px;
  overflow: hidden;
}

.day-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  background: #f8fafc;
  border-bottom: 1px solid #e4eaf1;
}

.day-title strong {
  color: #243447;
  font-size: 14px;
}

.day-title span {
  color: #718096;
  font-size: 12px;
}

.record-list {
  display: grid;
}

.record-item {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 14px;
  padding: 13px 14px;
  border-bottom: 1px solid #edf1f5;
}

.record-item:last-child {
  border-bottom: 0;
}

.record-time {
  color: #2f6f9f;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.6;
}

.record-body {
  min-width: 0;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.record-meta strong {
  color: #243447;
  font-size: 14px;
}

.record-meta span {
  color: #5d6878;
  font-size: 12px;
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

.insight-card ::v-deep .el-card__body {
  padding: 18px 20px;
}

.insight-title {
  color: #243447;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
}

.insight-card p {
  margin: 0;
  color: #606f80;
  line-height: 1.8;
  font-size: 14px;
}

@media (max-width: 760px) {
  .mood-chart-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .toolbar,
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .chart-shell,
  .chart-box {
    height: 280px;
    min-height: 280px;
  }
}

@media (max-width: 520px) {
  .record-item {
    grid-template-columns: 1fr;
  }

  .summary-card strong {
    font-size: 20px;
  }
}
</style>
