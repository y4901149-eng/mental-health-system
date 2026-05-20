<!-- 管理后台 - 心理健康监控首页 -->
<template>
  <div class="dashboard">
    <!-- 标题行 -->
    <div class="dash-header">
      <h2>📊 心理健康监控</h2>
      <span class="dash-header-time">{{ currentTime }}</span>
    </div>

    <!-- 第一行：6 张统计卡片 -->
    <el-row :gutter="10" style="margin-bottom:12px;">
      <el-col :span="4" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" class="stat-card" :body-style="{ padding: '14px 12px' }">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
            <div class="stat-body">
              <div class="stat-val">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：趋势图 + 分布图 -->
    <el-row :gutter="10" style="margin-bottom:12px;">
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="ch-hd"><i class="el-icon-data-line"></i> 近7天情绪趋势</div>
          <div ref="lineChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="ch-hd"><i class="el-icon-pie-chart"></i> 情绪类型分布</div>
          <div ref="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：异常用户 TOP5 + 最近预警 -->
    <el-row :gutter="10" style="margin-bottom:12px;">
      <el-col :span="14">
        <el-card shadow="never" class="tb-card">
          <div slot="header" class="ch-hd" style="justify-content:space-between;">
            <span><i class="el-icon-warning" style="color:#F56C6C;"></i> 异常用户 TOP5</span>
            <el-tag size="mini" type="danger" effect="plain">按风险降序</el-tag>
          </div>
          <el-table :data="riskUsers" stripe size="small" style="width:100%;"
            :header-cell-style="hdrStyle"
            :cell-style="cellStyle"
            :row-class-name="riskRow">
            <el-table-column label="用户" width="100" align="center">
              <template slot-scope="{row}">{{ row.username }}</template>
            </el-table-column>
            <el-table-column label="等级" width="80" align="center">
              <template slot-scope="{row}"><el-tag size="mini" :type="row.type" effect="dark">{{ row.level }}</el-tag></template>
            </el-table-column>
            <el-table-column label="最近情绪" width="100" align="center">
              <template slot-scope="{row}">{{ row.lastMood || '-' }}</template>
            </el-table-column>
            <el-table-column label="风险次数" width="80" align="center">
              <template slot-scope="{row}">{{ row.riskCount }}</template>
            </el-table-column>
            <el-table-column label="最近活跃" width="170" align="center">
              <template slot-scope="{row}">{{ fmt(row.lastActive) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="{row}">
                <el-button type="text" size="mini" style="color:#409EFF;" @click="$router.push({path:'/admin/users',query:{username:row.username}})">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="tb-card">
          <div slot="header" class="ch-hd"><i class="el-icon-warning" style="color:#F56C6C;"></i> 最近预警记录</div>
          <el-table :data="crisisList" stripe size="small" style="width:100%;"
            :header-cell-style="hdrStyle"
            :cell-style="cellStyle">
            <el-table-column label="用户" width="100" align="center">
              <template slot-scope="{row}">{{ row.username }}</template>
            </el-table-column>
            <el-table-column label="等级" width="80" align="center">
              <template slot-scope="{row}">
                <el-tag size="mini" :type="row.levelType" effect="dark">{{ row.levelText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="关键词" width="100" align="center">
              <template slot-scope="{row}"><el-tag size="mini" type="danger">{{ row.keyword }}</el-tag></template>
            </el-table-column>
            <el-table-column label="时间" width="170" align="center">
              <template slot-scope="{row}">{{ fmt(row.time) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第四行：系统运行状态 -->
    <el-card shadow="never" class="sys-card">
      <div slot="header" class="ch-hd"><i class="el-icon-monitor"></i> 系统运行状态</div>
      <el-row :gutter="10">
        <el-col :span="6" v-for="s in sysStatus" :key="s.label">
          <div class="sys-item">
            <span class="sd" :class="s.online?'g':'r'"></span>
            <span class="sl">{{ s.label }}</span>
            <span class="sv" :style="{color:s.online?'#67C23A':'#F56C6C'}">{{ s.online ? '正常' : '异常' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCrisisList } from '@/api/crisis'

export default {
  name: 'Dashboard',
  data() {
    return {
      statCards: [
        { icon:'👥', label:'总用户数', value:'...', bg:'linear-gradient(135deg,#EBF5FF,#D6E8FF)' },
        { icon:'🔥', label:'今日活跃', value:'...', bg:'linear-gradient(135deg,#E8F8F0,#C8E6D9)' },
        { icon:'⚠️', label:'高风险用户', value:'...', bg:'linear-gradient(135deg,#FFF1F0,#FFD9D8)' },
        { icon:'💬', label:'AI对话次数', value:'...', bg:'linear-gradient(135deg,#F0EFFF,#DDD9FF)' },
        { icon:'📝', label:'今日新增日记', value:'...', bg:'linear-gradient(135deg,#FFF7E6,#FFEAB3)' },
        { icon:'⏳', label:'待处理预警', value:'...', bg:'linear-gradient(135deg,#FFF5F5,#FFD6D6)' }
      ],
      riskUsers: [],
      crisisList: [],
      recentChats: [],
      sysStatus: [
        { label:'AI 服务', online:true }, { label:'数据库', online:true },
        { label:'预警系统', online:true }, { label:'后端服务', online:true }
      ],
      lineChart:null, pieChart:null,
      currentTime: '',
      hdrStyle: { background:'#F0F4FF', color:'#2C3E50', fontWeight:600, fontSize:'12px', padding:'6px 0' },
      cellStyle: { textAlign:'center', padding:'4px 0', fontSize:'12px' }
    }
  },
  created() {
    this.updateTime()
    setInterval(() => this.updateTime(), 60000)
  },
  mounted() {
    this.fetchData()
    this.fetchCrisis()
    this.$nextTick(() => { this.initLineChart(); this.initPieChart(); this.updateTime() })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if(this.lineChart)this.lineChart.dispose()
    if(this.pieChart)this.pieChart.dispose()
  },
  methods: {
    updateTime() {
      const d = new Date()
      this.currentTime = d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0') + ' ' + String(d.getHours()).padStart(2,'0') + ':' + String(d.getMinutes()).padStart(2,'0')
    },
    handleResize() { if(this.lineChart)this.lineChart.resize(); if(this.pieChart)this.pieChart.resize() },
    fetchData() {
      const token = localStorage.getItem('mental_health_token')
      const headers = { 'Authorization': 'Bearer ' + token }
      fetch('/api/admin/dashboard/summary', { headers }).then(r=>r.json()).then(res => {
        const d = res.data || {}
        this.statCards = [
          { icon:'👥', label:'总用户数', value:d.userCount||0, bg:'linear-gradient(135deg,#EBF5FF,#D6E8FF)' },
          { icon:'🔥', label:'今日活跃', value:d.userCount||0, bg:'linear-gradient(135deg,#E8F8F0,#C8E6D9)' },
          { icon:'⚠️', label:'高风险用户', value:d.pendingCrisisCount||0, bg:'linear-gradient(135deg,#FFF1F0,#FFD9D8)' },
          { icon:'💬', label:'AI对话次数', value:d.chatCount||0, bg:'linear-gradient(135deg,#F0EFFF,#DDD9FF)' },
          { icon:'📝', label:'今日新增日记', value:d.diaryCount||0, bg:'linear-gradient(135deg,#FFF7E6,#FFEAB3)' },
          { icon:'⏳', label:'待处理预警', value:d.pendingCrisisCount||0, bg:'linear-gradient(135deg,#FFF5F5,#FFD6D6)' }
        ]
      }).catch(()=>{})
      fetch('/api/admin/dashboard/risk-users', { headers }).then(r=>r.json()).then(res => {
        const users = res.data || []
        this.riskUsers = users.map(u => ({
          username: u.username, level: u.avgScore < 20 ? '高危' : u.avgScore < 40 ? '中危' : '关注',
          type: u.avgScore < 20 ? 'danger' : u.avgScore < 40 ? 'warning' : 'warning',
          lastMood: '-', riskCount: '-', lastActive: u.lastDiaryTime || '-', id: u.id
        }))
      }).catch(()=>{})
    },
    fetchCrisis() {
      getCrisisList({ pageNum:1, pageSize:50 }).then(res => {
        const records = (res.data && res.data.records) || []
        // 最近预警列表（取前5条）
        this.crisisList = records.slice(0,5).map(r => ({
          username: r.username || '用户' + r.userId,
          levelType: r.alertLevel >= 4 ? 'danger' : r.alertLevel >= 3 ? 'warning' : 'info',
          levelText: r.alertLevel >= 4 ? '高危' : r.alertLevel >= 3 ? '中危' : '关注',
          keyword: this.extractCrisisKeyword(r.triggerReason),
          time: this.fmt(r.createTime)
        }))
        // 从预警记录统计异常用户 TOP5
        const userMap = {}
        records.forEach(r => {
          const name = r.username || '用户' + r.userId
          if (!userMap[name]) userMap[name] = { username: name, count:0, maxLevel:0, lastTime:'' }
          userMap[name].count++
          if ((r.alertLevel||0) > userMap[name].maxLevel) userMap[name].maxLevel = r.alertLevel
          if (r.createTime && r.createTime > userMap[name].lastTime) userMap[name].lastTime = r.createTime
        })
        const sorted = Object.values(userMap)
          .sort((a,b) => b.count - a.count || b.maxLevel - a.maxLevel)
          .slice(0,5)
        this.riskUsers = sorted.map(u => ({
          username: u.username,
          level: u.maxLevel >= 4 ? '高危' : u.maxLevel >= 3 ? '中危' : '关注',
          type: u.maxLevel >= 4 ? 'danger' : u.maxLevel >= 3 ? 'warning' : 'warning',
          lastMood: u.maxLevel >= 4 ? '高危波动' : u.maxLevel >= 3 ? '中危波动' : '需关注',
          riskCount: u.count,
          lastActive: u.lastTime || ''
        }))
      }).catch(()=>{})
    },
    fmt(t) {
      if (!t) return ''
      const d = new Date(t)
      if (isNaN(d.getTime())) return t
      const Y = d.getFullYear()
      const M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0')
      return Y + '-' + M + '-' + D + ' ' + h + ':' + m
    },
    extractCrisisKeyword(text) {
      if (!text) return '-'
      const kws = ['自杀','想死','不想活','活不下去','结束生命','崩溃','绝望','伤害自己','自残','好累','没有意义']
      for (const kw of kws) { if (text.includes(kw)) return kw }
      return '异常'
    },
    initLineChart() {
      const el = this.$refs.lineChart
      if(!el)return; this.lineChart = echarts.init(el)
      this.lineChart.setOption({
        tooltip:{ trigger:'axis' }, grid:{ left:45, right:15, top:15, bottom:35 },
        legend:{ data:['平均分','高危人数'], bottom:0, icon:'circle', itemWidth:8, itemHeight:8, textStyle:{fontSize:11} },
        xAxis:{ type:'category', data:['05-05','05-06','05-07','05-08','05-09','05-10','05-11'], axisLabel:{ color:'#909399',fontSize:11 } },
        yAxis:[
          { type:'value', min:0, max:100, splitLine:{ lineStyle:{ color:'#F0F4FF' } }, axisLabel:{ color:'#909399',fontSize:10 } },
          { type:'value', min:0, max:10, splitLine:{ show:false }, axisLabel:{ color:'#909399',fontSize:10 } }
        ],
        series:[
          { name:'平均分', type:'line', data:[62,55,70,48,58,72,65], smooth:true, showSymbol:true, symbolSize:6,
            lineStyle:{ color:'#409EFF', width:2.5 },
            areaStyle:{ color:new echarts.graphic.LinearGradient(0,0,0,1,[
              { offset:0, color:'rgba(64,158,255,0.3)' }, { offset:1, color:'rgba(64,158,255,0.02)' }
            ])},
            itemStyle:{ color:'#409EFF' }
          },
          { name:'高危人数', type:'line', yAxisIndex:1, data:[2,1,3,2,4,1,2], smooth:true, showSymbol:true, symbolSize:5,
            lineStyle:{ color:'#F56C6C', width:2 },
            itemStyle:{ color:'#F56C6C' }
          }
        ]
      })
    },
    initPieChart() {
      const el = this.$refs.pieChart
      if(!el)return; this.pieChart = echarts.init(el)
      this.pieChart.setOption({
        tooltip:{ trigger:'item', formatter:'{b}: {c}' },
        series:[{
          type:'pie', radius:['38%','65%'], center:['50%','50%'],
          padAngle:2, itemStyle:{ borderRadius:6, borderColor:'#fff', borderWidth:2 },
          label:{ show:true, color:'#606266', fontSize:11 },
          data:[
            { name:'开心', value:45, itemStyle:{ color:'#409EFF' } },
            { name:'平静', value:32, itemStyle:{ color:'#67C23A' } },
            { name:'焦虑', value:12, itemStyle:{ color:'#E6A23C' } },
            { name:'难过', value:8, itemStyle:{ color:'#F56C6C' } },
            { name:'愤怒', value:3, itemStyle:{ color:'#fa541c' } }
          ]
        }]
      })
    },
    riskRow({row}) {
      if(!row)return''
      if(row.level==='高危')return'risk-row-danger'
      if(row.level==='中危')return'risk-row-orange'
      return''
    }
  }
}
</script>

<style scoped>
.dashboard { max-width:1400px; margin:0 auto; padding:0 12px 30px; }

/* ===== 标题行 ===== */
.dash-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.dash-header h2 { font-size:18px; font-weight:700; color:#1A2332; margin:0; letter-spacing:-0.3px; }
.dash-header-time { font-size:12px; color:#8C9AB7; background:#F0F4FF; padding:4px 12px; border-radius:6px; }

/* ===== 统计卡片 ===== */
.stat-card { border-radius:12px !important; border:1px solid #E8ECF4 !important; transition:all 0.2s !important; }
.stat-card:hover { border-color:#C8D6E5 !important; box-shadow:0 4px 12px rgba(0,0,0,0.04) !important; }
.stat-inner { display:flex; align-items:center; gap:10px; }
.stat-icon { width:34px; height:34px; border-radius:10px; display:flex; align-items:center; justify-content:center; font-size:16px; flex-shrink:0; }
.stat-body { display:flex; flex-direction:column; }
.stat-val { font-size:20px; font-weight:800; color:#1A2332; line-height:1.1; letter-spacing:-0.3px; }
.stat-label { font-size:11px; color:#8C9AB7; margin-top:1px; }

/* ===== 图表卡片 ===== */
.chart-card { border-radius:12px !important; border:1px solid #E8ECF4 !important; }
.chart-card .chart-box { width:100%; height:260px; }
.ch-hd { font-weight:700; font-size:13px; color:#1A2332; display:flex; align-items:center; gap:6px; }
.ch-hd i { font-size:15px; color:#409EFF; }

/* ===== 表格卡片 ===== */
.tb-card { border-radius:12px !important; border:1px solid #E8ECF4 !important; }
.tb-card :deep(.el-table__body-wrapper) { overflow-x:hidden; }

/* ===== 系统状态 ===== */
.sys-card { border-radius:12px !important; border:1px solid #E8ECF4 !important; }
.sys-item { display:flex; align-items:center; gap:10px; padding:10px 12px; background:#F8FAFF; border-radius:8px; border:1px solid #E8ECF4; transition:all 0.15s; }
.sys-item:hover { background:#F0F4FF; border-color:#C8D6E5; }
.sd { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.sd.g { background:#67C23A; box-shadow:0 0 6px rgba(103,194,58,0.4); }
.sd.r { background:#F56C6C; box-shadow:0 0 6px rgba(245,108,108,0.4); }
.sl { font-size:12px; color:#606266; flex:1; }
.sv { font-size:12px; font-weight:700; }
</style>
