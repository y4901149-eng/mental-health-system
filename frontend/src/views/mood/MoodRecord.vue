<template>
  <div class="mood-record-page">
    <section class="module-header">
      <div>
        <h1>每日情绪记录</h1>
        <p>记录当天主要情绪和评分，用于后续趋势分析。</p>
      </div>
      <el-button plain @click="$router.push('/mood/chart')">
        <i class="el-icon-data-line"></i>
        查看分析
      </el-button>
    </section>

    <el-card shadow="never" class="status-card" v-loading="loadingToday">
      <div class="status-content">
        <div>
          <span class="status-label">今日状态</span>
          <h2>{{ hasTodayRecord ? '已完成记录' : '未完成记录' }}</h2>
          <p v-if="hasTodayRecord">
            {{ selectedMood.label }}，{{ moodScore }} 分<span v-if="recordTime">，{{ recordTime }}</span>
          </p>
          <p v-else>请选择当前最接近的情绪状态，并完成评分。</p>
        </div>
        <el-tag :type="hasTodayRecord ? 'success' : 'info'" effect="plain">
          {{ hasTodayRecord ? '已记录' : '待记录' }}
        </el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="form-card">
      <div class="section-title">
        <h3>主要情绪</h3>
        <span>选择一个最接近的状态</span>
      </div>

      <div class="mood-grid">
        <button
          v-for="item in moodOptions"
          :key="item.value"
          type="button"
          class="mood-option"
          :class="{ active: moodTag === item.value }"
          @click="moodTag = item.value">
          <i :class="item.icon"></i>
          <strong>{{ item.label }}</strong>
          <span>{{ item.description }}</span>
        </button>
      </div>

      <div class="score-panel">
        <div class="section-title compact">
          <h3>情绪评分</h3>
          <span>{{ scoreLabel }}：{{ scoreDescription }}</span>
        </div>
        <div class="score-main">
          <div class="score-number">{{ moodScore }}</div>
          <el-slider
            v-model="moodScore"
            :min="1"
            :max="10"
            :marks="marks"
            show-stops>
          </el-slider>
        </div>
      </div>

      <div class="note-panel">
        <div class="section-title compact">
          <h3>备注</h3>
          <span>可简单记录当天事件或状态，选填</span>
        </div>
        <el-input
          v-model="note"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="例如：今天课程较多，下午有些疲惫。">
        </el-input>
      </div>

      <div class="action-row">
        <el-button type="primary" :loading="submitting" @click="save">
          {{ hasTodayRecord ? '更新记录' : '保存记录' }}
        </el-button>
      </div>
    </el-card>

    <div class="note-tip">持续记录可以帮助你观察压力变化和情绪波动，但记录本身不替代专业评估。</div>
  </div>
</template>

<script>
import { recordMood, getTodayMood } from '@/api/mood'

const moodOptions = [
  { value: 'happy', label: '开心', icon: 'el-icon-sunny', description: '愉快、积极' },
  { value: 'calm', label: '平静', icon: 'el-icon-moon', description: '稳定、放松' },
  { value: 'sad', label: '悲伤', icon: 'el-icon-heavy-rain', description: '低落、难过' },
  { value: 'anxious', label: '焦虑', icon: 'el-icon-warning-outline', description: '紧张、担心' },
  { value: 'angry', label: '愤怒', icon: 'el-icon-bell', description: '烦躁、不满' },
  { value: 'tired', label: '疲惫', icon: 'el-icon-time', description: '乏力、倦怠' }
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
      if (this.moodScore <= 5) return '一般'
      if (this.moodScore <= 7) return '平稳'
      return '良好'
    },
    scoreDescription() {
      if (this.moodScore <= 3) return '建议适当休息并关注压力来源'
      if (this.moodScore <= 5) return '可能存在波动，可继续观察'
      if (this.moodScore <= 7) return '整体处于可调节状态'
      return '当前状态较好，注意保持'
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
        this.$message.warning('今日记录读取失败，请稍后重试。')
      }).finally(() => {
        this.loadingToday = false
      })
    },
    save() {
      const existed = this.hasTodayRecord
      this.submitting = true
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
        this.$message.success(existed ? '记录已更新。' : '记录已保存。')
      }).catch(() => {
        this.$message.error('保存失败，请稍后重试。')
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
}

.module-header p {
  margin: 8px 0 0;
  color: #6f7d8f;
  font-size: 14px;
}

.status-card,
.form-card {
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  margin-bottom: 18px;
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.status-label {
  color: #7a8798;
  font-size: 13px;
}

.status-content h2 {
  margin: 6px 0;
  color: #2c3e50;
  font-size: 20px;
}

.status-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
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

.section-title h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.section-title span {
  color: #7a8798;
  font-size: 13px;
}

.section-title.compact {
  margin-bottom: 12px;
}

.mood-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}

.mood-option {
  min-height: 92px;
  border: 1px solid #dfe7f1;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  display: grid;
  grid-template-columns: 24px 1fr;
  column-gap: 10px;
  row-gap: 4px;
  align-items: center;
  padding: 14px;
  text-align: left;
}

.mood-option i {
  grid-row: span 2;
  color: #7a8798;
  font-size: 20px;
}

.mood-option strong {
  color: #2c3e50;
  font-size: 15px;
}

.mood-option span {
  color: #909399;
  font-size: 12px;
}

.mood-option.active {
  border-color: #409eff;
  background: #f6faff;
}

.mood-option.active i {
  color: #409eff;
}

.score-panel {
  background: #f8fafc;
  border: 1px solid #edf1f7;
  border-radius: 4px;
  padding: 18px;
  margin-bottom: 22px;
}

.score-main {
  display: grid;
  grid-template-columns: 72px 1fr;
  align-items: center;
  gap: 18px;
}

.score-number {
  width: 58px;
  height: 58px;
  border: 1px solid #dfe7f1;
  background: #fff;
  color: #2c3e50;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
}

.note-panel {
  margin-bottom: 22px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.action-row .el-button {
  min-width: 128px;
}

.note-tip {
  color: #7a8798;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 6px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 760px) {
  .mood-record-page {
    padding: 20px 14px 34px;
  }

  .module-header,
  .status-content,
  .section-title {
    flex-direction: column;
    align-items: stretch;
  }

  .mood-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .score-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 460px) {
  .mood-grid {
    grid-template-columns: 1fr;
  }
}
</style>
