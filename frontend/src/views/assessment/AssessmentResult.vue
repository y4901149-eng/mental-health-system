<!-- 评估结果页面 -->
<!-- 展示评估得分、等级和建议 -->

<template>
  <div class="page-container" style="max-width: 600px; margin: 0 auto; text-align: center;">
    <el-card>
      <div style="padding: 20px 0;">
        <!-- 结果图标 -->
        <div style="font-size: 64px; margin-bottom: 20px;">
          {{ levelIcon }}
        </div>

        <h2>评估结果</h2>
        <div style="font-size: 48px; color: #409EFF; margin: 20px 0;">
          {{ record.totalScore }} <span style="font-size: 16px;">分</span>
        </div>

        <!-- 等级标签 -->
        <el-tag :type="levelType" size="medium" style="margin-bottom: 20px;">
          {{ levelText }}
        </el-tag>

        <!-- 建议 -->
        <el-alert
          :title="record.suggestion"
          type="warning"
          :closable="false"
          show-icon
          style="text-align: left; margin-top: 20px;">
        </el-alert>

        <!-- 按钮 -->
        <div style="margin-top: 30px;">
          <el-button @click="$router.push('/assessment')">返回量表列表</el-button>
          <el-button type="primary" @click="$router.push('/home')">回到首页</el-button>
        </div>

        <div style="margin-top: 20px; font-size: 12px; color: #909399;">
          * 评估结果仅供参考，不构成医学诊断。如有需要请寻求专业帮助。
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAssessmentRecord } from '@/api/assessment'

const LEVEL_MAP = {
  normal: { icon: '😊', type: 'success', text: '正常' },
  mild: { icon: '🙂', type: 'warning', text: '轻度困扰' },
  moderate: { icon: '😐', type: 'warning', text: '中度困扰' },
  severe: { icon: '😔', type: 'danger', text: '重度困扰' }
}

export default {
  name: 'AssessmentResult',
  data() {
    return {
      record: {
        totalScore: 0,
        level: 'normal',
        suggestion: ''
      }
    }
  },
  computed: {
    levelInfo() {
      return LEVEL_MAP[this.record.level] || LEVEL_MAP.normal
    },
    levelIcon() { return this.levelInfo.icon },
    levelType() { return this.levelInfo.type },
    levelText() { return this.levelInfo.text }
  },
  created() {
    const id = this.$route.params.id
    if (id) {
      getAssessmentRecord(id).then(res => {
        if (res.data) this.record = res.data
      }).catch(() => {
        this.$message.error('获取评估结果失败')
      })
    }
  }
}
</script>
