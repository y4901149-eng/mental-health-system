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
        <el-table-column label="判定方式" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="primaryTagType(row.triggerReason)" effect="dark" style="font-size:11px;">{{ primaryTagText(row.triggerReason) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100" align="center">
          <template slot-scope="{row}">
            <span v-if="row.triggerReason && row.triggerReason.includes('AI对话')">AI对话</span>
            <span v-else-if="row.triggerReason && row.triggerReason.includes('情绪分触发')">情绪记录</span>
            <span v-else-if="row.triggerReason && row.triggerReason.includes('系统自动')">系统自动</span>
            <span v-else>日记</span>
          </template>
        </el-table-column>
        <el-table-column label="触发内容" min-width="160" show-overflow-tooltip>
          <template slot-scope="{row}">
            <span v-if="row.triggerReason && row.triggerReason.includes('情绪分触发')">
              最近7天情绪持续偏低 ({{ (row.triggerReason.match(/情绪分:([\d.]+)/) || ['',''])[1] }}分)
            </span>
            <span v-else>{{ row.triggerReason ? formatTriggerReason(row.triggerReason) : '-' }}</span>
          </template>
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
            <el-button type="text" size="mini" style="color:#409EFF;" @click="showHandle(row)">处理</el-button>
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
          <el-tag size="small" color="#FFF7E6" effect="plain" v-else-if="detail.triggerReason && detail.triggerReason.includes('情绪分触发')">来源：情绪记录</el-tag>
          <el-tag size="small" color="#E8F8F0" effect="plain" v-else>来源：日记</el-tag>
        </div>

        <!-- 基本信息 -->
        <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:10px;">📋 基本信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户">{{ detail.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ alertLevel(detail.alertLevel) }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.triggerReason && detail.triggerReason.includes('AI对话') ? 'AI对话' : detail.triggerReason && detail.triggerReason.includes('情绪分触发') ? '情绪记录' : detail.triggerReason && detail.triggerReason.includes('系统自动') ? '系统自动' : '日记' }}</el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ fmt(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="默认联系人">
            <span v-if="detail._defaultContact">{{ detail._defaultContact.name }}（{{ detail._defaultContact.relation }}）{{ detail._defaultContact.phone }}</span>
            <span v-else-if="detail.emergencyContact">{{ detail.emergencyContact }}</span>
            <span v-else style="color:#C0C4CC;">未设置默认联系人</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 判定结果（按 | 分隔独立展示） -->
        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:10px;">🔍 判定结果</div>
          <div style="background:#F8FAFF;border-radius:8px;border:1px solid #E8ECF4;padding:12px 14px;font-size:13px;line-height:1.8;">
            <div v-for="(line, i) in parseAnalysis(detail.triggerReason)" :key="i" style="display:flex;align-items:baseline;gap:6px;padding:3px 0;">
              <span v-if="line.level && line.level>0"
                :style="{display:'inline-block',width:'8px',height:'8px',borderRadius:'50%',background:line.level>=4?'#F56C6C':line.level>=3?'#E6A23C':'#909399',flexShrink:0}"></span>
              <span style="color:#909399;min-width:76px;font-size:12px;">{{ line.label }}</span>
              <span :style="{fontWeight:line.bold?'600':'400',color:line.level&&line.level>0?'#2C3E50':'#B0B8C5'}">{{ line.text }}</span>
            </div>
            <div style="border-top:1px solid #E8ECF4;margin:6px 0;"></div>
            <div style="font-size:12px;color:#909399;">
              原文: {{ detail._originalContent || extractOriginalText(detail.triggerReason) }}
            </div>
          </div>
        </div>

        <!-- 建议处理方案（mock） -->
        <!-- 监护人通知记录 -->
        <div style="margin-top:16px;padding:12px;background:#F8FAFF;border-radius:8px;">
          <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">📞 通知记录</div>
          <el-tag size="small" :type="detail.guardianNotified === 1 ? 'success' : 'danger'">
            {{ detail.guardianNotified === 1 ? '✅ 已通知' : '❌ 未通知' }}
          </el-tag>
          <template v-if="detail.guardianNotified === 1 && detail.guardianNotifyRemark">
            <div v-for="(log, i) in parseNotifyLog(detail.guardianNotifyRemark)" :key="i"
              style="background:#fff;border:1px solid #E8ECF4;border-radius:8px;padding:10px 14px;margin-top:10px;font-size:13px;line-height:1.7;color:#2C3E50;">
              <div style="font-weight:600;color:#409EFF;margin-bottom:2px;">{{ log.time }}</div>
              <div><span style="color:#606266;">方式：</span>{{ log.method }}</div>
              <div><span style="color:#606266;">联系人：</span>{{ log.contactName }} <span style="color:#909399;font-size:12px;">（{{ log.relation }}）</span></div>
              <div><span style="color:#606266;">电话：</span>{{ log.phone }}</div>
              <div v-if="log.remark"><span style="color:#606266;">备注：</span>{{ log.remark }}</div>
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

    <!-- 统一处理弹窗（通知+标记解决分离） -->
    <el-dialog title="预警处理" :visible.sync="handleVisible" width="580px" :close-on-click-modal="false" top="4vh">
      <div style="max-height:65vh;overflow-y:auto;padding-right:6px;">

        <!-- ===== 通知记录 ===== -->
        <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">📞 通知记录</div>
        <div v-if="handleNotifyLog.length === 0" style="color:#C0C4CC;font-size:13px;margin-bottom:14px;padding:8px 0;">
          暂无通知记录
        </div>
        <div v-for="(log, i) in handleNotifyLog" :key="i"
          style="background:#F8FAFF;border-radius:6px;padding:8px 12px;margin-bottom:6px;font-size:13px;color:#2C3E50;line-height:1.6;border:1px solid #E8ECF4;">
          {{ log }}
        </div>

        <!-- ===== 分割线 ===== -->
        <div style="border-top:1px solid #E8ECF4;margin:14px 0;"></div>

        <!-- ===== 通知联系人 ===== -->
        <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:8px;">📞 通知联系人</div>
        <div v-if="handleContacts.length === 0" style="color:#909399;font-size:13px;margin-bottom:14px;">
          该用户暂无紧急联系人。
        </div>
        <div v-else style="margin-bottom:12px;">
          <div v-for="c in handleContacts" :key="c.id"
            style="display:flex;align-items:center;justify-content:space-between;padding:8px 12px;border:1px solid #E8ECF4;border-radius:8px;margin-bottom:6px;">
            <div>
              <strong>{{ c.name }}</strong>
              <span style="color:#909399;font-size:12px;margin-left:8px;">{{ c.relation }}</span>
              <el-tag v-if="c.isDefault === 1" size="mini" type="success" style="margin-left:6px;">默认</el-tag>
              <div style="font-size:12px;color:#909399;margin-top:2px;">{{ c.phone }}</div>
            </div>
            <el-button type="primary" size="mini" @click="notifyContact(c)" :loading="notifyingContactId === c.id">通知</el-button>
          </div>
        </div>

        <!-- ===== 通知方式（当前联系人用） ===== -->
        <div v-if="handleContacts.length > 0" style="display:flex;gap:12px;margin-bottom:10px;">
          <div style="flex:1;">
            <div style="font-size:12px;color:#606266;margin-bottom:4px;">通知方式</div>
            <el-select v-model="handleNotifyMethod" placeholder="选择方式" style="width:100%;" size="small">
              <el-option label="电话" value="电话" />
              <el-option label="短信" value="短信" />
              <el-option label="站内通知" value="站内通知" />
            </el-select>
          </div>
          <div style="flex:1;">
            <div style="font-size:12px;color:#606266;margin-bottom:4px;">通知备注</div>
            <el-input v-model="handleNotifyRemark" placeholder="备注（选填）" size="small" />
          </div>
        </div>

        <!-- ===== 分割线 ===== -->
        <div style="border-top:1px solid #E8ECF4;margin:14px 0;"></div>

        <!-- ===== 处理区 ===== -->
        <div style="font-size:14px;font-weight:600;color:#2C3E50;margin-bottom:12px;">📌 预警处理</div>
        <el-checkbox v-model="handleMarkResolved" style="margin-bottom:12px;display:flex;">
          {{ handleAlert && handleAlert.handleStatus === 'RESOLVED' ? '撤销已解决，恢复待处理' : '标记为已解决' }}
        </el-checkbox>
        <div style="margin-bottom:8px;">
          <div style="font-size:12px;color:#606266;margin-bottom:4px;">处理备注</div>
          <el-input v-model="handleRemark" type="textarea" :rows="3" placeholder="输入处理说明（选填）" />
        </div>
      </div>
      <div slot="footer" style="display:flex;justify-content:flex-end;gap:8px;">
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handling">确认处理</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCrisisList, handleCrisis, deleteCrisis, notifyGuardian, getEmergencyContacts } from '@/api/crisis'
import { extractKeyword, alertLevelText, alertLevelType } from '@/utils/crisisKeywords'

export default {
  name: 'CrisisManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:10, loading:false,
      filters:{ keyword:'', level:'', status:'', userId:null },
      dateRange:null,
      statCards:[
        { icon:'⏳', label:'待处理', value:0, bg:'#FFF1F0' },
        { icon:'📝', label:'今日新增', value:0, bg:'#FFF7E6' },
        { icon:'🔴', label:'高危预警', value:0, bg:'#FDEEEE' },
        { icon:'✅', label:'已处理', value:0, bg:'#E8F8F0' }
      ],
      detailVisible:false, detail:null,
      handleVisible:false, handling:false,
      handleAlert:null,
      handleContacts:[], handleNotifyLog:[],
      notifyingContactId:null,
      handleNotifyMethod:'电话', handleNotifyRemark:'',
      handleMarkResolved:false, handleRemark:''
    }
  },
  created(){
    const q = this.$route.query
    if (q.userId) this.filters.userId = parseInt(q.userId)
    this.fetchList()
  },
  watch: {
    '$route.query.userId': function() { this.filters.userId = parseInt(this.$route.query.userId); this.search() }
  },
  methods: {
    fetchList(){
      this.loading=true
      const p={pageNum:this.pageNum,pageSize:this.pageSize}
      if(this.filters.userId)p.userId=this.filters.userId
      if(this.filters.keyword)p.keyword=this.filters.keyword
      if(this.filters.status)p.status=this.filters.status
      if(this.dateRange){
        p.startDate=this.dateRange[0].toISOString().substring(0,10)
        p.endDate=this.dateRange[1].toISOString().substring(0,10)
      }
      getCrisisList(p).then(resp=>{
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
          records = records.filter(x => this.alertLevel(x.alertLevel) === this.alertLevel(lv))
        }
        if (this.dateRange && this.dateRange[0] && this.dateRange[1]) {
          const sd = this.dateRange[0].toISOString()
          const ed = this.dateRange[1].toISOString()
          records = records.filter(x => {
            const t = x.createTime || ''
            return t >= sd && t <= ed
          })
        }
        // 过滤：没有有效判定结果的记录不显示
        records = records.filter(x => {
          const r = x.triggerReason || ''
          return r.match(/关键词:.+\(\d+级\)/) || r.includes('AI识别') || (r.includes('情绪分:') && !r.includes('情绪分:0(0级)'))
        })
        // 去重：每用户只保留风险等级最高、时间最新的那条
        const userMap = {}
        records.forEach(x => {
          const uid = x.userId
          if (!userMap[uid] || x.alertLevel > userMap[uid].alertLevel ||
              (x.alertLevel === userMap[uid].alertLevel && x.createTime > userMap[uid].createTime)) {
            userMap[uid] = x
          }
        })
        const deduped = Object.values(userMap)
        // 排序：高危 > 中危 > 关注，同级按时间倒序
        deduped.sort((a,b)=>{
          const ra=a.alertLevel>=4?0:a.alertLevel===3?1:2
          const rb=b.alertLevel>=4?0:b.alertLevel===3?1:2
          if(ra!==rb)return ra-rb
          return (b.createTime||'').localeCompare(a.createTime||'')
        })
        this.list = deduped
        this.total = deduped.length
        const todayStr=new Date().toISOString().substring(0,10)
        const pending=records.filter(x=>x.handleStatus==='PENDING').length
        const todayNew=records.filter(x=>x.createTime&&String(x.createTime).substring(0,10)===todayStr).length
        const high=records.filter(x=>x.alertLevel>=4).length
        const resolved=records.filter(x=>x.handleStatus==='RESOLVED').length
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
    resetSearch(){this.filters={keyword:'',level:'',status:'',userId:null};this.dateRange=null;this.pageNum=1;this.fetchList()},
    getKeyword(s){return extractKeyword(s)},
    // 主要判定方式：关键词 > AI分析 > 情绪趋势 > 频率升级
    // 等级数字转文字
    levelText(lv){return lv>=4?'高危':lv>=3?'中危':'关注'},
    // 从 triggerReason 提取等级数值
    parseLevel(reason, pattern){
      if(!reason)return 0
      const m=reason.match(pattern)
      return m?parseInt(m[1]):0
    },
    // 判定方式：谁分高显示谁（同分优先级:关键词>AI>情绪>频率）
    primaryTagText(r){
      if(!r)return'-'
      // 关键词等级：排除"未命中"
      const kwMatch=r.match(/关键词:(?!未命中).*?\((\d+)级\)/)
      const kwLv=kwMatch?parseInt(kwMatch[1]):0
      // AI等级：只取 AI识别为Xxx 形式的，忽略 "AI分析:-"
      const aiMatch=r.match(/AI分析:AI识别为.+?\((\d+)级\)/)
      const aiLv=aiMatch?parseInt(aiMatch[1]):0
      const moodLv=this.parseLevel(r,/情绪分:[\d.]+\((\d+)级\)/)
      // 情绪分中等级0表示正常，不计入
      const moodEffective=moodLv>0?moodLv:0
      // 比较：取最高分，同分按优先级
      const items=[
        {name:'关键词', level:kwLv, priority:4},
        {name:'AI分析', level:aiLv, priority:3},
        {name:'情绪趋势', level:moodEffective, priority:2},
      ]
      const sorted=items.filter(i=>i.level>0).sort((a,b)=>{
        if(b.level!==a.level)return b.level-a.level
        return b.priority-a.priority
      })
      return sorted.length>0?sorted[0].name:'-'
    },
    primaryTagType(r){
      const t=this.primaryTagText(r)
      if(t==='关键词')return'danger'
      if(t==='AI分析')return'warning'
      if(t==='情绪趋势')return'warning'
      return'info'
    },
    parseAnalysis(reason){
      if(!reason)return[]
      const parts=reason.split(' | ')
      const lines=[]
      for(const p of parts){
        // 来源
        if(p.match(/^(AI对话|日记|系统自动|情绪分触发)/)){
          const src=p.includes('情绪分触发')?'情绪记录':p.replace('触发预警','').replace('触发','')
          lines.push({label:'来源',text:src,bold:false})
        }
        // 关键词
        else if(p.startsWith('关键词:')){
          const txt=p.replace('关键词:','')
          const m=txt.match(/^(.+?)\((\d+)级\)/)
          if(m && m[1]!=='未命中'){
            const lv=parseInt(m[2])
            lines.push({label:'关键词',text:m[1]+'（'+this.levelText(lv)+'）',level:lv,bold:true})
          }else{
            lines.push({label:'关键词',text:'未命中',bold:false,level:0})
          }
        }
        // AI分析
        else if(p.startsWith('AI分析:')){
          const txt=p.replace('AI分析:','')
          const m=txt.match(/AI识别为(.+?)\((\d+)级\)/)
          if(m){
            const lv=parseInt(m[2])
            lines.push({label:'AI分析',text:this.levelText(lv),level:lv,bold:true})
          }else if(txt.includes('已跳过')){
            lines.push({label:'AI分析',text:'已跳过（高危关键词直接触发）',bold:false,level:0})
          }else{
            lines.push({label:'AI分析',text:'无',bold:false,level:0})
          }
        }
        // 情绪分
        else if(p.startsWith('情绪分:')){
          const m=p.match(/情绪分:([\d.]+)\((\d+)级\)/)
          if(m){
            const lv=parseInt(m[2])
            const scoreVal=m[1]
            const riskText=this.moodLevelText(scoreVal)
            lines.push({label:'情绪分数',text:scoreVal+'分'+(riskText?'（'+riskText+'）':''),level:lv>0?lv:0,bold:lv>0})
          }
        }
        // 连续低分
        else if(p.startsWith('连续低分')){
          lines.push({label:'低分天数',text:p.replace('连续低分:','')+'天持续低分',bold:false})
        }
        // 频率
        else if(p.includes('近24h同类触发')){
          const m=p.match(/近24h同类触发(\d+)次/)
          if(m) lines.push({label:'频率',text:'24h内已触发'+m[1]+'次，已升级',bold:false})
        }
        // 最终
        else if(p.startsWith('最终:')){
          const m=p.match(/最终:(\d+)级/)
          if(m){
            const lv=parseInt(m[1])
            lines.push({label:'最终等级',text:this.levelText(lv),level:lv,bold:true})
          }
        }
      }
      return lines
    },
    extractOriginalText(reason){
      if(!reason)return'暂无原文记录'
      // 只从 `原文:` 字段提取真实用户内容
      const idx=reason.indexOf('原文:')
      if(idx>=0){
        let txt=reason.substring(idx+3)
        txt=txt.replace(/ \| .*$/,'').trim()
        if(txt)return txt
      }
      return'暂无原文记录'
    },
    /** 情绪分数转风险等级文本（用于展示） */
    /** 格式化 triggerReason 显示，将等级数字转为文字 */
    formatTriggerReason(r){
      if(!r)return'-'
      return r
        .replace(/ \| 原文:.*/,'')
        .replace(/\((\d+)级\)/g,(m,l)=>'（'+this.levelText(parseInt(l))+'）')
        .replace(/(\d+)级-/g,(m,l)=>this.levelText(parseInt(l))+'级-')
        .replace(/(\d+)级(?![^|]*\d)/g,(m,l)=>this.levelText(parseInt(l)))
    },
    moodLevelText(score){
      if(score==null||score==='')return''
      const s=parseFloat(score)
      if(s<=2.5)return'高危'
      if(s<=3.5)return'中危'
      if(s<=4.0)return'关注'
      return'正常'
    },
    alertLevel(l){return alertLevelText(l)},
    alertType(l){return alertLevelType(l)},
    statusType(s){const m={'PENDING':'danger','PROCESSING':'warning','RESOLVED':'success'};return m[s]||'info'},
    statusText(s){const m={'PENDING':'待处理','PROCESSING':'待处理','RESOLVED':'已解决'};return m[s]||s},
    riskRowClass({row}){if(!row)return'';if(row.alertLevel>=4)return'risk-row-danger';if(row.alertLevel>=3)return'risk-row-orange';return''},
    fmt(t){if(!t)return'';const d=new Date(t);if(isNaN(d.getTime()))return t;return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')+' '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')},
    parseNotifyLog(remark){
      if(!remark)return[]
      const lines=remark.split('\n').filter(Boolean)
      return lines.map(line=>{
        // [MM-dd HH:mm] method → name(relation/phone) | remark
        const timeMatch=line.match(/^\[([^\]]+)\]/)
        const time=timeMatch?timeMatch[1]:''
        const rest=line.replace(/^\[[^\]]+\]\s*/,'')
        const parts=rest.split('→')
        const method=(parts[0]||'').trim()
        const contactPart=parts[1]?parts[1].split('|')[0].trim():''
        const remark=parts[1]?parts[1].split('|').slice(1).join('|').trim():''
        // contactPart = "name(relation/phone)"
        const nameMatch=contactPart.match(/^([^(]+)\(/)
        const contactName=nameMatch?nameMatch[1].trim():contactPart
        const detailPart=contactPart.match(/\(([^)]+)\)/)
        let relation='',phone=''
        if(detailPart){
          const dp=detailPart[1].split('/')
          relation=dp[0]||''
          phone=dp[1]||''
        }
        return {time,method,contactName,relation,phone,remark}
      })
    },
    showDetail(row){
      this.detail={...row, _defaultContact:null, _originalContent:null}
      // 尝试获取原文（仅当 triggerReason 中无原文时）
      const r=row.triggerReason||''
      if(!r.includes('原文:') && !r.includes('情绪分触发') && r.includes('日记')){
        import('@/api/diary').then(m=>{
          m.getDiaryList({userId:row.userId,pageNum:1,pageSize:5}).then(res=>{
            const records=(res.data&&res.data.records)||res.data||[]
            if(records.length>0){
              const content=records[0].content||records[0].title
              if(content) this.detail._originalContent=content
            }
          }).catch(()=>{})
        })
      }
      getEmergencyContacts(row.userId).then(res=>{
        const contacts=res.data||[]
        const def=contacts.find(c=>c.isDefault===1)||contacts[0]
        if(def) this.detail._defaultContact=def
      }).catch(()=>{})
      this.detailVisible=true
    },
    showHandle(row){
      this.handleAlert=row
      this.handleRemark=''
      this.handleMarkResolved=false
      this.handleContacts=[]
      this.handleNotifyLog=[]
      this.handleNotifyMethod='电话'
      this.handleNotifyRemark=''
      this.notifyingContactId=null
      // 解析已有通知记录
      if(row.guardianNotifyRemark){
        this.handleNotifyLog=row.guardianNotifyRemark.split('\n').filter(Boolean)
      }
      // 获取该用户的紧急联系人
      getEmergencyContacts(row.userId).then(res=>{
        this.handleContacts=res.data||[]
      }).catch(()=>{})
      this.handleVisible=true
    },
    notifyContact(contact){
      this.notifyingContactId=contact.id
      const body={
        id:this.handleAlert.id,
        contactName:contact.name,
        contactRelation:contact.relation,
        contactPhone:contact.phone,
        notifyMethod:this.handleNotifyMethod,
        notifyRemark:this.handleNotifyRemark
      }
      body.notifyTime=new Date().toISOString()
      notifyGuardian(body).then(()=>{
        this.$message.success('已通知 ' + contact.name)
        // 追加到日志（前端直接插入，不用重新请求）
        const now=new Date()
        const timeStr=String(now.getMonth()+1).padStart(2,'0')+'-'+String(now.getDate()).padStart(2,'0')+' '+String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0')
        const entry='['+timeStr+'] '+this.handleNotifyMethod+' → '+contact.name+'('+contact.relation+'/'+contact.phone+')'+(this.handleNotifyRemark?' | '+this.handleNotifyRemark:'')
        this.handleNotifyLog.unshift(entry)
        this.handleNotifyRemark=''
        // 同时更新 handleAlert 供下次查看
        this.handleAlert.guardianNotified=1
        this.handleAlert.guardianNotifyMethod=this.handleNotifyMethod
        this.handleAlert.guardianNotifyRemark=entry+'\n'+(this.handleAlert.guardianNotifyRemark||'')
      }).catch(()=>{
        this.$message.error('通知失败，请重试')
      }).finally(()=>{this.notifyingContactId=null})
    },
    submitHandle(){
      if(!this.handleMarkResolved){
        this.$message.warning('请先勾选操作项')
        return
      }
      this.handling=true
      const newStatus = this.handleAlert.handleStatus === 'RESOLVED' ? 'PENDING' : 'RESOLVED'
      const actionText = newStatus === 'RESOLVED' ? '已标记为解决' : '已恢复待处理'
      handleCrisis(this.handleAlert.id, this.handleRemark||'已处理', newStatus).then(()=>{
        this.$message.success(actionText)
        this.handleVisible=false
        this.fetchList()
      }).catch(()=>{
        this.$message.error('操作失败，请重试')
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
