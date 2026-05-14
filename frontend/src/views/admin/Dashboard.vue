<!-- 管理后台 - AI 心理健康监控首页 -->
<template>
  <div class="dashboard">
    <h2 style="font-size:20px;font-weight:700;color:#2C3E50;margin-bottom:20px;">📊 AI 心理健康监控</h2>

    <!-- 第一行：6 张统计卡片 -->
    <el-row :gutter="14" style="margin-bottom:16px;">
      <el-col :span="4" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" class="stat-card" :body-style="{ padding: '16px' }">
          <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
          <div class="stat-body">
            <div class="stat-val">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
            <div class="stat-trend">{{ s.trend }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：趋势图 + 分布图 -->
    <el-row :gutter="14" style="margin-bottom:16px;">
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-hd"><i class="el-icon-data-line" style="color:#409EFF;"></i> 近7天情绪趋势</div>
          <div ref="lineChart" style="width:100%;height:280px;"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-hd"><i class="el-icon-pie-chart" style="color:#6C63FF;"></i> 情绪类型分布</div>
          <div ref="pieChart" style="width:100%;height:280px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：异常用户 TOP5 + 最近 AI 对话 -->
    <el-row :gutter="14" style="margin-bottom:16px;">
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-hd" style="justify-content:space-between;">
            <span><i class="el-icon-warning" style="color:#F56C6C;"></i> 异常用户 TOP5</span>
            <el-tag size="mini" type="danger">按风险降序</el-tag>
          </div>
          <el-table :data="riskUsers" stripe size="small" style="width:100%;"
            :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center',fontSize:'12px'}"
            :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}"
            :row-class-name="riskRow">
            <el-table-column prop="username" label="用户" min-width="80" />
            <el-table-column label="风险等级" min-width="80">
              <template slot-scope="{row}"><el-tag size="mini" :type="row.type" effect="dark">{{ row.level }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="lastMood" label="最近情绪" min-width="70" />
            <el-table-column prop="riskCount" label="风险次数" min-width="70" />
            <el-table-column prop="lastActive" label="最近活跃" min-width="120" />
            <el-table-column label="操作" min-width="90">
              <template slot-scope="{row}">
                <el-button type="text" size="mini" @click="$router.push({path:'/admin/users',query:{username:row.username}})">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-hd"><i class="el-icon-chat-dot-round" style="color:#67C23A;"></i> 最近 AI 对话</div>
          <div class="chat-feed">
            <div v-for="(c,i) in recentChats" :key="i" class="chat-item">
              <div class="chat-avatar" :style="{background:c.bg}">{{ c.initial }}</div>
              <div class="chat-body">
                <div class="chat-user">{{ c.username }} <el-tag size="mini" :type="c.riskType" style="margin-left:4px;" v-if="c.riskType">{{ c.riskLabel }}</el-tag></div>
                <div class="chat-text">{{ c.summary }}</div>
                <div class="chat-time">{{ c.time }}</div>
              </div>
            </div>
            <div v-if="recentChats.length===0" style="text-align:center;padding:30px;color:#C0C4CC;font-size:13px;">暂无对话</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第四行：系统运行状态 -->
    <el-card shadow="never">
      <div slot="header" class="card-hd"><i class="el-icon-monitor" style="color:#409EFF;"></i> 系统运行状态</div>
      <el-row :gutter="14">
        <el-col :span="6" v-for="s in sysStatus" :key="s.label">
          <div class="sys-status-item">
            <span class="status-dot" :class="s.online?'green':'red'"></span>
            <span class="status-label">{{ s.label }}</span>
            <span class="status-val" :style="{color:s.online?'#67C23A':'#F56C6C'}">{{ s.online ? '正常' : '异常' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

const COLORS = ['#409EFF','#67C23A','#E6A23C','#F56C6C','#6C63FF','#fa541c']
const avatarBg = ['#EBF5FF','#E8F8F0','#FFF1F0','#F0EFFF','#FFF7E6']

export default {
  name: 'Dashboard',
  data() {
    return {
      statCards: [
        { icon:'👥', label:'总用户数', value:'...', trend:'', bg:COLORS[0]+'15' },
        { icon:'🔥', label:'今日活跃', value:'...', trend:'', bg:COLORS[1]+'15' },
        { icon:'⚠️', label:'高风险用户', value:'...', trend:'', bg:COLORS[3]+'15' },
        { icon:'💬', label:'AI对话次数', value:'...', trend:'', bg:COLORS[4]+'15' },
        { icon:'📝', label:'今日新增日记', value:'...', trend:'', bg:COLORS[2]+'15' },
        { icon:'⏳', label:'待处理预警', value:'...', trend:'', bg:COLORS[5]+'15' }
      ],
      riskUsers: [],
      recentChats: [
        { username:'student', initial:'学', summary:'我最近压力很大，总是睡不好...', time:'14:30', riskType:'danger', riskLabel:'高危', bg:avatarBg[0] },
        { username:'liyutong', initial:'李', summary:'我好累，感觉撑不下去了...', time:'23:16', riskType:'danger', riskLabel:'高危', bg:avatarBg[2] },
        { username:'zhangsan', initial:'张', summary:'最近和室友关系不太好...', time:'10:15', riskType:'', riskLabel:'', bg:avatarBg[3] },
        { username:'lisi', initial:'李', summary:'今天终于把论文写完了！', time:'18:40', riskType:'', riskLabel:'', bg:avatarBg[1] },
        { username:'admin', initial:'管', summary:'今天工作进展顺利...', time:'09:20', riskType:'', riskLabel:'', bg:avatarBg[4] }
      ],
      sysStatus: [
        { label:'AI 服务', online:true }, { label:'数据库', online:true },
        { label:'预警系统', online:true }, { label:'在线用户', online:true, extra:'12 人' }
      ],
      lineChart:null, pieChart:null
    }
  },
  mounted() {
    this.fetchData()
    this.$nextTick(() => {
      this.initLineChart()
      this.initPieChart()
    })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if(this.lineChart)this.lineChart.dispose()
    if(this.pieChart)this.pieChart.dispose()
  },
  methods: {
    handleResize() {
      if(this.lineChart)this.lineChart.resize()
      if(this.pieChart)this.pieChart.resize()
    },
    fetchData() {
      const token = localStorage.getItem('mental_health_token')
      const headers = { 'Authorization': 'Bearer ' + token }
      // 统计卡片
      fetch('/api/admin/dashboard/summary', { headers }).then(r=>r.json()).then(res => {
        const d = res.data || {}
        this.statCards = [
          { icon:'👥', label:'总用户数', value:d.userCount||0, trend:'注册用户', bg:COLORS[0]+'15' },
          { icon:'🔥', label:'今日活跃', value:d.userCount||0, trend:'', bg:COLORS[1]+'15' },
          { icon:'⚠️', label:'高风险用户', value:d.pendingCrisisCount||0, trend:'待处理', bg:COLORS[3]+'15' },
          { icon:'💬', label:'AI对话次数', value:d.chatCount||0, trend:'总计', bg:COLORS[4]+'15' },
          { icon:'📝', label:'今日新增日记', value:d.diaryCount||0, trend:'总计', bg:COLORS[2]+'15' },
          { icon:'⏳', label:'待处理预警', value:d.pendingCrisisCount||0, trend:'', bg:COLORS[5]+'15' }
        ]
      }).catch(()=>{})
      // 异常用户 TOP5
      fetch('/api/admin/dashboard/risk-users', { headers }).then(r=>r.json()).then(res => {
        const users = res.data || []
        this.riskUsers = users.map(u => ({
          username: u.username,
          level: u.avgScore < 20 ? '高危' : u.avgScore < 40 ? '中危' : '关注',
          type: u.avgScore < 20 ? 'danger' : u.avgScore < 40 ? 'warning' : 'warning',
          lastMood: '-',
          riskCount: '-',
          lastActive: u.lastDiaryTime || '-',
          id: u.id
        }))
      }).catch(()=>{})
    },
    initLineChart() {
      const el = this.$refs.lineChart
      if(!el)return
      this.lineChart = echarts.init(el)
      this.lineChart.setOption({
        tooltip:{ trigger:'axis' },
        legend:{ data:['平均分','高危人数'], bottom:0, icon:'circle', itemWidth:8 },
        grid:{ left:45, right:15, top:10, bottom:40 },
        xAxis:{ type:'category', data:['05-05','05-06','05-07','05-08','05-09','05-10','05-11'], axisLabel:{ color:'#909399',fontSize:11 } },
        yAxis:[
          { type:'value', min:0, max:100, splitLine:{ lineStyle:{ color:'#F0F5FF' } }, axisLabel:{ color:'#909399' } },
          { type:'value', min:0, max:10, splitLine:{ show:false }, axisLabel:{ color:'#909399' } }
        ],
        series:[
          { name:'平均分', type:'line', data:[62,55,70,48,58,72,65], smooth:true, showSymbol:true, symbolSize:6,
            lineStyle:{ color:'#409EFF', width:2.5 },
            areaStyle:{ color:new echarts.graphic.LinearGradient(0,0,0,1,[
              { offset:0, color:'rgba(64,158,255,0.3)' }, { offset:1, color:'rgba(64,158,255,0.02)' }
            ])},
            itemStyle:{ color:'#409EFF' }
          },
          { name:'高危人数', type:'line', yAxisIndex:1, data:[2,1,3,2,4,1,2], smooth:true, showSymbol:true, symbolSize:6,
            lineStyle:{ color:'#F56C6C', width:2 },
            itemStyle:{ color:'#F56C6C' }
          }
        ]
      })
    },
    initPieChart() {
      const el = this.$refs.pieChart
      if(!el)return
      this.pieChart = echarts.init(el)
      this.pieChart.setOption({
        tooltip:{ trigger:'item', formatter:'{b}: {c} ({d}%)' },
        series:[{
          type:'pie', radius:['38%','65%'], center:['50%','50%'],
          padAngle:2, itemStyle:{ borderRadius:6, borderColor:'#fff', borderWidth:2 },
          label:{ show:true, color:'#606266', fontSize:12 },
          data:[
            { name:'😊 开心', value:45, itemStyle:{ color:'#409EFF' } },
            { name:'😌 平静', value:32, itemStyle:{ color:'#67C23A' } },
            { name:'😰 焦虑', value:12, itemStyle:{ color:'#E6A23C' } },
            { name:'😢 难过', value:8, itemStyle:{ color:'#F56C6C' } },
            { name:'😠 愤怒', value:3, itemStyle:{ color:'#fa541c' } }
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
.dashboard { max-width:1400px; margin:0 auto; padding:0 8px 40px; }
.stat-card { cursor:default; transition: all 0.3s ease !important; }
.stat-card:hover { transform:translateY(-3px); box-shadow:0 8px 25px rgba(64,158,255,0.12) !important; }
.stat-icon { width:36px; height:36px; border-radius:10px; display:flex; align-items:center; justify-content:center; font-size:18px; margin-bottom:8px; }
.stat-body { display:flex; flex-direction:column; }
.stat-val { font-size:22px; font-weight:700; color:#2C3E50; line-height:1.2; }
.stat-label { font-size:12px; color:#909399; margin-top:2px; }
.stat-trend { font-size:11px; color:#A0AEC0; margin-top:4px; }
.card-hd { font-weight:600; font-size:14px; color:#2C3E50; display:flex; align-items:center; gap:6px; }

/* 聊天流 */
.chat-feed { max-height:340px; overflow-y:auto; }
.chat-item { display:flex; gap:10px; padding:10px 0; border-bottom:1px solid #F5F7FA; }
.chat-item:last-child { border-bottom:none; }
.chat-avatar { width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:13px; font-weight:600; color:#2C3E50; flex-shrink:0; }
.chat-body { flex:1; min-width:0; }
.chat-user { font-size:12px; font-weight:600; color:#2C3E50; margin-bottom:2px; }
.chat-text { font-size:12px; color:#606266; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.chat-time { font-size:11px; color:#C0C4CC; margin-top:2px; }

/* 系统状态 */
.sys-status-item { display:flex; align-items:center; gap:8px; padding:8px 12px; background:#F8FAFF; border-radius:8px; }
.status-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.status-dot.green { background:#67C23A; box-shadow:0 0 6px rgba(103,194,58,0.4); }
.status-dot.red { background:#F56C6C; box-shadow:0 0 6px rgba(245,108,108,0.4); }
.status-label { font-size:13px; color:#606266; flex:1; }
.status-val { font-size:13px; font-weight:600; }
</style>
