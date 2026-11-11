export function getUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const random = (Math.random() * 16) | 0
    const value = c === 'x' ? random : (random & 0x3) | 0x8
    return value.toString(16)
  })
}

export function getApiBaseUrl() {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:9186/sqx_fast/'
  return baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`
}

export function getCaptchaUrl(uuid) {
  const base = getApiBaseUrl()
  const query = `uuid=${encodeURIComponent(uuid || '')}&t=${Date.now()}`
  return `${base}captcha.jpg?${query}`
}

export function normalizePermissions(raw) {
  if (Array.isArray(raw)) return raw.filter(Boolean)
  if (raw && typeof raw === 'object') return Object.values(raw).filter(Boolean)
  return []
}

export function isAuth(key) {
  const permissions = normalizePermissions(JSON.parse(window.sessionStorage.getItem('permissions') || '[]'))
  if (permissions.indexOf(key) !== -1) return true
  // 兼容旧菜单权限标识
  const legacyMap = {
    'sys:config:list': 'allocationList:list',
    'sys:config:update': 'allocationList:update',
    'sys:config:save': 'allocationList:update',
    'sys:config:delete': 'allocationList:update',
    'sys:user:list': 'sys:user:list',
    'lvyou:merchant:save': 'lvyou:merchant:page',
    'lvyou:merchant:update': 'lvyou:merchant:page',
    'lvyou:merchant:delete': 'lvyou:merchant:page',
  }
  const legacy = legacyMap[key]
  return legacy ? permissions.indexOf(legacy) !== -1 : false
}

export function setPermissions(permissions = []) {
  window.sessionStorage.setItem('permissions', JSON.stringify(normalizePermissions(permissions)))
}

export function clearPermissions() {
  window.sessionStorage.removeItem('permissions')
}

export function clearAuthCache() {
  window.sessionStorage.removeItem('admin-auth')
  clearPermissions()
}

const routePathMap = {
  home: '/dashboard',
  dashboard: '/dashboard',
  user: '/system/admins',
  role: '/system/roles',
  menu: '/system/menus',
  dict: '/system/dicts',
  log: '/system/logs',
  permission: '/system/permissions',
  perms: '/system/permissions',
  'sys/user': '/system/admins',
  'sys/role': '/system/roles',
  'sys/menu': '/system/menus',
  'sys/dict': '/system/dicts',
  'sys/config': '/system/configs',
  'sys/log': '/system/logs',
  userList: '/users',
  'business-users': '/users',
  cancelAccountList: '/users/cancel-account',
  'business-cancel-account': '/users/cancel-account',
  bannerList: '/business/banners',
  message: '/business/messages',
  messageList: '/business/messages',
  smsTemplate: '/system/sms-templates',
  'business-sms-templates': '/system/sms-templates',
  'system-sms-templates': '/system/sms-templates',
  merchantApply: '/business/merchant-audit',
  'business-merchant-audit': '/business/merchant-audit',
  merchantManage: '/business/merchant-manage',
  'business-merchant-manage': '/business/merchant-manage',
  merchantStoreType: '/business/merchant-store-type',
  'business-merchant-store-type': '/business/merchant-store-type',
  lvyouOrder: '/business/order',
  'business-order': '/business/order',
  hotelList: '/business/hotel',
  'business-hotel': '/business/hotel',
  routeList: '/business/route',
  'business-route': '/business/route',
  activityList: '/business/activity',
  'business-activity': '/business/activity',
  scenicList: '/business/scenic',
  'business-scenic': '/business/scenic',
  noticeList: '/business/notice',
  'business-notice': '/business/notice',
  couponTemplate: '/business/coupon',
  'business-coupon': '/business/coupon',
  withdrawAudit: '/business/withdraw',
  'business-withdraw': '/business/withdraw',
  heritageManage: '/business/heritage',
  'business-heritage': '/business/heritage',
  paymentManage: '/business/payment',
  'business-payment': '/business/payment',
  platformConfig: '/business/platform-config',
  'business-platform-config': '/business/platform-config',
  bannerList: '/business/home-module',
  'business-banners': '/business/home-module',
  homeModule: '/business/home-module',
  'business-home-module': '/business/home-module',
  canteenList: '/business/canteen',
  'business-canteen': '/business/canteen',
  inviteManage: '/business/invite',
  'business-invite': '/business/invite',
  feedbackManage: '/business/feedback',
  'business-feedback': '/business/feedback',
  kefuIm: '/kefu-im',
  'business-kefu-im': '/kefu-im',
  allocationList: '/system/configs',
  common: '/system/configs',
}

export function mapMenuUrlToPath(url) {
  const key = String(url || '').trim()
  if (key.startsWith('/')) {
    return key
  }
  if (key === 'bannerList' || key === 'business-banners') {
    return '/business/home-module?tab=banner'
  }
  return routePathMap[key] || ''
}

export function buildMissingPagePath(menuUrl = '', title = '') {
  const query = new URLSearchParams({
    menuUrl,
    title,
  })
  return `/404?${query.toString()}`
}

function isGarbledMenuName(name = '') {
  const text = String(name).trim()
  return !text || /^[?？]+$/.test(text)
}

export function normalizeMenuTree(menuList = []) {
  return menuList
    .map((item) => ({
      id: item.menuId,
      title: item.name,
      icon: item.icon,
      type: item.type,
      perms: item.perms || '',
      path: item.type === 1 ? mapMenuUrlToPath(item.url) || buildMissingPagePath(item.url, item.name) : '',
      children: normalizeMenuTree(item.list || []),
    }))
    .filter((item) => item.type !== 2 && !isGarbledMenuName(item.title))
}
