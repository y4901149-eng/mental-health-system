<template>
  <div class="mood-record-page">
    <section class="page-header">
      <div>
        <p class="eyebrow">每日情绪打卡</p>
        <h1>记录今天的情绪状态</h1>
        <p class="subtitle">用一分钟留下今天的感受，帮助自己看见情绪变化。</p>
      </div>
      <el-button plain @click="$router.push('/mood/chart')">
        <i class="el-icon-data-line"></i>
        查看分析
      </el-button>
    </section>

    <el-card shadow="never" class="status-card" v-loading="loadingToday">
      <div class="status-main">
        <div class="status-icon" :class="{ done: hasTodayRecord }">
          <i :class="hasTodayRecord ? 'el-icon-check' : 'el-icon-date'"></i>
        </div>
        <div>
          <h2>{{ hasTodayRecord ? '今天已完成记录' : '今天还没有记录' }}</h2>
          <p v-if="hasTodayRecord">
            今日情绪：{{ selectedMood.label }}，评分 {{ moodScore }} 分
            <span v-if="recordTime">，记录于 {{ recordTime }}</span>
          </p>
          <p v-else>选择一个最贴近当下的情绪，再给今天的状态打个分。</p>
        </div>
      </div>
      <el-tag :type="hasTodayRecord ? 'success' : 'info'" effect="plain">
        {{ hasTodayRecord ? '已打卡' : '待完成' }}
      </el-tag>
    </el-card>

    <el-card shadow="never" class="form-card">
      <div class="section-title">
        <span>今天的主要情绪</span>
        <small>选择一个最接近的状态</small>
      </div>

      <div class="mood-grid">
        <button
          v-for="item in moodOptions"
          :key="item.value"
          type="button"
          class="mood-option"
          :class="{ active: moodTag === item.value }"
          @click="moodTag = item.value">
          <span class="mood-emoji">{{ item.emoji }}</span>
          <span class="mood-label">{{ item.label }}</span>
          <span class="mood-desc">{{ item.description }}</span>
        </button>
      </div>

      <div class="score-section">
        <div class="section-title compact">
          <span>情绪评分</span>
          <small>{{ scoreLabel }}，{{ scoreDescription }}</small>
        </div>
        <div class="score-value">{{ moodScore }}</div>
        <el-slider
          v-model="moodScore"
          :min="1"
          :max="10"
          :marks="marks"
          show-stops>
        </el-slider>
      </div>

      <div class="note-section">
        <div class="section-title compact">
          <span>简单备注</span>
          <small>可以写下今天发生了什么，选填</small>
        </div>
        <el-input
          v-model="note"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="例如：今天工作压力有点大，但晚饭后散步让我放松了一些。">
        </el-input>
      </div>

      <div class="action-row">
        <el-button type="primary" :loading="submitting" @click="save">
          {{ hasTodayRecord ? '更新今日记录' : '保存今日记录' }}
        </el-button>
      </div>
    </el-card>

    <div class="tip-card">
      <i class="el-icon-light-rain"></i>
      <span>持续记录情绪有助于发现压力变化，也能帮助你更早看到自己的恢复迹象。</span>
    </div>
  </div>
</template>

<script>
import { recordMood, getTodayMood } from '@/api/mood'

const moodOptions = [
  { value: 'happy', label: '开心', emoji: '😊', description: '愉快、有活力' },
  { value: 'calm', label: '平静', emoji: '😌', description: '稳定、放松' },
  { value: 'sad', label: '悲伤', emoji: '😔', description: '低落、难过' },
  { value: 'anxious', label: '焦虑', emoji: '😟', description: '紧张、担心' },
  { value: 'angry', label: '愤怒', emoji: '😠', description: '烦躁、不满' },
  { value: 'tired', label: '疲惫', emoji: '😴', description: '乏力、倦怠' }
]

export default {
  name: 'MoodRecord',
  data() {
    return {
      loadingToday: false,
      submitting: false,
      todayRecord: null,
      moodScore: 5,
      moodTag: 'calm',
      note: '',
      moodOptions,
      marks: {
        1: '较低',
        4: '一般',
        7: '良好',
        10: '很好'
      }
    }
  },
  computed: {
    hasTodayRecord() {
      return !!this.todayRecord
    },
    selectedMood() {
      return this.moodOptions.find(item => item.value === this.moodTag) || this.moodOptions[1]
    },
    scoreLabel() {
      if (this.moodScore <= 3) return '较低'
      if (this.moodScore <= 5) return '有些波动'
      if (this.moodScore <= 7) return '平稳'
      if (this.moodScore <= 9) return '良好'
      return '很好'
    },
    scoreDescription() {
      if (this.moodScore <= 3) return '可以给自己更多休息和支持'
      if (this.moodScore <= 5) return '适合留意压力来源'
      if (this.moodScore <= 7) return '整体处在可调节范围'
      if (this.moodScore <= 9) return '状态不错，继续保持'
      return '今天的状态非常积极'
    },
    recordTime() {
      if (!this.todayRecord || !this.todayRecord.createTime) return ''
      return this.formatTime(this.todayRecord.createTime)
    }
  },
  created() {
    this.fetchTodayMood()
  },
  methods: {
    fetchTodayMood() {
      this.loadingToday = true
      getTodayMood().then(res => {
        this.todayRecord = res.data || null
        if (this.todayRecord) {
          this.moodScore = this.todayRecord.moodScore || 5
          this.moodTag = this.todayRecord.moodTag || 'calm'
          this.note = this.todayRecord.note || ''
        }
      }).catch(() => {
        this.$message.warning('暂时无法读取今日记录，请稍后重试。')
      }).finally(() => {
        this.loadingToday = false
      })
    },
    save() {
      this.submitting = true
      const existed = this.hasTodayRecord
      recordMood({
        moodScore: this.moodScore,
        moodTag: this.moodTag,
        note: this.note
      }).then(() => {
        this.todayRecord = {
          moodScore: this.moodScore,
          moodTag: this.moodTag,
          note: this.note,
          createTime: new Date().toISOString()
        }
        this.$message.success(existed ? '今日情绪记录已更新。' : '今日情绪记录已保存。')
      }).catch(() => {
        this.$message.error('保存失败，请检查网络或稍后再试。')
      }).finally(() => {
        this.submitting = false
      })
    },
    formatTime(value) {
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${hour}:${minute}`
    }
  }
}
</script>

<style scoped>
.mood-record-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 28px 24px 44px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 26px;
  line-height: 1.35;
}

.subtitle {
  margin: 8px 0 0;
  color: #7a8798;
  font-size: 14px;
}

.status-card,
.form-card {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  margin-bottom: 18px;
}

.status-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
}

.status-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: #f4f7fb;
  color: #8a97a8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.status-icon.done {
  color: #67c23a;
  background: #edf8e9;
}

.status-main h2 {
  margin: 0 0 6px;
  color: #2c3e50;
  font-size: 18px;
}

.status-main p {
  margin: 0;
  color: #7a8798;
  font-size: 14px;
  line-height: 1.6;
}

.form-card ::v-deep .el-card__body {
  padding: 24px;
}

.section-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-title span {
  color: #2c3e50;
  font-size: 16px;
  font-weight: 700;
}

.section-title small {
  color: #909399;
  font-size: 13px;
}

.section-title.compact {
  margin-bottom: 10px;
}

.mood-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 26px;
}

.mood-option {
  min-height: 98px;
  border: 1px solid #e5ebf5;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 14px;
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.mood-option:hover {
  border-color: #b9d8ff;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.08);
}

.mood-option.active {
  border-color: #409eff;
  background: #f5f9ff;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.12);
}

.mood-emoji {
  font-size: 24px;
  margin-bottom: 8px;
}

.mood-label {
  color: #2c3e50;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 4px;
}

.mood-desc {
  color: #909399;
  font-size: 12px;
}

.score-section {
  padding: 20px;
  border-radius: 10px;
  background: #f8fbff;
  margin-bottom: 24px;
}

.score-value {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  margin: 4px auto 10px;
  border: 1px solid #e1efff;
}

.score-section ::v-deep .el-slider {
  padding: 0 8px;
}

.note-section {
  margin-bottom: 22px;
}

.action-row {
  display: flex;
  justify-content: center;
}

.action-row .el-button {
  min-width: 180px;
}

.tip-card {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6f7d8f;
  background: #fff;
  border: 1px solid #e8eef7;
  border-radius: 10px;
  padding: 14px 16px;
  font-size: 13px;
  line-height: 1.6;
}

.tip-card i {
  color: #409eff;
  font-size: 18px;
  flex-shrink: 0;
}

@media (max-width: 760px) {
  .mood-record-page {
    padding: 20px 14px 34px;
  }

  .page-header,
  .status-card ::v-deep .el-card__body,
  .section-title {
    flex-direction: column;
    align-items: stretch;
  }

  .mood-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 460px) {
  .mood-grid {
    grid-template-columns: 1fr;
  }
}
</style>
