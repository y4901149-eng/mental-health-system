<!-- 文章详情页 -->

<template>
  <div class="page-container" style="max-width: 800px; margin: 0 auto;">
    <el-card v-if="article">
      <div style="margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between;">
        <el-tag size="mini">{{ article.category }}</el-tag>
        <el-button size="small" icon="el-icon-arrow-left" @click="$router.push('/article')">返回列表</el-button>
      </div>
      <h1 style="font-size: 24px; margin-bottom: 10px;">{{ article.title }}</h1>
      <div style="color: #909399; font-size: 13px; margin-bottom: 20px;">
        <span>{{ article.author }}</span>
        <span style="margin-left: 20px;">👁 {{ article.viewCount || 0 }}</span>
      </div>
      <div class="article-content" v-html="article.content"></div>
    </el-card>
  </div>
</template>

<script>
import { getArticleDetail } from '@/api/article'

export default {
  name: 'ArticleDetail',
  data() {
    return { article: null }
  },
  created() {
    const id = this.$route.params.id
    getArticleDetail(id).then(res => {
      this.article = res.data
    })
  }
}
</script>

<style scoped>
.article-content {
  line-height: 1.8;
  color: #303133;
}
</style>
