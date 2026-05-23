<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">健康知识管理</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-input v-model="keyword" placeholder="搜索标题/作者" size="medium" clearable prefix-icon="el-icon-search"
          style="width:220px;" @keyup.enter.native="search" @clear="search" />
        <el-button type="primary" size="medium" icon="el-icon-plus" @click="openCreate">新增文章</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center',fontSize:'12px'}"
        :cell-style="{textAlign:'center',padding:'5px 0',fontSize:'12px'}">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="catType(row.category)">{{ catText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column label="状态" width="80">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column label="操作" width="240">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" style="color:#409EFF;" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" :style="{color: row.status === 'published' ? '#E6A23C' : '#67C23A'}"
              @click="toggleStatus(row)">{{ row.status === 'published' ? '下架' : '发布' }}</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="p=>{pageNum=p;fetchList()}" style="margin-top:16px;text-align:center;" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="editingId ? '编辑文章' : '新增文章'" :visible.sync="showDialog" width="700px" :close-on-click-modal="false" top="4vh">
      <el-form :model="form" label-width="80px" size="medium">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width:100%;">
                <el-option label="心理健康知识" value="knowledge" />
                <el-option label="治疗方法" value="therapy" />
                <el-option label="案例分享" value="case" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="form.author" placeholder="作者姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="文章摘要" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="图片URL，留空则不显示封面" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="支持HTML格式" />
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
import { getAdminArticleList, createArticle, updateArticle, deleteArticle } from '@/api/article'

export default {
  name: 'ArticleManage',
  data() {
    return {
      list: [], total: 0, pageNum: 1, pageSize: 15, loading: false,
      keyword: '',
      showDialog: false, submitting: false, editingId: null,
      form: { title: '', category: 'knowledge', author: '', summary: '', content: '', coverImage: '' }
    }
  },
  created() { this.fetchList() },
  methods: {
    fetchList() {
      this.loading = true
      const params = { pageNum: this.pageNum, pageSize: this.pageSize }
      if (this.keyword) params.keyword = this.keyword
      getAdminArticleList(params).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      }).finally(() => { this.loading = false })
    },
    search() { this.pageNum = 1; this.fetchList() },
    openCreate() {
      this.editingId = null
      this.form = { title: '', category: 'knowledge', author: '', summary: '', content: '', coverImage: '' }
      this.showDialog = true
    },
    openEdit(row) {
      this.editingId = row.id
      this.form = {
        title: row.title || '',
        category: row.category || 'knowledge',
        author: row.author || '',
        summary: row.summary || '',
        content: row.content || '',
        coverImage: row.coverImage || ''
      }
      this.showDialog = true
    },
    submitForm() {
      if (!this.form.title || !this.form.title.trim()) {
        this.$message.warning('请输入文章标题')
        return
      }
      this.submitting = true
      const promise = this.editingId
        ? updateArticle(this.editingId, this.form)
        : createArticle(this.form)
      promise.then(() => {
        this.$message.success(this.editingId ? '已保存' : '已创建')
        this.showDialog = false
        this.fetchList()
      }).finally(() => { this.submitting = false })
    },
    toggleStatus(row) {
      const newStatus = row.status === 'published' ? 'draft' : 'published'
      const act = newStatus === 'published' ? '发布' : '下架'
      this.$confirm('确定' + act + '该文章？', '提示').then(() => {
        updateArticle(row.id, { status: newStatus }).then(() => {
          this.$message.success('已' + act)
          this.fetchList()
        })
      }).catch(() => {})
    },
    handleDelete(id) {
      this.$confirm('确定删除该文章？', '提示', { type: 'warning' }).then(() => {
        deleteArticle(id).then(() => {
          this.$message.success('已删除')
          this.fetchList()
        })
      }).catch(() => {})
    },
    catType(cat) {
      const m = { knowledge: 'primary', therapy: 'success', case: 'warning' }
      return m[cat] || 'info'
    },
    catText(cat) {
      const m = { knowledge: '心理健康知识', therapy: '治疗方法', case: '案例分享' }
      return m[cat] || cat
    }
  }
}
</script>
<style scoped>
.page-container { padding: 0 12px 30px; }
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:18px; font-weight:700; color:#1A2332; margin:0; }
</style>
