<!-- 管理员 - 对话管理（风险消息聚合） -->
<template>
  <div class="page-container">
    <div class="page-title-row">
      <h2 class="page-title">💬 风险消息监控</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px;">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" :body-style="{ padding: '16px 20px' }">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
            <div class="stat-info">
              <div class="stat-num">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12">
        <el-col :span="6">
          <el-input v-model="filters.keyword" placeholder="用户名" size="small" clearable @clear="search" @keyup.enter.native="search" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="filters.riskKeyword" placeholder="风险关键词" size="small" clearable @clear="search" @keyup.enter.native="search" />
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="~" size="small"
            start-placeholder="开始" end-placeholder="结束" style="width:100%;" @change="search" />
        </el-col>
        <el-col :span="4" style="text-align:right;">
          <el-button size="small" type="primary" @click="search">筛选</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;"
        size="small"
        :header-cell-style="{ background:'#F8FAFF', color:'#2C3E50', textAlign:'center' }"
        :cell-style="{ textAlign:'center', padding:'6px 0' }">
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column label="风险关键词" width="120">
          <template slot-scope="{row}">{{ extractRiskWord(row.content) }}</template>
        </el-table-column>
        <el-table-column label="消息摘要" min-width="250">
          <template slot-scope="{row}">{{ truncate(row.content, 40) }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="140">
          <template slot-scope="{row}">{{ formatTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="riskType(row.content)" effect="dark">
              {{ riskLevel(row.content) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showContext(row)">查看上下文</el-button>
            <el-button type="text" size="mini" style="color:#C0C4CC;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next"
        :total="total" :page-size="pageSize" :current-page="pageNum"
        @current-change="p=>{pageNum=p;fetch()}" style="margin-top:12px;text-align:center;" />
      <div v-if="!loading && total===0" style="text-align:center;padding:40px 0;color:#C0C4CC;">暂无风险消息</div>
    </el-card>

    <!-- 上下文弹窗 -->
    <el-dialog title="聊天上下文" :visible.sync="contextVisible" width="600px" top="5vh" :close-on-click-modal="true">
      <div class="msg-list" v-if="contextMessages.length > 0">
        <div v-for="m in contextMessages" :key="m.id" class="msg-item" :class="m.sender_role === 'USER' ? 'user' : 'ai'">
          <div class="msg-label">{{ m.sender_role === 'USER' ? '😊 用户' : '🤖 AI' }}</div>
          <div class="msg-text">{{ m.content }}</div>
          <div class="msg-time">{{ m.create_time }}</div>
        </div>
      </div>
      <div v-else style="text-align:center;padding:40px;color:#C0C4CC;">加载中...</div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { extractKeyword, riskLevelFromKeyword, riskTypeFromKeyword } from '@/utils/crisisKeywords'

export default {
  name: 'ChatManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:10, loading:false,
      filters: { keyword:'', riskKeyword:'' },
      dateRange: null,
      contextVisible:false, contextMessages:[],
      statCards: [
        { icon:'💬', label:'总消息数', value:0, bg:'#EBF5FF' },
        { icon:'⚠️', label:'风险消息', value:0, bg:'#FFF1F0' },
        { icon:'👥', label:'涉及用户', value:0, bg:'#F0EFFF' },
        { icon:'🔴', label:'今日风险', value:0, bg:'#FFF7E6' }
      ]
    }
  },
  created() { this.fetch() },
  methods: {
    fetch() {
      this.loading=true
      const params = { page:this.pageNum, size:this.pageSize }
      if (this.filters.keyword) params.keyword = this.filters.keyword
      if (this.filters.riskKeyword) params.riskKeyword = this.filters.riskKeyword
      if (this.dateRange) {
        params.startDate = this.dateRange[0].toISOString().substring(0,10)
        params.endDate = this.dateRange[1].toISOString().substring(0,10)
      }
      request.get('/admin/chat/risk', { params }).then(r => {
        const d = r.data || {}
        this.list = d.records || []
        this.total = d.total || 0
        const s = d.stats || {}
        this.statCards = [
          { icon:'💬', label:'总消息数', value:s.totalMessages||0, bg:'#EBF5FF' },
          { icon:'⚠️', label:'风险消息', value:s.riskMessageCount||0, bg:'#FFF1F0' },
          { icon:'👥', label:'涉及用户', value:s.involvedUsers||0, bg:'#F0EFFF' },
          { icon:'🔴', label:'今日风险', value:s.todayRiskCount||0, bg:'#FFF7E6' }
        ]
      }).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetch()},

    extractRiskWord(content) { return extractKeyword(content) || '-' },
    riskLevel(content) { return riskLevelFromKeyword(content) },
    riskType(content) { return riskTypeFromKeyword(content) },

    showContext(row) {
      this.contextMessages = []
      this.contextVisible = true
      // 获取该会话所有消息
      request.get('/admin/chat/' + row.session_id + '/messages').then(r => {
        this.contextMessages = r.data || []
      })
    },

    handleDelete(id) {
      this.$confirm('确定删除此消息？','提示',{type:'warning'}).then(()=>{
        request.delete('/admin/chat/message/' + id).then(()=>{
          this.$message.success('已删除')
          this.fetch()
        })
      }).catch(()=>{})
    },

    truncate(text, len) {
      if (!text) return ''
      return text.length > len ? text.substring(0, len) + '...' : text
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      if (isNaN(d.getTime())) return t
      const Y = d.getFullYear()
      const M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0')
      return Y + '-' + M + '-' + D + ' ' + h + ':' + m
    }
  }
}
</script>

<style scoped>
.page-title-row { display:flex; align-items:center; justify-content:space-between; margin-bottom:20px; }
.page-title { margin:0 !important; }
.stat-item { display:flex; align-items:center; gap:12px; }
.stat-icon { width:40px; height:40px; border-radius:10px; display:flex; align-items:center; justify-content:center; font-size:18px; flex-shrink:0; }
.stat-info { display:flex; flex-direction:column; gap:2px; }
.stat-num { font-size:20px; font-weight:700; color:#2C3E50; }
.stat-label { font-size:12px; color:#909399; }
.filter-card { margin-bottom:16px; }
.filter-card :deep(.el-card__body) { padding:14px 18px; }
.table-card { width:100%; }
.table-card :deep(.el-card__body) { padding:0; }
.table-card :deep(.el-table__header th) { font-weight:600; color:#2C3E50; font-size:13px; }
.table-card :deep(.el-table__body td) { font-size:13px; }
.table-card :deep(.el-pagination) { padding:12px 0; }
.msg-list { max-height:450px; overflow-y:auto; }
.msg-item { margin-bottom:10px; padding:10px 14px; background:#F8FAFF; border-radius:8px; }
.msg-item.user { background:#EBF5FF; }
.msg-label { font-size:12px; font-weight:600; color:#909399; margin-bottom:4px; }
.msg-text { font-size:14px; color:#2C3E50; line-height:1.6; white-space:pre-wrap; }
.msg-time { font-size:11px; color:#C0C4CC; margin-top:4px; }
</style>
