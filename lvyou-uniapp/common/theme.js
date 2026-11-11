/**
 * 荣e游 uniapp 主题常量（JS 侧）
 * 与 static/css/shell-tokens.scss 保持同步
 */

/** 缺图统一占位路径 */
export const PLACEHOLDER_LOGO = '/static/logo.png'

/** 品牌 Logo 图片（与 static/logo.png 一致） */
export const BRAND_LOGO = PLACEHOLDER_LOGO

/** 品牌主色 */
export const BRAND_COLOR = '#FF6600'

/** 模块 accent 色（对应 layout-specs brandHex） */
export const MODULE_COLORS = {
	default: '#FF6600',
	goose: '#FF7A00',
	scenic: '#2EAF6A',
	hotel: '#FF6600',
	route: '#FF6600',
	activity: '#9C27B0',
	activityDetail: '#9D35F0',
	heritage: '#5D4037',
	heritageShop: '#B45D2D',
	ai: '#FF701F',
	store: '#F37B1D',
	feedback: '#FF7920',
	transport: '#FF6600',
}

/**
 * 获取模块 accent 色
 * @param {string} module 模块键名，如 scenic / hotel
 * @returns {string} hex 色值
 */
export function getModuleColor(module) {
	return MODULE_COLORS[module] || MODULE_COLORS.default
}

/** uview 全局默认 props 建议值（页面可按模块覆盖 activeColor 等） */
export const UVIEW_DEFAULTS = {
	navbar: {
		bgColor: '#FFFFFF',
		titleColor: '#333333',
		leftIconColor: '#333333',
		rightIconColor: '#333333',
	},
	tabs: {
		activeColor: BRAND_COLOR,
		inactiveColor: '#666666',
	},
	button: {
		color: BRAND_COLOR,
	},
}
