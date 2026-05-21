<!-- 紧急联系人 -->
<template>
  <div class="emergency-page">
    <section class="emergency-hero">
      <div>
        <p class="eyebrow">安全支持</p>
        <h2>紧急联系人</h2>
        <p class="hero-copy">提前维护可信联系人，在需要支持时更快找到可以求助的人。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="openCreateDialog">
        新增联系人
      </el-button>
    </section>

    <section class="default-panel" v-if="defaultContact">
      <div class="default-mark">默认</div>
      <div class="default-info">
        <p class="eyebrow">优先联系</p>
        <h3>{{ defaultContact.name }}</h3>
        <span>{{ defaultContact.relation || '未填写关系' }}</span>
      </div>
      <div class="default-actions">
        <a class="phone-link" :href="'tel:' + defaultContact.phone">
          <i class="el-icon-phone"></i>
          {{ defaultContact.phone }}
        </a>
        <el-button size="mini" plain @click="editContact(defaultContact)">编辑</el-button>
      </div>
    </section>

    <section class="safety-guide">
      <div class="guide-item">
        <span>1</span>
        <div>
          <strong>维护可信联系人</strong>
          <small>建议至少添加一位能及时响应的家人、朋友或咨询师。</small>
        </div>
      </div>
      <div class="guide-item">
        <span>2</span>
        <div>
          <strong>设置默认联系人</strong>
          <small>默认联系人会优先展示，紧急时减少查找时间。</small>
        </div>
      </div>
      <div class="guide-item">
        <span>3</span>
        <div>
          <strong>必要时拨打热线</strong>
          <small>无法联系身边的人时，可以直接使用下方援助热线。</small>
        </div>
      </div>
    </section>

    <section class="contact-section">
      <div class="section-header">
        <div>
          <p class="eyebrow">我的联系人</p>
          <h3>联系人列表</h3>
        </div>
        <el-button plain size="small" icon="el-icon-refresh" :loading="loading" @click="fetchList">
          刷新
        </el-button>
      </div>

      <div v-loading="loading" class="contact-grid">
        <article
          v-for="contact in contactList"
          :key="contact.id"
          class="contact-card"
          :class="{ 'is-default': contact.isDefault === 1 }"
        >
          <div v-if="contact.isDefault === 1" class="card-badge">默认</div>
          <div class="contact-avatar" :style="{ background: getAvatarColor(contact.name) }">
            {{ firstChar(contact.name) }}
          </div>
          <h3>{{ contact.name }}</h3>
          <p>{{ contact.relation || '未填写关系' }}</p>
          <a class="contact-phone" :href="'tel:' + contact.phone">{{ contact.phone }}</a>
          <div class="contact-actions">
            <el-button v-if="contact.isDefault !== 1" type="text" size="mini" @click="setDefault(contact.id)">
              设为默认
            </el-button>
            <el-button type="text" size="mini" @click="editContact(contact)">编辑</el-button>
            <el-button type="text" size="mini" class="danger-text" @click="handleDelete(contact)">删除</el-button>
          </div>
        </article>

        <div v-if="!loading && contactList.length === 0" class="empty-card">
          <div class="empty-icon">SOS</div>
          <h3>还没有紧急联系人</h3>
          <p>添加一位家人、朋友或咨询师，方便在需要时快速求助。</p>
          <el-button type="primary" size="small" @click="openCreateDialog">添加联系人</el-button>
        </div>
      </div>
    </section>

    <section class="hotline-panel">
      <div class="section-header">
        <div>
          <p class="eyebrow">24 小时支持</p>
          <h3>心理援助热线</h3>
        </div>
      </div>
      <div class="hotline-grid">
        <a v-for="hotline in hotlines" :key="hotline.name" class="hotline-item" :href="'tel:' + hotline.number">
          <span>{{ hotline.name }}</span>
          <strong>{{ hotline.number }}</strong>
        </a>
      </div>
    </section>

    <el-dialog
      :title="isEdit ? '编辑联系人' : '新增联系人'"
      :visible.sync="showDialog"
      width="430px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="form" :model="form" :rules="rules" label-position="top" size="medium">
        <el-form-item label="姓名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="关系" prop="relation">
          <el-select v-model="form.relation" placeholder="选择或输入关系" style="width: 100%;" allow-create filterable>
            <el-option v-for="relation in relationOptions" :key="relation" :label="relation" :value="relation" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault" :disabled="isEditingDefault">
            {{ isEditingDefault ? '当前默认联系人' : '设为默认联系人' }}
          </el-checkbox>
          <p v-if="isEditingDefault" class="form-tip">如需更换默认联系人，请在其他联系人卡片中点击“设为默认”。</p>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ isEdit ? '保存修改' : '添加联系人' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getEmergencyList,
  createEmergency,
  updateEmergency,
  deleteEmergency,
  setDefaultEmergency
} from '@/api/emergency'

const avatarColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#8E7CF6', '#13C2C2']

export default {
  name: 'Emergency',
  data() {
    return {
      contactList: [],
      defaultContact: null,
      showDialog: false,
      isEdit: false,
      submitting: false,
      loading: false,
      editingId: null,
      form: this.emptyForm(),
      relationOptions: ['家人', '朋友', '同学', '同事', '医生', '咨询师'],
      hotlines: [
        { name: '全国心理援助热线', number: '12356' },
        { name: '生命热线', number: '400-821-1215' },
        { name: '北京心理危机干预热线', number: '800-810-1117' },
        { name: '上海心理援助热线', number: '021-962525' }
      ],
      rules: {
        name: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    isEditingDefault() {
      return this.isEdit && this.form.isDefault
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    emptyForm() {
      return { name: '', phone: '', relation: '', isDefault: false }
    },

    fetchList() {
      this.loading = true
      return getEmergencyList()
        .then(listRes => {
          this.contactList = listRes.data || []
          this.defaultContact = this.contactList.find(contact => contact.isDefault === 1) || null
        })
        .finally(() => {
          this.loading = false
        })
    },

    openCreateDialog() {
      this.isEdit = false
      this.editingId = null
      this.form = this.emptyForm()
      this.showDialog = true
    },

    editContact(contact) {
      this.isEdit = true
      this.editingId = contact.id
      this.form = {
        name: contact.name,
        phone: contact.phone,
        relation: contact.relation || '',
        isDefault: contact.isDefault === 1
      }
      this.showDialog = true
    },

    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid || this.submitting) return
        this.submitting = true
        const payload = {
          id: this.editingId,
          name: this.form.name,
          phone: this.form.phone,
          relation: this.form.relation,
          isDefault: this.form.isDefault ? 1 : 0
        }
        const action = this.isEdit ? updateEmergency(payload) : createEmergency(payload)
        action
          .then(() => {
            this.$message.success(this.isEdit ? '联系人已更新。' : '联系人已添加。')
            this.showDialog = false
            this.fetchList()
          })
          .finally(() => {
            this.submitting = false
          })
      })
    },

    setDefault(id) {
      setDefaultEmergency(id).then(() => {
        this.$message.success('已设为默认联系人。')
        this.fetchList()
      })
    },

    handleDelete(contact) {
      this.$confirm('确定删除联系人“' + contact.name + '”吗？', '删除联系人', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteEmergency(contact.id).then(() => {
          this.$message.success('联系人已删除。')
          this.fetchList()
        })
      }).catch(() => {})
    },

    resetForm() {
      this.form = this.emptyForm()
      this.editingId = null
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },

    firstChar(name) {
      return name ? name.charAt(0) : '联'
    },

    getAvatarColor(name) {
      if (!name) return avatarColors[0]
      return avatarColors[name.charCodeAt(0) % avatarColors.length]
    }
  }
}
</script>

<style scoped>
.emergency-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 24px 36px;
  color: #24364b;
}

.emergency-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 24px;
  border: 1px solid rgba(255, 214, 204, 0.9);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%),
    #fff;
  box-shadow: 0 14px 34px rgba(120, 55, 55, 0.08);
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #7b8ca5;
  font-size: 12px;
}

.emergency-hero h2,
.section-header h3,
.default-info h3,
.contact-card h3,
.empty-card h3 {
  margin: 0;
  color: #24364b;
}

.emergency-hero h2 {
  font-size: 28px;
  letter-spacing: 0;
}

.hero-copy {
  margin: 8px 0 0;
  color: #6d7d93;
}

.default-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #fff7f5 0%, #fff 100%);
  border: 1px solid #ffd6cc;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgba(245, 108, 108, 0.1);
}

.default-mark {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.default-info {
  flex: 1;
  min-width: 0;
}

.default-info span {
  color: #7b8ca5;
  font-size: 13px;
}

.phone-link,
.contact-phone {
  color: #f56c6c;
  font-weight: 700;
  text-decoration: none;
}

.default-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.phone-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #ffd6cc;
  transition: transform 0.18s, box-shadow 0.18s;
}

.phone-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(245, 108, 108, 0.12);
}

.safety-guide {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.guide-item {
  display: flex;
  gap: 10px;
  padding: 15px;
  background: #fff;
  border: 1px solid rgba(231, 237, 246, 0.95);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(41, 80, 130, 0.06);
  transition: transform 0.18s, box-shadow 0.18s;
}

.guide-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(41, 80, 130, 0.08);
}

.guide-item span {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.guide-item strong {
  display: block;
  margin-bottom: 4px;
  color: #24364b;
}

.guide-item small {
  color: #7b8ca5;
  line-height: 1.5;
}

.contact-section,
.hotline-panel {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(231, 237, 246, 0.95);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 18px;
  box-shadow: 0 14px 32px rgba(41, 80, 130, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.contact-grid {
  min-height: 160px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 14px;
}

.contact-card {
  position: relative;
  padding: 20px 16px 15px;
  border: 1px solid rgba(231, 237, 246, 0.95);
  border-radius: 16px;
  text-align: center;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}

.contact-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(41, 80, 130, 0.1);
  border-color: #d6e6f8;
}

.contact-card.is-default {
  border-color: #f56c6c;
  background: linear-gradient(180deg, #fff 0%, #fff7f5 100%);
}

.card-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 3px 12px;
  color: #fff;
  background: #f56c6c;
  border-radius: 0 16px 0 12px;
  font-size: 12px;
}

.contact-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.contact-card p {
  margin: 6px 0 8px;
  color: #7b8ca5;
  font-size: 13px;
}

.contact-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #e7edf6;
}

.danger-text {
  color: #f56c6c;
}

.empty-card {
  grid-column: 1 / -1;
  padding: 34px 16px;
  text-align: center;
  color: #7b8ca5;
}

.empty-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: #fff2f0;
  color: #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.hotline-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.hotline-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 14px;
  background: linear-gradient(180deg, #fff 0%, #fff7f5 100%);
  border: 1px solid #ffd6cc;
  border-radius: 14px;
  text-decoration: none;
  transition: transform 0.18s, box-shadow 0.18s;
}

.hotline-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(245, 108, 108, 0.1);
}

.emergency-page :deep(.el-input__inner) {
  border-radius: 12px;
  border-color: #dbe8f8;
  background: #fbfdff;
}

.hotline-item span {
  color: #5f6f84;
}

.hotline-item strong {
  color: #f56c6c;
}

.form-tip {
  margin: 6px 0 0;
  color: #8a98aa;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 760px) {
  .emergency-hero,
  .default-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .default-actions {
    justify-content: flex-start;
  }

  .safety-guide {
    grid-template-columns: 1fr;
  }
}
</style>
