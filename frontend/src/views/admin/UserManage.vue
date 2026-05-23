<!-- 管理后台 - 用户管理（完整 CRUD） -->
<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">👥 用户管理</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号/邮箱" size="medium" clearable
          prefix-icon="el-icon-search" style="width:280px;" @keyup.enter.native="search" @clear="search" />
        <el-button type="primary" size="medium" icon="el-icon-search" @click="search">搜索</el-button>
        <el-button size="medium" @click="resetSearch">重置</el-button>
        <el-button type="primary" size="medium" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="userList" stripe style="width:100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="nickname" label="昵称" width="110" />
        <el-table-column label="性别" width="50">
          <template slot-scope="{row}">{{ {0:'-',1:'男',2:'女'}[row.gender] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="150" show-overflow-tooltip />
        <el-table-column label="角色" width="80">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.role === 'admin' ? 'danger' : 'primary'">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" style="color:#409EFF;" @click="openDetail(row)">查看详情</el-button>
            <el-button type="text" size="mini" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" @click="handleResetPwd(row.id)">重置密码</el-button>
            <el-button type="text" size="mini" :style="{color: row.status === 1 ? '#E6A23C' : '#67C23A'}"
              @click="handleToggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" background layout="prev,pager,next" :total="total" :page-size="15"
        @current-change="p => { pageNum = p; fetchList() }" style="margin-top:16px;text-align:center;" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="editingId ? '编辑用户' : '新增用户'" :visible.sync="showDialog" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px" size="medium">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" v-if="!editingId">
              <el-input v-model="form.username" placeholder="登录用" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" v-if="!editingId">
              <el-input v-model="form.password" type="password" placeholder="默认 123456" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.gender" style="width:100%;">
              <el-option :label="'保密'" :value="0" />
              <el-option :label="'男'" :value="1" />
              <el-option :label="'女'" :value="2" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="年龄"><el-input v-model="form.age" type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="职业"><el-input v-model="form.occupation" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio label="user">普通用户</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" v-if="editingId">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ editingId ? '保存' : '创建' }}</el-button>
      </div>
    </el-dialog>

    <!-- ===== 用户详情弹窗（1200px 宽） ===== -->
    <el-dialog title="用户详情" :visible.sync="detailVisible" width="1200px" top="4vh" :close-on-click-modal="true" @closed="disposeSparkCharts">
      <div v-if="detailUser">
        <el-tabs v-model="activeTab" @tab-click="onTabClick">
          <el-tab-pane label="📋 基本信息" name="info">
            <el-descriptions :column="3" border style="margin-top:12px;">
              <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ detailUser.nickname || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag size="mini" :type="detailUser.role === 'admin' ? 'danger' : 'primary'">{{ detailUser.role }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="性别">{{ {0:'保密',1:'男',2:'女'}[detailUser.gender] || '-' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ detailUser.age || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ detailUser.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detailUser.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag size="mini" :type="detailUser.status === 1 ? 'success' : 'info'">{{ detailUser.status === 1 ? '正常' : '禁用' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ detailUser.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="📖 日记记录" name="diary">
            <el-table :data="detailDiary" stripe v-loading="detailLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}">
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column label="时间" width="150">
                <template slot-scope="{row}">{{ formatTime(row.create_time) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="90">
                <template slot-scope="{row}">
                  <el-button type="text" size="mini" @click="viewDiaryDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- ===== 情绪记录 ===== -->
          <el-tab-pane label="📊 情绪记录" name="mood">
            <el-table :data="detailMood" stripe v-loading="moodLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}">
              <el-table-column prop="mood_tag" label="情绪标签" width="120" />
              <el-table-column prop="mood_score" label="分数" width="80" />
              <el-table-column label="风险等级" width="100">
                <template slot-scope="{row}">
                  <el-tag size="mini" :type="moodRiskType(row.mood_score)" effect="dark">{{ moodRiskLevel(row.mood_score) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="record_date" label="日期" width="120" />
              <el-table-column prop="note" label="备注" min-width="200" show-overflow-tooltip />
            </el-table>
            <el-empty v-if="!moodLoading && detailMood.length===0" description="暂无数据" />
          </el-tab-pane>

          <!-- ===== 周报记录 ===== -->
          <el-tab-pane label="📋 周报记录" name="report">
            <el-table :data="detailReport" stripe v-loading="reportLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}"
              :row-class-name="r => reportRiskRow(r, detailReport)">
              <el-table-column label="周期" min-width="180">
                <template slot-scope="{row}">{{ row.period_start }} ~ {{ row.period_end }}</template>
              </el-table-column>
              <el-table-column label="平均分" width="80">
                <template slot-scope="{row}">{{ parseStats(row.statistics_json).avgScore || '-' }}</template>
              </el-table-column>
              <el-table-column label="主要情绪" width="100">
                <template slot-scope="{row}">{{ parseStats(row.statistics_json).topMood || '-' }}</template>
              </el-table-column>
              <el-table-column label="趋势" width="120">
                <template slot-scope="{row}">
                  <span :ref="'spark_' + row.id" style="display:inline-block;width:100px;height:28px;"></span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template slot-scope="{row}">
                  <el-button type="text" size="mini" @click="viewReportDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!reportLoading && detailReport.length===0" description="暂无数据" />
          </el-tab-pane>

          <!-- ===== AI 对话（风险消息聚合） ===== -->
          <el-tab-pane label="💬 AI 对话" name="chat">
            <el-table :data="detailChat" stripe v-loading="chatLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}"
              :row-class-name="r => chatRiskRow(r, detailChat)">
              <el-table-column label="风险关键词" width="120">
                <template slot-scope="{row}">
                  <el-tag size="mini" type="danger" v-if="extractRiskWord(row.content)">{{ extractRiskWord(row.content) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="消息摘要" min-width="260">
                <template slot-scope="{row}">{{ truncate(row.content, 50) }}</template>
              </el-table-column>
              <el-table-column label="时间" width="150">
                <template slot-scope="{row}">{{ formatTime(row.create_time) }}</template>
              </el-table-column>
              <el-table-column label="风险等级" width="100">
                <template slot-scope="{row}">
                  <el-tag size="mini" :type="chatRiskType(row.content)" effect="dark">{{ chatRiskLevel(row.content) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template slot-scope="{row}">
                  <el-button type="text" size="mini" @click="viewChatContext(row)">查看上下文</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!chatLoading && detailChat.length===0" description="暂无风险消息" />
          </el-tab-pane>

          <!-- ===== 危机预警 ===== -->
          <el-tab-pane label="🚨 危机预警" name="crisis">
            <el-table :data="detailCrisis" stripe v-loading="crisisLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}">
              <el-table-column label="风险等级" width="100">
                <template slot-scope="{row}">
                  <el-tag size="mini" :type="alertRiskType(row.alertLevel)" effect="dark">{{ alertRiskLevel(row.alertLevel) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="triggerReason" label="触发原因" min-width="250" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template slot-scope="{row}">
                  <el-tag size="mini" :type="crisisStatusType(row.handleStatus)">{{ crisisStatusText(row.handleStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="处理人" width="100">
                <template slot-scope="{row}">{{ row.handledBy || '-' }}</template>
              </el-table-column>
              <el-table-column label="时间" width="150">
                <template slot-scope="{row}">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!crisisLoading && detailCrisis.length===0" description="暂无预警" />
          </el-tab-pane>

          <!-- ===== 预约记录 ===== -->
          <el-tab-pane label="📅 预约记录" name="appointment">
            <el-table :data="detailAppointment" stripe v-loading="aptLoading" size="small" style="width:100%;margin-top:12px;"
              :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
              :cell-style="{textAlign:'center',padding:'6px 0'}">
              <el-table-column prop="counselor_name" label="咨询师" min-width="120" />
              <el-table-column prop="appointment_date" label="日期" min-width="120" />
              <el-table-column prop="time_slot" label="时间段" min-width="120" />
              <el-table-column label="状态" min-width="100">
                <template slot-scope="{row}">
                  <el-tag size="mini" :type="aptStatusType(row.status)">{{ aptStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" min-width="150">
                <template slot-scope="{row}">{{ formatTime(row.create_time) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!aptLoading && detailAppointment.length===0" description="暂无预约" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 日记详情弹窗 -->
    <el-dialog title="日记详情" :visible.sync="diaryDetailVisible" width="700px" top="5vh" :close-on-click-modal="true">
      <div v-if="diaryDetail">
        <div style="margin-bottom:14px;display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
          <el-tag size="small" :type="riskType(diaryDetail.emotion_score)" effect="dark">{{ riskLevel(diaryDetail.emotion_score) }}</el-tag>
          <el-tag size="small" effect="plain">{{ diaryDetail.mood_tags || '-' }}</el-tag>
          <span style="font-size:12px;color:#909399;">情绪分数: <b>{{ diaryDetail.emotion_score != null ? diaryDetail.emotion_score : '-' }}</b></span>
        </div>
        <h3 style="margin:0 0 10px;font-size:17px;color:#2C3E50;">{{ diaryDetail.title || '(无标题)' }}</h3>
        <div v-if="diaryDetail.imageUrl" style="margin-bottom:12px;">
          <img :src="diaryDetail.imageUrl" style="max-width:100%;max-height:300px;border-radius:8px;cursor:pointer;" @click="windowOpen(diaryDetail.imageUrl)" />
        </div>
        <p style="white-space:pre-wrap;line-height:1.8;color:#4A5568;margin:0 0 14px;font-size:14px;">{{ diaryDetail.content }}</p>
        <div style="font-size:12px;color:#A0AEC0;border-top:1px solid #F0F5FF;padding-top:12px;">
          <span>🕐 {{ formatTime(diaryDetail.create_time) }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- AI 上下文弹窗 -->
    <el-dialog title="聊天上下文" :visible.sync="chatContextVisible" width="600px" top="5vh" :close-on-click-modal="true">
      <div class="msg-list" v-if="chatContextMsgs.length > 0">
        <div v-for="m in chatContextMsgs" :key="m.id" class="msg-item" :class="m.sender_role === 'USER' ? 'user' : 'ai'">
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
import { getAdminUserList, createAdminUser, updateAdminUser, deleteAdminUser, resetPassword, toggleUserStatus } from '@/api/admin/users'
import request from '@/utils/request'
import * as echarts from 'echarts'
import { extractKeyword, riskLevelFromKeyword, riskTypeFromKeyword } from '@/utils/crisisKeywords'

export default {
  name: 'UserManage',
  data() {
    return {
      userList: [], total: 0, pageNum: 1, pageSize: 15,
      keyword: '', loading: false, showDialog: false, submitting: false, editingId: null,
      form: { username: '', password: '123456', nickname: '', phone: '', email: '', gender: 0, age: null, occupation: '', role: 'user', status: 1 },
      /* 详情弹窗 */
      detailVisible: false, detailUser: null, activeTab: 'info', sparkCharts: [],
      detailDiary: [], detailLoading: false,
      diaryDetailVisible: false, diaryDetail: null,
      detailMood: [], moodLoading: false,
      detailReport: [], reportLoading: false,
      detailChat: [], chatLoading: false,
      chatContextVisible: false, chatContextMsgs: [],
      detailCrisis: [], crisisLoading: false,
      detailAppointment: [], aptLoading: false
    }
  },
  created() {
    this.handleRouteQuery()
  },
  watch: {
    '$route.query.userId': function() { this.handleRouteQuery() },
    '$route.query.username': function() { this.handleRouteQuery() },
    '$route.query.id': function() { this.handleRouteQuery() }
  },

  methods: {
    handleRouteQuery() {
      const userId = this.$route.query.userId || this.$route.query.id
      const username = this.$route.query.username
      if (!userId && !username) {
        this.fetchList()
        return
      }
      const params = { pageNum: 1, pageSize: 15 }
      if (userId) { params.userId = parseInt(userId) }
      else if (username) { params.keyword = username }
      this.loading = true
      return getAdminUserList(params).then(res => {
        const records = res.data.records || []
        this.userList = records
        this.total = res.data.total || 0
        // 按 username 查找时，可能会有多条匹配（用户名精确匹配取第一条）
        let target = null
        if (userId && records.length > 0) {
          target = records[0]
        } else if (username) {
          target = records.find(u => u.username === username) || records[0]
        }
        if (target) {
          this.$nextTick(() => this.openDetail(target))
        } else {
          this.$message.warning('未找到该用户')
        }
      }).finally(() => { this.loading = false })
    },
    fetchList() {
      this.loading = true
      return getAdminUserList({ pageNum: this.pageNum, pageSize: this.pageSize, keyword: this.keyword || undefined })
        .then(res => { this.userList = res.data.records || []; this.total = res.data.total || 0 })
        .finally(() => { this.loading = false })
    },
    search() { this.pageNum = 1; this.fetchList() },
    resetSearch() { this.keyword = ''; this.pageNum = 1; this.fetchList() },
    openCreate() {
      this.editingId = null
      this.form = { username: '', password: '123456', nickname: '', phone: '', email: '', gender: 0, age: null, occupation: '', role: 'user', status: 1 }
      this.showDialog = true
    },
    openEdit(row) {
      this.editingId = row.id
      this.form = {
        nickname: row.nickname || '', phone: row.phone || '', email: row.email || '',
        gender: row.gender != null ? row.gender : 0, age: row.age, occupation: row.occupation || '',
        role: row.role || 'user', status: row.status != null ? row.status : 1
      }
      this.showDialog = true
    },
    submitForm() {
      this.submitting = true
      const promise = this.editingId
        ? updateAdminUser(this.editingId, this.form)
        : createAdminUser(this.form)
      promise.then(() => {
        this.$message.success(this.editingId ? '已更新' : '已创建')
        this.showDialog = false; this.fetchList()
      }).finally(() => { this.submitting = false })
    },
    handleResetPwd(id) {
      this.$confirm('确定将密码重置为 123456？', '提示').then(() => {
        resetPassword(id).then(() => this.$message.success('已重置为 123456'))
      }).catch(() => {})
    },
    handleToggle(row) {
      const act = row.status === 1 ? '禁用' : '启用'
      this.$confirm('确定' + act + '该用户？', '提示').then(() => {
        toggleUserStatus(row.id).then(() => { this.$message.success('已' + act); this.fetchList() })
      }).catch(() => {})
    },
    handleDelete(id) {
      this.$confirm('确定删除该用户？', '提示', { type: 'warning' }).then(() => {
        deleteAdminUser(id).then(() => { this.$message.success('已删除'); this.fetchList() })
      }).catch(() => {})
    },
    /* ===== 用户详情 ===== */
    openDetail(row) {
      this.detailUser = row
      this.detailVisible = true
      this.activeTab = 'info'
      this.disposeSparkCharts()
      const uid = row.id
      this.fetchDetailDiary(uid)
      this.fetchDetailMood(uid)
      this.fetchDetailReport(uid)  // 只加载数据，不渲染 sparkline
      this.fetchDetailChat(uid)
      this.fetchDetailCrisis(uid)
      this.fetchDetailAppt(uid)
    },
    fetchDetailDiary(uid) {
      this.detailLoading = true
      request.get('/admin/diary', { params: { page: 1, size: 20, userId: uid } }).then(r => {
        this.detailDiary = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.detailLoading = false })
    },
    fetchDetailMood(uid) {
      this.moodLoading = true
      request.get('/admin/mood', { params: { page: 1, size: 20, userId: uid } }).then(r => {
        this.detailMood = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.moodLoading = false })
    },
    fetchDetailReport(uid) {
      this.reportLoading = true
      request.get('/admin/report', { params: { page: 1, size: 10, userId: uid } }).then(r => {
        this.detailReport = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.reportLoading = false })
    },
    fetchDetailChat(uid) {
      this.chatLoading = true
      request.get('/admin/chat/risk', { params: { page: 1, size: 20, userId: uid } }).then(r => {
        this.detailChat = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.chatLoading = false })
    },
    fetchDetailCrisis(uid) {
      this.crisisLoading = true
      request.get('/crisis/list', { params: { pageNum: 1, pageSize: 20, userId: uid } }).then(r => {
        this.detailCrisis = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.crisisLoading = false })
    },
    fetchDetailAppt(uid) {
      this.aptLoading = true
      request.get('/admin/appointment', { params: { page: 1, size: 20, userId: uid } }).then(r => {
        this.detailAppointment = (r.data && r.data.records) || []
      }).catch(() => {}).finally(() => { this.aptLoading = false })
    },
    /* ===== 日记详情 ===== */
    viewDiaryDetail(row) {
      this.diaryDetail = row
      this.diaryDetailVisible = true
    },
    windowOpen(url) { window.open(url, '_blank') },

    /* ===== 工具方法 ===== */
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      if (isNaN(d.getTime())) return t
      const Y = d.getFullYear()
      const M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0')
      return Y + '-' + M + '-' + D + ' ' + h + ':' + m
    },
    /* ===== 风险等级方法 ===== */
    diaryRiskRow({row}) {
      if (row.emotion_score == null) return ''
      if (row.emotion_score < 20) return 'risk-row-danger'
      if (row.emotion_score < 40) return 'risk-row-orange'
      return ''
    },
    riskLevel(score) {
      if (score == null) return '未知'
      if (score >= 60) return '正常'
      if (score >= 40) return '关注'
      if (score >= 20) return '中危'
      return '高危'
    },
    riskType(score) {
      if (score == null) return 'info'
      if (score >= 60) return 'success'
      if (score >= 40) return 'warning'
      if (score >= 20) return 'warning'
      return 'danger'
    },
    /* 情绪记录风险 */
    moodRiskLevel(s) { if (s == null) return '未知'; if (s >= 7) return '正常'; if (s >= 5) return '关注'; if (s >= 3) return '中危'; return '高危' },
    moodRiskType(s) { if (s == null) return 'info'; if (s >= 7) return 'success'; if (s >= 5) return 'warning'; if (s >= 3) return 'warning'; return 'danger' },
    /* 周报 */
    reportRiskRow({row}) {
      if (!row.statistics_json) return ''
      const s = this.parseStats(row.statistics_json)
      if (s.avgScore != null && s.avgScore < 45) return 'risk-row-orange'
      return ''
    },
    parseStats(j) { try { return typeof j === 'string' ? JSON.parse(j) : j } catch(e) { return {} } },
    /** 销毁所有 sparkline 图表 */
    disposeSparkCharts() {
      this.sparkCharts.forEach(c => { try { c.dispose() } catch(e) {} })
      this.sparkCharts = []
    },

    /** Tab 切换时触发 */
    onTabClick(tab) {
      if (tab.name === 'report') {
        this.$nextTick(() => this.renderSparklines())
      }
    },

    /** 安全渲染 sparkline 趋势图 */
    renderSparklines() {
      // 先销毁旧的
      this.disposeSparkCharts()
      this.$nextTick(() => {
        this.detailReport.forEach(r => {
          const stats = this.parseStats(r.statistics_json)
          const scores = (stats.trend && stats.trend.scores) || []
          if (scores.length === 0) return
          const refs = this.$refs['spark_' + r.id]
          if (!refs || !refs[0]) return
          const dom = refs[0]
          if (dom.clientWidth === 0) return
          try {
            const chart = echarts.init(dom)
            this.sparkCharts.push(chart)
            chart.setOption({
              grid: { left:0, right:0, top:0, bottom:0 },
              xAxis: { show: false }, yAxis: { show: false },
              series: [{
                type:'line', data:scores, smooth:true, showSymbol:false,
                lineStyle:{ color:scores[scores.length-1] >= scores[0] ? '#67C23A' : '#F56C6C', width:1.5 }
              }]
            })
          } catch(e) { /* 静默处理 ECharts 错误 */ }
        })
      })
    },
    viewReportDetail(row) {
      const stats = this.parseStats(row.statistics_json)
      this.$alert(
        '<b>周期：</b>' + row.period_start + ' ~ ' + row.period_end + '<br>' +
        '<b>平均分：</b>' + (stats.avgScore || '-') + '<br>' +
        '<b>主要情绪：</b>' + (stats.topMood || '-') + '<br>' +
        '<b>日记数：</b>' + (stats.diaryCount || 0) + '<br><br>' +
        '<b>总结：</b><br>' + (row.summary || '暂无'),
        '周报详情', { dangerouslyUseHTMLString: true, confirmButtonText:'关闭' }
      )
    },
    /* AI 对话风险消息 */
    extractRiskWord(c) { return extractKeyword(c) },
    chatRiskLevel(c) { return riskLevelFromKeyword(c) },
    chatRiskType(c) { return riskTypeFromKeyword(c) },
    chatRiskRow({row}) {
      const lv = this.chatRiskLevel(row.content)
      if (lv === '高危') return 'risk-row-danger'
      if (lv === '中危') return 'risk-row-orange'
      return ''
    },
    viewChatContext(row) {
      this.chatContextMsgs = []
      this.chatContextVisible = true
      request.get('/admin/chat/' + row.session_id + '/messages').then(r => {
        this.chatContextMsgs = r.data || []
      })
    },
    truncate(t, n) { if (!t) return ''; return t.length > n ? t.substring(0, n) + '...' : t },
    /* 危机预警 */
    alertRiskLevel(l) { if (l >= 4) return '高危'; if (l >= 3) return '中危'; return '关注' },
    alertRiskType(l) { if (l >= 4) return 'danger'; if (l >= 3) return 'warning'; return 'warning' },
    crisisStatusType(s) { const m={'PENDING':'danger','PROCESSING':'warning','RESOLVED':'success','CLOSED':'info'}; return m[s]||'info' },
    crisisStatusText(s) { const m={'PENDING':'待处理','PROCESSING':'处理中','RESOLVED':'已解决','CLOSED':'已关闭'}; return m[s]||s },
    /* 预约 */
    aptStatusType(s) { const m={'pending':'warning','confirmed':'success','completed':'info','cancelled':'danger'}; return m[s]||'info' },
    aptStatusText(s) { const m={'pending':'待确认','confirmed':'已确认','completed':'已完成','cancelled':'已取消'}; return m[s]||s }
  }
}
</script>

<style scoped>
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-title { font-size: 22px; font-weight: 700; color: #2C3E50; margin: 0; }
.msg-list { max-height:450px; overflow-y:auto; }
.msg-item { margin-bottom:10px; padding:10px 14px; background:#F8FAFF; border-radius:8px; }
.msg-item.user { background:#EBF5FF; }
.msg-label { font-size:12px; font-weight:600; color:#909399; margin-bottom:4px; }
.msg-text { font-size:14px; color:#2C3E50; line-height:1.6; white-space:pre-wrap; }
.msg-time { font-size:11px; color:#C0C4CC; margin-top:4px; }
</style>
