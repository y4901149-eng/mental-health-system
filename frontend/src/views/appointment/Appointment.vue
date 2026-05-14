<!-- 心理咨询预约页面 -->

<template>
  <div class="page-container" style="max-width: 700px; margin: 0 auto;">
    <el-card>
      <div slot="header">
        <h2>📅 心理咨询预约</h2>
      </div>

      <!-- 预约表单 -->
      <el-form :model="form" label-width="100px" size="medium">
        <el-form-item label="咨询师" required>
          <el-select v-model="form.counselorName" placeholder="请选择咨询师" style="width: 100%;">
            <el-option label="张老师（擅长焦虑管理）" value="张老师"></el-option>
            <el-option label="李老师（擅长情绪调节）" value="李老师"></el-option>
            <el-option label="王老师（擅长压力管理）" value="王老师"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="预约日期" required>
          <el-date-picker
            v-model="form.appointmentDate"
            type="date"
            placeholder="选择日期"
            :picker-options="pickerOptions"
            style="width: 100%;">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="时间段" required>
          <el-select v-model="form.timeSlot" placeholder="请选择时间段" style="width: 100%;">
            <el-option label="09:00 - 10:00" value="09:00-10:00"></el-option>
            <el-option label="10:00 - 11:00" value="10:00-11:00"></el-option>
            <el-option label="14:00 - 15:00" value="14:00-15:00"></el-option>
            <el-option label="15:00 - 16:00" value="15:00-16:00"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="咨询类型">
          <el-radio-group v-model="form.type">
            <el-radio label="individual">个体咨询</el-radio>
            <el-radio label="group">团体咨询</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请简要描述您想咨询的问题（选填）">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitAppointment" :loading="submitting">
            提交预约
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的预约记录 -->
    <el-card style="margin-top: 20px;">
      <div slot="header"><strong>我的预约记录</strong></div>
      <el-table :data="appointments" stripe style="width: 100%;" v-if="appointments.length > 0">
        <el-table-column prop="counselorName" label="咨询师" width="120"></el-table-column>
        <el-table-column prop="appointmentDate" label="日期" width="120"></el-table-column>
        <el-table-column prop="timeSlot" label="时间" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.status)" size="mini">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 'pending' || scope.row.status === 'confirmed'"
              type="danger"
              size="mini"
              @click="cancel(scope.row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无预约记录"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { createAppointment, getMyAppointments, cancelAppointment } from '@/api/appointment'

export default {
  name: 'Appointment',
  data() {
    return {
      form: {
        counselorName: '',
        appointmentDate: '',
        timeSlot: '',
        type: 'individual',
        remark: ''
      },
      appointments: [],
      submitting: false,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 86400000
        }
      }
    }
  },
  created() {
    this.fetchAppointments()
  },
  methods: {
    fetchAppointments() {
      getMyAppointments().then(res => {
        this.appointments = res.data || []
      })
    },
    submitAppointment() {
      if (!this.form.counselorName || !this.form.appointmentDate || !this.form.timeSlot) {
        this.$message.warning('请填写完整信息')
        return
      }
      this.submitting = true
      // 将 Date 对象转为 YYYY-MM-DD 字符串
      const data = { ...this.form }
      if (data.appointmentDate instanceof Date) {
        const d = data.appointmentDate
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        data.appointmentDate = d.getFullYear() + '-' + m + '-' + day
      }
      createAppointment(data).then(() => {
        this.$message.success('预约提交成功，请等待确认')
        this.form = { counselorName: '', appointmentDate: '', timeSlot: '', type: 'individual', remark: '' }
        this.fetchAppointments()
      }).catch(err => {
        this.$message.error(err.message || '提交失败，请稍后重试')
      }).finally(() => {
        this.submitting = false
      })
    },
    cancel(id) {
      this.$confirm('确定取消此预约？', '提示').then(() => {
        cancelAppointment(id).then(() => {
          this.$message.success('已取消')
          this.fetchAppointments()
        })
      })
    },
    statusType(status) {
      const map = { pending: 'warning', confirmed: 'success', completed: 'info', cancelled: 'danger' }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = { pending: '待确认', confirmed: '已确认', completed: '已完成', cancelled: '已取消' }
      return map[status] || status
    }
  }
}
</script>
