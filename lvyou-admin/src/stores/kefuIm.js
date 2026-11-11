import { ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchKefuImUnreadCount } from '@/api/modules/kefu-im'
import { isAuth } from '@/utils/tool'

export const useKefuImStore = defineStore('kefuIm', () => {
  const unreadCount = ref(0)
  let pollTimer = null

  async function refreshUnread() {
    if (!isAuth('lvyou:kefuIm:session:page')) {
      unreadCount.value = 0
      return 0
    }
    try {
      const data = await fetchKefuImUnreadCount()
      unreadCount.value = Number(data?.count || 0)
    } catch {
      unreadCount.value = 0
    }
    return unreadCount.value
  }

  function startPolling(intervalMs = 15000) {
    stopPolling()
    refreshUnread()
    pollTimer = window.setInterval(() => {
      refreshUnread()
    }, intervalMs)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function getMenuBadge(path = '') {
    if (path === '/kefu-im' && unreadCount.value > 0) {
      return unreadCount.value
    }
    return 0
  }

  return {
    unreadCount,
    refreshUnread,
    startPolling,
    stopPolling,
    getMenuBadge,
  }
})
