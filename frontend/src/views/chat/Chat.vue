<!-- AI 共情对话 -->
<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>💬 对话列表</h3>
          <el-button type="primary" size="small" circle icon="el-icon-plus" @click="newSession" :disabled="creating" />
        </div>
        <div class="session-list" v-if="sessions.length > 0">
          <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: activeSession && activeSession.id === s.id }"
            @click="switchSession(s)"
          >
            <div class="session-title">{{ s.title }}</div>
            <div class="session-time">{{ formatTime(s.updateTime) }}</div>
            <i class="el-icon-close session-del" @click.stop="handleDelete(s.id)"></i>
          </div>
        </div>
        <div v-else class="sidebar-empty">暂无对话，点击 + 开始</div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-main">
        <div v-if="activeSession" class="chat-messages" ref="msgBox">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.senderRole === 'USER' ? 'msg-user' : 'msg-ai'"
          >
            <div class="msg-avatar">{{ msg.senderRole === 'USER' ? '😊' : '🤖' }}</div>
            <div class="msg-bubble">
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
          <div v-if="aiThinking" class="msg-row msg-ai">
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble thinking">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <div v-else class="chat-empty">
          <div class="empty-icon">🤖</div>
          <h3>AI 共情陪伴</h3>
          <p>选择或创建一个对话，和我聊聊你的心情吧</p>
          <el-button type="primary" size="medium" @click="newSession">开始新对话</el-button>
        </div>

        <!-- 输入区 -->
        <div class="chat-input" v-if="activeSession">
          <el-input
            v-model="inputText"
            placeholder="说说你的感受..."
            size="large"
            :disabled="aiThinking"
            @keyup.enter.native="send"
          />
          <el-button type="primary" size="large" :loading="aiThinking" @click="send" class="send-btn">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getSessions, createSession, getSessionMessages, sendMessage, deleteSession } from '@/api/chat'

export default {
  name: 'Chat',
  data() {
    return {
      sessions: [],
      activeSession: null,
      messages: [],
      inputText: '',
      aiThinking: false,
      creating: false
    }
  },
  created() {
    this.fetchSessions()
  },
  methods: {
    fetchSessions() {
      getSessions().then(res => { this.sessions = res.data || [] })
    },

    newSession() {
      this.creating = true
      createSession('新的对话').then(res => {
        const session = res.data
        this.sessions.unshift(session)
        this.switchSession(session)
      }).finally(() => { this.creating = false })
    },

    switchSession(session) {
      this.activeSession = session
      this.messages = []
      this.inputText = ''
      getSessionMessages(session.id).then(res => {
        this.messages = res.data || []
        this.$nextTick(() => this.scrollToBottom())
      })
    },

    send() {
      const text = this.inputText.trim()
      if (!text || !this.activeSession || this.aiThinking) return
      this.inputText = ''

      // 乐观添加用户消息
      const tempMsg = { id: Date.now(), senderRole: 'USER', content: text, createTime: new Date().toISOString() }
      this.messages.push(tempMsg)
      this.scrollToBottom()
      this.aiThinking = true

      sendMessage(this.activeSession.id, text).then(res => {
        const data = res.data
        if (data && data.aiMessage) {
          this.messages.push(data.aiMessage)
          // 更新会话标题
          if (this.activeSession) {
            this.activeSession.title = data.aiMessage.sessionId
            this.fetchSessions()
          }
        }
        this.scrollToBottom()
      }).catch(() => {
        this.messages.push({ id: Date.now() + 1, senderRole: 'ASSISTANT', content: '抱歉，我现在有点忙，请稍后再试。', createTime: new Date().toISOString() })
        this.scrollToBottom()
      }).finally(() => { this.aiThinking = false })
    },

    handleDelete(id) {
      this.$confirm('确定删除此对话？', '提示').then(() => {
        deleteSession(id).then(() => {
          if (this.activeSession && this.activeSession.id === id) {
            this.activeSession = null
            this.messages = []
          }
          this.fetchSessions()
        })
      }).catch(() => {})
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.msgBox
        if (box) box.scrollTop = box.scrollHeight
      })
    },

    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 100px);
  padding: 0 24px;
}
.chat-container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(64,158,255,0.06);
}
.chat-sidebar {
  width: 260px;
  background: #F8FAFF;
  border-right: 1px solid #EBEEF5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
}
.sidebar-header h3 { margin: 0; font-size: 15px; color: #2C3E50; }
.session-list { flex: 1; overflow-y: auto; padding: 0 8px; }
.session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
  position: relative;
  transition: background 0.2s;
}
.session-item:hover { background: #EEF4FF; }
.session-item.active { background: #EBF5FF; }
.session-title { font-size: 13px; color: #2C3E50; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 20px; }
.session-time { font-size: 11px; color: #C0C4CC; margin-top: 2px; }
.session-del { position: absolute; right: 8px; top: 12px; font-size: 12px; color: #C0C4CC; display: none; }
.session-item:hover .session-del { display: block; }
.session-del:hover { color: #F56C6C; }
.sidebar-empty { text-align: center; padding: 40px 16px; color: #C0C4CC; font-size: 13px; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px 24px; }
.chat-empty {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #909399;
}
.chat-empty .empty-icon { font-size: 64px; margin-bottom: 16px; }
.chat-empty h3 { margin: 0 0 8px; color: #2C3E50; }
.chat-empty p { margin: 0 0 20px; font-size: 14px; }
.msg-row { display: flex; gap: 10px; margin-bottom: 20px; align-items: flex-start; }
.msg-user { flex-direction: row-reverse; }
.msg-avatar {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 18px; flex-shrink: 0;
  background: #F0F5FF;
}
.msg-user .msg-avatar { background: #EBF5FF; }
.msg-bubble {
  max-width: 70%; padding: 12px 16px; border-radius: 16px; font-size: 14px; line-height: 1.6;
  position: relative;
}
.msg-user .msg-bubble {
  background: linear-gradient(135deg, #409EFF, #6C63FF);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-ai .msg-bubble {
  background: #F0F5FF;
  color: #2C3E50;
  border-bottom-left-radius: 4px;
}
.msg-text { word-break: break-word; white-space: pre-wrap; }
.msg-time { font-size: 11px; margin-top: 4px; opacity: 0.6; text-align: right; }
.thinking { display: flex; gap: 4px; padding: 16px 20px !important; }
.dot {
  width: 8px; height: 8px; background: #909399; border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce { 0%,80%,100% { transform: scale(0); } 40% { transform: scale(1); } }
.chat-input {
  display: flex; gap: 10px; padding: 16px 20px;
  border-top: 1px solid #EBEEF5; background: #FAFCFF;
}
.chat-input .el-input { flex: 1; }
.send-btn { border-radius: 12px !important; padding: 12px 24px !important; }
</style>
