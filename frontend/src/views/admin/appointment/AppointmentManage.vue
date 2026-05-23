<!-- 管理员/心理师 - 预约管理 -->
<template>
  <div class="page-container">
    <h2 class="page-title">📅 预约管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px;">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" :body-style="{ padding: '16px 20px' }">
          <div style="display:flex;align-items:center;gap:12px;">
            <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
            <div>
              <div style="font-size:20px;font-weight:700;color:#2C3E50;">{{ s.value }}</div>
              <div style="font-size:12px;color:#909399;">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区 -->
    <el-card shadow="never" style="margin-bottom:16px;">
      <el-row :gutter="12">
        <el-col :span="6"><el-input v-model="filters.keyword" placeholder="用户名" size="small" clearable @clear="search" @keyup.enter.native="search" /></el-col>
        <el-col :span="4"><el-input v-model="filters.counselor" placeholder="心理师" size="small" clearable @clear="search" @keyup.enter.native="search" /></el-col>
        <el-col :span="4">
          <el-select v-model="filters.status" placeholder="状态" size="small" clearable @change="search" style="width:100%;">
            <el-option label="待确认" value="pending" /><el-option label="已确认" value="confirmed" />
            <el-option label="已完成" value="completed" /><el-option label="已取消" value="cancelled" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="~" size="small"
            start-placeholder="开始" end-placeholder="结束" style="width:100%;" @change="search" />
        </el-col>
        <el-col :span="5" style="text-align:right;white-space:nowrap;">
          <el-button size="small" type="primary" icon="el-icon-search" @click="search" style="margin-right:12px;">搜索</el-button>
          <el-button size="small" @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;" size="small"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center',fontSize:'12px'}"
        :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}">
        <el-table-column prop="username" label="用户" min-width="100" />
        <el-table-column prop="counselor_name" label="心理师" min-width="100" />
        <el-table-column prop="appointment_date" label="日期" min-width="90" />
        <el-table-column prop="time_slot" label="时间段" min-width="90" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{row}"><el-tag size="mini" :type="aptType(row.status)">{{ aptText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="咨询主题" min-width="90">
          <template slot-scope="{row}">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template slot-scope="{row}">{{ fmt(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" align="center">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status==='pending'" type="text" size="mini" style="color:#67C23A;font-weight:600;" @click="changeStatus(row.id,'confirmed')">确认</el-button>
            <el-button v-if="row.status!=='cancelled' && row.status!=='completed'" type="text" size="mini" style="color:#E6A23C;font-weight:600;" @click="changeStatus(row.id,'cancelled')">取消</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="p=>{pageNum=p;fetch()}" style="margin-top:12px;text-align:center;" />
      <div v-if="!loading && total===0" style="text-align:center;padding:40px;color:#C0C4CC;">暂无预约数据</div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="预约详情" :visible.sync="detailVisible" width="550px" :close-on-click-modal="true">
      <div v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户">{{ detail.username }}</el-descriptions-item>
          <el-descriptions-item label="心理师">{{ detail.counselor_name }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ detail.appointment_date }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ detail.time_slot }}</el-descriptions-item>
          <el-descriptions-item label="咨询主题">{{ typeText(detail.type) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="mini" :type="aptType(detail.status)">{{ aptText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ fmt(detail.create_time) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <!-- 操作按钮 -->
        <div style="margin-top:16px;text-align:center;border-top:1px solid #F0F4FF;padding-top:16px;">
          <template v-if="detail.status==='pending'">
            <el-button type="success" size="medium" @click="changeStatus(detail.id,'confirmed');detailVisible=false" style="margin-right:12px;">✅ 确认预约</el-button>
            <el-button type="warning" size="medium" @click="changeStatus(detail.id,'cancelled');detailVisible=false">❌ 取消预约</el-button>
          </template>
          <el-tag v-else-if="detail.status==='confirmed'" type="success" size="large">✅ 已确认 — 该预约已完成确认流程</el-tag>
          <el-tag v-else-if="detail.status==='cancelled'" type="danger" size="large">❌ 已取消 — 该预约已被取消</el-tag>
          <el-tag v-else type="info" size="large">已完成</el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AppointmentManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:10, loading:false,
      filters:{ keyword:'', counselor:'', status:'' },
      dateRange:null,
      statCards:[
        { icon:'⏳', label:'待确认', value:0, bg:'#FFF7E6' },
        { icon:'📅', label:'今日预约', value:0, bg:'#EBF5FF' },
        { icon:'✅', label:'已确认', value:0, bg:'#E8F8F0' },
        { icon:'❌', label:'已取消', value:0, bg:'#FFF1F0' }
      ],
      detailVisible:false, detail:null
    }
  },
  created(){this.fetch()},
  methods: {
    fetch(){
      this.loading=true
      const p={page:this.pageNum,size:this.pageSize}
      if(this.filters.status)p.status=this.filters.status
      if(this.filters.keyword)p.keyword=this.filters.keyword
      if(this.filters.counselor)p.counselor=this.filters.counselor
      if(this.dateRange){
        p.startDate=this.dateRange[0].toISOString().substring(0,10)
        p.endDate=this.dateRange[1].toISOString().substring(0,10)
      }
      request.get('/admin/appointment',{params:p}).then(r=>{
        const d=r.data||{}; const rec=d.records||[]
        // 本地过滤（后端不支持 keyword/counselor 参数）
        let filtered = rec
        if (this.filters.keyword) {
          const kw = this.filters.keyword.toLowerCase()
          filtered = filtered.filter(x => (x.username || '').toLowerCase().includes(kw))
        }
        if (this.filters.counselor) {
          const c = this.filters.counselor.toLowerCase()
          filtered = filtered.filter(x => (x.counselor_name || '').toLowerCase().includes(c))
        }
        this.list = filtered; this.total = filtered.length
        this.statCards=[
          { icon:'⏳', label:'待确认', value:rec.filter(x=>x.status==='pending').length, bg:'#FFF7E6' },
          { icon:'📅', label:'今日预约', value:rec.filter(x=>x.appointment_date===new Date().toISOString().substring(0,10)).length, bg:'#EBF5FF' },
          { icon:'✅', label:'已确认', value:rec.filter(x=>x.status==='confirmed').length, bg:'#E8F8F0' },
          { icon:'❌', label:'已取消', value:rec.filter(x=>x.status==='cancelled').length, bg:'#FFF1F0' }
        ]
      }).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetch()},
    resetSearch(){this.filters={keyword:'',counselor:'',status:''};this.dateRange=null;this.pageNum=1;this.fetch()},
    changeStatus(id,s){
      request.put('/admin/appointment/'+id+'/status',{status:s}).then(()=>{
        this.$message.success('状态已更新');this.fetch()
      })
    },
    handleDelete(id){
      this.$confirm('确定删除此预约？','提示',{type:'warning'}).then(()=>{
        request.delete('/admin/appointment/'+id).then(()=>{this.$message.success('已删除');this.fetch()})
      }).catch(()=>{})
    },
    showDetail(row){this.detail=row;this.detailVisible=true},
    typeText(t){const m={individual:'个体咨询',group:'团体咨询'};return m[t]||t||'-'},
    aptType(s){const m={pending:'warning',confirmed:'success',completed:'info',cancelled:'danger'};return m[s]||'info'},
    aptText(s){const m={pending:'待确认',confirmed:'已确认',completed:'已完成',cancelled:'已取消'};return m[s]||s},
    fmt(t){if(!t)return'';const d=new Date(t);if(isNaN(d.getTime()))return t;return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')+' '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')}
  }
}
</script>
<style scoped>
.stat-icon{width:36px;height:36px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:17px;flex-shrink:0;}
</style>
