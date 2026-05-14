<!-- 紧急联系人 -->
<template>
  <div class="emergency-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">🆘 紧急联系人</h1>
        <p class="page-subtitle">设置你的紧急联系人，在需要时一键求助</p>
      </div>
      <el-button type="primary" size="medium" @click="showDialog = true; isEdit = false; form = { name: '', phone: '', relation: '' }">
        <i class="el-icon-plus"></i> 新增联系人
      </el-button>
    </div>

    <!-- 联系人列表 -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="c in contactList" :key="c.id" style="margin-bottom:16px;">
        <el-card shadow="never" :class="['contact-card', { 'is-default': c.isDefault === 1 }]">
          <div class="card-badge" v-if="c.isDefault === 1">默认</div>
          <div class="contact-body">
            <div class="contact-avatar" :style="{ background: getAvatarColor(c.name) }">
              {{ c.name.charAt(0) }}
            </div>
            <h3 class="contact-name">{{ c.name }}</h3>
            <div class="contact-phone">
              <el-tag size="small" type="primary">{{ c.phone }}</el-tag>
            </div>
            <div class="contact-relation" v-if="c.relation">
              <span class="relation-tag">{{ c.relation }}</span>
            </div>
            <div class="contact-actions">
              <el-button v-if="c.isDefault !== 1" type="text" size="mini" @click="setDefault(c.id)">设为默认</el-button>
              <el-button type="text" size="mini" @click="editContact(c)">编辑</el-button>
              <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(c.id)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8" v-if="contactList.length === 0" style="margin-bottom:16px;">
        <el-card shadow="never">
          <div style="text-align:center;padding:40px 0;color:#C0C4CC;">
            <div style="font-size:40px;margin-bottom:12px;">🆘</div>
            <p>暂无紧急联系人</p>
            <p style="font-size:13px;margin-top:4px;">添加一个联系人以便在危机时快速求助</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 心理援助热线 -->
    <el-card shadow="never" class="hotline-card">
      <div slot="header" class="card-header">
        <span><i class="el-icon-phone-outline" style="color:#F56C6C;"></i> 心理援助热线（24小时）</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="12" v-for="h in hotlines" :key="h.name">
          <div class="hotline-item">
            <span class="hotline-name">{{ h.name }}</span>
            <span class="hotline-num">{{ h.number }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="isEdit ? '编辑联系人' : '新增联系人'" :visible.sync="showDialog" width="420px" :close-on-click-modal="false">
      <el-form :model="form" label-position="top" size="medium">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="输入联系人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item label="关系">
          <el-select v-model="form.relation" placeholder="选择或输入关系" style="width:100%;" allow-create filterable>
            <el-option label="家人" value="家人" />
            <el-option label="朋友" value="朋友" />
            <el-option label="同事" value="同事" />
            <el-option label="同学" value="同学" />
            <el-option label="医生" value="医生" />
            <el-option label="咨询师" value="咨询师" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ isEdit ? '保存修改' : '添加' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmergencyList, createEmergency, updateEmergency, deleteEmergency, setDefaultEmergency } from '@/api/emergency'

const avatarColors = ['#409EFF','#6C63FF','#67C23A','#E6A23C','#F56C6C','#A78BFA','#FF6B9D','#4FC3F7']

export default {
  name: 'Emergency',
  data() {
    return {
      contactList: [],
      showDialog: false,
      isEdit: false,
      submitting: false,
      form: { name: '', phone: '', relation: '' },
      editingId: null,
      hotlines: [
        { name: '全国心理援助热线', number: '12356' },
        { name: '生命热线', number: '400-821-1215' }
      ]
    }
  },
  created() { this.fetchList() },
  methods: {
    fetchList() {
      getEmergencyList().then(res => { this.contactList = res.data || [] })
    },
    editContact(c) {
      this.isEdit = true
      this.editingId = c.id
      this.form = { name: c.name, phone: c.phone, relation: c.relation || '' }
      this.showDialog = true
    },
    setDefault(id) {
      setDefaultEmergency(id).then(() => { this.$message.success('已设为默认'); this.fetchList() })
    },
    handleDelete(id) {
      this.$confirm('确定删除此联系人？', '提示').then(() => {
        deleteEmergency(id).then(() => { this.$message.success('已删除'); this.fetchList() })
      }).catch(() => {})
    },
    submitForm() {
      if (!this.form.name || !this.form.phone) {
        this.$message.warning('请填写姓名和手机号')
        return
      }
      this.submitting = true
      const action = this.isEdit
        ? updateEmergency({ id: this.editingId, ...this.form })
        : createEmergency(this.form)
      action.then(() => {
        this.$message.success(this.isEdit ? '已更新' : '已添加')
        this.showDialog = false
        this.fetchList()
      }).finally(() => { this.submitting = false })
    },
    getAvatarColor(name) {
      if (!name) return avatarColors[0]
      const idx = name.charCodeAt(0) % avatarColors.length
      return avatarColors[idx]
    }
  }
}
</script>

<style scoped>
.emergency-page {
  max-width: var(--page-width, 1200px); margin: 0 auto; padding: 0 24px 40px;
}
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 24px; padding-top: 28px;
}
.page-title { font-size: 22px; font-weight: 700; color: #2C3E50; }
.page-subtitle { font-size: 14px; color: #909399; margin-top: 4px; }
.contact-card { position: relative; text-align: center; padding-top: 8px; transition: all 0.3s ease; }
.contact-card.is-default { border-color: #409EFF !important; background: #FAFCFF; }
.card-badge {
  position: absolute; top: 0; right: 0;
  background: #409EFF; color: #fff; font-size: 11px;
  padding: 2px 12px; border-radius: 0 12px 0 12px;
}
.contact-body { padding: 8px 0; }
.contact-avatar {
  width: 56px; height: 56px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700; color: #fff;
  margin: 0 auto 10px;
}
.contact-name { font-size: 16px; font-weight: 700; color: #2C3E50; margin: 0 0 8px; }
.contact-phone { margin-bottom: 6px; }
.contact-relation { margin-bottom: 12px; }
.relation-tag { font-size: 12px; color: #909399; background: #F0F5FF; padding: 2px 10px; border-radius: 10px; }
.contact-actions { display: flex; justify-content: center; gap: 4px; border-top: 1px solid #F0F5FF; padding-top: 10px; }
.hotline-card { margin-top: 8px; }
.card-header { font-weight: 600; font-size: 15px; color: #2C3E50; }
.card-header i { margin-right: 6px; }
.hotline-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; background: #FFF5F5; border-radius: 10px; margin-bottom: 8px;
}
.hotline-name { font-size: 14px; color: #606266; }
.hotline-num { font-size: 16px; font-weight: 700; color: #F56C6C; letter-spacing: 0.5px; }
</style>
