<!-- 心理评估列表页 -->
<!-- 展示所有可用的心理量表，用户选择后开始评估 -->

<template>
  <div class="page-container">
    <h2 class="page-title">📋 心理评估量表</h2>
    <p style="color: #909399; margin-bottom: 20px;">
      选择下面的量表进行心理健康自测。评估结果仅作为参考，不能替代专业诊断。
    </p>

    <!-- 量表列表 -->
    <el-row :gutter="20">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 20px;">
        <el-card shadow="hover" @click.native="startAssessment(item.id)">
          <div slot="header">
            <span style="font-weight: bold;">{{ item.title }}</span>
            <el-tag size="mini" style="float: right;">{{ item.type }}</el-tag>
          </div>
          <p style="color: #606266; font-size: 13px; min-height: 40px;">
            {{ item.description }}
          </p>
          <div style="font-size: 12px; color: #909399;">
            {{ item.questionCount }} 道题
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="list.length === 0" description="暂无量表数据"></el-empty>
  </div>
</template>

<script>
import { getAssessmentList } from '@/api/assessment'

export default {
  name: 'AssessmentList',
  data() {
    return { list: [] }
  },
  created() {
    this.fetchList()
  },
  methods: {
    fetchList() {
      getAssessmentList().then(res => {
        this.list = res.data || []
      })
    },
    startAssessment(id) {
      this.$router.push('/assessment/' + id)
    }
  }
}
</script>
