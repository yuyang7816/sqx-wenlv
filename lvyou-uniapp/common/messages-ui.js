/**
 * 消息页文案（集中维护，避免 .vue 文件编码损坏后出现 ??）
 */
export const MSG_UI = {
	tabs: [
		{ name: '全部', badge: 0 },
		{ name: '系统通知', badge: 0 },
		{ name: '活动提醒', badge: 0 }
	],
	emptyNotLoggedIn: '请先登录后查看消息',
	emptyNotLoggedInHint: '登录后可查看',
	emptyNoMessagePrefix: '暂无',
	emptyNoMessageSuffix: '消息',
	emptyHint: '新消息将第一时间通知您',
	markAllRead: '全部已读',
	markAllReadSuccess: '已全部标记为已读',
	markAllReadEmpty: '暂无未读消息',
	toastLoadFail: '加载失败',
	toastNetworkError: '网络异常，请稍后重试'
};
