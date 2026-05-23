// Vue Router — 用户端 UserLayout / 管理员端 AdminLayout 分离

import Vue from 'vue'
import Router from 'vue-router'

import Login from '@/views/login/Login.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import Home from '@/views/Home.vue'

import AssessmentList from '@/views/assessment/AssessmentList.vue'
import AssessmentDetail from '@/views/assessment/AssessmentDetail.vue'
import AssessmentResult from '@/views/assessment/AssessmentResult.vue'
import ArticleList from '@/views/article/ArticleList.vue'
import ArticleDetail from '@/views/article/ArticleDetail.vue'
import Appointment from '@/views/appointment/Appointment.vue'
import MoodRecord from '@/views/mood/MoodRecord.vue'
import MoodChart from '@/views/mood/MoodChart.vue'
import Diary from '@/views/diary/Diary.vue'
import Chat from '@/views/chat/Chat.vue'
import Profile from '@/views/profile/Profile.vue'
import Emergency from '@/views/emergency/Emergency.vue'
import Report from '@/views/report/Report.vue'

import Dashboard from '@/views/admin/Dashboard.vue'
import UserManage from '@/views/admin/UserManage.vue'
import AssessmentManage from '@/views/admin/AssessmentManage.vue'
import CrisisManage from '@/views/admin/crisis/CrisisManage.vue'
import DiaryManage from '@/views/admin/diary/DiaryManage.vue'
import MoodManage from '@/views/admin/mood/MoodManage.vue'
import ReportManage from '@/views/admin/report/ReportManage.vue'
import ChatManage from '@/views/admin/chat/ChatManage.vue'
import AppointmentManage from '@/views/admin/appointment/AppointmentManage.vue'
import CounselorManage from '@/views/admin/counselor/CounselorManage.vue'
import ArticleManage from '@/views/admin/article/ArticleManage.vue'

Vue.use(Router)

function getRole() {
  try {
    const user = JSON.parse(localStorage.getItem('mental_health_user') || '{}')
    return user.role || 'user'
  } catch(e) { return 'user' }
}

const routes = [
  {
    path: '/login', name: 'Login', component: Login, meta: { title: '登录' }
  },
  // === 用户端 ===
  {
    path: '/', component: UserLayout, redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: Home, meta: { title: '首页' } },
      { path: 'assessment', name: 'AssessmentList', component: AssessmentList, meta: { title: '心理评估' } },
      { path: 'assessment/:id', name: 'AssessmentDetail', component: AssessmentDetail, meta: { title: '开始评估' } },
      { path: 'assessment/result/:id', name: 'AssessmentResult', component: AssessmentResult, meta: { title: '评估结果' } },
      { path: 'article', name: 'ArticleList', component: ArticleList, meta: { title: '心理健康知识' } },
      { path: 'article/:id', name: 'ArticleDetail', component: ArticleDetail, meta: { title: '文章详情' } },
      { path: 'appointment', name: 'Appointment', component: Appointment, meta: { title: '心理咨询预约' } },
      { path: 'diary', name: 'Diary', component: Diary, meta: { title: '心情日记' } },
      { path: 'mood', name: 'MoodRecord', component: MoodRecord, meta: { title: '情绪记录' } },
      { path: 'mood/chart', name: 'MoodChart', component: MoodChart, meta: { title: '情绪趋势' } },
      { path: 'chat', name: 'Chat', component: Chat, meta: { title: 'AI 共情对话' } },
      { path: 'profile', name: 'Profile', component: Profile, meta: { title: '个人中心' } },
      { path: 'emergency', name: 'Emergency', component: Emergency, meta: { title: '紧急联系人' } },
      { path: 'report', name: 'Report', component: Report, meta: { title: '情绪周报' } }
    ]
  },
  // === 管理员端 ===
  {
    path: '/admin', component: AdminLayout, redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '数据概览', role: 'admin' } },
      { path: 'users', name: 'UserManage', component: UserManage, meta: { title: '用户管理', role: 'admin' } },
      { path: 'assessments', name: 'AssessmentManage', component: AssessmentManage, meta: { title: '量表管理', role: 'admin' } },
      { path: 'crisis', name: 'CrisisManage', component: CrisisManage, meta: { title: '危机预警', role: 'admin' } },
      { path: 'diary', name: 'DiaryManage', component: DiaryManage, meta: { title: '日记管理', role: 'admin' } },
      { path: 'mood', name: 'MoodManage', component: MoodManage, meta: { title: '情绪管理', role: 'admin' } },
      { path: 'report', name: 'ReportManage', component: ReportManage, meta: { title: '周报管理', role: 'admin' } },
      { path: 'chat', name: 'ChatManage', component: ChatManage, meta: { title: '对话管理', role: 'admin' } },
      { path: 'articles', name: 'ArticleManage', component: ArticleManage, meta: { title: '健康知识管理', role: 'admin' } },
      { path: 'counselors', name: 'CounselorManage', component: CounselorManage, meta: { title: '心理老师管理', role: 'admin' } },
      { path: 'appointment', name: 'AppointmentManage', component: AppointmentManage, meta: { title: '预约管理', role: 'admin' } }
    ]
  }
]

const router = new Router({ mode: 'hash', routes })

// 全局静默处理所有导航异常
const originalPush = Router.prototype.push
Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(() => {})
}

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 心理健康平台' : '心理健康平台'

  const token = localStorage.getItem('mental_health_token')
  const role = getRole()

  // 未登录且不在登录页 → 去登录
  if (!token && to.path !== '/login') { next('/login'); return }

  // 已登录且在登录页 → 按角色去首页或管理后台
  if (token && to.path === '/login') {
    next(role === 'admin' ? '/admin/dashboard' : '/home')
    return
  }

  // 普通用户闯管理后台 → 回首页
  if (role !== 'admin' && to.path.startsWith('/admin')) { next('/home'); return }

  // 管理员闯用户端 → 回管理后台
  if (role === 'admin' && !to.path.startsWith('/admin')) { next('/admin/dashboard'); return }

  // 其余情况放行
  next()
})

export default router
