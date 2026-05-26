<template>
  <div class="page-container">
    <h2 class="page-title">心理健康知识</h2>

    <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="心理健康知识" name="knowledge"></el-tab-pane>
      <el-tab-pane label="治疗方法" name="therapy"></el-tab-pane>
      <el-tab-pane label="案例分享" name="case"></el-tab-pane>
    </el-tabs>

    <el-row :gutter="20">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 20px;">
        <el-card shadow="hover" @click.native="$router.push('/article/' + item.id)">
          <div class="article-cover" v-if="item.coverImage">
            <img :src="item.coverImage" alt="" style="width: 100%; height: 160px; object-fit: cover;">
          </div>
          <div style="padding: 10px 0;">
            <h3 style="font-size: 16px; margin-bottom: 8px;">{{ item.title }}</h3>
            <p style="color: #606266; font-size: 13px; height: 40px; overflow: hidden;">
              {{ item.summary }}
            </p>
            <div style="margin-top: 10px; font-size: 12px; color: #909399;">
              <span>{{ item.author }}</span>
              <span style="float: right;">浏览 {{ item.viewCount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination
      v-if="total > 0"
      background
      layout="prev, pager, next"
      :current-page="pageNum"
      :total="total"
      :page-size="pageSize"
      @current-change="handlePageChange"
      style="text-align: center; margin-top: 20px;">
    </el-pagination>
  </div>
</template>

<script>
import { getArticleList } from '@/api/article'

export default {
  name: 'ArticleList',
  data() {
    return {
      activeCategory: 'all',
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 9
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    fetchList() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }

      if (this.activeCategory !== 'all') {
        params.category = this.activeCategory
      }

      getArticleList(params).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      })
    },
    handleCategoryChange() {
      this.pageNum = 1
      this.fetchList()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.fetchList()
    }
  }
}
</script>

<style scoped>
.article-cover {
  margin: -20px -20px 0;
  border-radius: 8px 8px 0 0;
  overflow: hidden;
}
</style>
