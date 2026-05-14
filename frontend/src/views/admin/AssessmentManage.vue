<!-- 管理员 - 量表管理 -->
<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">📋 量表管理</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-input v-model="keyword" placeholder="搜索量表名称" size="medium" clearable
          prefix-icon="el-icon-search" style="width:240px;" @keyup.enter.native="search" @clear="search" />
        <el-button size="medium" type="primary" icon="el-icon-search" @click="search">搜索</el-button>
        <el-button size="medium" @click="keyword='';search()">重置</el-button>
        <el-button size="medium" type="primary" @click="openCreate">新建量表</el-button>
      </div>
    </div>
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
        :cell-style="{textAlign:'center',padding:'6px 0'}" size="small">
        <el-table-column prop="title" label="名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="questionCount" label="题目数" width="80" />
        <el-table-column label="状态" width="90">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showQuestions(row)">查看题目</el-button>
            <el-button type="text" size="mini" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" :style="{color: row.status === 'published' ? '#E6A23C' : '#67C23A'}"
              @click="toggleStatus(row)">{{ row.status === 'published' ? '下架' : '发布' }}</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="p=>{pageNum=p;fetch()}" style="margin-top:12px;text-align:center;" />
    </el-card>

    <!-- 新建/编辑量表弹窗 -->
    <el-dialog :title="editingId ? '编辑量表' : '新建量表'" :visible.sync="showDialog" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px" size="medium">
        <el-form-item label="名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="form.type" style="width:100%;">
                <el-option label="抑郁" value="depression" /><el-option label="焦虑" value="anxiety" />
                <el-option label="压力" value="stress" /><el-option label="睡眠" value="sleep" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%;">
                <el-option label="草稿" value="draft" /><el-option label="已发布" value="published" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ editingId ? '保存' : '创建' }}</el-button>
      </div>
    </el-dialog>

    <!-- 题目管理弹窗 -->
    <el-dialog title="题目管理" :visible.sync="qVisible" width="700px" top="4vh" :close-on-click-modal="false">
      <div v-if="currentAssessment" style="margin-bottom:12px;">
        <strong>{{ currentAssessment.title }}</strong>
        <el-tag size="mini" style="margin-left:8px;">{{ questions.length }} 题</el-tag>
        <el-button size="mini" type="primary" style="float:right;" @click="openQCreate">新增题目</el-button>
      </div>
      <el-table :data="questions" stripe size="small" style="width:100%;"
        :header-cell-style="{background:'#F8FAFF',color:'#2C3E50',textAlign:'center'}"
        :cell-style="{textAlign:'center',padding:'6px 0'}">
        <el-table-column prop="sortOrder" label="序号" width="60" />
        <el-table-column prop="questionText" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="选项" width="180">
          <template slot-scope="{row}">A:{{ row.scoreA }} B:{{ row.scoreB }} C:{{ row.scoreC }} D:{{ row.scoreD }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="openQEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="handleQDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!qLoading && questions.length===0" description="暂无题目" />
    </el-dialog>

    <!-- 新增/编辑题目弹窗 -->
    <el-dialog :title="qEditingId ? '编辑题目' : '新增题目'" :visible.sync="qFormVisible" width="550px" :close-on-click-modal="false">
      <el-form :model="qForm" label-width="100px" size="small">
        <el-form-item label="题目内容"><el-input v-model="qForm.questionText" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="选项A"><el-input v-model="qForm.optionA" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分值A"><el-input-number v-model="qForm.scoreA" :min="0" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="选项B"><el-input v-model="qForm.optionB" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分值B"><el-input-number v-model="qForm.scoreB" :min="0" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="选项C"><el-input v-model="qForm.optionC" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分值C"><el-input-number v-model="qForm.scoreC" :min="0" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="选项D"><el-input v-model="qForm.optionD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分值D"><el-input-number v-model="qForm.scoreD" :min="0" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="排序号"><el-input-number v-model="qForm.sortOrder" :min="1" :max="99" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="qFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQForm" :loading="qSubmitting">{{ qEditingId ? '保存' : '添加' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAssessmentList, createAssessment, updateAssessment, deleteAssessment,
  getAssessmentQuestions, createQuestion, updateQuestion, deleteQuestion } from '@/api/admin/assessment'

export default {
  name: 'AssessmentManage',
  data() {
    return {
      list:[], total:0, pageNum:1, pageSize:15, keyword:'', loading:false,
      showDialog:false, submitting:false, editingId:null,
      form:{ title:'', description:'', type:'depression', status:'draft' },
      /* 题目管理 */
      qVisible:false, qLoading:false, currentAssessment:null, questions:[],
      qFormVisible:false, qSubmitting:false, qEditingId:null,
      qForm:{ questionText:'', optionA:'', optionB:'', optionC:'', optionD:'', scoreA:0, scoreB:1, scoreC:2, scoreD:3, sortOrder:1 }
    }
  },
  created(){this.fetch()},
  methods: {
    fetch(){
      this.loading=true
      getAssessmentList({pageNum:this.pageNum,pageSize:this.pageSize,keyword:this.keyword||undefined})
        .then(r=>{this.list=r.data.records||[];this.total=r.data.total||0}).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetch()},
    openCreate(){
      this.editingId=null; this.form={title:'',description:'',type:'depression',status:'draft'}; this.showDialog=true
    },
    openEdit(row){
      this.editingId=row.id; this.form={title:row.title,description:row.description||'',type:row.type||'depression',status:row.status||'draft'}; this.showDialog=true
    },
    submitForm(){
      this.submitting=true
      const p = this.editingId ? updateAssessment(this.editingId, this.form) : createAssessment(this.form)
      p.then(()=>{this.$message.success(this.editingId?'已更新':'已创建');this.showDialog=false;this.fetch()}).finally(()=>{this.submitting=false})
    },
    toggleStatus(row){
      const s = row.status === 'published' ? 'draft' : 'published'
      updateAssessment(row.id, {status:s}).then(()=>{this.$message.success(s==='published'?'已发布':'已下架');this.fetch()})
    },
    handleDelete(id){
      this.$confirm('确定删除？关联题目也将删除。','提示',{type:'warning'}).then(()=>{deleteAssessment(id).then(()=>{this.$message.success('已删除');this.fetch()})}).catch(()=>{})
    },
    /* 题目管理 */
    showQuestions(row){
      this.currentAssessment=row; this.qVisible=true; this.qLoading=true
      getAssessmentQuestions(row.id).then(r=>{this.questions=r.data||[]}).finally(()=>{this.qLoading=false})
    },
    openQCreate(){
      this.qEditingId=null; this.qForm={questionText:'',optionA:'',optionB:'',optionC:'',optionD:'',scoreA:0,scoreB:1,scoreC:2,scoreD:3,sortOrder:(this.questions.length||0)+1}
      this.qFormVisible=true
    },
    openQEdit(row){
      this.qEditingId=row.id; this.qForm={
        questionText:row.questionText, optionA:row.optionA||'', optionB:row.optionB||'', optionC:row.optionC||'', optionD:row.optionD||'',
        scoreA:row.scoreA, scoreB:row.scoreB, scoreC:row.scoreC, scoreD:row.scoreD, sortOrder:row.sortOrder
      }; this.qFormVisible=true
    },
    submitQForm(){
      this.qSubmitting=true
      const data={...this.qForm, assessmentId:this.currentAssessment.id}
      const p = this.qEditingId ? updateQuestion(this.qEditingId, data) : createQuestion(data)
      p.then(()=>{
        this.$message.success(this.qEditingId?'已更新':'已添加'); this.qFormVisible=false
        getAssessmentQuestions(this.currentAssessment.id).then(r=>{this.questions=r.data||[]})
      }).finally(()=>{this.qSubmitting=false})
    },
    handleQDelete(id){
      this.$confirm('确定删除此题？','提示').then(()=>{
        deleteQuestion(id).then(()=>{this.$message.success('已删除');getAssessmentQuestions(this.currentAssessment.id).then(r=>{this.questions=r.data||[]})})
      }).catch(()=>{})
    }
  }
}
</script>

<style scoped>
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;}
.page-title{margin:0!important;}
</style>
