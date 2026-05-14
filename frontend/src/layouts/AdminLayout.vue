<!-- 管理员布局 - 只有管理员菜单 -->
<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon"><span class="logo-emoji">⚙️</span></div>
        <div class="logo-text">
          <span class="logo-title">管理后台</span>
          <span class="logo-version">v1.0</span>
        </div>
      </div>
      <el-menu :default-active="activeMenu" background-color="transparent"
        text-color="#8C9AB7" active-text-color="#409EFF" router class="sidebar-menu">
        <el-menu-item index="/admin/dashboard"><i class="el-icon-monitor"></i><span>数据概览</span></el-menu-item>
        <el-menu-item index="/admin/users"><i class="el-icon-user"></i><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/assessments"><i class="el-icon-setting"></i><span>量表管理</span></el-menu-item>
        <el-menu-item index="/admin/crisis"><i class="el-icon-warning"></i><span>危机预警</span></el-menu-item>
        <el-menu-item index="/admin/appointment"><i class="el-icon-date"></i><span>预约管理</span></el-menu-item>
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
              <div class="user-avatar">{{ userInitial }}</div>
              <div class="user-meta">
                <span class="user-name">{{ userInfo ? userInfo.nickname : '管理员' }}</span>
                <span class="user-role">管理员</span>
              </div>
              <i class="el-icon-arrow-down user-arrow"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout"><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
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
  '/admin/dashboard':'数据概览','/admin/users':'用户管理',
  '/admin/assessments':'量表管理','/admin/crisis':'危机预警',
  '/admin/diary':'日记管理','/admin/mood':'情绪管理','/admin/report':'周报管理','/admin/chat':'对话管理','/admin/appointment':'预约管理'
}
export default {
  name: 'AdminLayout',
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() { return this.$route.path },
    currentPageTitle() { return titleMap[this.$route.path] || '管理后台' },
    userInitial() {
      const name = this.userInfo ? this.userInfo.nickname : '管'
      return name.charAt(0).toUpperCase()
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？','提示',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'})
          .then(() => { this.$store.dispatch('logout'); this.$router.push('/login'); this.$message.success('已安全退出') })
          .catch(() => {})
      }
    }
  }
}
</script>
<style scoped>
.admin-layout { height: 100vh; display: flex; overflow: hidden; }
.sidebar {
  width: var(--sidebar-width, 230px); height: 100vh; background: var(--sidebar-bg, #1A2332);
  display: flex; flex-direction: column; flex-shrink: 0; position: fixed; left: 0; top: 0; z-index: 100;
}
.sidebar-logo {
  height: 60px; display: flex; align-items: center; padding: 0 20px; gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.logo-icon { width: 36px; height: 36px; background: linear-gradient(135deg,#F56C6C,#E6A23C); border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.logo-emoji { font-size: 18px; }
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
.user-avatar { width: 34px; height: 34px; background: linear-gradient(135deg,#F56C6C,#E6A23C); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; color: #fff; }
.user-meta { display: flex; flex-direction: column; line-height: 1.3; }
.user-name { font-size: 13px; font-weight: 600; color: #2C3E50; }
.user-role { font-size: 11px; color: #909399; }
.user-arrow { font-size: 12px; color: #C0C4CC; }
.main-content { flex: 1; overflow-y: auto; background: #F0F5FF; min-height: calc(100vh - 60px); }
</style>
