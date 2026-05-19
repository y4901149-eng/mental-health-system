<template>
  <div class="mood-chart-page">
    <section class="module-header">
      <div>
        <h1>情绪分析中心</h1>
        <p>基于每日情绪记录展示趋势、分布和基础统计。</p>
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
            <strong>情绪趋势</strong>
            <span>分数范围 1-10</span>
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
            <strong>情绪分布</strong>
            <span>按情绪类型统计</span>
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

    <el-card shadow="never" class="insight-card">
      <div class="insight-title">情绪洞察</div>
      <p>{{ insightText }}</p>
    </el-card>
  </div>
</template>

<script>
import { getMoodTrend, getMoodDistribution, getMoodSummary } from '@/api/mood'
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
      error: '',
      summary: {},
      trendData: [],
      distributionData: [],
      lineChart: null,
      pieChart: null
    }
  },
  computed: {
    summaryCards() {
      return [
        { label: '记录天数', value: this.formatValue(this.summary.activeDays, '0'), note: `近 ${this.activeDays} 天` },
        { label: '平均分', value: this.formatValue(this.summary.avgScore, '--'), note: '情绪评分均值' },
        { label: '最高 / 最低', value: `${this.formatValue(this.summary.maxScore, '--')} / ${this.formatValue(this.summary.minScore, '--')}`, note: '波动范围' },
        { label: '主要情绪', value: this.getMoodLabel(this.summary.topMood) || '--', note: '出现频率最高' }
      ]
    },
    hasRecords() {
      return Number(this.summary.totalRecords || this.summary.activeDays || 0) > 0
    },
    insightText() {
      if (this.loading) return '正在整理近期数据。'
      if (this.error) return '当前数据加载失败，请稍后再试。'
      if (!this.hasRecords) return '暂无情绪记录。完成每日情绪记录后，可在此查看趋势变化。'

      const activeDays = Number(this.summary.activeDays || 0)
      const avgScore = Number(this.summary.avgScore || 0)
      if (activeDays < 3) return '近期记录次数较少，建议连续记录几天后再观察趋势。'
      if (avgScore >= 7) return '近期平均分较高，整体状态较稳定。建议继续保持规律作息。'
      if (avgScore >= 5) return '近期情绪整体处于中间水平，可继续观察压力来源和睡眠情况。'
      return '近期平均分偏低，建议适当休息，并考虑向老师、同学或专业人员寻求支持。'
    }
  },
  mounted() {
    this.initCharts()
    this.fetchData()
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
    initCharts() {
      if (this.$refs.lineChart) this.lineChart = echarts.init(this.$refs.lineChart)
      if (this.$refs.pieChart) this.pieChart = echarts.init(this.$refs.pieChart)
    },
    normalizeTrend(list) {
      return list.map(item => {
        const rawDate = item.record_date || item.recordDate || item.date || ''
        const score = item.avgScore != null ? item.avgScore : item.avg_score
        return {
          date: rawDate ? String(rawDate).slice(5) : '',
          score: Number(score || 0)
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
    renderLineChart() {
      if (!this.lineChart) return
      this.lineChart.clear()
      this.lineChart.setOption({
        tooltip: { trigger: 'axis', formatter: params => {
          const item = params && params[0] ? params[0] : null
          return item ? `${item.axisValue}<br/>情绪分：${item.value}` : ''
        }},
        grid: { left: 44, right: 18, top: 24, bottom: 34 },
        xAxis: {
          type: 'category',
          data: this.trendData.map(item => item.date),
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#dfe7f1' } },
          axisLabel: { color: '#7a8798' }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 10,
          splitNumber: 5,
          axisLabel: { color: '#7a8798' },
          splitLine: { lineStyle: { color: '#edf1f7' } }
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
  color: #2c3e50;
  font-size: 26px;
}

.module-header p {
  margin: 8px 0 0;
  color: #6f7d8f;
  font-size: 14px;
}

.module-header .el-button span {
  display: inline-block;
}

.toolbar,
.summary-card,
.panel-card,
.insight-card {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
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
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.toolbar ::v-deep .el-radio-button__inner {
  border-radius: 0 !important;
}

.error-alert,
.summary-row,
.chart-row {
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
  color: #7a8798;
  font-size: 13px;
}

.summary-card strong {
  display: block;
  margin: 8px 0;
  color: #2c3e50;
  font-size: 24px;
}

.panel-card {
  margin-bottom: 18px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header strong {
  color: #2c3e50;
  font-size: 15px;
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
  background: rgba(255, 255, 255, 0.9);
}

.insight-card ::v-deep .el-card__body {
  padding: 18px 20px;
}

.insight-title {
  color: #2c3e50;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
}

.insight-card p {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  font-size: 14px;
}

@media (max-width: 760px) {
  .mood-chart-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .chart-shell,
  .chart-box {
    height: 280px;
    min-height: 280px;
  }
}
</style>
