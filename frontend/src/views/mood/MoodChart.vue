<!-- 情绪分析中心 -->
<!-- 替换文件：src/views/mood/MoodChart.vue -->
<!-- 功能：情绪趋势折线图 + 情绪分布图 + 统计摘要 + 时间筛选 -->

<template>
  <div class="mood-analysis-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">📊 情绪分析中心</h1>
        <p class="page-subtitle">追踪你的情绪变化趋势</p>
      </div>
      <el-button size="medium" @click="$router.push('/mood')">
        <i class="el-icon-edit"></i> 记录情绪
      </el-button>
    </div>

    <!-- 时间筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeDays" size="medium" @change="onDaysChange">
        <el-radio-button :label="7">近 7 天</el-radio-button>
        <el-radio-button :label="14">近 14 天</el-radio-button>
        <el-radio-button :label="30">近 30 天</el-radio-button>
        <el-radio-button :label="90">近 90 天</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计摘要卡片 -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="6" v-for="s in summaryCards" :key="s.label">
        <el-card shadow="never" :body-style="{ padding: '20px' }">
          <div class="summary-item">
            <div class="summary-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
            <div class="summary-info">
              <span class="summary-value">{{ s.value }}</span>
              <span class="summary-label">{{ s.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 情绪趋势折线图 -->
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-line" style="color: #409EFF;"></i> 情绪趋势</span>
          </div>
          <div ref="lineChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 情绪分布饼图 -->
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-pie-chart" style="color: #6C63FF;"></i> 情绪分布</span>
          </div>
          <div ref="pieChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMoodTrend, getMoodDistribution, getMoodSummary } from '@/api/mood'
import * as echarts from 'echarts'

const moodTagMap = {
  'happy': { label: '开心', color: '#409EFF' },
  'calm':  { label: '平静', color: '#67C23A' },
  'sad':   { label: '悲伤', color: '#F56C6C' },
  'anxious': { label: '焦虑', color: '#E6A23C' },
  'angry': { label: '愤怒', color: '#fa541c' },
  'tired': { label: '疲惫', color: '#6C63FF' }
}

const moodEmojiMap = {
  'happy': '😊', 'calm': '😌', 'sad': '😢',
  'anxious': '😰', 'angry': '😠', 'tired': '😴'
}

export default {
  name: 'MoodChart',
  data() {
    return {
      activeDays: 30,
      lineChart: null,
      pieChart: null,
      summaryCards: [
        { icon: '📊', label: '平均分', value: '--', bg: '#EBF5FF' },
        { icon: '🏆', label: '最高分', value: '--', bg: '#E8F8F0' },
        { icon: '📅', label: '记录天数', value: '--', bg: '#F0EFFF' },
        { icon: '🔥', label: '最常情绪', value: '--', bg: '#FDF5E6' }
      ]
    }
  },
  mounted() {
    this.initCharts()
    this.fetchData()
  },
  beforeDestroy() {
    if (this.lineChart) this.lineChart.dispose()
    if (this.pieChart) this.pieChart.dispose()
  },
  methods: {
    onDaysChange() {
      this.fetchData()
    },

    fetchData() {
      const days = this.activeDays
      Promise.all([
        getMoodTrend(days),
        getMoodDistribution(days),
        getMoodSummary(days)
      ]).then(([trendRes, distRes, summaryRes]) => {
        this.renderLineChart(trendRes.data || [])
        this.renderPieChart(distRes.data || [])
        this.renderSummary(summaryRes.data)
      })
    },

    renderSummary(data) {
      if (!data) return
      const topMood = (data.topMood || '').toLowerCase()
      this.summaryCards = [
        { icon: '📊', label: '平均分', value: data.avgScore || '--', bg: '#EBF5FF' },
        { icon: '🏆', label: '最高分', value: data.maxScore || '--', bg: '#E8F8F0' },
        { icon: '📅', label: '记录天数', value: (data.activeDays || 0) + ' 天', bg: '#F0EFFF' },
        { icon: '🔥', label: '最常情绪', value: moodTagMap[topMood] ? moodTagMap[topMood].label : (topMood || '--'), bg: '#FDF5E6' }
      ]
    },

    renderLineChart(data) {
      const dates = data.map(d => d.record_date ? d.record_date.substr(5) : '')
      const scores = data.map(d => d.avgScore)

      this.lineChart.setOption({
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const p = params[0]
            const val = p.value
            let emoji = '😐'
            if (val >= 8) emoji = '😄'
            else if (val >= 6) emoji = '😊'
            else if (val >= 4) emoji = '🙂'
            else if (val >= 2) emoji = '😔'
            else emoji = '😢'
            return p.axisValue + '<br/>' + emoji + ' ' + val + ' 分'
          }
        },
        grid: { left: 45, right: 20, bottom: 30, top: 10 },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: { lineStyle: { color: '#E8ECF4' } },
          axisLabel: { color: '#909399', fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 10,
          splitLine: { lineStyle: { color: '#F0F5FF', type: 'dashed' } },
          axisLabel: { color: '#909399' }
        },
        series: [{
          type: 'line',
          data: scores,
          smooth: true,
          showSymbol: true,
          symbolSize: 8,
          lineStyle: { color: '#409EFF', width: 3 },
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.35)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
            ])
          }
        }]
      })
    },

    renderPieChart(data) {
      if (!data || data.length === 0) {
        this.pieChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#C0C4CC', fontSize: 14 } }
        })
        return
      }

      const colors = []
      const chartData = data.map(d => {
        const meta = moodTagMap[d.name ? d.name.toLowerCase() : '']
        colors.push(meta ? meta.color : '#909399')
        return { name: meta ? meta.label : d.name, value: d.value }
      })

      this.pieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
        series: [{
          type: 'pie',
          radius: ['42%', '70%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: true,
          padAngle: 2,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: {
            show: true,
            formatter: function(p) {
              return moodEmojiMap[data[p.dataIndex]?.name?.toLowerCase()] + ' ' + p.name
            },
            color: '#606266',
            fontSize: 12
          },
          labelLine: { length: 12, length2: 16 },
          data: chartData,
          color: colors
        }]
      })
    },

    initCharts() {
      this.lineChart = echarts.init(this.$refs.lineChart)
      this.pieChart = echarts.init(this.$refs.pieChart)

      // 空状态占位
      const emptyOption = { title: { text: '加载中...', left: 'center', top: 'center', textStyle: { color: '#C0C4CC', fontSize: 14 } } }
      this.lineChart.setOption(emptyOption)
      this.pieChart.setOption(emptyOption)

      // 响应式
      window.addEventListener('resize', this.handleResize)
    },

    handleResize() {
      if (this.lineChart) this.lineChart.resize()
      if (this.pieChart) this.pieChart.resize()
    }
  }
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.mood-analysis-page {
  max-width: var(--page-width, 1200px);
  margin: 0 auto;
  padding: 0 24px 40px;
}

/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-top: 28px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary, #2C3E50);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-muted, #909399);
  margin-top: 4px;
}

/* ===== 时间筛选 ===== */
.filter-bar {
  margin-bottom: 24px;
}

.filter-bar :deep(.el-radio-button__inner) {
  border-radius: 20px !important;
  padding: 8px 22px;
  border-color: #E8ECF4 !important;
  transition: all 0.2s;
}

.filter-bar :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 20px 0 0 20px !important;
}

.filter-bar :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 20px 20px 0 !important;
}

.filter-bar :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #409EFF, #6C63FF);
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* ===== 统计摘要 ===== */
.summary-row {
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.summary-info {
  display: flex;
  flex-direction: column;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary, #2C3E50);
  line-height: 1.2;
}

.summary-label {
  font-size: 13px;
  color: var(--text-muted, #909399);
  margin-top: 2px;
}

/* ===== 图表 ===== */
.card-header {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary, #2C3E50);
}

.card-header i {
  margin-right: 6px;
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
