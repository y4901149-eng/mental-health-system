<!-- AI 对话 -->
<template>
  <div class="chat-page">
    <div class="chat-container">
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <div>
            <p class="sidebar-label">AI 对话</p>
            <h3>会话列表</h3>
          </div>
          <el-button
            type="primary"
            size="small"
            circle
            icon="el-icon-plus"
            :disabled="creating"
            @click="newSession"
          />
        </div>

        <div v-if="loadingSessions" class="sidebar-empty">正在加载会话...</div>
        <div v-else-if="sessions.length > 0" class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: activeSession && activeSession.id === session.id }"
            @click="switchSession(session)"
          >
            <div class="session-title">{{ session.title || '新的对话' }}</div>
            <div class="session-time">{{ formatTime(session.updateTime || session.createTime) }}</div>
            <i class="el-icon-close session-del" @click.stop="handleDelete(session.id)"></i>
          </div>
        </div>
        <div v-else class="sidebar-empty">
          还没有对话，点击 + 开始。
        </div>
      </aside>

      <main class="chat-main">
        <header class="chat-header">
          <div>
            <p class="chat-kicker">心理健康辅助陪伴</p>
            <h2>{{ activeSession ? activeSession.title || '新的对话' : 'AI 共情陪伴' }}</h2>
          </div>
          <el-button
            plain
            size="small"
            icon="el-icon-edit-outline"
            :loading="creating"
            @click="newSession"
          >
            新对话
          </el-button>
        </header>

        <section v-if="activeSession" class="chat-messages" ref="msgBox">
          <div v-if="loadingMessages" class="messages-loading">
            正在读取聊天记录...
          </div>

          <div v-else-if="messages.length === 0" class="welcome-card">
            <div class="welcome-title">今天想聊点什么？</div>
            <p>可以直接说感受、压力来源，或者只是记录当下的心情。</p>
            <div class="prompt-list">
              <button type="button" @click="usePrompt('我最近有点焦虑，想找人聊聊。')">我最近有点焦虑</button>
              <button type="button" @click="usePrompt('我今天情绪很低落，不知道该怎么办。')">今天情绪很低落</button>
              <button type="button" @click="usePrompt('我想做一次简单的情绪梳理。')">做一次情绪梳理</button>
            </div>
          </div>

          <div
            v-for="message in messages"
            :key="message.id"
            class="msg-row"
            :class="message.senderRole === 'USER' ? 'msg-user' : 'msg-ai'"
          >
            <div class="msg-avatar">{{ message.senderRole === 'USER' ? '我' : 'AI' }}</div>
            <div class="msg-bubble">
              <div class="msg-text">{{ message.content }}</div>
              <div class="msg-time">{{ formatTime(message.createTime) }}</div>
            </div>
          </div>

          <div v-if="aiThinking" class="msg-row msg-ai">
            <div class="msg-avatar">AI</div>
            <div class="msg-bubble thinking">
              <span class="thinking-label">Thinking</span>
              <span class="thinking-dots">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </div>
          </div>
        </section>

        <section v-else class="chat-empty">
          <div class="empty-mark">AI</div>
          <h3>开始一次新的对话</h3>
          <p>系统会保存你的会话记录，方便之后继续查看。</p>
          <el-button type="primary" size="medium" :loading="creating" @click="newSession">
            开始新对话
          </el-button>
        </section>

        <footer v-if="activeSession" class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="说说你的感受，Enter 发送，Shift + Enter 换行"
            :disabled="aiThinking"
            @keydown.native.enter.exact.prevent="send"
          />
          <el-button
            type="primary"
            size="large"
            class="send-btn"
            :loading="aiThinking"
            :disabled="!canSend"
            @click="send"
          >
            发送
          </el-button>
        </footer>
      </main>
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
      creating: false,
      loadingSessions: false,
      loadingMessages: false
    }
  },
  computed: {
    canSend() {
      return !!this.inputText.trim() && !!this.activeSession && !this.aiThinking
    }
  },
  created() {
    this.fetchSessions(true)
  },
  methods: {
    fetchSessions(autoSelect = false) {
      this.loadingSessions = true
      return getSessions()
        .then(res => {
          this.sessions = res.data || []
          if (autoSelect && !this.activeSession) {
            if (this.sessions.length > 0) {
              this.switchSession(this.sessions[0])
            } else {
              this.newSession()
            }
          }
        })
        .catch(() => {
          this.sessions = []
        })
        .finally(() => {
          this.loadingSessions = false
        })
    },

    newSession() {
      if (this.creating) return
      this.creating = true
      return createSession('新的对话')
        .then(res => {
          const session = res.data
          if (!session) return
          this.sessions = [session, ...this.sessions.filter(item => item.id !== session.id)]
          this.switchSession(session)
        })
        .finally(() => {
          this.creating = false
        })
    },

    switchSession(session) {
      if (!session || (this.activeSession && this.activeSession.id === session.id)) return
      this.activeSession = session
      this.messages = []
      this.inputText = ''
      this.loadingMessages = true
      getSessionMessages(session.id)
        .then(res => {
          this.messages = res.data || []
          this.scrollToBottom()
        })
        .finally(() => {
          this.loadingMessages = false
        })
    },

    usePrompt(text) {
      this.inputText = text
      this.$nextTick(() => this.send())
    },

    send() {
      const text = this.inputText.trim()
      if (!text || !this.activeSession || this.aiThinking) return

      const tempId = 'temp-' + Date.now()
      const tempMsg = {
        id: tempId,
        senderRole: 'USER',
        content: text,
        createTime: new Date().toISOString()
      }

      this.inputText = ''
      this.messages.push(tempMsg)
      this.aiThinking = true
      this.scrollToBottom()

      sendMessage(this.activeSession.id, text)
        .then(res => {
          const data = res.data || {}
          const tempIndex = this.messages.findIndex(item => item.id === tempId)
          if (tempIndex !== -1 && data.userMessage) {
            this.$set(this.messages, tempIndex, data.userMessage)
          }
          if (data.aiMessage) {
            this.messages.push(data.aiMessage)
          }
          this.refreshActiveSession()
          this.scrollToBottom()
        })
        .catch(() => {
          this.messages = this.messages.filter(item => item.id !== tempId)
          this.inputText = text
          this.$message.error('消息发送失败，请稍后重试。')
        })
        .finally(() => {
          this.aiThinking = false
        })
    },

    refreshActiveSession() {
      const activeId = this.activeSession && this.activeSession.id
      this.fetchSessions().then(() => {
        if (!activeId) return
        const latest = this.sessions.find(item => item.id === activeId)
        if (latest) {
          this.activeSession = latest
        }
      })
    },

    handleDelete(id) {
      this.$confirm('确定删除这个对话吗？删除后将不会在列表中显示。', '删除对话', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
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
      const date = new Date(dateStr)
      if (Number.isNaN(date.getTime())) return ''
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return month + '-' + day + ' ' + hour + ':' + minute
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
  min-height: 560px;
  background: #fff;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(41, 80, 130, 0.08);
}

.chat-sidebar {
  width: 276px;
  background: #f7f9fc;
  border-right: 1px solid #e7edf6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 16px 14px;
  border-bottom: 1px solid #e7edf6;
}

.sidebar-label,
.chat-kicker {
  margin: 0 0 4px;
  color: #7b8ca5;
  font-size: 12px;
}

.sidebar-header h3,
.chat-header h2 {
  margin: 0;
  color: #24364b;
}

.sidebar-header h3 {
  font-size: 16px;
}

.chat-header h2 {
  font-size: 18px;
  max-width: 520px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 8px;
}

.session-item {
  padding: 12px 34px 12px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 6px;
  position: relative;
  transition: background 0.2s, box-shadow 0.2s;
}

.session-item:hover {
  background: #eef4ff;
}

.session-item.active {
  background: #e8f2ff;
  box-shadow: inset 3px 0 0 #409eff;
}

.session-title {
  font-size: 14px;
  color: #24364b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #96a3b5;
  margin-top: 4px;
}

.session-del {
  position: absolute;
  right: 10px;
  top: 16px;
  font-size: 13px;
  color: #a8b3c3;
  display: none;
}

.session-item:hover .session-del {
  display: block;
}

.session-del:hover {
  color: #f56c6c;
}

.sidebar-empty {
  text-align: center;
  padding: 42px 18px;
  color: #96a3b5;
  font-size: 13px;
  line-height: 1.6;
}

.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 72px;
  padding: 0 22px;
  border-bottom: 1px solid #e7edf6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-shrink: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 22px 24px;
  background: linear-gradient(180deg, #fbfdff 0%, #f7faff 100%);
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #738198;
  text-align: center;
  padding: 24px;
}

.empty-mark {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #e8f2ff;
  color: #2678d9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-bottom: 16px;
}

.chat-empty h3 {
  margin: 0 0 8px;
  color: #24364b;
  font-size: 20px;
}

.chat-empty p {
  margin: 0 0 20px;
  font-size: 14px;
}

.welcome-card {
  max-width: 620px;
  margin: 12px auto 26px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e7edf6;
  border-radius: 8px;
}

.messages-loading {
  max-width: 260px;
  margin: 32px auto;
  padding: 12px 14px;
  color: #6d7d93;
  font-size: 14px;
  text-align: center;
  background: #fff;
  border: 1px solid #e7edf6;
  border-radius: 8px;
}

.welcome-title {
  font-size: 18px;
  font-weight: 700;
  color: #24364b;
  margin-bottom: 8px;
}

.welcome-card p {
  margin: 0 0 14px;
  color: #6d7d93;
  line-height: 1.7;
}

.prompt-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.prompt-list button {
  border: 1px solid #cfe0f7;
  background: #f7fbff;
  color: #2f6fad;
  border-radius: 8px;
  padding: 7px 10px;
  cursor: pointer;
}

.prompt-list button:hover {
  border-color: #409eff;
  color: #1f69c7;
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  align-items: flex-start;
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  background: #edf3fb;
  color: #4b6685;
}

.msg-user .msg-avatar {
  background: #e5f1ff;
  color: #2678d9;
}

.msg-bubble {
  max-width: min(70%, 720px);
  padding: 12px 15px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  position: relative;
}

.msg-user .msg-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-ai .msg-bubble {
  background: #fff;
  color: #24364b;
  border: 1px solid #e7edf6;
  border-bottom-left-radius: 4px;
}

.msg-text {
  word-break: break-word;
  white-space: pre-wrap;
}

.msg-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.65;
  text-align: right;
}

.thinking {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px 20px !important;
}

.thinking-label {
  color: #52708f;
  font-weight: 700;
  letter-spacing: 0;
  animation: thinkingFlash 1.2s infinite ease-in-out;
}

.thinking-dots {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #8fa2b8;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes thinkingFlash {
  0%, 100% {
    opacity: 0.45;
  }
  45% {
    opacity: 1;
  }
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #e7edf6;
  background: #fff;
  flex-shrink: 0;
}

.chat-input .el-textarea {
  flex: 1;
}

.send-btn {
  border-radius: 8px !important;
  padding: 12px 24px !important;
}

@media (max-width: 900px) {
  .chat-page {
    height: calc(100vh - 80px);
    padding: 0 12px;
  }

  .chat-container {
    min-height: 520px;
  }

  .chat-sidebar {
    width: 220px;
  }

  .msg-bubble {
    max-width: 82%;
  }
}
</style>
