/** 精彩活动页 UI 文案（非 mock 数据） */
export const ACTIVITY_UI = {
	listTitle: '精彩活动',
	sectionIntro: '活动介绍',
	sectionHighlights: '活动亮点',
	sectionNotice: '参与须知',
	labelLocation: '活动地点',
	labelTime: '活动时间',
	labelPrice: '参考费用',
	labelEnroll: '报名人数',
	enrollUnlimited: '不限',
	labelParticipants: '人参与',
	emptyText: '暂无相关活动',
	enrollBtn: '立即报名',
	enrolledBtn: '已报名',
	endedBtn: '活动已结束',
	loginTip: '请先登录后再报名',
	priceFree: '免费',
	priceOfflineTip: '线上仅报名，费用请在线下签到时支付',
};

/** 活动阶段 Tab（对齐服务端 ActivityPhase） */
export const ACTIVITY_TABS = [
	{ key: 'all', label: '全部' },
	{ key: 'ongoing', label: '进行中' },
	{ key: 'upcoming', label: '即将开始' },
	{ key: 'ended', label: '已结束' }
];

/** 格式化参考价格展示（线上不收费） */
export function formatActivityPrice(price, priceTip) {
	const tip = priceTip || ACTIVITY_UI.priceOfflineTip;
	if (price == null || price === '') {
		return { label: ACTIVITY_UI.priceFree, tip };
	}
	const num = Number(price);
	if (!Number.isFinite(num) || num <= 0) {
		return { label: ACTIVITY_UI.priceFree, tip };
	}
	return { label: `¥${num}`, tip };
}
