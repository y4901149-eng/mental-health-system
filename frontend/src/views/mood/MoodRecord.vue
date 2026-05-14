<!-- 情绪记录页面 -->

<template>
  <div class="page-container" style="max-width: 600px; margin: 0 auto;">
    <el-card>
      <div slot="header">
        <h2>📊 今日情绪记录</h2>
      </div>

      <div style="text-align: center; padding: 20px 0;">
        <!-- 情绪评分滑块 -->
        <div style="margin-bottom: 30px;">
          <p style="color: #606266; margin-bottom: 10px;">你今天的心情怎么样？（1-10分）</p>
          <el-slider
            v-model="moodScore"
            :min="1"
            :max="10"
            :marks="marks"
            show-stops
            style="padding: 0 20px;">
          </el-slider>
        </div>

        <!-- 情绪标签 -->
        <div style="margin-bottom: 30px;">
          <p style="color: #606266; margin-bottom: 10px;">情绪标签</p>
          <el-radio-group v-model="moodTag">
            <el-radio-button label="happy">😊 开心</el-radio-button>
            <el-radio-button label="calm">😌 平静</el-radio-button>
            <el-radio-button label="sad">😢 悲伤</el-radio-button>
            <el-radio-button label="anxious">😰 焦虑</el-radio-button>
            <el-radio-button label="angry">😠 愤怒</el-radio-button>
            <el-radio-button label="tired">😴 疲惫</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 备注 -->
        <el-input
          v-model="note"
          type="textarea"
          :rows="3"
          placeholder="记录今天的心情故事（选填）"
          style="margin-bottom: 20px;">
        </el-input>

        <el-button type="primary" size="large" @click="save" :loading="submitting" style="width: 200px;">
          保存今日记录
        </el-button>

        <div style="margin-top: 15px;">
          <el-button type="text" @click="$router.push('/mood/chart')">
            查看情绪趋势变化
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { recordMood, getTodayMood } from '@/api/mood'

export default {
  name: 'MoodRecord',
  data() {
    return {
      moodScore: 5,
      moodTag: 'calm',
      note: '',
      submitting: false,
      marks: { 1: '很差', 5: '一般', 10: '很好' }
    }
  },
  created() {
    // 加载今天已记录的情绪
    getTodayMood().then(res => {
      if (res.data) {
        this.moodScore = res.data.moodScore
        this.moodTag = res.data.moodTag || 'calm'
        this.note = res.data.note || ''
      }
    })
  },
  methods: {
    save() {
      this.submitting = true
      recordMood({
        moodScore: this.moodScore,
        moodTag: this.moodTag,
        note: this.note
      }).then(() => {
        this.$message.success('今日情绪已记录')
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>
