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
            <div class="msg-avatar" :class="message.senderRole === 'USER' ? 'user-avatar' : 'ai-avatar'">
              <template v-if="message.senderRole === 'USER'">我</template>
              <span v-else class="ai-core" aria-label="AI助手头像" role="img">
                <span class="ai-eye left-eye"></span>
                <span class="ai-eye right-eye"></span>
                <span class="ai-smile"></span>
              </span>
            </div>
            <div class="msg-bubble">
              <div class="msg-text">{{ message.content }}</div>
              <div class="msg-time">{{ formatTime(message.createTime) }}</div>
            </div>
          </div>

          <div v-if="aiThinking" class="msg-row msg-ai">
            <div class="msg-avatar ai-avatar">
              <span class="ai-core" aria-label="AI助手头像" role="img">
                <span class="ai-eye left-eye"></span>
                <span class="ai-eye right-eye"></span>
                <span class="ai-smile"></span>
              </span>
            </div>
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
  background:
    linear-gradient(135deg, rgba(64, 158, 255, 0.06), rgba(103, 194, 58, 0.04)),
    #f5f8fc;
}

.chat-container {
  display: flex;
  height: 100%;
  min-height: 560px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(219, 232, 248, 0.92);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 48px rgba(41, 80, 130, 0.12);
}

.chat-sidebar {
  width: 276px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef5fb 100%);
  border-right: 1px solid #e7edf6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 18px 16px;
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
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 8px;
  position: relative;
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.session-item:hover {
  background: #fff;
  border-color: #dce9f8;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(41, 80, 130, 0.08);
}

.session-item.active {
  background: linear-gradient(135deg, #e8f3ff 0%, #f5fbff 100%);
  border-color: #bcd9fa;
  box-shadow: inset 3px 0 0 #409eff, 0 10px 22px rgba(64, 158, 255, 0.12);
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
  min-height: 78px;
  padding: 0 24px;
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
  background:
    linear-gradient(180deg, rgba(251, 253, 255, 0.92) 0%, rgba(242, 247, 252, 0.92) 100%),
    repeating-linear-gradient(135deg, rgba(64, 158, 255, 0.035) 0 1px, transparent 1px 12px);
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
  padding: 22px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(219, 232, 248, 0.95);
  border-radius: 14px;
  box-shadow: 0 14px 30px rgba(41, 80, 130, 0.08);
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
  background: linear-gradient(180deg, #fff 0%, #f7fbff 100%);
  color: #2f6fad;
  border-radius: 999px;
  padding: 8px 12px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}

.prompt-list button:hover {
  border-color: #409eff;
  color: #1f69c7;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.12);
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
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  background: #edf3fb;
  color: #4b6685;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.8);
  position: relative;
}

.user-avatar {
  background: linear-gradient(135deg, #dff0ff, #f4fbff);
  color: #2678d9;
}

.ai-avatar {
  color: transparent;
  background:
    radial-gradient(circle at 30% 24%, rgba(255, 255, 255, 0.72) 0 5px, transparent 6px),
    linear-gradient(135deg, #37b6a5 0%, #409eff 58%, #5f8df7 100%);
  box-shadow:
    0 10px 22px rgba(64, 158, 255, 0.18),
    inset 0 0 0 1px rgba(255, 255, 255, 0.34);
  overflow: visible;
  isolation: isolate;
}

.ai-avatar::before {
  content: "";
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 1px solid rgba(64, 158, 255, 0.22);
  animation: aiHalo 2.8s infinite ease-in-out;
}

.ai-avatar::after {
  content: "";
  position: absolute;
  right: -1px;
  bottom: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #67c23a;
  border: 2px solid #fff;
}

.ai-core {
  width: 25px;
  height: 20px;
  position: relative;
  display: block;
  z-index: 1;
  border-radius: 10px 10px 12px 12px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow:
    inset 0 -2px 0 rgba(64, 158, 255, 0.12),
    0 2px 8px rgba(20, 70, 120, 0.12);
}

.ai-core::before {
  content: "";
  position: absolute;
  left: 50%;
  top: -6px;
  width: 2px;
  height: 6px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(-50%);
}

.ai-core::after {
  content: "";
  position: absolute;
  left: 50%;
  top: -9px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.65);
  transform: translateX(-50%);
}

.ai-eye {
  position: absolute;
  top: 7px;
  width: 4px;
  height: 5px;
  border-radius: 50%;
  background: #2678d9;
  animation: aiBlink 4.8s infinite ease-in-out;
}

.left-eye {
  left: 7px;
}

.right-eye {
  right: 7px;
}

.ai-smile {
  position: absolute;
  left: 50%;
  bottom: 5px;
  width: 9px;
  height: 5px;
  border-bottom: 2px solid #37b6a5;
  border-radius: 0 0 9px 9px;
  transform: translateX(-50%);
}

@keyframes aiHalo {
  0%, 100% {
    opacity: 0.55;
    transform: scale(1);
  }
  50% {
    opacity: 0.95;
    transform: scale(1.08);
  }
}

@keyframes aiBlink {
  0%, 88%, 100% {
    transform: scaleY(1);
  }
  92% {
    transform: scaleY(0.2);
  }
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
  background: linear-gradient(135deg, #409eff 0%, #2f7fdc 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.2);
}

.msg-ai .msg-bubble {
  background: rgba(255, 255, 255, 0.98);
  color: #24364b;
  border: 1px solid rgba(222, 233, 247, 0.95);
  border-bottom-left-radius: 4px;
  box-shadow: 0 8px 20px rgba(41, 80, 130, 0.07);
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
  background: rgba(255, 255, 255, 0.96);
  flex-shrink: 0;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #dbe8f8;
  box-shadow: inset 0 1px 2px rgba(41, 80, 130, 0.04);
}

.chat-input .el-textarea {
  flex: 1;
}

.send-btn {
  border-radius: 12px !important;
  padding: 12px 24px !important;
  box-shadow: 0 10px 22px rgba(64, 158, 255, 0.2);
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
