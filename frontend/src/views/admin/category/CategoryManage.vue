<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">测评板块管理</h2>
      <el-button type="primary" size="medium" icon="el-icon-plus" @click="openCreate">新增板块</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center',fontSize:'12px'}"
        :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="板块名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" style="color:#409EFF;" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" :style="{color: row.enabled === 1 ? '#E6A23C' : '#67C23A'}"
              @click="handleToggle(row)">{{ row.enabled === 1 ? '停用' : '启用' }}</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog :title="editingId ? '编辑板块' : '新增板块'" :visible.sync="showDialog" width="400px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px" size="medium">
        <el-form-item label="板块名称" required>
          <el-input v-model="form.name" placeholder="如：情绪状态、学习压力" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="99" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ editingId ? '保存' : '创建' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getCategoryList, createCategory, updateCategory, deleteCategory, toggleCategory } from '@/api/assessment'
export default {
  name: 'CategoryManage',
  data() {
    return {
      list: [], loading: false, showDialog: false, submitting: false, editingId: null,
      form: { name: '', sortOrder: 0 }
    }
  },
  created() { this.fetchList() },
  methods: {
    fetchList() {
      this.loading = true
      getCategoryList().then(res => { this.list = res.data || [] }).finally(() => { this.loading = false })
    },
    openCreate() { this.editingId = null; this.form = { name: '', sortOrder: 0 }; this.showDialog = true },
    openEdit(row) { this.editingId = row.id; this.form = { name: row.name, sortOrder: row.sortOrder || 0 }; this.showDialog = true },
    submitForm() {
      if (!this.form.name || !this.form.name.trim()) { this.$message.warning('请输入板块名称'); return }
      this.submitting = true
      const promise = this.editingId ? updateCategory(this.editingId, this.form) : createCategory(this.form)
      promise.then(() => { this.$message.success(this.editingId ? '已保存' : '已创建'); this.showDialog = false; this.fetchList() }).finally(() => { this.submitting = false })
    },
    handleToggle(row) {
      const act = row.enabled === 1 ? '停用' : '启用'
      this.$confirm('确定' + act + '该板块？' + (act === '停用' ? '停用后用户端将不再显示该板块。' : ''), '提示').then(() => {
        toggleCategory(row.id).then(() => { this.$message.success('已' + act); this.fetchList() })
      }).catch(() => {})
    },
    handleDelete(id) {
      this.$confirm('确定删除该板块？', '提示', { type: 'warning' }).then(() => {
        deleteCategory(id).then(() => { this.$message.success('已删除'); this.fetchList() }).catch(e => {
          const msg = (e.response && e.response.data && e.response.data.message) || '删除失败'
          this.$message.error(msg)
        })
      }).catch(() => {})
    }
  }
}
</script>
<style scoped>
.page-container { padding: 0 12px 30px; }
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:18px; font-weight:700; color:#1A2332; margin:0; }
</style>
