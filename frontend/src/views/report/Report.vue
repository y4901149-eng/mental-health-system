<!-- 情绪周报 -->
<template>
  <div class="report-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">📊 情绪周报</h1>
        <p class="page-subtitle">自动生成每周情绪总结与分析</p>
      </div>
      <el-button type="primary" size="medium" @click="handleGenerate" :loading="generating">
        <i class="el-icon-refresh"></i> 生成周报
      </el-button>
    </div>

    <!-- 最新周报 -->
    <template v-if="report">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="8" v-for="s in statItems" :key="s.label">
          <el-card shadow="never" :body-style="{ padding: '20px' }">
            <div class="stat-inner">
              <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
              <div class="stat-info">
                <span class="stat-num">{{ s.value }}</span>
                <span class="stat-label">{{ s.label }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 周报周期 -->
      <div class="period-bar">
        <i class="el-icon-date"></i>
        统计周期：{{ report.periodStart }} ~ {{ report.periodEnd }}
      </div>

      <!-- 趋势图 -->
      <el-card shadow="never" class="chart-card">
        <div slot="header" class="card-header">
          <span><i class="el-icon-data-line" style="color:#409EFF;"></i> 本周情绪趋势</span>
        </div>
        <div ref="trendChart" class="chart-box"></div>
      </el-card>

      <!-- AI 总结 -->
      <el-card shadow="never" class="summary-card">
        <div class="summary-body">
          <div class="summary-avatar">🤖</div>
          <div class="summary-content">
            <div class="summary-label">AI 周报总结</div>
            <p class="summary-text">{{ report.summary || '暂无总结' }}</p>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 空状态 -->
    <el-empty v-else-if="!loading" :image-size="100" description="还没有周报，点击「生成周报」创建">
      <el-button type="primary" size="medium" @click="handleGenerate" :loading="generating">生成周报</el-button>
    </el-empty>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap"><i class="el-icon-loading"></i> 加载中...</div>

    <!-- 历史周报 -->
    <el-card shadow="never" class="history-card" v-if="historyList.length > 0">
      <div slot="header" class="card-header">
        <span><i class="el-icon-time" style="color:#6C63FF;"></i> 历史周报</span>
      </div>
      <div class="history-item" v-for="h in historyList" :key="h.id" @click="viewReport(h)">
        <div class="history-period">{{ h.periodStart }} ~ {{ h.periodEnd }}</div>
        <div class="history-score" v-if="h.statisticsJson">
          <el-tag size="mini" type="primary">{{ parseStats(h.statisticsJson).avgScore || '-' }}分</el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getLatestReport, getReportList, generateReport } from '@/api/report'
import * as echarts from 'echarts'

export default {
  name: 'Report',
  data() {
    return {
      report: null,
      historyList: [],
      loading: false,
      generating: false,
      trendChart: null,
      statItems: [
        { icon: '📊', label: '平均情绪', value: '--', bg: '#EBF5FF' },
        { icon: '📖', label: '日记数量', value: '--', bg: '#F0EFFF' },
        { icon: '🏷️', label: '主要情绪', value: '--', bg: '#E8F8F0' }
      ]
    }
  },
  created() {
    this.fetchData()
  },
  mounted() {
    this.$nextTick(() => { this.tryInitChart() })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.trendChart) this.trendChart.dispose()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    /** 尝试初始化图表（容器可能还没渲染） */
    tryInitChart() {
      const el = this.$refs && this.$refs.trendChart
      if (el && !this.trendChart) {
        this.trendChart = echarts.init(el)
      }
    },

    fetchData() {
      this.loading = true
      Promise.all([getLatestReport(), getReportList()]).then(([latestRes, listRes]) => {
        this.report = latestRes.data
        this.historyList = (listRes.data || []).filter(h => !this.report || h.id !== this.report.id)
        this.updateStats()
        this.$nextTick(() => this.renderChart())
      }).finally(() => { this.loading = false })
    },
    handleGenerate() {
      this.generating = true
      generateReport().then(() => {
        this.$message.success('周报已生成')
        this.fetchData()
      }).finally(() => { this.generating = false })
    },
    viewReport(h) {
      this.report = h
      this.updateStats()
      this.$nextTick(() => { this.renderChart(); window.scrollTo({ top: 0, behavior: 'smooth' }) })
    },
    updateStats() {
      if (!this.report) return
      const stats = this.parseStats(this.report.statisticsJson)
      this.statItems = [
        { icon: '📊', label: '平均情绪', value: stats.avgScore != null ? stats.avgScore + ' 分' : '--', bg: '#EBF5FF' },
        { icon: '📖', label: '日记数量', value: stats.diaryCount != null ? stats.diaryCount + ' 篇' : '--', bg: '#F0EFFF' },
        { icon: '🏷️', label: '主要情绪', value: stats.topMood || '--', bg: '#E8F8F0' }
      ]
    },
    renderChart() {
      // 确保图表已初始化（容器可能刚渲染）
      this.tryInitChart()
      if (!this.trendChart || !this.report) return
      const stats = this.parseStats(this.report.statisticsJson)
      const trend = stats.trend || {}
      const dates = trend.dates || ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']
      const scores = trend.scores || [50, 55, 48, 62, 58, 70, 65]
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 15, top: 10, bottom: 25 },
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#909399' } },
        yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { color: '#F0F5FF' } } },
        series: [{
          type: 'line', data: scores,
          smooth: true, showSymbol: true, symbolSize: 7,
          lineStyle: { color: '#409EFF', width: 2 },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.25)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])},
          itemStyle: { color: '#409EFF' }
        }]
      })
      this.trendChart.resize()
    },

    handleResize() {
      if (this.trendChart) this.trendChart.resize()
    },

    parseStats(json) {
      if (!json) return {}
      try { return typeof json === 'string' ? JSON.parse(json) : json }
      catch(e) { return {} }
    }
  }
}
</script>

<style scoped>
.report-page {
  max-width: var(--page-width, 1200px); margin: 0 auto; padding: 0 24px 40px;
}
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; padding-top: 28px;
}
.page-title { font-size: 22px; font-weight: 700; color: #2C3E50; }
.page-subtitle { font-size: 14px; color: #909399; margin-top: 4px; }
.stats-row { margin-bottom: 16px; }
.stat-inner { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { font-size: 20px; font-weight: 700; color: #2C3E50; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.period-bar { font-size: 13px; color: #909399; margin-bottom: 16px; padding: 8px 14px; background: #F8FAFF; border-radius: 8px; display: inline-block; }
.period-bar i { margin-right: 6px; }
.chart-card { margin-bottom: 16px; }
.chart-box { width: 100%; height: 280px; }
.card-header { font-weight: 600; font-size: 15px; color: #2C3E50; }
.card-header i { margin-right: 6px; }
.summary-card { margin-bottom: 16px; background: #F0F5FF !important; border-color: transparent !important; }
.summary-body { display: flex; gap: 16px; align-items: flex-start; }
.summary-avatar { font-size: 36px; flex-shrink: 0; }
.summary-content { flex: 1; }
.summary-label { font-size: 14px; font-weight: 600; color: #409EFF; margin-bottom: 8px; }
.summary-text { font-size: 14px; color: #606266; line-height: 1.8; margin: 0; white-space: pre-wrap; }
.loading-wrap { text-align: center; padding: 60px 0; color: #909399; font-size: 14px; }
.loading-wrap i { font-size: 24px; margin-right: 8px; }
.history-card { margin-top: 8px; }
.history-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; cursor: pointer; border-radius: 8px; transition: background 0.2s;
}
.history-item:hover { background: #F0F5FF; }
.history-period { font-size: 14px; color: #606266; }
</style>
