<!-- 登录/注册页 - 浅蓝色卡片风格 -->
<!-- 替换文件：src/views/login/Login.vue -->
<!-- 功能逻辑完全不变，仅重构 UI -->

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
          <div class="brand-icon">🧠</div>
          <h1 class="brand-title">心理健康辅助干预平台</h1>
          <p class="brand-desc">关注心理健康 · 守护美好生活</p>
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
          <p>{{ isLoginMode ? '请登录您的账号以继续' : '加入我们，关注心理健康' }}</p>
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
              :placeholder="isLoginMode ? '请输入用户名' : '请设置用户名（3-20位）'"
              prefix-icon="el-icon-user"
              size="large"
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
              size="large"
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
              size="large"
            />
          </el-form-item>

          <!-- 昵称（注册模式） -->
          <el-form-item prop="nickname" v-if="!isLoginMode">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称（选填）"
              prefix-icon="el-icon-edit"
              size="large"
            />
          </el-form-item>

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
        nickname: ''
      }
    }
  },

  computed: {
    rules() {
      const baseRules = {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
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
      }

      return baseRules
    }
  },

  methods: {
    switchMode() {
      this.isLoginMode = !this.isLoginMode
      this.$refs.form.resetFields()
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
            nickname: this.form.nickname || undefined
          }).then(() => {
            this.$message.success('注册成功！请登录')
            this.isLoginMode = true
            this.form.password = ''
            this.form.confirmPassword = ''
          }).catch(() => {}).finally(() => {
            this.submitting = false
          })
        }
      })
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
  background: linear-gradient(135deg, #EAF3FF 0%, #F0F5FF 50%, #EDF0FF 100%);
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰 ===== */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
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
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(64, 158, 255, 0.10);
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
  background: linear-gradient(135deg, #409EFF 0%, #6C63FF 100%);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.brand-content {
  color: #FFFFFF;
}

.brand-icon {
  font-size: 48px;
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
  font-size: 24px;
  font-weight: 700;
  color: #2C3E50;
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
}

.submit-btn {
  width: 100%;
  height: 46px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 2px;
  border-radius: 12px !important;
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
