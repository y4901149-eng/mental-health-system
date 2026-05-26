<!-- 日记页面 - 瀑布流卡片布局 -->
<!-- 替换文件：src/views/diary/Diary.vue -->

<template>
  <div class="diary-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">📖 心情日记</h1>
        <p class="page-subtitle">记录每一天的情绪与感悟</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="medium" @click="showEditor = true">
          <i class="el-icon-edit"></i> 写日记
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索日记内容..."
        prefix-icon="el-icon-search"
        size="medium"
        class="search-input"
        clearable
        @keyup.enter.native="handleSearch"
        @clear="handleSearch"
      />
    </div>

    <!-- 瀑布流日记列表 -->
    <div v-if="diaryList.length > 0" class="masonry-grid">
      <div
        class="masonry-item"
        v-for="item in diaryList"
        :key="item.id"
      >
        <div class="diary-card">
          <!-- 封面图 -->
          <div class="card-cover" v-if="item.imageUrl" @click="previewImage(item.imageUrl)">
            <img :src="item.imageUrl" class="cover-img" />
          </div>
          <div class="card-banner" v-else :style="{ background: getBannerGradient(item.id) }">
            <div class="banner-date">{{ formatDate(item.createTime) }}</div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-body">
            <!-- 标题 -->
            <h3 class="diary-title" v-if="item.title">{{ item.title }}</h3>

            <!-- 日记文字 -->
            <p class="diary-content">{{ item.content }}</p>

            <!-- 底部 -->
            <div class="card-footer">
              <span class="footer-time">{{ formatTime(item.createTime) }}</span>
              <div class="footer-actions">
                <el-button type="text" class="edit-btn" @click="handleEdit(item)">编辑</el-button>
                <el-button type="text" class="delete-btn" @click="handleDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="!loading"
      :image-size="120"
      description="还没有日记，开始记录今天的心情吧"
    >
      <el-button type="primary" size="medium" @click="showEditor = true">
        写第一篇日记
      </el-button>
    </el-empty>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <i class="el-icon-loading"></i> 加载中...
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <el-button type="text" @click="loadMore" :loading="loadingMore">
        加载更多
      </el-button>
    </div>

    <!-- ===== 写/编辑日记对话框 ===== -->
    <el-dialog
      :visible.sync="showEditor"
      :title="editingDiary ? '编辑日记' : '写日记'"
      width="560px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <el-form :model="diaryForm" label-position="top" size="medium">
        <el-form-item label="标题">
          <el-input v-model="diaryForm.title" placeholder="给日记取个标题（选填）" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="日记内容">
          <el-input
            v-model="diaryForm.content"
            type="textarea"
            :rows="5"
            placeholder="记录今天的心情和感悟..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="配图">
          <el-upload
            ref="upload"
            :show-file-list="false"
            :before-upload="handleUpload"
            accept="image/*"
          >
            <el-button size="small" type="primary" plain class="image-upload-btn">
              <i class="el-icon-upload"></i> 选择图片
            </el-button>
            <span style="margin-left:10px;font-size:12px;color:#909399;">支持 JPG/PNG</span>
          </el-upload>
          <div v-if="diaryForm.imageUrl" class="upload-preview">
            <img :src="diaryForm.imageUrl" class="preview-img" />
            <el-button type="text" size="mini" style="color:#F56C6C;" @click="diaryForm.imageUrl = ''">删除</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="showEditor = false">取消</el-button>
        <el-button type="primary" @click="submitDiary" :loading="submitting">
          {{ editingDiary ? '保存修改' : '保存日记' }}
        </el-button>
      </div>
    </el-dialog>
    <!-- ===== 图片预览弹窗 ===== -->
    <el-dialog :visible.sync="previewVisible" width="auto" top="5vh"
      :close-on-click-modal="true" :show-close="true" :modal-append-to-body="false"
      :before-close="closePreview">
      <img :src="previewUrl" style="max-width:80vw;max-height:80vh;border-radius:8px;display:block;margin:0 auto;" />
    </el-dialog>
  </div>
</template>

<script>
import { getDiaryList, createDiary, updateDiary, deleteDiary, uploadImage } from '@/api/diary'

// 情绪标签 → 渐变色
const bannerGradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)'
]

const tagColorMap = {
  '开心': '#EBF5FF',
  '平静': '#E8F8F0',
  '悲伤': '#FDEEEE',
  '焦虑': '#FDF5E6',
  '愤怒': '#FDEEEE',
  '疲惫': '#F5F0FF',
  '快乐': '#EBF5FF',
  '忧伤': '#FDEEEE',
  '感动': '#FDF5E6',
  '期待': '#F0EFFF'
}

const tagTextColorMap = {
  '开心': '#409EFF',
  '平静': '#67C23A',
  '悲伤': '#F56C6C',
  '焦虑': '#E6A23C',
  '愤怒': '#F56C6C',
  '疲惫': '#6C63FF'
}

const moodEmojiMap = {
  '开心': '😊', '快乐': '😊', '高兴': '😄',
  '平静': '😌',
  '悲伤': '😢', '忧伤': '😢', '难过': '😢',
  '焦虑': '😰',
  '愤怒': '😠', '生气': '😠',
  '疲惫': '😴', '累': '😴'
}

export default {
  name: 'Diary',
  data() {
    return {
      diaryList: [],
      pageNum: 1,
      pageSize: 20,
      hasMore: true,
      loading: false,
      loadingMore: false,
      searchKeyword: '',
      showEditor: false,
      submitting: false,
      editingDiary: null,
      previewVisible: false,
      previewUrl: '',
      diaryForm: {
        title: '',
        content: '',
        imageUrl: ''
      }
    }
  },
  created() {
    this.fetchDiary()
  },
  watch: {
    showEditor(val) {
      if (!val) this.editingDiary = null
    }
  },
  methods: {
    fetchDiary() {
      this.loading = true
      return getDiaryList({
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        keyword: this.searchKeyword || undefined
      }).then(res => {
        const data = res.data
        if (this.pageNum === 1) {
          this.diaryList = data.records || []
        } else {
          this.diaryList = [...this.diaryList, ...(data.records || [])]
        }
        this.hasMore = this.pageNum < data.pages
      }).finally(() => {
        this.loading = false
      })
    },

    loadMore() {
      this.pageNum++
      this.loadingMore = true
      this.fetchDiary().finally(() => {
        this.loadingMore = false
      })
    },

    handleSearch() {
      this.pageNum = 1
      this.fetchDiary()
    },

    submitDiary() {
      if (!this.diaryForm.content.trim()) {
        this.$message.warning('请输入日记内容')
        return
      }
      this.submitting = true

      const data = {
        title: this.diaryForm.title || undefined,
        content: this.diaryForm.content,
        imageUrl: this.diaryForm.imageUrl || undefined
      }
      if (this.editingDiary) data.id = this.editingDiary.id

      const savePromise = this.editingDiary ? updateDiary(data) : createDiary(data)

      savePromise.then(() => {
        this.$message.success(this.editingDiary ? '日记已更新' : '日记已保存')
        this.showEditor = false
        this.editingDiary = null
        this.diaryForm = { title: '', content: '', imageUrl: '' }
        this.pageNum = 1
        this.fetchDiary()
      }).finally(() => {
        this.submitting = false
      })
    },

    handleEdit(item) {
      this.editingDiary = item
      this.diaryForm = {
        title: item.title || '',
        content: item.content || '',
        imageUrl: item.imageUrl || ''
      }
      this.showEditor = true
    },

    handleDelete(id) {
      this.$confirm('确定删除这篇日记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteDiary(id).then(() => {
          this.$message.success('已删除')
          this.diaryList = this.diaryList.filter(d => d.id !== id)
        })
      }).catch(() => {})
    },

    handleUpload(file) {
      uploadImage(file).then(res => {
        this.diaryForm.imageUrl = res.data
        this.$message.success('图片已上传')
      })
      return false  // 阻止默认上传
    },

    previewImage(url) {
      this.previewUrl = url
      this.previewVisible = true
    },
    closePreview() {
      this.previewVisible = false
      this.previewUrl = ''
    },

    getScoreColor(score) {
      if (score == null) return '#909399'
      if (score >= 70) return '#67C23A'
      if (score >= 45) return '#E6A23C'
      return '#F56C6C'
    },

    /* ===== 工具方法 ===== */
    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags
      return tags.split(',').map(t => t.trim()).filter(Boolean)
    },

    getBannerGradient(id) {
      return bannerGradients[Number(id) % bannerGradients.length]
    },

    getMoodEmoji(tags) {
      const list = this.parseTags(tags)
      for (const t of list) {
        if (moodEmojiMap[t]) return moodEmojiMap[t]
      }
      return '💭'
    },

    getTagColor(tag) {
      return tagColorMap[tag] || '#F0F5FF'
    },

    getTagTextColor(tag) {
      return tagTextColorMap[tag] || '#409EFF'
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const month = d.getMonth() + 1
      const day = d.getDate()
      return month + '月' + day + '日'
    },

    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      return h + ':' + m
    },

    onImgError(e) {
      e.target.style.display = 'none'
    }
  }
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.diary-page {
  max-width: var(--page-width, 1200px);
  margin: 0 auto;
  padding: 0 24px 40px;
}

/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-top: 28px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary, #2C3E50);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-muted, #909399);
  margin-top: 4px;
}

/* ===== 搜索栏 ===== */
.search-bar {
  margin-bottom: 28px;
}

.search-input {
  max-width: 400px;
}

.search-input :deep(.el-input__inner) {
  border-radius: 20px !important;
  height: 42px !important;
  padding-left: 44px !important;
  background: #F5F7FA;
  border-color: transparent !important;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__inner:focus) {
  background: #fff;
  border-color: var(--primary, #409EFF) !important;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1) !important;
}

.search-input :deep(.el-input__prefix) {
  left: 16px !important;
  top: 1px;
}

.search-input :deep(.el-input__icon) {
  color: #A0AEC0;
  font-size: 16px;
}

/* ===== 瀑布流容器 ===== */
.masonry-grid {
  column-count: 3;
  column-gap: 20px;
}

.masonry-item {
  break-inside: avoid;
  margin-bottom: 20px;
}

/* ===== 日记卡片 ===== */
.diary-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.06);
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: default;
  border: 1px solid #F0F5FF;
}

.diary-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.15);
}

/* ===== 卡片顶部色条 ===== */
.card-banner {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: relative;
}

.banner-mood {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.banner-emoji {
  font-size: 22px;
}

.banner-date {
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

/* ===== 卡片内容 ===== */
.card-body {
  padding: 16px 18px 14px;
}

/* 情绪标签 */
.mood-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.mood-tag {
  font-size: 12px;
  padding: 2px 12px;
  border-radius: 20px;
  font-weight: 500;
  color: var(--text-primary, #2C3E50);
  letter-spacing: 0.3px;
}

/* 日记文字 */
.diary-content {
  font-size: 14px;
  color: var(--text-secondary, #606266);
  line-height: 1.7;
  margin: 0 0 12px;
  word-break: break-word;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 图片列表 */
.diary-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.diary-img {
  width: calc(50% - 3px);
  border-radius: 8px;
  object-fit: cover;
  max-height: 160px;
}

/* 底部 */
/* 封面图 */
.card-cover {
  width: 100%; height: 160px; overflow: hidden; cursor: pointer;
  border-radius: 12px 12px 0 0;
}
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.diary-card:hover .cover-img { transform: scale(1.05); }

/* 标题 */
.diary-title { font-size: 16px; font-weight: 700; color: #2C3E50; margin: 0 0 8px; line-height: 1.4; }

/* 情绪行 */
.mood-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }

/* 情绪分数 */
.emotion-score { font-size: 13px; font-weight: 600; flex-shrink: 0; }

/* 上传预览 */
.upload-preview { margin-top: 10px; display: flex; align-items: center; gap: 10px; }
.preview-img { width: 80px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid #EBEEF5; }

.image-upload-btn {
  min-width: 120px;
  height: 40px;
  border-radius: 12px;
  color: #fff !important;
  background: #409EFF !important;
  border-color: #409EFF !important;
  font-weight: 700;
}

.image-upload-btn :deep(span),
.image-upload-btn :deep(i) {
  color: #fff !important;
}

.image-upload-btn:hover,
.image-upload-btn:focus {
  color: #fff !important;
  background: #2678d9 !important;
  border-color: #2678d9 !important;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #F0F5FF;
}

.footer-time {
  font-size: 12px;
  color: var(--text-muted, #A0AEC0);
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.edit-btn {
  font-size: 12px !important;
  color: #C0C4CC !important;
}

.edit-btn:hover {
  color: #409EFF !important;
}

.delete-btn {
  font-size: 12px !important;
  color: #C0C4CC !important;
}

.delete-btn:hover {
  color: #F56C6C !important;
}

/* ===== 加载状态 ===== */
.loading-wrap {
  text-align: center;
  padding: 60px 0;
  color: var(--text-muted, #909399);
  font-size: 14px;
}

.loading-wrap i {
  font-size: 24px;
  margin-right: 8px;
}

.load-more {
  text-align: center;
  padding: 20px 0 40px;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .masonry-grid {
    column-count: 2;
  }
}

@media (max-width: 600px) {
  .masonry-grid {
    column-count: 1;
  }
}
</style>
