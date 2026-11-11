import { get, post, resolveBusinessResponse } from '@/api/http'

/** 管理端 Shiro 鉴权接口（与 C 端 App JWT 隔离） */
export const authApi = {
  login: '/sys/login',
  nav: '/sys/menu/nav',
  logout: '/sys/logout',
  userInfo: '/sys/user/info',
}

export function mapUserProfile(user = {}, fallbackName = '') {
  return {
    name: user.username || user.name || fallbackName,
    roleCode: user.roleIdList?.[0] ? String(user.roleIdList[0]) : '',
    roleName: user.roleName || '',
    email: user.email || '',
    mobile: user.mobile || '',
    userId: user.userId,
  }
}

export function loginApi(data) {
  return post(authApi.login, {
    username: data.username,
    password: data.password,
    uuid: data.uuid,
    captcha: data.captcha,
  }, {
    silentError: true,
  }).then((response) => {
    const payload = response?.data || response || {}
    const token = payload.token || payload.accessToken || response?.token || response?.accessToken || ''
    const expire = Number(payload.expire || response?.expire || 0)
    if (!token) {
      return Promise.reject(new Error('登录成功但未返回 token'))
    }
    return {
      token,
      refreshToken: payload.refreshToken || response?.refreshToken || '',
      expiresAt: Date.now() + (expire > 0 ? expire * 1000 : 2 * 60 * 60 * 1000),
      profile: mapUserProfile(payload.user || payload.userInfo || {}, data.username),
      permissions: payload.permissions || response?.permissions || [],
      raw: response,
    }
  })
}

export function logoutApi() {
  return post(authApi.logout, {}, { silentError: true })
    .then(resolveBusinessResponse)
    .catch(() => true)
}

export function getUserInfoApi() {
  return get(authApi.userInfo, {}, { silentError: true })
    .then(resolveBusinessResponse)
    .then((data) => data?.user || data || {})
}

export function getNavApi() {
  return get(authApi.nav, {}, { silentError: true })
}
