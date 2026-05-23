<template>
  <div class="profile-page">
    <section class="profile-grid">
      <aside class="profile-card identity-card">
        <div class="avatar-shell">
          <img v-if="avatarPreview" :src="avatarPreview" alt="用户头像" class="avatar-image" />
          <span v-else>{{ userInitial }}</span>
          <div class="avatar-ring"></div>
        </div>
        <h2>{{ displayName }}</h2>
        <p class="username">@{{ userInfo ? userInfo.username : 'user' }}</p>
        <div class="profile-actions">
          <span class="role-pill" :class="{ admin: isAdmin }">{{ isAdmin ? '管理员' : '普通用户' }}</span>
          <label class="avatar-upload">
            <input type="file" accept="image/*" @change="handleAvatarChange" />
            <i class="el-icon-camera"></i>
            更换头像
          </label>
        </div>
        <p class="upload-tip">支持 JPG、PNG、WebP，大小不超过 2MB。</p>

        <div class="profile-facts">
          <div class="fact-row">
            <span>手机号</span>
            <strong>{{ form.phone || '未填写' }}</strong>
          </div>
          <div class="fact-row">
            <span>邮箱</span>
            <strong>{{ form.email || '未填写' }}</strong>
          </div>
          <div class="fact-row">
            <span>职业</span>
            <strong>{{ form.occupation || '未填写' }}</strong>
          </div>
        </div>
      </aside>

      <main class="profile-main">
        <section class="profile-card">
          <div class="card-title">
            <div>
              <p class="eyebrow">Profile</p>
              <h3>基础资料</h3>
            </div>
            <el-button size="small" plain icon="el-icon-refresh" @click="resetForm">重置</el-button>
          </div>

          <el-form ref="profileForm" :model="form" :rules="profileRules" label-position="top" class="profile-form">
            <div class="form-row">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model.trim="form.nickname" maxlength="30" show-word-limit placeholder="输入你的昵称" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model.trim="form.phone" maxlength="11" placeholder="例如 13800138000" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model.trim="form.email" maxlength="80" placeholder="name@example.com" />
              </el-form-item>
              <el-form-item label="职业" prop="occupation">
                <el-input v-model.trim="form.occupation" maxlength="50" placeholder="学生 / 教师 / 咨询师等" />
              </el-form-item>
            </div>

            <div class="form-row compact-row">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio-button :label="0">保密</el-radio-button>
                  <el-radio-button :label="1">男</el-radio-button>
                  <el-radio-button :label="2">女</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="form.age" :min="1" :max="120" controls-position="right" placeholder="年龄" />
              </el-form-item>
            </div>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model.trim="form.bio"
                type="textarea"
                :rows="4"
                maxlength="200"
                show-word-limit
                placeholder="简单介绍你的关注方向、当前状态或希望获得的支持。"
              />
            </el-form-item>

            <div class="form-actions">
              <el-button type="primary" icon="el-icon-check" :loading="saving" @click="saveProfile">
                保存资料
              </el-button>
            </div>
          </el-form>
        </section>

        <section class="profile-card security-card">
          <div class="card-title">
            <div>
              <p class="eyebrow">Security</p>
              <h3>修改密码</h3>
            </div>
          </div>

          <el-form ref="passwordForm" :model="pwdForm" :rules="passwordRules" label-position="top" class="profile-form">
            <div class="form-row">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-40位新密码" />
              </el-form-item>
            </div>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" plain icon="el-icon-lock" :loading="pwdSaving" @click="changePwd">
                更新密码
              </el-button>
            </div>
          </el-form>
        </section>
      </main>
    </section>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updateProfile, uploadAvatar, changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (!value) return callback()
      if (!/^1[3-9]\d{9}$/.test(value)) return callback(new Error('请输入正确的手机号'))
      callback()
    }
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) return callback(new Error('两次输入的新密码不一致'))
      callback()
    }
    return {
      form: this.emptyForm(),
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      saving: false,
      pwdSaving: false,
      avatarUploading: false,
      profileRules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 1, max: 30, message: '昵称长度需在1-30个字符之间', trigger: 'blur' }
        ],
        phone: [{ validator: validatePhone, trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
        occupation: [{ max: 50, message: '职业不能超过50个字符', trigger: 'blur' }],
        bio: [{ max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }]
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 40, message: '新密码长度需在6-40位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo', 'isAdmin']),
    displayName() {
      return this.form.nickname || (this.userInfo && (this.userInfo.nickname || this.userInfo.username)) || '用户'
    },
    avatarPreview() {
      return this.userInfo && this.userInfo.avatar ? this.userInfo.avatar : ''
    },
    userInitial() {
      const name = this.displayName || 'U'
      return name.charAt(0).toUpperCase()
    }
  },
  created() {
    this.resetForm()
  },
  methods: {
    emptyForm() {
      return {
        nickname: '',
        phone: '',
        email: '',
        gender: 0,
        age: undefined,
        occupation: '',
        bio: ''
      }
    },
    resetForm() {
      const info = this.userInfo || {}
      this.form = {
        nickname: info.nickname || info.username || '',
        phone: info.phone || '',
        email: info.email || '',
        gender: info.gender != null ? info.gender : 0,
        age: info.age || undefined,
        occupation: info.occupation || '',
        bio: info.bio || ''
      }
      this.$nextTick(() => {
        if (this.$refs.profileForm) this.$refs.profileForm.clearValidate()
      })
    },
    syncUserInfo(info) {
      const nextInfo = { ...(this.userInfo || {}), ...info }
      this.$store.commit('SET_USER_INFO', nextInfo)
      localStorage.setItem('mental_health_user', JSON.stringify(nextInfo))
    },
    saveProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        this.saving = true
        updateProfile(this.form)
          .then(res => {
            if (res.data) this.syncUserInfo(res.data)
            else this.syncUserInfo(this.form)
            this.$message.success('个人资料已保存')
          })
          .finally(() => {
            this.saving = false
          })
      })
    },
    handleAvatarChange(event) {
      const file = event.target.files && event.target.files[0]
      event.target.value = ''
      if (!file) return
      if (!file.type.startsWith('image/')) {
        this.$message.warning('请选择图片文件')
        return
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.warning('头像图片不能超过2MB')
        return
      }

      this.avatarUploading = true
      uploadAvatar(file)
        .then(res => {
          if (res.data) this.syncUserInfo(res.data)
          this.$message.success('头像已更新')
        })
        .finally(() => {
          this.avatarUploading = false
        })
    },
    changePwd() {
      this.$refs.passwordForm.validate(valid => {
        if (!valid) return
        this.pwdSaving = true
        changePassword({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        })
          .then(() => {
            this.$message.success('密码已修改，请重新登录')
            this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
            this.$store.dispatch('logout')
            this.$router.push('/login')
          })
          .finally(() => {
            this.pwdSaving = false
          })
      })
    }
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 24px 48px;
}

.eyebrow {
  margin: 0;
  color: #409eff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.profile-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

.profile-card {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(222, 233, 247, 0.95);
  border-radius: 16px;
  box-shadow: 0 14px 40px rgba(41, 80, 130, 0.07);
}

.identity-card {
  padding: 28px 24px;
  text-align: center;
  position: sticky;
  top: 82px;
}

.avatar-shell {
  width: 104px;
  height: 104px;
  margin: 0 auto 14px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  font-size: 38px;
  font-weight: 800;
  background: linear-gradient(135deg, #37b6a5, #409eff 58%, #6c63ff);
  overflow: hidden;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-ring {
  position: absolute;
  inset: 6px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.42);
  pointer-events: none;
}

.identity-card h2 {
  margin: 0;
  font-size: 22px;
  color: #223247;
}

.username {
  margin: 6px 0 12px;
  color: #8394aa;
  font-size: 13px;
}

.profile-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 18px auto 8px;
  flex-wrap: wrap;
}

.role-pill,
.avatar-upload {
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
  font-weight: 700;
  white-space: nowrap;
}

.role-pill {
  min-width: 88px;
  color: #2678d9;
  background: #edf6ff;
  border: 1px solid rgba(64, 158, 255, 0.22);
}

.role-pill.admin {
  color: #d76045;
  background: #fff1ed;
  border-color: rgba(215, 96, 69, 0.24);
}

.avatar-upload {
  gap: 7px;
  cursor: pointer;
  color: #2678d9;
  background: #edf6ff;
  border: 1px solid rgba(64, 158, 255, 0.24);
  transition: all 0.2s ease;
}

.avatar-upload:hover {
  color: #fff;
  background: #409eff;
  transform: translateY(-1px);
}

.avatar-upload input {
  display: none;
}

.upload-tip {
  margin: 0 0 18px;
  font-size: 12px;
  color: #8a9bb0;
}

.profile-facts {
  border-top: 1px solid #eef3f9;
  padding-top: 14px;
  text-align: left;
}

.fact-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  font-size: 13px;
  color: #7c8da3;
}

.fact-row strong {
  color: #2f4056;
  font-weight: 700;
  text-align: right;
  word-break: break-all;
}

.profile-main {
  display: grid;
  gap: 22px;
}

.profile-main .profile-card {
  padding: 24px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.card-title h3 {
  margin: 4px 0 0;
  color: #223247;
  font-size: 20px;
}

.profile-form :deep(.el-form-item__label) {
  color: #50647c;
  font-weight: 700;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.compact-row {
  align-items: end;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.security-card {
  margin-bottom: 12px;
}

@media (max-width: 920px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .identity-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .profile-page {
    padding: 18px 14px 36px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>
