<!-- 个人中心 -->
<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">👤 个人中心</h1>
      <p class="page-subtitle">管理你的个人信息</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：头像卡片 -->
      <el-col :span="8">
        <el-card shadow="never" class="avatar-card">
          <div class="avatar-wrap">
            <div class="avatar-circle">{{ userInitial }}</div>
            <h3>{{ userInfo ? userInfo.nickname : '用户' }}</h3>
            <el-tag size="small" :type="userInfo && userInfo.role === 'admin' ? 'danger' : 'primary'">
              {{ userInfo && userInfo.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <el-divider></el-divider>
          <div class="info-list">
            <div class="info-item"><span>用户名</span><span>{{ userInfo ? userInfo.username : '-' }}</span></div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑表单 -->
      <el-col :span="16">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-edit" style="color:#409EFF;"></i> 编辑资料</span>
          </div>
          <el-form :model="form" label-width="100px" size="medium">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="输入昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="输入邮箱" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="0">保密</el-radio>
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="简单介绍一下自己" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card shadow="never" style="margin-top:20px;">
          <div slot="header" class="card-header">
            <span><i class="el-icon-lock" style="color:#6C63FF;"></i> 修改密码</span>
          </div>
          <el-form :model="pwdForm" label-width="100px" size="medium">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码（至少6位）" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePwd" :loading="pwdSaving">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updateProfile } from '@/api/user'
import { changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    return {
      form: { nickname: '', phone: '', email: '', gender: 0, bio: '' },
      pwdForm: { oldPassword: '', newPassword: '' },
      saving: false,
      pwdSaving: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    userInitial() {
      const name = this.userInfo ? this.userInfo.nickname || this.userInfo.username : 'U'
      return name.charAt(0).toUpperCase()
    }
  },
  created() {
    if (this.userInfo) {
      this.form = {
        nickname: this.userInfo.nickname || '',
        phone: this.userInfo.phone || '',
        email: this.userInfo.email || '',
        gender: this.userInfo.gender != null ? this.userInfo.gender : 0,
        bio: this.userInfo.bio || ''
      }
    }
  },
  methods: {
    saveProfile() {
      this.saving = true
      updateProfile(this.form).then(() => {
        this.$message.success('资料已更新')
        // 更新 store
        const info = { ...this.userInfo, ...this.form }
        this.$store.commit('SET_USER_INFO', info)
        localStorage.setItem('mental_health_user', JSON.stringify(info))
      }).finally(() => { this.saving = false })
    },
    changePwd() {
      if (!this.pwdForm.oldPassword || !this.pwdForm.newPassword) {
        this.$message.warning('请填写完整')
        return
      }
      if (this.pwdForm.newPassword.length < 6) {
        this.$message.warning('新密码至少6位')
        return
      }
      this.pwdSaving = true
      changePassword(this.pwdForm).then(() => {
        this.$message.success('密码已修改，请重新登录')
        this.pwdForm = { oldPassword: '', newPassword: '' }
      }).finally(() => { this.pwdSaving = false })
    },
  }
}
</script>

<style scoped>
.profile-page {
  max-width: var(--page-width, 1200px); margin: 0 auto; padding: 0 24px 40px;
}
.page-header {
  display: flex; flex-direction: column; margin-bottom: 24px; padding-top: 28px;
}
.page-title { font-size: 22px; font-weight: 700; color: #2C3E50; }
.page-subtitle { font-size: 14px; color: #909399; margin-top: 4px; }
.avatar-card { text-align: center; }
.avatar-wrap { padding: 20px 0 10px; }
.avatar-circle {
  width: 80px; height: 80px; background: linear-gradient(135deg, #409EFF, #6C63FF);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: 700; color: #fff; margin: 0 auto 12px;
}
.avatar-wrap h3 { margin: 0 0 8px; font-size: 18px; color: #2C3E50; }
.info-list { font-size: 13px; color: #606266; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; }
.card-header { font-weight: 600; font-size: 15px; color: #2C3E50; }
.card-header i { margin-right: 6px; }
</style>
