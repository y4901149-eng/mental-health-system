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
            <el-option label="待处理" value="PENDING" /><el-option label="已解决" value="RESOLVED" />
          </el-select>
        </el-col>
        <el-col :span="6">
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
        :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}"
        :row-class-name="r => riskRowClass(r, list)">
        <el-table-column label="用户" width="120">
          <template slot-scope="{row}">{{ row.username || '用户' + row.userId }}</template>
        </el-table-column>
        <el-table-column label="风险等级" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="alertType(row.alertLevel)" effect="dark">{{ alertLevel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发关键词" width="120" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" type="danger" v-if="getKeyword(row.triggerReason)">{{ getKeyword(row.triggerReason) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="90" align="center">
          <template slot-scope="{row}">{{ row.triggerReason && row.triggerReason.includes('AI对话') ? 'AI对话' : '日记' }}</template>
        </el-table-column>
        <el-table-column label="触发内容" min-width="140" show-overflow-tooltip>
          <template slot-scope="{row}">{{ row.triggerReason }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="statusType(row.handleStatus)">{{ statusText(row.handleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="监护人通知" width="110" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.guardianNotified === 1 ? 'success' : 'danger'">
              {{ row.guardianNotified === 1 ? '已通知' : '未通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180" align="center">
          <template slot-scope="{row}">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.guardianNotified !== 1" type="text" size="mini" style="color:#409EFF;" @click="showNotify(row)">通知</el-button>
            <el-button v-if="row.handleStatus === 'PENDING' || row.handleStatus === 'PROCESSING'"
              type="text" size="mini" style="color:#67C23A;" @click="showHandle(row)">标记解决</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="p=>{pageNum=p;fetchList()}" style="margin-top:12px;text-align:center;" />
      <div v-if="!loading && total===0" style="text-align:center;padding:40px 0;color:#C0C4CC;">暂无预警数据</div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="预警详情" :visible.sync="detailVisible" width="650px" top="5vh" :close-on-click-modal="true">
      <div v-if="detail" class="detail-body">
        <!-- 头部标签 -->
        <div style="margin-bottom:16px;display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
          <el-tag size="small" :type="alertType(detail.alertLevel)" effect="dark">{{ alertLevel(detail.alertLevel) }}</el-tag>
          <el-tag size="small" effect="plain">{{ statusText(detail.handleStatus) }}</el-tag>
          <el-tag size="small" color="#EBF5FF" effect="plain" v-if="detail.triggerReason && detail.triggerReason.includes('AI对话')">来源：AI对话</el-tag>
          <el-tag size="small" color="#E8F8F0" effect="plain" v-else>来源：日记</el-tag>
        </div>

        <!-- 基本信息 -->
        <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:10px;">📋 基本信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户">{{ detail.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ alertLevel(detail.alertLevel) }}</el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ fmt(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ detail.emergencyContact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.emergencyPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ detail.handleRemark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 触发关键词 -->
        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">🔑 触发关键词</div>
          <el-tag size="small" type="danger" v-if="getKeyword(detail.triggerReason)" style="margin-right:4px;">{{ getKeyword(detail.triggerReason) }}</el-tag>
          <span v-else style="color:#909399;font-size:13px;">-</span>
        </div>

        <!-- 触发内容 -->
        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">💬 触发内容</div>
          <div class="detail-card">{{ detail.triggerReason || '无' }}</div>
        </div>

        <!-- AI 分析结果（mock） -->
        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">🤖 AI 分析结果</div>
          <div class="detail-card ai-card">
            <p style="margin:0;line-height:1.7;">根据对话内容分析，用户当前情绪状态为「<b style="color:#F56C6C;">{{ alertLevel(detail.alertLevel) }}风险</b>」，情绪评分低于正常阈值。</p>
            <p style="margin:8px 0 0;line-height:1.7;">检测到用户多次提及「{{ getKeyword(detail.triggerReason) || '负面情绪' }}」相关词汇，存在潜在心理危机，建议及时干预。</p>
          </div>
        </div>

        <!-- 建议处理方案（mock） -->
        <!-- 监护人通知状态 -->
        <div style="margin-top:16px;padding:12px;background:#F8FAFF;border-radius:8px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">📞 监护人通知状态</div>
          <el-tag size="small" :type="detail.guardianNotified === 1 ? 'success' : 'danger'">
            {{ detail.guardianNotified === 1 ? '✅ 已通知监护人' : '❌ 未通知监护人' }}
          </el-tag>
          <template v-if="detail.guardianNotified === 1">
            <div style="margin-top:8px;font-size:13px;color:#606266;">
              <div>通知方式：{{ detail.guardianNotifyMethod || '-' }}</div>
              <div>通知时间：{{ detail.guardianNotifyTime ? fmt(detail.guardianNotifyTime) : '-' }}</div>
              <div>通知备注：{{ detail.guardianNotifyRemark || '-' }}</div>
            </div>
          </template>
        </div>

        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">📌 建议处理方案</div>
          <div class="detail-card suggestion-card">
            <p style="margin:0;line-height:1.7;">1. 立即联系用户或其紧急联系人（{{ detail.emergencyContact || '未设置' }}）</p>
            <p style="margin:8px 0 0;line-height:1.7;">2. 建议用户拨打心理援助热线 400-161-9995</p>
            <p style="margin:8px 0 0;line-height:1.7;">3. 安排专业心理咨询师进行干预</p>
            <p style="margin:8px 0 0;line-height:1.7;">4. 持续跟踪用户情绪状态，必要时建议就医</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 标记通知监护人弹窗 -->
    <el-dialog title="标记已通知监护人" :visible.sync="notifyVisible" width="450px" :close-on-click-modal="false">
      <el-form :model="notifyForm" label-position="top" size="small">
        <el-form-item label="通知方式">
          <el-select v-model="notifyForm.method" placeholder="选择通知方式" style="width:100%;">
            <el-option label="电话" value="电话" />
            <el-option label="微信" value="微信" />
            <el-option label="面谈" value="面谈" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知时间">
          <el-date-picker v-model="notifyForm.time" type="datetime" placeholder="选择通知时间" style="width:100%;" />
        </el-form-item>
        <el-form-item label="通知备注">
          <el-input v-model="notifyForm.remark" type="textarea" :rows="3" placeholder="输入通知备注（选填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="notifyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotify" :loading="notifying">确认已通知</el-button>
      </div>
    </el-dialog>

    <!-- 标记解决弹窗 -->
    <el-dialog title="标记已解决" :visible.sync="handleVisible" width="450px" :close-on-click-modal="false">
      <el-input v-model="handleRemark" type="textarea" :rows="4" placeholder="输入处理说明..." />
      <div slot="footer">
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handling">确认解决</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { handleCrisis, deleteCrisis } from '@/api/crisis'
import request from '@/utils/request'
import { extractKeyword, alertLevelText, alertLevelType } from '@/utils/crisisKeywords'

const nowISO = () => new Date().toISOString()

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
      handleVisible:false, handleRemark:'', currentAlert:null, handling:false,
      notifyVisible:false, notifying:false,
      notifyForm:{ method:'电话', time:null, remark:'' }
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
      request.get('/crisis/list',{params:p}).then(resp=>{
        // resp = {code, message, data: {records, total, ...}}
        // 兼容 axios 响应和直接 JSON 两种结构
        const body = resp && resp.data ? resp : { data: resp }
        const d = (body && body.data) || {}
        let records = (d && d.records) || []
        // 本地过滤（后端不支持 keyword/level/dateRange 参数）
        if (this.filters.keyword) {
          const kw = this.filters.keyword.toLowerCase()
          records = records.filter(x => (x.username || '').toLowerCase().includes(kw))
        }
        if (this.filters.level) {
          const lv = parseInt(this.filters.level)
          records = records.filter(x => (x.alertLevel || 0) >= lv)
        }
        if (this.dateRange && this.dateRange[0] && this.dateRange[1]) {
          const sd = this.dateRange[0].toISOString()
          const ed = this.dateRange[1].toISOString()
          records = records.filter(x => {
            const t = x.createTime || ''
            return t >= sd && t <= ed
          })
        }
        this.list = records.slice()
        this.total = records.length
        const allRec = records
        const todayStr=new Date().toISOString().substring(0,10)
        const pending=allRec.filter(x=>x.handleStatus==='PENDING').length
        const todayNew=allRec.filter(x=>x.createTime&&String(x.createTime).substring(0,10)===todayStr).length
        const high=allRec.filter(x=>x.alertLevel>=4).length
        const resolved=allRec.filter(x=>x.handleStatus==='RESOLVED').length
        this.statCards=[
          { icon:'⏳', label:'待处理', value:pending, bg:'#FFF1F0' },
          { icon:'📝', label:'今日新增', value:todayNew, bg:'#FFF7E6' },
          { icon:'🔴', label:'高危预警', value:high, bg:'#FDEEEE' },
          { icon:'✅', label:'已处理', value:resolved, bg:'#E8F8F0' }
        ]
      }).catch(e=>{
        console.log('=== 预警加载错误 ===', e && e.response ? e.response.status : e)
      }).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetchList()},
    resetSearch(){this.filters={keyword:'',level:'',status:''};this.dateRange=null;this.pageNum=1;this.fetchList()},
    getKeyword(s){return extractKeyword(s)},
    alertLevel(l){return alertLevelText(l)},
    alertType(l){return alertLevelType(l)},
    statusType(s){const m={'PENDING':'danger','PROCESSING':'warning','RESOLVED':'success'};return m[s]||'info'},
    statusText(s){const m={'PENDING':'待处理','PROCESSING':'待处理','RESOLVED':'已解决'};return m[s]||s},
    riskRowClass({row}){if(!row)return'';if(row.alertLevel>=4)return'risk-row-danger';if(row.alertLevel>=3)return'risk-row-orange';return''},
    fmt(t){if(!t)return'';const d=new Date(t);if(isNaN(d.getTime()))return t;return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')+' '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')},
    showDetail(row){this.detail=row;this.detailVisible=true},
    showHandle(row){this.currentAlert=row;this.handleRemark='';this.handleVisible=true},
    showNotify(row){
      this.currentAlert=row
      this.notifyForm={ method:'电话', time:new Date(), remark:'' }
      this.notifyVisible=true
    },
    submitNotify(){
      this.notifying=true
      const body={ id:this.currentAlert.id, notifyMethod:this.notifyForm.method, notifyRemark:this.notifyForm.remark }
      if (this.notifyForm.time) body.notifyTime = new Date(this.notifyForm.time).toISOString()
      request.post('/crisis/notify', body).then(()=>{
        this.$message.success('已标记通知监护人')
        this.notifyVisible=false
        this.fetchList()
      }).catch(()=>{this.$message.error('操作失败')}).finally(()=>{this.notifying=false})
    },
    submitHandle(){
      this.handling=true
      handleCrisis(this.currentAlert.id,this.handleRemark||'已处理').then(()=>{
        this.$message.success('已标记为解决');this.handleVisible=false;this.fetchList()
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
.detail-body{max-height:70vh;overflow-y:auto;padding-right:4px;}
.detail-card{background:#F8FAFF;border-radius:8px;padding:12px 14px;font-size:13px;color:#4A5568;line-height:1.6;white-space:pre-wrap;}
.ai-card{background:#F0F5FF;border-left:3px solid #409EFF;}
.suggestion-card{background:#FFF8F0;border-left:3px solid #E6A23C;}
</style>
