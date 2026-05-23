<template>
  <div class="page-container">
    <div class="page-title-row">
      <h2 class="page-title">📖 日记管理</h2>
      <el-tag size="medium" type="primary" effect="plain" v-if="total > 0">共 {{ total }} 条记录</el-tag>
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
        <el-col :span="8">
          <el-input v-model="filters.keyword" placeholder="用户名/标题/内容" size="small" clearable @clear="search" @keyup.enter.native="search" />
        </el-col>
        <el-col :span="8">
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
      <el-table :data="list" stripe v-loading="loading" style="width:100%;" size="small"
        :header-cell-style="{ background:'#F8FAFF', color:'#2C3E50', textAlign:'center' }"
        :cell-style="{ textAlign:'center', padding:'6px 0' }">
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column label="时间" min-width="140">
          <template slot-scope="{row}">{{ formatTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="100">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next"
        :total="total" :page-size="pageSize" :current-page="pageNum"
        @current-change="p=>{pageNum=p;fetch()}" style="margin-top:12px;text-align:center;" />
      <div v-if="!loading && total===0" style="text-align:center;padding:40px 0;color:#C0C4CC;">暂无日记数据</div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="日记详情" :visible.sync="detailVisible" width="600px" :close-on-click-modal="true" top="6vh">
      <div v-if="detail">
        <h3 style="margin:0 0 10px;font-size:17px;color:#2C3E50;">{{ detail.title || '(无标题)' }}</h3>
        <div v-if="detail.imageUrl" style="margin-bottom:12px;">
          <img :src="detail.imageUrl" style="max-width:100%;max-height:300px;border-radius:8px;cursor:pointer;" @click="previewImage(detail.imageUrl)" />
        </div>
        <p style="white-space:pre-wrap;line-height:1.8;color:#4A5568;margin:0 0 14px;font-size:14px;">{{ detail.content }}</p>
        <div style="font-size:12px;color:#A0AEC0;border-top:1px solid #F0F5FF;padding-top:12px;">
          <span>👤 {{ detail.username }}</span>
          <span style="margin-left:20px;">🕐 {{ detail.create_time }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminDiaryList, deleteAdminDiary } from '@/api/admin/manage'

export default {
  name: 'DiaryManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:10, loading:false,
      filters: { keyword:'' },
      dateRange: null,
      detailVisible:false, detail:null,
      statCards: [
        { icon:'📝', label:'今日新增', value:0, bg:'#EBF5FF' },
        { icon:'⚠️', label:'低分用户(7d)', value:0, bg:'#FFF1F0' },
        { icon:'📊', label:'7天平均分', value:0, bg:'#F0EFFF' },
        { icon:'🔴', label:'严重低分', value:0, bg:'#FFF7E6' }
      ]
    }
  },
  created() { this.fetch() },
  methods: {
    fetch() {
      this.loading=true
      const params = { page:this.pageNum, size:this.pageSize }
      if (this.filters.keyword) params.keyword = this.filters.keyword
      if (this.dateRange) {
        params.startDate = this.dateRange[0].toISOString().substring(0,10)
        params.endDate = this.dateRange[1].toISOString().substring(0,10)
      }
      getAdminDiaryList(params).then(r => {
        const d = r.data || {}
        this.list = d.records || []
        this.total = d.total || 0
        const s = d.stats || {}
        this.statCards = [
          { icon:'📝', label:'今日新增', value:s.todayCount||0, bg:'#EBF5FF' },
          { icon:'⚠️', label:'低分用户(7d)', value:s.highRiskCount||0, bg:'#FFF1F0' },
          { icon:'📊', label:'7天平均分', value:s.avgScore7d||0, bg:'#F0EFFF' },
          { icon:'🔴', label:'严重低分', value:s.lowScoreCount||0, bg:'#FFF7E6' }
        ]
      }).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetch()},
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      if (isNaN(d.getTime())) return t
      const Y = d.getFullYear()
      const M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0')
      return Y + '-' + M + '-' + D + ' ' + h + ':' + m
    },
    previewImage(url) { window.open(url, '_blank') },
    showDetail(row){this.detail=row;this.detailVisible=true},
    handleDelete(id) {
      this.$confirm('确定删除？','提示',{type:'warning'}).then(()=>{
        deleteAdminDiary(id).then(()=>{this.$message.success('已删除');this.fetch()})
      }).catch(()=>{})
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
</style>
