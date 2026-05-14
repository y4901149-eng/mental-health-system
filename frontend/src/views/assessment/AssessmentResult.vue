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
    levelIcon() {
      const map = { normal: '😊', mild: '🙂', moderate: '😐', severe: '😔' }
      return map[this.record.level] || '😊'
    },
    levelType() {
      const map = { normal: 'success', mild: 'warning', moderate: 'warning', severe: 'danger' }
      return map[this.record.level] || 'info'
    },
    levelText() {
      const map = { normal: '正常', mild: '轻度困扰', moderate: '中度困扰', severe: '重度困扰' }
      return map[this.record.level] || '未知'
    }
  },
  created() {
    // 从路由参数获取评估结果 (简化版，实际应该调API获取)
    if (this.$route.query) {
      // 将在后端对接时完善
    }
  }
}
</script>
