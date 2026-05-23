<!-- 主布局 - 左侧导航 + 固定顶栏 -->
<!-- 替换文件：src/layouts/MainLayout.vue -->

<template>
  <div class="main-layout">
    <!-- ===== 左侧导航栏 (固定) ===== -->
    <div class="sidebar">
      <!-- Logo 区域 -->
      <div class="sidebar-logo">
        <div class="logo-icon">
          <span class="logo-emoji">🧠</span>
        </div>
        <div class="logo-text">
          <span class="logo-title">心理健康平台</span>
          <span class="logo-version">v1.0</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        background-color="transparent"
        text-color="#8C9AB7"
        active-text-color="#409EFF"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/home">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/assessment">
          <i class="el-icon-document"></i>
          <span>心理评估</span>
        </el-menu-item>
        <el-menu-item index="/article">
          <i class="el-icon-reading"></i>
          <span>健康知识</span>
        </el-menu-item>
        <el-menu-item index="/mood">
          <i class="el-icon-data-line"></i>
          <span>情绪记录</span>
        </el-menu-item>
        <el-menu-item index="/appointment">
          <i class="el-icon-date"></i>
          <span>预约咨询</span>
        </el-menu-item>
        <el-menu-item index="/diary">
          <i class="el-icon-notebook-2"></i>
          <span>心情日记</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <i class="el-icon-chat-dot-round"></i>
          <span>AI 共情</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <i class="el-icon-data-analysis"></i>
          <span>情绪周报</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-user"></i>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="/emergency">
          <i class="el-icon-warning-outline"></i>
          <span>紧急联系人</span>
        </el-menu-item>

        <!-- 管理员专属分组 -->
        <div class="menu-divider" v-if="isAdmin"></div>
        <div class="menu-group-label" v-if="isAdmin">管理</div>
        <el-menu-item index="/admin/dashboard" v-if="isAdmin">
          <i class="el-icon-monitor"></i>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users" v-if="isAdmin">
          <i class="el-icon-user"></i>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/assessments" v-if="isAdmin">
          <i class="el-icon-setting"></i>
          <span>量表管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/crisis" v-if="isAdmin">
          <i class="el-icon-warning"></i>
          <span>危机预警</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- ===== 右侧区域 ===== -->
    <div class="main-area">
      <!-- 顶部导航栏 (固定) -->
      <header class="top-header">
        <div class="header-left">
          <h1 class="header-page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <!-- 通知 -->
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="3" class="header-badge">
              <i class="el-icon-bell header-icon"></i>
            </el-badge>
          </el-tooltip>

          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="header-user">
              <div class="user-avatar">
                <img v-if="userInfo && userInfo.avatar" :src="userInfo.avatar" alt="用户头像" />
                <span v-else>{{ userInitial }}</span>
              </div>
              <div class="user-meta">
                <span class="user-name">{{ userInfo ? userInfo.nickname : '用户' }}</span>
                <span class="user-role">{{ userInfo && userInfo.role === 'admin' ? '管理员' : '用户' }}</span>
              </div>
              <i class="el-icon-arrow-down user-arrow"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile"><i class="el-icon-user"></i> 个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>

      <!-- 主体内容 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

const titleMap = {
  '/home': '首页',
  '/assessment': '心理评估',
  '/article': '健康知识',
  '/mood': '情绪记录',
  '/appointment': '预约咨询',
  '/diary': '心情日记',
  '/chat': 'AI 共情对话',
  '/report': '情绪周报',
  '/profile': '个人中心',
  '/emergency': '紧急联系人',
  '/admin/dashboard': '数据概览',
  '/admin/users': '用户管理',
  '/admin/assessments': '量表管理',
  '/admin/crisis': '危机预警'
}

export default {
  name: 'MainLayout',
  computed: {
    ...mapGetters(['isLoggedIn', 'userInfo', 'isAdmin']),

    activeMenu() {
      return this.$route.path
    },

    currentPageTitle() {
      return titleMap[this.$route.path] || '心理健康平台'
    },

    /** 用户头像首字 */
    userInitial() {
      const name = this.userInfo ? this.userInfo.nickname : '用'
      return name.charAt(0).toUpperCase()
    }
  },

  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
          this.$message.success('已安全退出')
        }).catch(() => {})
      } else if (command === 'profile') {
        if (this.$route.path !== '/profile') this.$router.push('/profile')
      }
    }
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.main-layout {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* ===== 左侧导航 ===== */
.sidebar {
  width: var(--sidebar-width, 230px);
  height: 100vh;
  background: var(--sidebar-bg, #1A2332);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

/* Logo */
.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #409EFF, #6C63FF);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-emoji {
  font-size: 18px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  color: #E8EDF5;
  letter-spacing: 0.3px;
}

.logo-version {
  font-size: 11px;
  color: #5A6A8A;
  margin-top: 1px;
}

/* 菜单 */
.sidebar-menu {
  flex: 1;
  border-right: none !important;
  padding: 12px 0;
  overflow-y: auto;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 10px;
  border-radius: 10px;
  font-size: 14px;
  padding: 0 16px !important;
  transition: all 0.2s ease;
}

.sidebar-menu .el-menu-item i {
  font-size: 18px;
  margin-right: 10px;
  color: inherit;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(64, 158, 255, 0.08) !important;
  color: #B0C5E5 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(64, 158, 255, 0.12) !important;
  color: #409EFF !important;
  font-weight: 600;
}

.menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 12px 20px;
}

.menu-group-label {
  font-size: 11px;
  color: #5A6A8A;
  padding: 0 20px 6px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* ===== 右侧区域 ===== */
.main-area {
  margin-left: var(--sidebar-width, 230px);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ===== 顶部导航 ===== */
.top-header {
  height: 60px;
  background: #FFFFFF;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #2C3E50);
  letter-spacing: -0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.header-icon:hover {
  color: #409EFF;
}

.header-badge :deep(.el-badge__content) {
  border: none;
  font-size: 11px;
}

/* 用户信息 */
.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 30px;
  transition: background 0.2s;
}

.header-user:hover {
  background: #F0F5FF;
}

.user-avatar {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #409EFF, #6C63FF);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #2C3E50);
}

.user-role {
  font-size: 11px;
  color: var(--text-muted, #909399);
}

.user-arrow {
  font-size: 12px;
  color: #C0C4CC;
}

/* ===== 主体内容 ===== */
.main-content {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-page, #F0F5FF);
  min-height: calc(100vh - 60px);
}
</style>
