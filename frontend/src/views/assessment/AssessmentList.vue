<template>
  <div class="assessment-list-page">
    <section class="module-header">
      <div>
        <h1>心理测评</h1>
        <p>选择适合的自评项目，结果仅供自我了解和学习参考，不作为医学诊断。</p>
      </div>
      <div class="header-meta">
        <strong>{{ filteredList.length }}</strong>
        <span>项测评</span>
      </div>
    </section>

    <section class="filter-panel">
      <span class="filter-label">测评分类</span>
      <el-radio-group v-model="activeCategoryId" size="small">
        <el-radio-button :label="0">全部</el-radio-button>
        <el-radio-button v-for="item in categories" :key="item.id" :label="item.id">
          {{ item.name }}
        </el-radio-button>
      </el-radio-group>
    </section>

    <div v-if="loading" class="loading-panel">
      <i class="el-icon-loading"></i>
      <span>正在加载测评列表...</span>
    </div>

    <el-empty v-else-if="filteredList.length === 0" description="暂无符合条件的测评" class="empty-panel"></el-empty>

    <section v-else class="assessment-grid">
      <article v-for="item in filteredList" :key="item.id" class="assessment-card" @click="startAssessment(item.id)">
        <div class="card-top">
          <el-tag size="mini" effect="plain">{{ getCategoryName(item.categoryId) }}</el-tag>
          <span>{{ getQuestionCount(item) }} 题</span>
        </div>
        <h2>{{ item.title }}</h2>
        <p>{{ item.description || '暂无说明' }}</p>
        <div class="card-footer">
          <el-button type="text">开始测评</el-button>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
import { getAssessmentList, getAssessmentCategories } from '@/api/assessment'

export default {
  name: 'AssessmentList',
  data() {
    return {
      loading: false, list: [], activeCategoryId: 0, categories: []
    }
  },
  computed: {
    filteredList() {
      if (this.activeCategoryId === 0) return this.list
      return this.list.filter(item => item.categoryId === this.activeCategoryId)
    }
  },
  created() {
    this.fetchList()
    this.fetchCategories()
  },
  methods: {
    fetchList() {
      this.loading = true
      getAssessmentList().then(res => { this.list = res.data || [] }).catch(() => {
        this.$message.error('测评列表加载失败，请稍后重试。')
      }).finally(() => { this.loading = false })
    },
    fetchCategories() {
      getAssessmentCategories().then(res => { this.categories = res.data || [] }).catch(() => {})
    },
    startAssessment(id) { this.$router.push('/assessment/' + id) },
    getCategoryName(categoryId) {
      if (!categoryId) return '未分类'
      const c = this.categories.find(x => x.id === categoryId)
      return c ? c.name : '未分类'
    },
    getQuestionCount(item) {
      return item.questionCount || item.question_count || (item.questions ? item.questions.length : 0) || 10
    }
  }
}
</script>

<style scoped>
.assessment-list-page { max-width: 1100px; margin: 0 auto; padding: 24px 20px 40px; }
.module-header { display: flex; justify-content: space-between; align-items: center; gap: 20px; margin-bottom: 10px; }
.module-header h1 { margin: 0; color: #2C3E50; font-size: 28px; }
.module-header p { margin: 6px 0 0; color: #606266; font-size: 14px; }
.header-meta { background: #F0F5FF; border-radius: 10px; padding: 10px 18px; text-align: center; }
.header-meta strong { display: block; font-size: 24px; color: #409EFF; }
.header-meta span { font-size: 12px; color: #909399; }
.filter-panel { display: flex; align-items: center; gap: 14px; margin: 18px 0; flex-wrap: wrap; }
.filter-label { font-size: 14px; color: #606266; white-space: nowrap; }
.loading-panel { text-align: center; padding: 60px 0; color: #909399; }
.assessment-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.assessment-card { border: 1px solid #E8ECF4; border-radius: 12px; padding: 20px; cursor: pointer; background: #fff; transition: box-shadow 0.2s; }
.assessment-card:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06); border-color: #C8D6E5; }
.assessment-card h2 { font-size: 17px; margin: 10px 0 8px; color: #2C3E50; }
.assessment-card p { font-size: 13px; color: #606266; line-height: 1.6; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-top { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.card-footer { margin-top: 14px; display: flex; justify-content: flex-end; }
@media (max-width: 860px) { .assessment-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 540px) { .assessment-grid { grid-template-columns: 1fr; } }
</style>
