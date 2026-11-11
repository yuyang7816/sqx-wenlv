/**
 * 用户端页面跳转规范（uni-app）
 *
 * ┌─────────────────┬──────────────────────────────────────────────────────────┐
 * │ API             │ 使用场景                                                  │
 * ├─────────────────┼──────────────────────────────────────────────────────────┤
 * │ switchTab       │ 仅 TabBar 三页：首页 / 订单 / 我的                         │
 * │ navigateTo      │ 普通子页入栈（详情、设置、登录、mobile-design/* 等）       │
 * │ navigateBack    │ 子页返回；表单提交成功后关闭当前页                         │
 * │ redirectTo      │ 替换当前页且不可返回（入驻结果、地图重定向、提现成功等）   │
 * │ reLaunch        │ 清空全部页面栈（退出登录、注销成功、冷启动重置）           │
 * └─────────────────┴──────────────────────────────────────────────────────────┘
 *
 * 页面层级：
 *   TabBar（switchTab / reLaunch）
 *     ├─ /pages/index/index        首页
 *     ├─ /pages/order/list         订单
 *     └─ /pages/my/my              我的
 *   我的模块（navigateTo）
 *     ├─ login / register / forget / profile / settings / asset / wallet*
 *     ├─ invite / team / commission / coupons / service / help / agreement
 *     └─ msg / msg/detail
 *   业务分包 mobile-design/*（navigateTo）
 *     └─ 列表 → 详情 → 预订/下单 → 登录（未登录时 navigateTo login）
 *   商户分包 merchant-portal/*（navigateTo / redirectTo）
 *     └─ 入驻 apply → redirectTo result → redirectTo center
 *
 * 推荐用法：业务代码统一调用本文件导出方法，避免直接散落 uni.navigate* 调用。
 */

/** TabBar 页面路径（与 pages.json tabBar.list 保持一致） */
export const TAB_PAGES = [
	'/pages/index/index',
	'/pages/order/list',
	'/pages/my/my'
];

/** 常用页面路径 */
export const PAGES = {
	INDEX: '/pages/index/index',
	ORDER_LIST: '/pages/order/list',
	MY: '/pages/my/my',
	LOGIN: '/pages/my/login',
	REGISTER: '/pages/my/register',
	FORGET: '/pages/my/forget',
	PROFILE: '/pages/my/profile',
	SETTINGS: '/pages/my/settings',
	ASSET: '/pages/my/asset',
	WALLET: '/pages/my/wallet',
	WALLET_RECHARGE: '/pages/my/wallet-recharge',
	WALLET_WITHDRAW: '/pages/my/wallet-withdraw',
	WALLET_WITHDRAW_ACCOUNT: '/pages/my/wallet-withdraw-account',
	WALLET_TRANSACTIONS: '/pages/my/wallet-transactions',
	WALLET_WITHDRAW_RECORDS: '/pages/my/wallet-withdraw-records',
	MSG: '/pages/msg/msg',
	HELP: '/pages/my/help',
	SERVICE: '/pages/my/service',
	IM_CHAT: '/pages/my/im-chat',
	COUPON_CENTER: '/pages/my/coupon-center',
	COUPONS: '/pages/my/coupons',
	INVITE: '/pages/my/invite',
	TEAM: '/pages/my/team',
	COMMISSION: '/pages/my/commission'
};

/** 去掉 query，统一为 / 开头的路径 */
export function normalizePath(url) {
	if (!url) return '';
	let path = String(url).trim();
	if (!path.startsWith('/')) path = `/${path}`;
	const q = path.indexOf('?');
	return q >= 0 ? path.slice(0, q) : path;
}

/** 是否为 TabBar 页 */
export function isTabPage(url) {
	return TAB_PAGES.includes(normalizePath(url));
}

/** 拼接 query 参数 */
export function buildUrl(path, query = {}) {
	if (!query || typeof query !== 'object') return path;
	const parts = Object.entries(query)
		.filter(([, v]) => v !== undefined && v !== null && v !== '')
		.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`);
	if (!parts.length) return path;
	const sep = path.includes('?') ? '&' : '?';
	return `${path}${sep}${parts.join('&')}`;
}

/**
 * 打开页面（智能选择 API）
 * - TabBar → switchTab
 * - 其它   → navigateTo（保留返回栈）
 */
export function openPage(url, options = {}) {
	if (!url) return;
	const { fail } = options;
	if (isTabPage(url)) {
		uni.switchTab({ url: normalizePath(url), fail });
	} else {
		uni.navigateTo({ url, fail });
	}
}

/** 强制打开 TabBar 页 */
export function openTab(url) {
	uni.switchTab({ url: normalizePath(url) || url });
}

/**
 * 替换当前页（关闭当前页，不可返回）
 * - TabBar → switchTab（redirectTo 不支持 Tab 页）
 * - 其它   → redirectTo
 */
export function replacePage(url, options = {}) {
	if (!url) return;
	if (isTabPage(url)) {
		uni.switchTab({ url: normalizePath(url), ...options });
	} else {
		uni.redirectTo({ url, ...options });
	}
}

/** 清空页面栈并打开指定页（logout、注销成功等） */
export function reLaunchApp(url, options = {}) {
	if (!url) return;
	uni.reLaunch({ url, ...options });
}

/**
 * 返回上一页；若无栈则 fallback（默认「我的」Tab）
 */
export function goBack(fallbackUrl = PAGES.MY) {
	uni.navigateBack({
		fail: () => {
			if (fallbackUrl) openPage(fallbackUrl);
		}
	});
}

/** 跳转登录页（保留返回栈，登录后可 navigateBack） */
export function goLogin(options = {}) {
	const { delay = 0 } = options;
	const run = () => openPage(PAGES.LOGIN);
	if (delay > 0) setTimeout(run, delay);
	else run();
}

/** 登录 / 注册成功后进入「我的」Tab */
export function afterLogin() {
	uni.switchTab({
		url: PAGES.MY,
		fail: () => reLaunchApp(PAGES.MY)
	});
}

/** 退出登录 / 注销成功后回到首页并清空栈 */
export function afterLogout() {
	reLaunchApp(PAGES.INDEX);
}

/** 表单提交成功后延迟返回 */
export function goBackDelayed(delay = 600) {
	setTimeout(() => goBack(), delay);
}
