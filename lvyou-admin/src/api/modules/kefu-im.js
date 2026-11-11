import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi, resolvePageData } from '@/api/admin'

const PREFIX = 'admin/kefu-im'

export const kefuImSessionApi = createPageApi(`${PREFIX}/session`, { pageMethod: 'page' })

export function fetchKefuImMessages(data = {}) {
  return post(`${PREFIX}/message/list`, data).then(resolveBusinessResponse)
}

export function replyKefuImMessage(data = {}) {
  return post(`${PREFIX}/message/reply`, data).then(resolveBusinessResponse)
}

export function closeKefuImSession(sessionId) {
  return post(`${PREFIX}/session/close`, { sessionId }).then(resolveBusinessResponse)
}

export function fetchKefuImUnreadCount() {
  return post(`${PREFIX}/session/unread-count`, {}).then(resolveBusinessResponse)
}

export { resolvePageData }
