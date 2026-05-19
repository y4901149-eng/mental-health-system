<!-- 心理咨询预约 -->
<template>
  <div class="appointment-page">
    <section class="appointment-hero">
      <div>
        <p class="eyebrow">预约咨询</p>
        <h2>心理咨询预约</h2>
        <p class="hero-copy">选择咨询师和时间段，提交后等待老师确认。</p>
      </div>
      <div class="hero-stats">
        <div>
          <strong>{{ appointments.length }}</strong>
          <span>全部预约</span>
        </div>
        <div>
          <strong>{{ activeCount }}</strong>
          <span>进行中</span>
        </div>
      </div>
    </section>

    <section class="appointment-layout">
      <div class="panel form-panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">提交信息</p>
            <h3>新建预约</h3>
          </div>
        </div>

        <el-form ref="form" :model="form" :rules="rules" label-position="top" size="medium">
          <el-form-item label="咨询师" prop="counselorName">
            <el-select v-model="form.counselorName" placeholder="请选择咨询师" style="width: 100%;">
              <el-option
                v-for="counselor in counselors"
                :key="counselor.value"
                :label="counselor.label"
                :value="counselor.value"
              >
                <div class="counselor-option">
                  <span>{{ counselor.value }}</span>
                  <small>{{ counselor.specialty }}</small>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="预约日期" prop="appointmentDate">
            <el-date-picker
              v-model="form.appointmentDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择日期"
              :picker-options="pickerOptions"
              style="width: 100%;"
            />
          </el-form-item>

          <el-form-item label="时间段" prop="timeSlot">
            <el-select v-model="form.timeSlot" placeholder="请选择时间段" style="width: 100%;">
              <el-option v-for="slot in timeSlots" :key="slot" :label="slot" :value="slot" />
            </el-select>
          </el-form-item>

          <el-form-item label="咨询类型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio-button label="individual">个体咨询</el-radio-button>
              <el-radio-button label="group">团体咨询</el-radio-button>
              <el-radio-button label="crisis">危机干预</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="可以简单描述想咨询的问题，便于老师提前了解。"
            />
          </el-form-item>

          <el-button
            type="primary"
            icon="el-icon-check"
            class="submit-btn"
            :loading="submitting"
            @click="submitAppointment"
          >
            提交预约
          </el-button>
        </el-form>
      </div>

      <div class="panel list-panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">我的记录</p>
            <h3>预约列表</h3>
          </div>
          <el-button plain size="small" icon="el-icon-refresh" :loading="loading" @click="fetchAppointments">
            刷新
          </el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="appointments"
          stripe
          style="width: 100%;"
          empty-text="暂无预约记录"
        >
          <el-table-column prop="counselorName" label="咨询师" min-width="110" />
          <el-table-column label="时间" min-width="180">
            <template slot-scope="scope">
              <div class="time-cell">
                <span>{{ scope.row.appointmentDate }}</span>
                <small>{{ formatSlot(scope.row.timeSlot) }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template slot-scope="scope">
              {{ typeText(scope.row.type) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="statusType(scope.row.status)" size="mini">
                {{ statusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="right">
            <template slot-scope="scope">
              <el-button
                v-if="canCancel(scope.row.status)"
                type="text"
                class="cancel-btn"
                @click="cancel(scope.row)"
              >
                取消
              </el-button>
              <span v-else class="muted-action">不可取消</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script>
import { createAppointment, getMyAppointments, cancelAppointment } from '@/api/appointment'

export default {
  name: 'Appointment',
  data() {
    return {
      form: this.emptyForm(),
      appointments: [],
      submitting: false,
      loading: false,
      counselors: [
        { value: '张老师', label: '张老师（擅长焦虑管理）', specialty: '焦虑管理' },
        { value: '李老师', label: '李老师（擅长情绪调节）', specialty: '情绪调节' },
        { value: '王老师', label: '王老师（擅长压力管理）', specialty: '压力管理' }
      ],
      timeSlots: ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00'],
      rules: {
        counselorName: [{ required: true, message: '请选择咨询师', trigger: 'change' }],
        appointmentDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
        timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
        type: [{ required: true, message: '请选择咨询类型', trigger: 'change' }]
      },
      pickerOptions: {
        disabledDate(time) {
          const today = new Date()
          today.setHours(0, 0, 0, 0)
          return time.getTime() < today.getTime()
        }
      }
    }
  },
  computed: {
    activeCount() {
      return this.appointments.filter(item => ['pending', 'confirmed'].includes(item.status)).length
    }
  },
  created() {
    this.fetchAppointments()
  },
  methods: {
    emptyForm() {
      return {
        counselorName: '',
        appointmentDate: '',
        timeSlot: '',
        type: 'individual',
        remark: ''
      }
    },

    fetchAppointments() {
      this.loading = true
      return getMyAppointments()
        .then(res => {
          this.appointments = res.data || []
        })
        .finally(() => {
          this.loading = false
        })
    },

    submitAppointment() {
      this.$refs.form.validate(valid => {
        if (!valid || this.submitting) return
        this.submitting = true
        createAppointment({ ...this.form })
          .then(() => {
            this.$message.success('预约提交成功，请等待确认。')
            this.form = this.emptyForm()
            this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
            this.fetchAppointments()
          })
          .finally(() => {
            this.submitting = false
          })
      })
    },

    cancel(row) {
      this.$confirm('确定取消这条预约吗？', '取消预约', {
        confirmButtonText: '确认取消',
        cancelButtonText: '暂不取消',
        type: 'warning'
      }).then(() => {
        cancelAppointment(row.id).then(() => {
          this.$message.success('预约已取消。')
          this.fetchAppointments()
        })
      }).catch(() => {})
    },

    canCancel(status) {
      return ['pending', 'confirmed'].includes(status)
    },

    statusType(status) {
      const map = {
        pending: 'warning',
        confirmed: 'success',
        completed: 'info',
        cancelled: 'danger'
      }
      return map[status] || 'info'
    },

    statusText(status) {
      const map = {
        pending: '待确认',
        confirmed: '已确认',
        completed: '已完成',
        cancelled: '已取消'
      }
      return map[status] || status
    },

    typeText(type) {
      const map = {
        individual: '个体咨询',
        group: '团体咨询',
        crisis: '危机干预'
      }
      return map[type] || type
    },

    formatSlot(slot) {
      return slot ? slot.replace('-', ' - ') : ''
    }
  }
}
</script>

<style scoped>
.appointment-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 24px 32px;
}

.appointment-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 22px 0 18px;
  border-bottom: 1px solid #e7edf6;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  color: #7b8ca5;
}

.appointment-hero h2,
.panel-header h3 {
  margin: 0;
  color: #24364b;
}

.appointment-hero h2 {
  font-size: 26px;
}

.hero-copy {
  margin: 8px 0 0;
  color: #6d7d93;
}

.hero-stats {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.hero-stats div {
  min-width: 96px;
  padding: 12px 14px;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #fff;
}

.hero-stats strong {
  display: block;
  color: #2678d9;
  font-size: 24px;
}

.hero-stats span {
  color: #7b8ca5;
  font-size: 12px;
}

.appointment-layout {
  display: grid;
  grid-template-columns: minmax(320px, 390px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.panel {
  background: #fff;
  border: 1px solid #e7edf6;
  border-radius: 8px;
}

.form-panel {
  padding: 20px;
}

.list-panel {
  padding: 18px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.counselor-option {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.counselor-option small {
  color: #8a98aa;
}

.submit-btn {
  width: 100%;
  border-radius: 8px;
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-cell small {
  color: #7b8ca5;
}

.cancel-btn {
  color: #f56c6c;
}

.muted-action {
  color: #a8b3c3;
  font-size: 12px;
}

@media (max-width: 980px) {
  .appointment-hero {
    flex-direction: column;
  }

  .appointment-layout {
    grid-template-columns: 1fr;
  }
}
</style>
