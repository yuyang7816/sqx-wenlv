import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getMenuBadges } from '@/api/modules/stats'

const MENU_BADGE_RESOLVERS = {
  '/business/merchant-audit': (counts) => Number(counts.pendingMerchantCount || 0),
  '/business/order': (counts) => Number(counts.pendingOrderCount || 0) + Number(counts.refundingOrderCount || 0),
  '/business/withdraw': (counts) => Number(counts.pendingWithdrawCount || 0),
  '/users/cancel-account': (counts) => Number(counts.pendingCancelAccountCount || 0),
  '/business/canteen': (counts) => Number(counts.pendingCanteenReservationCount || 0),
}

export const useMenuBadgeStore = defineStore('menuBadge', () => {
  const counts = ref(createEmptyCounts())
  let pollTimer = null

  function createEmptyCounts() {
    return {
      pendingMerchantCount: 0,
      pendingOrderCount: 0,
      refundingOrderCount: 0,
      pendingWithdrawCount: 0,
      pendingCancelAccountCount: 0,
      pendingCanteenReservationCount: 0,
    }
  }

  async function refresh() {
    try {
      const data = await getMenuBadges()
      counts.value = {
        ...createEmptyCounts(),
        ...data,
      }
    } catch {
      counts.value = createEmptyCounts()
    }
    return counts.value
  }

  function startPolling(intervalMs = 30000) {
    stopPolling()
    refresh()
    pollTimer = window.setInterval(() => {
      refresh()
    }, intervalMs)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function getMenuBadge(path = '') {
    const resolver = MENU_BADGE_RESOLVERS[path]
    if (!resolver) {
      return 0
    }
    const value = resolver(counts.value)
    return value > 0 ? value : 0
  }

  return {
    counts,
    refresh,
    startPolling,
    stopPolling,
    getMenuBadge,
  }
})
