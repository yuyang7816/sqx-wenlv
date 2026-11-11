<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  closeKefuImSession,
  fetchKefuImMessages,
  kefuImSessionApi,
  replyKefuImMessage,
} from '@/api/modules/kefu-im'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useKefuImStore } from '@/stores/kefuIm'

const loadingSessions = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const sessions = ref([])
const sessionTotal = ref(0)
const sessionPage = ref(1)
const sessionLimit = ref(20)
const selectedSession = ref(null)
const messages = ref([])
const replyText = ref('')
const messageWrapRef = ref(null)
const pollTimer = ref(null)

const filters = ref({
  status: 'open',
  userPhone: '',
  unreadOnly: false,
})

const kefuImStore = useKefuImStore()
const canReply = computed(() => isAuth('lvyou:kefuIm:message:reply'))
const canClose = computed(() => isAuth('lvyou:kefuIm:session:close'))

const STATUS_OPTIONS = [
  { label: '进行中', value: 'open' },
  { label: '已关闭', value: 'closed' },
  { label: '全部', value: '' },
]

function formatUser(session) {
  if (!session) return '-'
  const name = session.userName || `用户${session.userId || ''}`
  return session.userPhone ? `${name}（${session.userPhone}）` : name
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

async function loadSessions(resetPage = false) {
  if (resetPage) sessionPage.value = 1
  loadingSessions.value = true
  try {
    const { list, total } = await kefuImSessionApi.page({
      page: sessionPage.value,
      limit: sessionLimit.value,
      status: filters.value.status || undefined,
      userPhone: filters.value.userPhone || undefined,
      unreadOnly: filters.value.unreadOnly || undefined,
    })
    sessions.value = list
    sessionTotal.value = total
    if (selectedSession.value) {
      const matched = list.find((item) => item.id === selectedSession.value.id)
      if (matched) selectedSession.value = matched
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载会话失败')
  } finally {
    loadingSessions.value = false
  }
}

async function loadMessages(initial = false) {
  if (!selectedSession.value?.id) {
    messages.value = []
    return
  }
  if (initial) loadingMessages.value = true
  try {
    const sinceId = initial ? undefined : messages.value[messages.value.length - 1]?.id
    const list = await fetchKefuImMessages({
      sessionId: selectedSession.value.id,
      sinceId: sinceId || undefined,
      limit: 100,
    })
    if (initial) {
      messages.value = list || []
    } else if (list?.length) {
      messages.value = [...messages.value, ...list]
    }
    await nextTick()
    scrollToBottom()
    if (initial) {
      await loadSessions()
      await kefuImStore.refreshUnread()
    }
  } catch (error) {
    if (initial) showError(error?.msg || error?.message || '加载消息失败')
  } finally {
    if (initial) loadingMessages.value = false
  }
}

function scrollToBottom() {
  const el = messageWrapRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function selectSession(session) {
  selectedSession.value = session
  await loadMessages(true)
  startPolling()
}

function startPolling() {
  stopPolling()
  pollTimer.value = window.setInterval(() => {
    if (selectedSession.value?.id) {
      loadMessages(false)
      loadSessions(false)
    }
  }, 5000)
}

function stopPolling() {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
    pollTimer.value = null
  }
}

async function handleReply() {
  const content = replyText.value.trim()
  if (!content || !selectedSession.value?.id || sending.value) return
  if (!canReply.value) return
  sending.value = true
  try {
    await replyKefuImMessage({
      sessionId: selectedSession.value.id,
      content,
    })
    replyText.value = ''
    await loadMessages(true)
    showSuccess('回复成功')
  } catch (error) {
    showError(error?.msg || error?.message || '回复失败')
  } finally {
    sending.value = false
  }
}

async function handleClose() {
  if (!selectedSession.value?.id || !canClose.value) return
  try {
    await closeKefuImSession(selectedSession.value.id)
    showSuccess('会话已关闭')
    selectedSession.value = null
    messages.value = []
    stopPolling()
    await loadSessions(true)
    await kefuImStore.refreshUnread()
  } catch (error) {
    showError(error?.msg || error?.message || '关闭会话失败')
  }
}

function handleSearch() {
  selectedSession.value = null
  messages.value = []
  stopPolling()
  loadSessions(true)
}

onMounted(() => {
  loadSessions(true)
})

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<template>
  <div class="kefu-im-page">
    <div class="kefu-im-layout">
      <aside class="kefu-im-sessions">
        <div class="kefu-im-panel-head">
          <strong>会话列表</strong>
        </div>

        <el-form class="kefu-im-filters" label-width="56px" @submit.prevent="handleSearch">
          <el-form-item label="状态">
            <el-select v-model="filters.status" style="width: 100%">
              <el-option
                v-for="item in STATUS_OPTIONS"
                :key="item.value || 'all'"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="filters.userPhone" clearable placeholder="用户手机号" />
          </el-form-item>
          <el-form-item label="筛选">
            <el-checkbox v-model="filters.unreadOnly">仅未读</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>

        <div v-loading="loadingSessions" class="kefu-im-session-list">
          <div
            v-for="item in sessions"
            :key="item.id"
            class="kefu-im-session-item"
            :class="{ 'kefu-im-session-item--active': selectedSession?.id === item.id }"
            @click="selectSession(item)"
          >
            <div class="kefu-im-session-item__top">
              <span class="kefu-im-session-item__name">{{ formatUser(item) }}</span>
              <el-badge v-if="item.adminUnreadCount > 0" :value="item.adminUnreadCount" />
            </div>
            <div class="kefu-im-session-item__preview">{{ item.lastMessage || '暂无消息' }}</div>
            <div class="kefu-im-session-item__meta">
              <el-tag size="small" :type="item.status === 'open' ? 'success' : 'info'">
                {{ item.statusLabel || item.status }}
              </el-tag>
              <span>{{ formatTime(item.lastMessageTime || item.createTime) }}</span>
            </div>
          </div>
          <el-empty v-if="!loadingSessions && !sessions.length" description="暂无会话" />
        </div>

        <div v-if="sessionTotal > sessionLimit" class="kefu-im-session-pagination">
          <el-pagination
            v-model:current-page="sessionPage"
            :page-size="sessionLimit"
            layout="prev, pager, next"
            :total="sessionTotal"
            small
            @current-change="loadSessions(false)"
          />
        </div>
      </aside>

      <section class="kefu-im-chat">
        <template v-if="selectedSession">
          <div class="kefu-im-chat__header">
            <div class="kefu-im-chat__header-main">
              <strong>{{ formatUser(selectedSession) }}</strong>
              <el-tag size="small" :type="selectedSession.status === 'open' ? 'success' : 'info'">
                {{ selectedSession.statusLabel || selectedSession.status }}
              </el-tag>
            </div>
            <el-button v-if="canClose && selectedSession.status === 'open'" link type="danger" @click="handleClose">
              关闭会话
            </el-button>
          </div>

          <div ref="messageWrapRef" v-loading="loadingMessages" class="kefu-im-chat__messages">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="kefu-im-message"
              :class="{ 'kefu-im-message--staff': msg.senderType === 'staff' }"
            >
              <div class="kefu-im-message__meta">
                <span>{{ msg.senderType === 'staff' ? '客服' : '用户' }}</span>
                <span>{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="kefu-im-message__bubble">{{ msg.content }}</div>
            </div>
            <el-empty v-if="!loadingMessages && !messages.length" description="暂无消息，等待用户咨询" />
          </div>

          <div v-if="selectedSession.status === 'open'" class="kefu-im-chat__composer">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="输入回复内容，Ctrl + Enter 发送"
              @keydown.ctrl.enter="handleReply"
            />
            <div class="kefu-im-chat__actions">
              <el-button type="primary" :loading="sending" :disabled="!canReply || !replyText.trim()" @click="handleReply">
                发送回复
              </el-button>
            </div>
          </div>
          <div v-else class="kefu-im-chat__closed">会话已关闭，如需继续沟通请等待用户再次发起咨询。</div>
        </template>
        <div v-else class="kefu-im-chat__empty">
          <el-empty description="请选择左侧会话开始接待" />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.kefu-im-page {
  height: calc(100vh - 180px);
  min-height: 560px;
}

.kefu-im-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  height: 100%;
}

.kefu-im-sessions,
.kefu-im-chat {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: var(--el-bg-color);
  min-height: 0;
  overflow: hidden;
}

.kefu-im-sessions {
  display: flex;
  flex-direction: column;
}

.kefu-im-panel-head {
  padding: 14px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 15px;
}

.kefu-im-filters {
  padding: 12px 16px 0;
}

.kefu-im-filters :deep(.el-form-item) {
  margin-bottom: 12px;
}

.kefu-im-filters :deep(.el-form-item__label),
.kefu-im-panel-head strong,
.kefu-im-chat__header-main strong {
  color: var(--el-text-color-primary);
}

.kefu-im-filters :deep(.el-checkbox__label) {
  color: var(--el-text-color-regular);
}

.kefu-im-session-list {
  flex: 1;
  overflow: auto;
  padding: 0 12px 12px;
}

.kefu-im-session-item {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  margin-bottom: 8px;
  background: var(--el-fill-color-light);
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.kefu-im-session-item:hover {
  background: var(--el-fill-color);
}

.kefu-im-session-item--active {
  border-color: rgba(255, 102, 0, 0.45);
  background: rgba(255, 138, 61, 0.14);
}

.kefu-im-session-item--active .kefu-im-session-item__name {
  color: var(--el-text-color-primary);
}

.kefu-im-session-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.kefu-im-session-item__name {
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kefu-im-session-item__preview {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kefu-im-session-item__meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.kefu-im-session-pagination {
  padding: 8px 12px 12px;
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--el-border-color-lighter);
}

.kefu-im-chat {
  display: flex;
  flex-direction: column;
}

.kefu-im-chat__header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.kefu-im-chat__header-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.kefu-im-chat__header-main strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kefu-im-chat__messages {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background: var(--el-fill-color-dark);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.kefu-im-message {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: min(72%, 560px);
  width: fit-content;
}

.kefu-im-message--staff {
  align-self: flex-end;
  align-items: flex-end;
}

.kefu-im-message__meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
  line-height: 1.4;
}

.kefu-im-message__bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
  color: var(--el-text-color-primary);
}

.kefu-im-message--staff .kefu-im-message__bubble {
  background: rgba(255, 138, 61, 0.16);
  color: #ffb066;
}

.kefu-im-chat__composer {
  padding: 12px 16px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  background: var(--el-bg-color);
}

.kefu-im-chat__actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.kefu-im-chat__closed,
.kefu-im-chat__empty {
  padding: 24px 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-dark);
}

.kefu-im-chat__closed {
  border-top: 1px solid var(--el-border-color-lighter);
  flex: 0 0 auto;
  background: var(--el-bg-color);
}
</style>
