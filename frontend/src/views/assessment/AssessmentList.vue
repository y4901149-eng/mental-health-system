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
      <el-radio-group v-model="activeCategory" size="small">
        <el-radio-button
          v-for="item in categories"
          :key="item.value"
          :label="item.value">
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </section>

    <div v-if="loading" class="loading-panel">
      <i class="el-icon-loading"></i>
      <span>正在加载测评列表...</span>
    </div>

    <el-empty
      v-else-if="filteredList.length === 0"
      description="暂无符合条件的测评"
      class="empty-panel">
    </el-empty>

    <section v-else class="assessment-grid">
      <article
        v-for="item in filteredList"
        :key="item.id"
        class="assessment-card"
        @click="startAssessment(item.id)">
        <div class="card-top">
          <el-tag size="mini" effect="plain">{{ getCategory(item.type) }}</el-tag>
          <span>{{ getQuestionCount(item) }} 题</span>
        </div>
        <h2>{{ item.title }}</h2>
        <p>{{ item.description || '暂无说明' }}</p>
        <div class="card-footer">
          <span>适用方向：{{ getTypeLabel(item.type) }}</span>
          <el-button type="text">开始测评</el-button>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
import { getAssessmentList } from '@/api/assessment'

const categoryMap = {
  depression: '情绪状态',
  anxiety: '情绪状态',
  mood_fluctuation: '情绪状态',
  emotion_regulation: '情绪状态',
  stress: '学习压力',
  academic: '学习压力',
  exam_anxiety: '学习压力',
  learning_motivation: '学习压力',
  time_management: '学习压力',
  sleep: '睡眠健康',
  routine: '睡眠健康',
  daytime_fatigue: '睡眠健康',
  social: '人际关系',
  relationship: '人际关系',
  support: '人际关系',
  dorm_relationship: '人际关系',
  communication: '人际关系',
  self_esteem: '自我认知',
  resilience: '自我认知',
  self_efficacy: '自我认知',
  life_satisfaction: '自我认知',
  wellbeing: '自我认知',
}

const typeLabelMap = {
  depression: '抑郁情绪',
  anxiety: '焦虑倾向',
  mood_fluctuation: '情绪波动',
  emotion_regulation: '情绪调节',
  stress: '压力感知',
  academic: '学习压力',
  exam_anxiety: '考试焦虑',
  learning_motivation: '学习动力',
  time_management: '时间管理',
  sleep: '睡眠健康',
  routine: '作息规律',
  daytime_fatigue: '日间疲劳',
  social: '社交焦虑',
  relationship: '人际关系',
  support: '人际支持',
  dorm_relationship: '宿舍关系',
  communication: '沟通表达',
  self_esteem: '自尊水平',
  resilience: '心理韧性',
  self_efficacy: '自我效能',
  life_satisfaction: '生活满意度',
  wellbeing: '心理幸福感'
}

export default {
  name: 'AssessmentList',
  data() {
    return {
      loading: false,
      list: [],
      activeCategory: 'all',
      categories: [
        { value: 'all', label: '全部' },
        { value: '情绪状态', label: '情绪状态' },
        { value: '学习压力', label: '学习压力' },
        { value: '睡眠健康', label: '睡眠健康' },
        { value: '人际关系', label: '人际关系' },
        { value: '自我认知', label: '自我认知' }
      ]
    }
  },
  computed: {
    filteredList() {
      if (this.activeCategory === 'all') return this.list
      return this.list.filter(item => this.getCategory(item.type) === this.activeCategory)
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    fetchList() {
      this.loading = true
      getAssessmentList().then(res => {
        this.list = res.data || []
      }).catch(() => {
        this.$message.error('测评列表加载失败，请稍后重试。')
      }).finally(() => {
        this.loading = false
      })
    },
    startAssessment(id) {
      this.$router.push('/assessment/' + id)
    },
    getCategory(type) {
      return categoryMap[type] || '情绪状态'
    },
    getTypeLabel(type) {
      return typeLabelMap[type] || type || '心理测评'
    },
    getQuestionCount(item) {
      return item.questionCount || item.question_count || (item.questions ? item.questions.length : 0) || 10
    }
  }
}
</script>

<style scoped>
.assessment-list-page {
  max-width: 1160px;
  margin: 0 auto;
  padding: 28px 24px 44px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}

.module-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 26px;
  line-height: 1.35;
}

.module-header p {
  margin: 8px 0 0;
  color: #6f7d8f;
  font-size: 14px;
}

.header-meta {
  min-width: 96px;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  text-align: center;
}

.header-meta strong {
  display: block;
  color: #2c3e50;
  font-size: 24px;
  line-height: 1;
}

.header-meta span {
  color: #7a8798;
  font-size: 12px;
}

.filter-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  padding: 14px 16px;
  margin-bottom: 18px;
}

.filter-label {
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.filter-panel ::v-deep .el-radio-button__inner {
  border-radius: 0 !important;
}

.assessment-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.assessment-card {
  min-height: 220px;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s;
}

.assessment-card:hover {
  border-color: #8bbcf3;
  transform: translateY(-2px);
}

.card-top,
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-top span,
.card-footer span {
  color: #7a8798;
  font-size: 12px;
}

.assessment-card h2 {
  margin: 18px 0 10px;
  color: #2c3e50;
  font-size: 18px;
  line-height: 1.45;
}

.assessment-card p {
  flex: 1;
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
}

.card-footer {
  margin-top: 18px;
  padding-top: 12px;
  border-top: 1px solid #edf1f7;
}

.loading-panel,
.empty-panel {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  padding: 48px 20px;
  text-align: center;
  color: #7a8798;
}

.loading-panel i {
  margin-right: 8px;
  color: #409eff;
}

@media (max-width: 980px) {
  .assessment-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .assessment-list-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .filter-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .assessment-grid {
    grid-template-columns: 1fr;
  }
}
</style>
