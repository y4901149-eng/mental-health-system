<template>
  <div class="mood-chart-page">
    <section class="page-header">
      <div>
        <p class="eyebrow">情绪分析中心</p>
        <h1>查看近期情绪变化</h1>
        <p class="subtitle">基于你的每日情绪打卡，呈现趋势、分布和简单洞察。</p>
      </div>
      <el-button type="primary" plain @click="$router.push('/mood')">
        <i class="el-icon-edit"></i>
        记录情绪
      </el-button>
    </section>

    <div class="toolbar">
      <span>时间范围</span>
      <el-radio-group v-model="activeDays" size="small" @change="fetchData">
        <el-radio-button :label="7">7 天</el-radio-button>
        <el-radio-button :label="14">14 天</el-radio-button>
        <el-radio-button :label="30">30 天</el-radio-button>
      </el-radio-group>
    </div>

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
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-note">{{ item.note }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" class="chart-row">
      <el-col :xs="24" :sm="24" :md="15">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <div slot="header" class="panel-header">
            <div>
              <span>情绪趋势</span>
              <small>分数越高代表当天状态越积极</small>
            </div>
          </div>
          <div class="chart-shell">
            <div ref="lineChart" class="chart-box"></div>
            <el-empty
              v-if="!loading && !error && trendData.length === 0"
              description="暂无趋势数据"
              :image-size="90"
              class="chart-empty">
              <el-button type="primary" plain size="small" @click="$router.push('/mood')">去打卡</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="9">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <div slot="header" class="panel-header">
            <div>
              <span>情绪分布</span>
              <small>查看主要情绪出现比例</small>
            </div>
          </div>
          <div class="chart-shell">
            <div ref="pieChart" class="chart-box small"></div>
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
      <div class="insight-title">
        <i class="el-icon-reading"></i>
        <span>情绪洞察</span>
      </div>
      <p>{{ insightText }}</p>
    </el-card>
  </div>
</template>

<script>
import { getMoodTrend, getMoodDistribution, getMoodSummary } from '@/api/mood'
import * as echarts from 'echarts'

const moodTagMap = {
  happy: { label: '开心', color: '#5B8FF9' },
  calm: { label: '平静', color: '#61C9A8' },
  sad: { label: '悲伤', color: '#F08C8C' },
  anxious: { label: '焦虑', color: '#F3B45B' },
  angry: { label: '愤怒', color: '#E8684A' },
  tired: { label: '疲惫', color: '#8B7FD1' }
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
      const topMood = this.getMoodLabel(this.summary.topMood)
      return [
        {
          label: '记录天数',
          value: this.formatNumber(this.summary.activeDays, '0'),
          note: `近 ${this.activeDays} 天`
        },
        {
          label: '平均情绪分',
          value: this.formatNumber(this.summary.avgScore, '--'),
          note: '1-10 分'
        },
        {
          label: '最高 / 最低',
          value: `${this.formatNumber(this.summary.maxScore, '--')} / ${this.formatNumber(this.summary.minScore, '--')}`,
          note: '情绪波动参考'
        },
        {
          label: '主要情绪',
          value: topMood || '--',
          note: '出现频率最高'
        }
      ]
    },
    hasRecords() {
      return Number(this.summary.totalRecords || this.summary.activeDays || 0) > 0
    },
    insightText() {
      if (this.loading) return '正在整理近期情绪数据...'
      if (this.error) return '数据暂时无法读取，可以稍后再查看分析。'
      if (!this.hasRecords) return '目前还没有情绪记录。完成几次每日打卡后，这里会展示更清晰的趋势和分布。'

      const activeDays = Number(this.summary.activeDays || 0)
      const avgScore = Number(this.summary.avgScore || 0)
      if (activeDays < 3) return '近期记录次数还比较少，建议继续打卡几天，让趋势判断更稳定。'
      if (avgScore >= 7) return '近期整体情绪状态较好，可以继续保持当前节奏，并留意哪些活动让你更放松。'
      if (avgScore >= 5) return '近期情绪整体较平稳，偶尔波动属于正常现象，可以继续观察压力来源。'
      return '近期情绪分偏低，建议给自己安排休息，并及时向可信任的人表达感受。'
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
          key,
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
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const item = params && params[0] ? params[0] : null
            return item ? `${item.axisValue}<br/>情绪分：${item.value}` : ''
          }
        },
        grid: { left: 44, right: 18, top: 24, bottom: 34 },
        xAxis: {
          type: 'category',
          data: this.trendData.map(item => item.date),
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#E3EAF5' } },
          axisLabel: { color: '#8A97A8' }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 10,
          splitNumber: 5,
          axisLabel: { color: '#8A97A8' },
          splitLine: { lineStyle: { color: '#EEF3FA' } }
        },
        series: [{
          type: 'line',
          data: this.trendData.map(item => item.score),
          smooth: true,
          symbolSize: 7,
          lineStyle: { color: '#5B8FF9', width: 3 },
          itemStyle: { color: '#5B8FF9' },
          areaStyle: { color: 'rgba(91, 143, 249, 0.08)' }
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
          avoidLabelOverlap: true,
          itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 6 },
          label: {
            color: '#606266',
            formatter: '{b}'
          },
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
    formatNumber(value, fallback) {
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

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 26px;
  line-height: 1.35;
}

.subtitle {
  margin: 8px 0 0;
  color: #7a8798;
  font-size: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #fff;
  border: 1px solid #e8eef7;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.toolbar span {
  color: #2c3e50;
  font-size: 14px;
  font-weight: 700;
}

.error-alert {
  margin-bottom: 16px;
}

.summary-row {
  margin-bottom: 18px;
}

.summary-card {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  margin-bottom: 16px;
}

.summary-card ::v-deep .el-card__body {
  padding: 18px;
}

.summary-label {
  color: #7a8798;
  font-size: 13px;
  margin-bottom: 8px;
}

.summary-value {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
}

.summary-note {
  color: #a0a9b6;
  font-size: 12px;
  margin-top: 8px;
}

.chart-row {
  margin-bottom: 18px;
}

.panel-card,
.insight-card {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  margin-bottom: 18px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.panel-header span {
  color: #2c3e50;
  font-size: 15px;
  font-weight: 700;
}

.panel-header small {
  color: #909399;
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

.chart-box.small {
  height: 320px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.88);
}

.insight-card ::v-deep .el-card__body {
  padding: 20px 22px;
}

.insight-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-weight: 700;
  margin-bottom: 10px;
}

.insight-title i {
  color: #409eff;
  font-size: 18px;
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

  .page-header,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .chart-shell,
  .chart-box,
  .chart-box.small {
    height: 280px;
    min-height: 280px;
  }
}
</style>
