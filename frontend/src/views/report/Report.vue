<template>
  <div class="report-page">
    <section class="module-header">
      <div>
        <h1>情绪周报</h1>
        <p>基于近期记录生成情绪统计和建议，用于自我回顾。</p>
      </div>
      <el-button type="primary" :loading="generating" @click="handleGenerate">
        <i class="el-icon-refresh"></i>
        生成周报
      </el-button>
    </section>

    <div v-if="loading" class="state-panel">
      <i class="el-icon-loading"></i>
      <span>正在加载周报...</span>
    </div>

    <el-empty
      v-else-if="!report"
      description="暂无情绪周报"
      class="empty-panel">
      <el-button type="primary" :loading="generating" @click="handleGenerate">生成周报</el-button>
    </el-empty>

    <template v-else>
      <section class="period-panel">
        <span>统计周期</span>
        <strong>{{ report.periodStart }} 至 {{ report.periodEnd }}</strong>
      </section>

      <el-row :gutter="16" class="summary-row">
        <el-col :xs="12" :sm="12" :md="6" v-for="item in statItems" :key="item.label">
          <el-card shadow="never" class="summary-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.note }}</small>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="panel-card">
        <div slot="header" class="panel-header">
          <strong>周内趋势</strong>
          <span>情绪评分变化</span>
        </div>
        <div class="chart-shell">
          <div ref="trendChart" class="chart-box"></div>
          <el-empty
            v-if="trendDates.length === 0"
            description="暂无趋势数据"
            :image-size="90"
            class="chart-empty">
          </el-empty>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <div slot="header" class="panel-header">
          <strong>周报建议</strong>
          <span>仅供自我了解和学习参考</span>
        </div>
        <p class="summary-text">{{ report.summary || '暂无周报建议。' }}</p>
      </el-card>

      <el-card shadow="never" class="history-card" v-if="historyList.length > 0">
        <div slot="header" class="panel-header">
          <strong>历史周报</strong>
          <span>点击查看详情</span>
        </div>
        <div
          v-for="item in historyList"
          :key="item.id"
          class="history-item"
          @click="viewReport(item)">
          <span>{{ item.periodStart }} 至 {{ item.periodEnd }}</span>
          <strong>{{ parseStats(item.statisticsJson).avgScore || '--' }} 分</strong>
        </div>
      </el-card>
    </template>
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
      trendChart: null
    }
  },
  computed: {
    stats() {
      return this.parseStats(this.report ? this.report.statisticsJson : null)
    },
    trendDates() {
      return this.stats.trend && this.stats.trend.dates ? this.stats.trend.dates : []
    },
    trendScores() {
      return this.stats.trend && this.stats.trend.scores ? this.stats.trend.scores : []
    },
    statItems() {
      return [
        { label: '平均情绪', value: this.formatValue(this.stats.avgScore, '--'), note: '综合评分' },
        { label: '记录数量', value: this.formatValue(this.stats.diaryCount || this.stats.recordCount, '0'), note: '周内记录' },
        { label: '主要情绪', value: this.stats.topMood || '--', note: '出现较多' },
        { label: '报告类型', value: this.report ? this.report.reportType || 'WEEKLY' : '--', note: '统计口径' }
      ]
    }
  },
  created() {
    this.fetchData()
  },
  mounted() {
    this.$nextTick(() => this.initChart())
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) this.trendChart.dispose()
  },
  methods: {
    fetchData() {
      this.loading = true
      Promise.all([getLatestReport(), getReportList()]).then(([latestRes, listRes]) => {
        this.report = latestRes.data || null
        this.historyList = (listRes.data || []).filter(item => !this.report || item.id !== this.report.id)
        this.$nextTick(() => this.renderChart())
      }).catch(() => {
        this.$message.error('周报加载失败，请稍后重试。')
      }).finally(() => {
        this.loading = false
      })
    },
    handleGenerate() {
      this.generating = true
      generateReport().then(() => {
        this.$message.success('周报已生成。')
        this.fetchData()
      }).catch(() => {
        this.$message.error('周报生成失败，请稍后重试。')
      }).finally(() => {
        this.generating = false
      })
    },
    viewReport(item) {
      this.report = item
      this.$nextTick(() => {
        this.renderChart()
        window.scrollTo({ top: 0, behavior: 'smooth' })
      })
    },
    initChart() {
      if (this.$refs.trendChart && !this.trendChart) {
        this.trendChart = echarts.init(this.$refs.trendChart)
      }
    },
    renderChart() {
      this.initChart()
      if (!this.trendChart) return
      this.trendChart.clear()
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 44, right: 18, top: 24, bottom: 34 },
        xAxis: {
          type: 'category',
          data: this.trendDates,
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#dfe7f1' } },
          axisLabel: { color: '#7a8798' }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { color: '#7a8798' },
          splitLine: { lineStyle: { color: '#edf1f7' } }
        },
        series: [{
          type: 'line',
          data: this.trendScores,
          smooth: true,
          symbolSize: 6,
          lineStyle: { color: '#4C78A8', width: 2 },
          itemStyle: { color: '#4C78A8' },
          areaStyle: { color: 'rgba(76, 120, 168, 0.08)' }
        }]
      })
      this.trendChart.resize()
    },
    parseStats(json) {
      if (!json) return {}
      try {
        return typeof json === 'string' ? JSON.parse(json) : json
      } catch(e) {
        return {}
      }
    },
    formatValue(value, fallback) {
      if (value === null || value === undefined || value === '') return fallback
      return value
    },
    handleResize() {
      if (this.trendChart) this.trendChart.resize()
    }
  }
}
</script>

<style scoped>
.report-page {
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

.period-panel,
.summary-card,
.panel-card,
.history-card,
.state-panel,
.empty-panel {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
}

.period-panel {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 16px;
  color: #7a8798;
}

.period-panel strong {
  color: #2c3e50;
}

.summary-row {
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

.panel-card,
.history-card {
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
  min-height: 300px;
}

.chart-box {
  width: 100%;
  height: 300px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
}

.summary-text {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.history-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #edf1f7;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item strong {
  color: #2c3e50;
}

.state-panel,
.empty-panel {
  padding: 48px 20px;
  text-align: center;
  color: #7a8798;
}

.state-panel i {
  color: #409eff;
  margin-right: 8px;
}

@media (max-width: 760px) {
  .report-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .period-panel {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
