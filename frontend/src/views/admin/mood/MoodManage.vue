<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">📊 情绪记录管理</h2>
      <div><el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
        end-placeholder="结束日期" size="medium" style="width:260px;" @change="search" /></div>
    </div>
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column label="情绪" width="80">
          <template slot-scope="{row}"><el-tag size="mini">{{ row.mood_tag }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="mood_score" label="分数" width="70" />
        <el-table-column prop="record_date" label="日期" width="110" />
        <el-table-column prop="note" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template slot-scope="{row}"><el-button type="text" style="color:#F56C6C;" size="mini" @click="handleDelete(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="15"
        @current-change="p=>{pageNum=p;fetch()}" style="margin-top:16px;text-align:center;" />
    </el-card>
  </div>
</template>
<script>
import { getAdminMoodList, deleteAdminMood } from '@/api/admin/manage'
export default {
  name: 'MoodManage',
  data() { return { list:[], total:0, pageNum:1, dateRange:null, loading:false } },
  created() { this.fetch() },
  methods: {
    fetch() {
      this.loading=true; const p={page:this.pageNum,size:15}
      if(this.dateRange){p.startDate=this.dateRange[0].toISOString().substring(0,10);p.endDate=this.dateRange[1].toISOString().substring(0,10)}
      getAdminMoodList(p).then(r=>{this.list=r.data.records||[];this.total=r.data.total||0}).finally(()=>{this.loading=false})
    },
    search(){this.pageNum=1;this.fetch()},
    handleDelete(id) {
      this.$confirm('确定删除？','提示',{type:'warning'}).then(()=>{
        deleteAdminMood(id).then(()=>{this.$message.success('已删除');this.fetch()})
      }).catch(()=>{})
    }
  }
}
</script>
