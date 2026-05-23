<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">心理老师管理</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-button type="primary" size="medium" icon="el-icon-plus" @click="openCreate">新增老师</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center',fontSize:'12px'}"
        :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="specialty" label="擅长领域" min-width="160" />
        <el-table-column label="可预约时段" min-width="260">
          <template slot-scope="{row}">
            <div v-if="row._slots && Object.keys(row._slots).length > 0" style="font-size:12px;color:#606266;text-align:left;padding:4px 0;">
              <div v-for="(slots, wd) in row._slots" :key="wd" style="margin-bottom:2px;">
                <span style="display:inline-block;width:36px;font-weight:600;color:#409EFF;">{{ '一二三四五六日'[wd-1] }}</span>
                <span>{{ slots.join('、') }}</span>
              </div>
            </div>
            <span v-else style="color:#C0C4CC;font-size:12px;">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="intro" label="简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" style="color:#409EFF;" @click="openSlots(row)">时段设置</el-button>
            <el-button type="text" size="mini" style="color:#409EFF;" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" :style="{color: row.enabled === 1 ? '#E6A23C' : '#67C23A'}"
              @click="handleToggle(row)">{{ row.enabled === 1 ? '停用' : '启用' }}</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="editingId ? '编辑老师' : '新增老师'" :visible.sync="showDialog" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px" size="medium">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入老师姓名" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="form.specialty" placeholder="如：焦虑管理、情绪调节" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" maxlength="500" show-word-limit
            placeholder="请简要介绍老师的背景和擅长方向" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ editingId ? '保存' : '创建' }}</el-button>
      </div>
    </el-dialog>

    <!-- 可预约时间段配置弹窗（按星期分组） -->
    <el-dialog title="可预约时间段设置" :visible.sync="slotsVisible" width="600px" :close-on-click-modal="false">
      <p style="font-size:14px;color:#606266;margin:0 0 16px;">
        为 <b>{{ slotCounselorName }}</b> 设置每周可预约时间段：
      </p>
      <div v-for="wd in weekDays" :key="wd.value" style="margin-bottom:14px;padding:10px 14px;background:#F8FAFF;border-radius:8px;border:1px solid #E8ECF4;">
        <div style="font-weight:600;font-size:13px;color:#2C3E50;margin-bottom:8px;">{{ wd.label }}</div>
        <el-checkbox-group v-model="weekSlotMap[wd.value]" style="display:flex;flex-wrap:wrap;gap:8px;">
          <el-checkbox v-for="s in allTimeSlots" :key="s" :label="s" style="margin-right:0;">
            {{ s }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <div slot="footer">
        <el-button @click="slotsVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSlots" :loading="savingSlots">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCounselorList, createCounselor, updateCounselor, deleteCounselor, toggleCounselor, getCounselorSlots, saveCounselorSlots } from '@/api/counselor'

const ALL_TIME_SLOTS = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00']
const WEEK_DAYS = [
  { value: 1, label: '周一' }, { value: 2, label: '周二' }, { value: 3, label: '周三' },
  { value: 4, label: '周四' }, { value: 5, label: '周五' }, { value: 6, label: '周六' }, { value: 7, label: '周日' }
]

export default {
  name: 'CounselorManage',
  data() {
    return {
      list: [],
      loading: false,
      showDialog: false,
      submitting: false,
      editingId: null,
      form: { name: '', specialty: '', intro: '' },
      slotsVisible: false,
      slotCounselorId: null,
      slotCounselorName: '',
      weekSlotMap: {},
      savingSlots: false,
      allTimeSlots: ALL_TIME_SLOTS,
      weekDays: WEEK_DAYS
    }
  },
  created() { this.fetchList() },
  methods: {
    fetchList() {
      this.loading = true
      getCounselorList().then(res => {
        this.list = (res.data || []).map(c => ({ ...c, _slots: null }))
        // Load slots for each counselor (grouped by week_day)
        this.list.forEach(c => {
          getCounselorSlots(c.id).then(r => {
            const slots = r.data || []
            // Build weekDay -> timeSlot array mapping
            const map = {}
            slots.forEach(s => {
              if (s.enabled !== 1) return
              if (!map[s.weekDay]) map[s.weekDay] = []
              map[s.weekDay].push(s.timeSlot)
            })
            c._slots = map
          }).catch(() => {})
        })
      }).finally(() => { this.loading = false })
    },
    openCreate() {
      this.editingId = null
      this.form = { name: '', specialty: '', intro: '' }
      this.showDialog = true
    },
    openEdit(row) {
      this.editingId = row.id
      this.form = { name: row.name, specialty: row.specialty || '', intro: row.intro || '' }
      this.showDialog = true
    },
    submitForm() {
      if (!this.form.name || !this.form.name.trim()) {
        this.$message.warning('请输入老师姓名')
        return
      }
      this.submitting = true
      const promise = this.editingId
        ? updateCounselor(this.editingId, this.form)
        : createCounselor(this.form)
      promise.then(() => {
        this.$message.success(this.editingId ? '已保存' : '已创建')
        this.showDialog = false
        this.fetchList()
      }).finally(() => { this.submitting = false })
    },
    openSlots(row) {
      this.slotCounselorId = row.id
      this.slotCounselorName = row.name
      // Initialize all weekdays to empty arrays
      const map = {}
      WEEK_DAYS.forEach(wd => { map[wd.value] = [] })
      getCounselorSlots(row.id).then(res => {
        (res.data || []).forEach(s => {
          if (s.enabled === 1 && map[s.weekDay]) {
            map[s.weekDay].push(s.timeSlot)
          }
        })
      }).catch(() => {})
      this.weekSlotMap = map
      this.slotsVisible = true
    },
    submitSlots() {
      if (!this.slotCounselorId) return
      this.savingSlots = true
      saveCounselorSlots(this.slotCounselorId, this.weekSlotMap).then(() => {
        this.$message.success('时段设置已保存')
        this.slotsVisible = false
        this.fetchList()
      }).finally(() => { this.savingSlots = false })
    },
    handleToggle(row) {
      const act = row.enabled === 1 ? '停用' : '启用'
      this.$confirm('确定' + act + '该老师？' + (act === '停用' ? '停用后用户端将不再显示该老师，已有预约不受影响。' : ''), '提示').then(() => {
        toggleCounselor(row.id).then(() => {
          this.$message.success('已' + act)
          this.fetchList()
        })
      }).catch(() => {})
    },
    handleDelete(id) {
      this.$confirm('确定删除该老师？删除后不可恢复。', '提示', { type: 'warning' }).then(() => {
        deleteCounselor(id).then(() => {
          this.$message.success('已删除')
          this.fetchList()
        })
      }).catch(() => {})
    }
  }
}
</script>
<style scoped>
.page-container { padding: 0 12px 30px; }
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:18px; font-weight:700; color:#1A2332; margin:0; }
</style>
