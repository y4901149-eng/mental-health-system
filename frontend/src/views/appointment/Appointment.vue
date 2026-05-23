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

    <section class="flow-panel">
      <div class="flow-copy">
        <p class="eyebrow">处理进度</p>
        <h3>{{ nextAppointment ? '预约已确认' : '提交后系统自动确认' }}</h3>
        <p>
          {{ nextAppointment ? appointmentHint(nextAppointment) : '预约提交后系统会自动校验时间段并确认，无需等待人工审核。' }}
        </p>
      </div>
      <div class="flow-steps">
        <div
          v-for="step in flowSteps"
          :key="step.key"
          class="flow-step"
          :class="{ active: step.active, done: step.done }"
        >
          <span>{{ step.index }}</span>
          <div>
            <strong>{{ step.title }}</strong>
            <small>{{ step.desc }}</small>
          </div>
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
                :key="counselor.id"
                :label="counselor.name + '（擅长' + counselor.specialty + '）'"
                :value="counselor.name"
              >
                <div class="counselor-option">
                  <span>{{ counselor.name }}</span>
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
              <el-option
                v-for="slot in availableTimeSlots"
                :key="slot.value"
                :label="slot.label"
                :value="slot.value"
                :disabled="slot.disabled" />
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
import { createAppointment, getMyAppointments, cancelAppointment, getCounselors, getBookedSlots, getCounselorTimeSlots } from '@/api/appointment'

export default {
  name: 'Appointment',
  data() {
    return {
      form: this.emptyForm(),
      appointments: [],
      submitting: false,
      loading: false,
      counselors: [],
      bookedSlots: [],
      counselorTimeSlots: [],
      counselorsMap: {},
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
    availableTimeSlots() {
      return this.counselorTimeSlots.map(s => ({
        value: s,
        label: this.bookedSlots.includes(s) ? s + '（已预约）' : s,
        disabled: this.bookedSlots.includes(s)
      }))
    },

    activeCount() {
      return this.appointments.filter(item => ['pending', 'confirmed'].includes(item.status)).length
    },

    nextAppointment() {
      return this.appointments.find(item => ['pending', 'confirmed'].includes(item.status)) || null
    },

    flowSteps() {
      const status = this.nextAppointment ? this.nextAppointment.status : ''
      return [
        {
          key: 'submitted',
          index: '1',
          title: '提交预约',
          desc: this.nextAppointment ? '预约信息已提交' : '填写信息后提交',
          active: !status,
          done: !!status
        },
        {
          key: 'confirm',
          index: '2',
          title: '系统确认',
          desc: status === 'confirmed' ? '时间段校验通过，预约已确认' : '校验时间段中',
          active: status === 'pending',
          done: status === 'confirmed'
        },
        {
          key: 'consult',
          index: '3',
          title: '按时咨询',
          desc: status === 'confirmed' ? '请按预约时间参加' : '确认后进入咨询',
          active: status === 'confirmed',
          done: false
        }
      ]
    }
  },
  watch: {
    'form.counselorName': function() { this.fetchCounselorSlots(); this.fetchBookedSlots() },
    'form.appointmentDate': function() { this.fetchCounselorSlots(); this.fetchBookedSlots() }
  },

  created() {
    this.fetchAppointments()
    this.fetchCounselors()
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
          this.appointments = (res.data || []).filter(a => a.status !== 'cancelled')
        })
        .finally(() => {
          this.loading = false
        })
    },

    fetchCounselors() {
      getCounselors().then(res => {
        this.counselors = res.data || []
        const map = {}
        this.counselors.forEach(c => { map[c.name] = c.id })
        this.counselorsMap = map
      }).catch(() => {})
    },

    fetchCounselorSlots() {
      this.counselorTimeSlots = []
      this.form.timeSlot = ''
      if (!this.form.counselorName || !this.form.appointmentDate) return
      const id = this.counselorsMap[this.form.counselorName]
      if (!id) return
      getCounselorTimeSlots(id, this.form.appointmentDate).then(res => {
        this.counselorTimeSlots = res.data || []
      }).catch(() => {})
    },

    fetchBookedSlots() {
      if (!this.form.counselorName || !this.form.appointmentDate) {
        this.bookedSlots = []
        return
      }
      getBookedSlots(this.form.counselorName, this.form.appointmentDate).then(res => {
        this.bookedSlots = res.data || []
        // 如果当前所选时间段已被预约，清空
        if (this.form.timeSlot && this.bookedSlots.includes(this.form.timeSlot)) {
          this.form.timeSlot = ''
        }
      }).catch(() => {})
    },

    submitAppointment() {
      this.$refs.form.validate(valid => {
        if (!valid || this.submitting) return
        this.submitting = true
        createAppointment({ ...this.form })
          .then(() => {
            this.$message.success('预约提交成功，已自动确认。')
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
    },

    appointmentHint(appointment) {
      const timeText = appointment.appointmentDate + ' ' + this.formatSlot(appointment.timeSlot)
      if (appointment.status === 'confirmed') {
        return appointment.counselorName + ' 的 ' + timeText + ' 预约已确认，请按时参加。'
      }
      return appointment.counselorName + ' 的 ' + timeText + ' 预约已提交，系统正在确认中。'
    }
  }
}
</script>

<style scoped>
.appointment-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 24px 32px;
  color: #24364b;
}

.appointment-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  padding: 24px 24px;
  border: 1px solid rgba(219, 232, 248, 0.95);
  border-radius: 16px;
  margin-top: 4px;
  margin-bottom: 20px;
  background:
    linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.06) 100%),
    #fff;
  box-shadow: 0 14px 34px rgba(41, 80, 130, 0.08);
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
  font-size: 28px;
  letter-spacing: 0;
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
  padding: 13px 15px;
  border: 1px solid rgba(207, 224, 247, 0.95);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 10px 20px rgba(41, 80, 130, 0.06);
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

.flow-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(360px, 1.35fr);
  gap: 18px;
  align-items: center;
  margin-bottom: 18px;
  padding: 20px;
  border: 1px solid rgba(219, 232, 248, 0.95);
  border-radius: 16px;
  background: linear-gradient(135deg, #f7fbff 0%, #fff 100%);
  box-shadow: 0 14px 30px rgba(41, 80, 130, 0.07);
}

.flow-copy h3 {
  margin: 0;
  color: #24364b;
}

.flow-copy p:last-child {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.flow-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.flow-step {
  display: flex;
  gap: 10px;
  min-height: 72px;
  padding: 13px;
  border: 1px solid rgba(231, 237, 246, 0.95);
  border-radius: 14px;
  background: #fff;
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}

.flow-step:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(41, 80, 130, 0.06);
}

.flow-step span {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  background: #edf2f8;
  color: #6b7c93;
  font-weight: 700;
  font-size: 13px;
}

.flow-step strong {
  display: block;
  margin-bottom: 4px;
  color: #2f4057;
  font-size: 14px;
}

.flow-step small {
  color: #7b8ca5;
  line-height: 1.45;
}

.flow-step.active {
  border-color: #409eff;
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.14);
}

.flow-step.active span {
  background: #409eff;
  color: #fff;
}

.flow-step.done {
  border-color: #c7ead7;
}

.flow-step.done span {
  background: #67c23a;
  color: #fff;
}

.appointment-layout {
  display: grid;
  grid-template-columns: minmax(320px, 390px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.panel {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(231, 237, 246, 0.95);
  border-radius: 16px;
  box-shadow: 0 14px 32px rgba(41, 80, 130, 0.08);
  overflow: hidden;
}

.form-panel {
  padding: 22px;
}

.list-panel {
  padding: 20px;
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
  border-radius: 12px !important;
  height: 42px;
  box-shadow: 0 10px 22px rgba(64, 158, 255, 0.18);
}

.form-panel :deep(.el-input__inner),
.form-panel :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #dbe8f8;
  background: #fbfdff;
}

.form-panel :deep(.el-radio-button__inner) {
  border-color: #dbe8f8;
}

.list-panel :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.list-panel :deep(.el-table th.el-table__cell) {
  background: #f6f9fd;
  color: #4f6680;
}

.list-panel :deep(.el-table td.el-table__cell) {
  border-bottom-color: #edf2f8;
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

  .flow-panel {
    grid-template-columns: 1fr;
  }

  .flow-steps {
    grid-template-columns: 1fr;
  }
}
</style>
