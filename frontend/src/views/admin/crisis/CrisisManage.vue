<!-- 危机预警管理 -->
<template>
  <div class="page-container">
    <h2 class="page-title">🚨 危机预警管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px;">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="never" :body-style="{ padding: '16px 20px' }">
          <div style="display:flex;align-items:center;gap:12px;">
            <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
            <div>
              <div style="font-size:20px;font-weight:700;color:#2C3E50;">{{ s.value }}</div>
              <div style="font-size:12px;color:#909399;margin-top:2px;">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区 -->
    <el-card shadow="never" style="margin-bottom:16px;">
      <el-row :gutter="12">
        <el-col :span="5"><el-input v-model="filters.keyword" placeholder="用户名" size="small" clearable @clear="search" @keyup.enter.native="search" /></el-col>
        <el-col :span="4">
          <el-select v-model="filters.level" placeholder="风险等级" size="small" clearable @change="search" style="width:100%;">
            <el-option label="高危" value="4" /><el-option label="中危" value="3" /><el-option label="关注" value="2" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.status" placeholder="状态" size="small" clearable @change="search" style="width:100%;">
            <el-option label="待处理" value="PENDING" /><el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" /><el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="~" size="small"
            start-placeholder="开始" end-placeholder="结束" style="width:100%;" @change="search" />
        </el-col>
        <el-col :span="3" style="text-align:right;">
          <el-button size="small" type="primary" icon="el-icon-search" @click="search">搜索</el-button>
          <el-button size="small" @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;" size="small"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
        :cell-style="{textAlign:'center',padding:'6px 0'}"
        :row-class-name="r => riskRowClass(r, list)">
        <el-table-column prop="username" label="用户" min-width="100" />
        <el-table-column label="风险等级" width="100">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="alertType(row.alertLevel)" effect="dark">{{ alertLevel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发关键词" width="120">
          <template slot-scope="{row}">
            <el-tag size="mini" type="danger" v-if="extractKeyword(row.triggerReason)">{{ extractKeyword(row.triggerReason) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="80">
          <template slot-scope="{row}">{{ row.triggerReason && row.triggerReason.includes('AI') ? 'AI对话' : '日记' }}</template>
        </el-table-column>
        <el-table-column label="触发内容" min-width="220" show-overflow-tooltip>
          <template slot-scope="{row}">{{ row.triggerReason }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="statusType(row.handleStatus)">{{ statusText(row.handleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="140">
          <template slot-scope="{row}">{{ fmt(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.handleStatus === 'PENDING' || row.handleStatus === 'PROCESSING'"
              type="text" size="mini" style="color:#E6A23C;" @click="showHandle(row)">标记处理</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="p=>{pageNum=p;fetchList()}" style="margin-top:12px;text-align:center;" />
      <div v-if="!loading && total===0" style="text-align:center;padding:40px 0;color:#C0C4CC;">暂无预警数据</div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="预警详情" :visible.sync="detailVisible" width="600px" :close-on-click-modal="true">
      <div v-if="detail">
        <div style="margin-bottom:14px;display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
          <el-tag size="small" :type="alertType(detail.alertLevel)" effect="dark">{{ alertLevel(detail.alertLevel) }}</el-tag>
          <el-tag size="small" effect="plain">{{ statusText(detail.handleStatus) }}</el-tag>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户" v-if="detail.username">{{ detail.username }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ alertLevel(detail.alertLevel) }}</el-descriptions-item>
          <el-descriptions-item label="触发时间" :span="2">{{ fmt(detail.create_time) }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ detail.emergencyContact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.emergencyPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">{{ detail.handleRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:14px;">
          <div style="font-size:13px;font-weight:600;color:#606266;margin-bottom:6px;">触发内容</div>
          <p style="white-space:pre-wrap;line-height:1.6;color:#4A5568;font-size:13px;background:#F8FAFF;padding:12px;border-radius:8px;">{{ detail.triggerReason }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 标记处理弹窗 -->
    <el-dialog title="标记已处理" :visible.sync="handleVisible" width="450px" :close-on-click-modal="false">
      <el-input v-model="handleRemark" type="textarea" :rows="4" placeholder="输入处理说明..." />
      <div slot="footer">
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handling">确认处理</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCrisisList, handleCrisis, deleteCrisis } from '@/api/crisis'
import request from '@/utils/request'

const RISK_KEYS = ['自杀','想死','不想活','活不下去','结束生命','崩溃','绝望','伤害自己','自残']

export default {
  name: 'CrisisManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:10, loading:false,
      filters:{ keyword:'', level:'', status:'' },
      dateRange:null,
      statCards:[
        { icon:'⏳', label:'待处理', value:0, bg:'#FFF1F0' },
        { icon:'📝', label:'今日新增', value:0, bg:'#FFF7E6' },
        { icon:'🔴', label:'高危预警', value:0, bg:'#FDEEEE' },
        { icon:'✅', label:'已处理', value:0, bg:'#E8F8F0' }
      ],
      detailVisible:false, detail:null,
      handleVisible:false, handleRemark:'', currentAlert:null, handling:false
    }
  },
  created(){this.fetchList()},
  methods: {
    fetchList(){
      this.loading=true
      const p={pageNum:this.pageNum,pageSize:this.pageSize}
      if(this.filters.keyword)p.keyword=this.filters.keyword
      if(this.filters.status)p.status=this.filters.status
      if(this.dateRange){
        p.startDate=this.dateRange[0].toISOString().substring(0,10)
        p.endDate=this.dateRange[1].toISOString().substring(0,10)
      }
      request.get('/crisis/list',{params:p}).then(r=>{
        const d=r.data||{}
        const records=d.records||[]
        this.list=records
        this.total=d.total||0
        // 从返回数据计算统计
        const allRec=d.records||[]
        const pending=allRec.filter(x=>x.handleStatus==='PENDING').length
        const todayStr=new Date().toISOString().substring(0,10)
        const todayNew=allRec.filter(x=>x.create_time&&String(x.create_time).substring(0,10)===todayStr).length
        const high=allRec.filter(x=>x.alertLevel>=4).length
        const resolved=allRec.filter(x=>x.handleStatus==='RESOLVED').length
        this.statCards=[
          { icon:'⏳', label:'待处理', value:pending, bg:'#FFF1F0' },
          { icon:'📝', label:'今日新增', value:todayNew, bg:'#FFF7E6' },
          { icon:'🔴', label:'高危预警', value:high, bg:'#FDEEEE' },
          { icon:'✅', label:'已处理', value:resolved, bg:'#E8F8F0' }
        ]
      }).catch(()=>{
        this.$message.error('加载预警数据失败')
      }).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetchList()},
    resetSearch(){this.filters={keyword:'',level:'',status:''};this.dateRange=null;this.pageNum=1;this.fetchList()},
    extractKeyword(s){if(!s)return'';for(const kw of RISK_KEYS){if(s.includes(kw))return kw}return''},
    alertLevel(l){if(l>=4)return'高危';if(l>=3)return'中危';return'关注'},
    alertType(l){if(l>=4)return'danger';if(l>=3)return'warning';return'info'},
    statusType(s){const m={'PENDING':'danger','PROCESSING':'warning','RESOLVED':'success','CLOSED':'info'};return m[s]||'info'},
    statusText(s){const m={'PENDING':'待处理','PROCESSING':'处理中','RESOLVED':'已解决','CLOSED':'已关闭'};return m[s]||s},
    riskRowClass({row}){if(!row)return'';if(row.alertLevel>=4)return'risk-row-danger';if(row.alertLevel>=3)return'risk-row-orange';return''},
    fmt(t){if(!t)return'';const d=new Date(t);if(isNaN(d.getTime()))return t;return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')+' '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')},
    showDetail(row){this.detail=row;this.detailVisible=true},
    showHandle(row){this.currentAlert=row;this.handleRemark='';this.handleVisible=true},
    submitHandle(){
      this.handling=true
      handleCrisis(this.currentAlert.id,this.handleRemark||'已处理').then(()=>{
        this.$message.success('已标记处理');this.handleVisible=false;this.fetchList()
      }).catch(()=>{
        this.$message.error('处理失败，请重试')
      }).finally(()=>{this.handling=false})
    },
    handleDelete(id){
      this.$confirm('确定删除此预警？','提示',{type:'warning'}).then(()=>{
        deleteCrisis(id).then(()=>{this.$message.success('已删除');this.fetchList()})
        .catch(()=>{this.$message.error('删除失败')})
      }).catch(()=>{})
    }
  }
}
</script>
<style scoped>
.stat-icon{width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:18px;flex-shrink:0;}
</style>
