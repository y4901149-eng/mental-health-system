<!-- 登录/注册页 - 浅蓝色卡片风格 -->
<!-- 功能逻辑完全不变，仅替换 logo 图标为爱心心理健康图标 -->

<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
      <div class="circle c3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌展示区 -->
      <div class="brand-panel">
        <div class="brand-content">
          <!-- 爱心心理健康 SVG 图标（替换原来的🧠emoji） -->
          <div class="brand-icon">
            <svg viewBox="0 0 64 64" width="36" height="36" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="hg" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#fff" stop-opacity="0.95" />
                  <stop offset="100%" stop-color="#fff" stop-opacity="0.7" />
                </linearGradient>
              </defs>
              <!-- 心形 -->
              <path d="M32 50 C14 38 10 26 16 18 C22 10 28 10 32 18 C36 10 42 10 48 18 C54 26 50 38 32 50Z"
                fill="url(#hg)" />
              <!-- 脑电波 -->
              <path d="M14 22 Q18 18 22 22 Q26 26 30 22 Q34 18 38 22 Q42 26 46 22 Q50 18 52 22"
                fill="none" stroke="rgba(255,255,255,0.45)" stroke-width="1.5" stroke-linecap="round"
                transform="translate(0, -6)" />
            </svg>
          </div>
          <h1 class="brand-title">心晴港湾</h1>
          <p class="brand-desc">让情绪被看见，让支持更靠近</p>
          <div class="brand-features">
            <div class="brand-feature">
              <span class="bf-icon">✓</span>
              <span>专业心理评估量表</span>
            </div>
            <div class="brand-feature">
              <span class="bf-icon">✓</span>
              <span>科学情绪追踪分析</span>
            </div>
            <div class="brand-feature">
              <span class="bf-icon">✓</span>
              <span>在线心理咨询预约</span>
            </div>
            <div class="brand-feature">
              <span class="bf-icon">✓</span>
              <span>海量心理健康知识</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录/注册表单 -->
      <div class="form-panel">
        <div class="form-header">
          <h2>{{ isLoginMode ? '欢迎回来' : '创建账号' }}</h2>
          <p>{{ isLoginMode ? '登录心晴港湾，继续你的心理健康记录' : '加入心晴港湾，开启温柔的自我照护' }}</p>
        </div>

        <el-form
          ref="form"
          :model="form"
          :rules="rules"
          class="login-form"
        >
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              :placeholder="isLoginMode ? '请输入用户名' : '请设置用户名（2-20位）'"
              prefix-icon="el-icon-user"
              autocomplete="username"
              size="large"
              @keyup.enter.native="handleSubmit"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              :placeholder="isLoginMode ? '请输入密码' : '请设置密码（至少6位）'"
              prefix-icon="el-icon-lock"
              show-password
              :autocomplete="isLoginMode ? 'current-password' : 'new-password'"
              size="large"
              @keyup.enter.native="handleSubmit"
            />
          </el-form-item>

          <!-- 确认密码（注册模式） -->
          <el-form-item prop="confirmPassword" v-if="!isLoginMode">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="el-icon-lock"
              show-password
              autocomplete="new-password"
              size="large"
              @keyup.enter.native="handleSubmit"
            />
          </el-form-item>

          <!-- 昵称（注册模式） -->
          <el-form-item prop="nickname" v-if="!isLoginMode">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称（选填）"
              prefix-icon="el-icon-edit"
              size="large"
              @keyup.enter.native="handleSubmit"
            />
          </el-form-item>

          <!-- 邮箱（注册模式） -->
          <el-form-item prop="email" v-if="!isLoginMode">
            <el-input
              v-model.trim="form.email"
              placeholder="请输入邮箱（选填）"
              prefix-icon="el-icon-message"
              autocomplete="email"
              size="large"
              @keyup.enter.native="handleSubmit"
            />
          </el-form-item>

          <div v-if="isLoginMode" class="login-options">
            <el-checkbox v-model="rememberUsername">记住用户名</el-checkbox>
          </div>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              class="submit-btn"
              size="large"
              @click="handleSubmit"
            >
              {{ isLoginMode ? '登 录' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 切换登录/注册 -->
        <div class="switch-mode">
          <span v-if="isLoginMode">
            还没有账号？
            <a href="javascript:;" @click="switchMode">立即注册</a>
          </span>
          <span v-else>
            已有账号？
            <a href="javascript:;" @click="switchMode">立即登录</a>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/user'

const REMEMBER_USERNAME_KEY = 'mental_health_remember_username'

export default {
  name: 'Login',
  data() {
    return {
      isLoginMode: true,
      submitting: false,
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        nickname: '',
        email: ''
      },
      rememberUsername: false
    }
  },

  computed: {
    rules() {
      const baseRules = {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度2-20位', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 40, message: '密码长度6-40位', trigger: 'blur' }
        ]
      }

      if (!this.isLoginMode) {
        baseRules.confirmPassword = [
          { required: true, message: '请确认密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.form.password) {
                callback(new Error('两次密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
        baseRules.email = [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }

      return baseRules
    }
  },

  created() {
    const rememberedUsername = localStorage.getItem(REMEMBER_USERNAME_KEY)
    if (rememberedUsername) {
      this.form.username = rememberedUsername
      this.rememberUsername = true
    }
  },

  methods: {
    switchMode() {
      this.isLoginMode = !this.isLoginMode
      this.$refs.form.resetFields()
      if (this.isLoginMode && this.rememberUsername) {
        this.form.username = localStorage.getItem(REMEMBER_USERNAME_KEY) || ''
      }
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        this.submitting = true

        if (this.isLoginMode) {
          this.$store.dispatch('login', {
            username: this.form.username,
            password: this.form.password
          }).then(() => {
            this.saveRememberedUsername()
            this.$message.success('登录成功！')
            this.$router.push('/home').catch(() => {})
          }).catch(() => {}).finally(() => {
            this.submitting = false
          })
        } else {
          register({
            username: this.form.username,
            password: this.form.password,
            confirmPassword: this.form.confirmPassword,
            nickname: this.form.nickname || undefined,
            email: this.form.email || undefined
          }).then(() => {
            this.$message.success('注册成功！请登录')
            this.isLoginMode = true
            this.form.password = ''
            this.form.confirmPassword = ''
            this.form.email = ''
          }).catch(() => {}).finally(() => {
            this.submitting = false
          })
        }
      })
    },

    saveRememberedUsername() {
      if (this.rememberUsername) {
        localStorage.setItem(REMEMBER_USERNAME_KEY, this.form.username.trim())
      } else {
        localStorage.removeItem(REMEMBER_USERNAME_KEY)
      }
    }
  }
}
</script>

<style scoped>
/* ===== 页面背景 ===== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(64, 158, 255, 0.11) 0%, rgba(103, 194, 58, 0.08) 100%),
    #f4f8fb;
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰 ===== */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.5) 1px, transparent 1px),
    linear-gradient(180deg, rgba(255, 255, 255, 0.5) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.42;
}

.circle {
  display: none;
}

.c1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.08) 0%, transparent 70%);
  top: -150px;
  right: 10%;
}

.c2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(108, 99, 255, 0.06) 0%, transparent 70%);
  bottom: -100px;
  left: 5%;
}

.c3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.05) 0%, transparent 70%);
  top: 40%;
  left: 30%;
}

/* ===== 主容器 ===== */
.login-wrapper {
  display: flex;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(219, 232, 248, 0.95);
  border-radius: 22px;
  box-shadow: 0 24px 64px rgba(41, 80, 130, 0.16);
  overflow: hidden;
  width: 880px;
  max-width: 94vw;
  min-height: 520px;
  position: relative;
  z-index: 1;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  width: 400px;
  background:
    linear-gradient(145deg, rgba(40, 125, 218, 0.94) 0%, rgba(48, 152, 160, 0.92) 100%),
    #2678d9;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  position: relative;
}

.brand-panel::after {
  content: "";
  position: absolute;
  inset: auto 32px 32px 32px;
  height: 1px;
  background: rgba(255, 255, 255, 0.28);
}

.brand-content {
  color: #FFFFFF;
}

.brand-icon {
  width: 58px;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2);
  margin-bottom: 16px;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.3;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 32px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.9;
  padding: 9px 0;
}

.bf-icon {
  width: 22px;
  height: 22px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #24364b;
  margin-bottom: 6px;
}

.form-header p {
  font-size: 14px;
  color: #909399;
}

.login-form {
  max-width: 360px;
  margin: 0 auto;
  width: 100%;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__inner) {
  height: 46px !important;
  font-size: 14px;
  border-radius: 12px;
  border-color: #dbe8f8;
  background: #fbfdff;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.login-form :deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
}

.login-options {
  display: flex;
  justify-content: flex-start;
  max-width: 360px;
  margin: -4px auto 14px;
  color: #7b8ca5;
}

.submit-btn {
  width: 100%;
  height: 46px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 0;
  border-radius: 12px !important;
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.2);
}

.switch-mode {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #909399;
}

.switch-mode a {
  color: #409EFF;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.switch-mode a:hover {
  color: #6C63FF;
  text-decoration: underline;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 36px 28px;
  }
  .login-wrapper {
    width: 420px;
  }
}
</style>
