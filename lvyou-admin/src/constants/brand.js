/** 产品名称默认值（与 sys_config.sys_server_name 种子一致） */
export const DEFAULT_BRAND_NAME = '省钱兄文旅'

export const BRAND_COMPANY = '西安省钱兄网络科技有限公司'

export function buildAdminTitle(name = DEFAULT_BRAND_NAME) {
  return `${name}管理后台`
}

export function buildAdminSuffix(name = DEFAULT_BRAND_NAME) {
  return `${name}管理台`
}

export function buildLogoText(name = DEFAULT_BRAND_NAME) {
  const trimmed = String(name || '').trim()
  return trimmed.charAt(0) || '省'
}

/** @deprecated 请使用 useBrandStore().brandName */
export const BRAND_NAME = DEFAULT_BRAND_NAME

/** @deprecated 请使用 useBrandStore().adminTitle */
export const BRAND_ADMIN_TITLE = buildAdminTitle(DEFAULT_BRAND_NAME)

/** @deprecated 请使用 useBrandStore().adminSuffix */
export const BRAND_ADMIN_SUFFIX = buildAdminSuffix(DEFAULT_BRAND_NAME)

/** @deprecated 请使用 useBrandStore().logoText */
export const BRAND_LOGO_TEXT = buildLogoText(DEFAULT_BRAND_NAME)
