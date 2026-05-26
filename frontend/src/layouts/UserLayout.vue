<!-- 用户端布局 - 只有用户菜单 -->
<template>
  <div class="main-layout">
    <div class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 64 64" width="22" height="22" xmlns="http://www.w3.org/2000/svg">
            <path d="M32 50 C14 38 10 26 16 18 C22 10 28 10 32 18 C36 10 42 10 48 18 C54 26 50 38 32 50Z"
              fill="#fff" opacity="0.95" />
            <path d="M14 22 Q18 18 22 22 Q26 26 30 22 Q34 18 38 22 Q42 26 46 22 Q50 18 52 22"
              fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.8" stroke-linecap="round"
              transform="translate(0, -6)" />
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">心晴港湾</span>
          <span class="logo-version">v1.0</span>
        </div>
      </div>
      <el-menu :default-active="activeMenu" background-color="transparent"
        text-color="#8C9AB7" active-text-color="#409EFF" router class="sidebar-menu">
        <el-menu-item index="/home"><i class="el-icon-s-home"></i><span>首页</span></el-menu-item>
        <el-menu-item index="/assessment"><i class="el-icon-document"></i><span>心理评估</span></el-menu-item>
        <el-menu-item index="/article"><i class="el-icon-reading"></i><span>健康知识</span></el-menu-item>
        <el-menu-item index="/mood"><i class="el-icon-data-line"></i><span>情绪记录</span></el-menu-item>
        <el-menu-item index="/diary"><i class="el-icon-notebook-2"></i><span>心情日记</span></el-menu-item>
        <el-menu-item index="/report"><i class="el-icon-data-analysis"></i><span>情绪周报</span></el-menu-item>
        <el-menu-item index="/chat"><i class="el-icon-chat-dot-round"></i><span>AI 共情</span></el-menu-item>
        <el-menu-item index="/appointment"><i class="el-icon-date"></i><span>预约咨询</span></el-menu-item>
        <el-menu-item index="/profile"><i class="el-icon-user"></i><span>个人中心</span></el-menu-item>
        <el-menu-item index="/emergency"><i class="el-icon-warning-outline"></i><span>紧急联系人</span></el-menu-item>
      </el-menu>
    </div>
    <div class="main-area">
      <header class="top-header">
        <div class="header-left">
          <h1 class="header-page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="header-user">
              <div class="user-avatar">
                <img v-if="userInfo && userInfo.avatar" :src="userInfo.avatar" alt="用户头像" />
                <span v-else>{{ userInitial }}</span>
              </div>
              <div class="user-meta">
                <span class="user-name">{{ userInfo ? userInfo.nickname : '用户' }}</span>
                <span class="user-role">用户</span>
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
      <main class="main-content"><router-view /></main>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
const titleMap = {
  '/home':'首页','/assessment':'心理评估','/article':'健康知识','/mood':'情绪记录',
  '/appointment':'预约咨询','/diary':'心情日记','/chat':'AI 共情对话','/report':'情绪周报',
  '/profile':'个人中心','/emergency':'紧急联系人'
}
export default {
  name: 'UserLayout',
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() { return this.$route.path },
    currentPageTitle() { return titleMap[this.$route.path] || '心晴港湾' },
    userInitial() {
      const name = this.userInfo ? this.userInfo.nickname : '用'
      return name.charAt(0).toUpperCase()
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？','提示',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'})
          .then(() => { this.$store.dispatch('logout'); this.$router.push('/login'); this.$message.success('已安全退出') })
          .catch(() => {})
      } else if (command === 'profile') {
        if (this.$route.path !== '/profile') this.$router.push('/profile')
      }
    }
  }
}
</script>
<style scoped>
.main-layout { height: 100vh; display: flex; overflow: hidden; }
.sidebar {
  width: var(--sidebar-width, 230px); height: 100vh; background: var(--sidebar-bg, #1A2332);
  display: flex; flex-direction: column; flex-shrink: 0; position: fixed; left: 0; top: 0; z-index: 100;
}
.sidebar-logo {
  height: 60px; display: flex; align-items: center; padding: 0 20px; gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.logo-icon { width: 36px; height: 36px; background: linear-gradient(135deg,#409EFF,#6C63FF); border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.logo-text { display: flex; flex-direction: column; }
.logo-title { font-size: 15px; font-weight: 700; color: #E8EDF5; letter-spacing: 0.3px; }
.logo-version { font-size: 11px; color: #5A6A8A; margin-top: 1px; }
.sidebar-menu { flex: 1; border-right: none !important; padding: 12px 0; overflow-y: auto; }
.sidebar-menu .el-menu-item { height: 44px; line-height: 44px; margin: 2px 10px; border-radius: 10px; font-size: 14px; padding: 0 16px !important; transition: all 0.2s ease; }
.sidebar-menu .el-menu-item i { font-size: 18px; margin-right: 10px; color: inherit; }
.sidebar-menu .el-menu-item:hover { background: rgba(64,158,255,0.08) !important; color: #B0C5E5 !important; }
.sidebar-menu .el-menu-item.is-active { background: rgba(64,158,255,0.12) !important; color: #409EFF !important; font-weight: 600; }
.main-area { margin-left: var(--sidebar-width, 230px); flex: 1; display: flex; flex-direction: column; min-height: 100vh; }
.top-header {
  height: 60px; background: #FFFFFF; border-bottom: 1px solid #EBEEF5; display: flex;
  align-items: center; justify-content: space-between; padding: 0 28px; position: sticky; top: 0; z-index: 50;
}
.header-page-title { font-size: 18px; font-weight: 600; color: #2C3E50; }
.header-right { display: flex; align-items: center; gap: 20px; }
.header-user { display: flex; align-items: center; gap: 10px; cursor: pointer; padding: 4px 10px 4px 4px; border-radius: 30px; transition: background 0.2s; }
.header-user:hover { background: #F0F5FF; }
.user-avatar { width: 34px; height: 34px; background: linear-gradient(135deg,#409EFF,#6C63FF); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; color: #fff; overflow: hidden; }
.user-avatar img { width: 100%; height: 100%; object-fit: cover; display: block; }
.user-meta { display: flex; flex-direction: column; line-height: 1.3; }
.user-name { font-size: 13px; font-weight: 600; color: #2C3E50; }
.user-role { font-size: 11px; color: #909399; }
.user-arrow { font-size: 12px; color: #C0C4CC; }
.main-content { flex: 1; overflow-y: auto; background: #F0F5FF; min-height: calc(100vh - 60px); }
</style>
