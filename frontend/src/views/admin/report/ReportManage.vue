<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">📋 周报管理</h2></div>
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width:100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column label="周期" width="220"><template slot-scope="{row}">{{ row.period_start }} ~ {{ row.period_end }}</template></el-table-column>
        <el-table-column label="平均情绪" width="90">
          <template slot-scope="{row}">{{ parseStats(row.statistics_json).avgScore || '-' }}</template>
        </el-table-column>
        <el-table-column label="主要情绪" width="90">
          <template slot-scope="{row}">{{ parseStats(row.statistics_json).topMood || '-' }}</template>
        </el-table-column>
        <el-table-column prop="create_time" label="创建时间" width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
            <el-button type="text" style="color:#F56C6C;" size="mini" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" background layout="prev,pager,next" :total="total" :page-size="15"
        @current-change="p=>{pageNum=p;fetch()}" style="margin-top:16px;text-align:center;" />
    </el-card>
    <el-dialog title="周报详情" :visible.sync="showDialog" width="560px">
      <div v-if="detail">
        <p style="color:#909399;font-size:13px;">周期: {{ detail.period_start }} ~ {{ detail.period_end }}</p>
        <p style="white-space:pre-wrap;line-height:1.8;margin-top:12px;">{{ detail.summary }}</p>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getAdminReportList, deleteAdminReport } from '@/api/admin/manage'
export default {
  name: 'ReportManage',
  data() { return { list:[], total:0, pageNum:1, loading:false, showDialog:false, detail:null } },
  created() { this.fetch() },
  methods: {
    fetch() {
      this.loading=true
      getAdminReportList({page:this.pageNum,size:15}).then(r=>{this.list=r.data.records||[];this.total=r.data.total||0}).finally(()=>{this.loading=false})
    },
    showDetail(row){this.detail=row;this.showDialog=true},
    parseStats(j){try{return JSON.parse(j)}catch(e){return{}}},
    handleDelete(id) {
      this.$confirm('确定删除？','提示',{type:'warning'}).then(()=>{
        deleteAdminReport(id).then(()=>{this.$message.success('已删除');this.fetch()})
      }).catch(()=>{})
    }
  }
}
</script>
