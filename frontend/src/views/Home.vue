<!-- 首页 - 完整统计 -->
<template>
  <div class="home-page">
    <!-- ===== 欢迎区 ===== -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>你好，{{ userInfo ? userInfo.nickname : '用户' }}</h2>
        <p>{{ encouragements[currentEncouragement] }}</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" size="medium" @click="$router.push('/diary')">
          <i class="el-icon-edit"></i> 写日记
        </el-button>
        <el-button plain size="medium" @click="$router.push('/chat')">
          <i class="el-icon-chat-dot-round"></i> 和AI聊聊
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" :body-style="{ padding: '18px 20px' }">
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

    <!-- ===== 情绪趋势图 + 最近日记 ===== -->
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-line" style="color:#409EFF;"></i> 近7天情绪趋势</span>
          </div>
          <div ref="trendChart" style="width:100%;height:200px;"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-notebook-2" style="color:#6C63FF;"></i> 最近日记</span>
            <el-button type="text" size="mini" @click="$router.push('/diary')">查看全部</el-button>
          </div>
          <div v-if="recentDiaries.length > 0" class="recent-diary-list">
            <div class="rd-item" v-for="d in recentDiaries" :key="d.id">
              <div class="rd-body">
                <div class="rd-text">{{ summaryText(d.content) }}</div>
                <div class="rd-time">{{ formatDate(d.create_time) }}</div>
              </div>
            </div>
          </div>
          <div v-else style="text-align:center;padding:24px 0;color:#C0C4CC;font-size:13px;">
            还没有日记，开始记录吧
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 快捷入口 ===== -->
    <el-row :gutter="16" class="quick-row">
      <el-col :span="6" v-for="item in quickLinks" :key="item.path">
        <el-card shadow="never" class="quick-card" @click.native="$router.push(item.path)">
          <div class="quick-body">
            <div class="quick-icon" :style="{ background: item.bg }">{{ item.icon }}</div>
            <span class="quick-title">{{ item.title }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDashboardSummary } from '@/api/dashboard'
import * as echarts from 'echarts'

const moodEmojiMap = {
  'happy': '😊', '开心': '😊',
  'calm': '😌', '平静': '😌',
  'sad': '😢', '悲伤': '😢',
  'anxious': '😰', '焦虑': '😰',
  'angry': '😠', '愤怒': '😠',
  'tired': '😴', '疲惫': '😴'
}

export default {
  name: 'Home',
  data() {
    return {
      currentEncouragement: 0,
      encouragements: [
        '今天也是充满希望的一天 🌤️',
        '每一次努力都在让明天更好 💪',
        '关心心理健康，从今天开始 🌱',
        '你值得被温柔以待 💕',
        '保持积极心态，拥抱美好生活 ✨'
      ],
      statCards: [
        { icon: '📖', label: '日记总数', value: 0, bg: '#EBF5FF' },
        { icon: '📊', label: '情绪周报', value: 0, bg: '#F0EFFF' },
        { icon: '💬', label: 'AI对话', value: 0, bg: '#E8F8F0' },
        { icon: '🚨', label: '预警记录', value: 0, bg: '#FDF5E6' }
      ],
      recentDiaries: [],
      recentMoodTrend: [],
      trendChart: null,
      quickLinks: [
        { icon: '📖', title: '写日记', path: '/diary', bg: 'linear-gradient(135deg,#409EFF,#6C63FF)' },
        { icon: '💬', title: '和AI聊聊', path: '/chat', bg: 'linear-gradient(135deg,#67C23A,#85CE61)' },
        { icon: '📋', title: '心理评估', path: '/assessment', bg: 'linear-gradient(135deg,#E6A23C,#FFB347)' },
        { icon: '📊', title: '情绪分析', path: '/mood/chart', bg: 'linear-gradient(135deg,#6C63FF,#A78BFA)' }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  created() {
    this.fetchData()
    setInterval(() => {
      this.currentEncouragement = (this.currentEncouragement + 1) % this.encouragements.length
    }, 6000)
  },
  mounted() {
    this.initChart()
  },
  beforeDestroy() {
    if (this.trendChart) this.trendChart.dispose()
  },
  methods: {
    fetchData() {
      getDashboardSummary().then(res => {
        const d = res.data || {}
        this.statCards = [
          { icon: '📖', label: '日记总数', value: d.diaryCount || 0, bg: '#EBF5FF' },
          { icon: '📊', label: '情绪周报', value: d.reportCount || 0, bg: '#F0EFFF' },
          { icon: '💬', label: 'AI对话', value: d.chatCount || 0, bg: '#E8F8F0' },
          { icon: '🚨', label: '预警记录', value: d.crisisCount || 0, bg: '#FDF5E6' }
        ]
        this.recentDiaries = d.recentDiaries || []
        this.recentMoodTrend = d.recentMoodTrend || []
        this.renderChart()
      })
    },
    initChart() {
      this.trendChart = echarts.init(this.$refs.trendChart)
    },
    renderChart() {
      if (!this.trendChart) return
      const data = this.recentMoodTrend
      const dates = data.map(d => d.record_date ? d.record_date.substring(5) : '')
      const scores = data.map(d => d.avgScore)
      this.trendChart.setOption({
        tooltip: { trigger: 'axis', formatter: p => p[0].axisValue + '<br/>情绪评分: ' + p[0].value },
        grid: { left: 35, right: 10, top: 8, bottom: 20 },
        xAxis: { type: 'category', data: dates.length > 0 ? dates : ['无数据'], axisLabel: { fontSize: 11, color: '#909399' } },
        yAxis: { type: 'value', min: 0, max: 10, splitLine: { lineStyle: { color: '#F0F5FF' } }, show: true },
        series: [{
          type: 'line', data: scores.length > 0 ? scores : [0],
          smooth: true, showSymbol: true, symbolSize: 6,
          lineStyle: { color: '#409EFF', width: 2 },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.25)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])},
          itemStyle: { color: '#409EFF' }
        }]
      })
    },
    getMoodEmoji(tags) {
      if (!tags) return '💭'
      const list = tags.split(',').map(t => t.trim())
      for (const t of list) {
        if (moodEmojiMap[t]) return moodEmojiMap[t]
        if (moodEmojiMap[t.toLowerCase()]) return moodEmojiMap[t.toLowerCase()]
      }
      return '💭'
    },
    summaryText(text) {
      if (!text) return ''
      return text.length > 40 ? text.substring(0, 40) + '...' : text
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return (d.getMonth() + 1) + '/' + d.getDate()
    }
  }
}
</script>

<style scoped>
.home-page {
  max-width: var(--page-width, 1200px); margin: 0 auto; padding: 0 24px 40px;
}
.welcome-section {
  display: flex; align-items: center; justify-content: space-between;
  background: linear-gradient(135deg, #409EFF 0%, #6C63FF 100%);
  border-radius: 16px; padding: 28px 32px; margin-bottom: 20px; color: #fff;
}
.welcome-text h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.welcome-text p { font-size: 14px; opacity: 0.9; }
.welcome-actions { display: flex; gap: 10px; flex-shrink: 0; }
.welcome-actions .el-button--primary { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }
.welcome-actions .el-button--primary:hover { background: rgba(255,255,255,0.3) !important; }
.welcome-actions .el-button--default.is-plain { background: rgba(255,255,255,0.1) !important; border: 1px solid rgba(255,255,255,0.25) !important; color: #fff !important; }
.welcome-actions .el-button--default.is-plain:hover { background: rgba(255,255,255,0.25) !important; }
.stats-row { margin-bottom: 20px; }
.stat-inner { display: flex; align-items: center; gap: 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { font-size: 20px; font-weight: 700; color: #2C3E50; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; font-size: 14px; color: #2C3E50; }
.card-header i { margin-right: 6px; }
.recent-diary-list { max-height: 200px; overflow-y: auto; }
.rd-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #F5F7FA; }
.rd-item:last-child { border-bottom: none; }
.rd-mood { font-size: 20px; flex-shrink: 0; }
.rd-body { flex: 1; min-width: 0; }
.rd-text { font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rd-time { font-size: 11px; color: #C0C4CC; margin-top: 2px; }
.quick-row { margin-top: 20px; }
.quick-card { cursor: pointer; transition: all 0.3s ease !important; }
.quick-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(64,158,255,0.12) !important; }
.quick-body { display: flex; align-items: center; gap: 12px; padding: 2px 0; }
.quick-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.quick-title { font-size: 14px; font-weight: 600; color: #2C3E50; }
</style>
