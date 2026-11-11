/**
 * 商户中心：UI 文案、状态常量与展示辅助（非 mock 数据）
 */
import { getStoreTypeMeta, MERCHANT_STATUS, resolveStoreTypeLabel } from '@/common/merchant-onboarding.js';
import { getMerchantStoreTheme } from '@/common/merchant-theme.js';
import {
	fetchMerchantStatus,
	fetchMerchantWalletSummary,
	requireMerchantLogin
} from '@/common/api/merchant.js';
import { openPage } from '@/common/navigation.js';
import { resolveOrderType, ORDER_TYPE, isGoodsFlow } from '@/common/order-type.js';

export const SERVICE_STATUS = {
	ON_SALE: 'on_sale',
	OFF_SALE: 'off_sale',
	DRAFT: 'draft'
};

export const MERCHANT_BUSINESS_UI = {
	walletTitle: '资金管理',
	withdrawTitle: '申请提现',
	depositTitle: '缴纳保证金',
	fundRecordsTitle: '资金明细',
	withdrawRecordsTitle: '提现明细',
	ordersTitle: '订单管理',
	orderDetailTitle: '订单详情',
	servicesTitle: '服务管理',
	serviceEditTitle: '编辑服务',
	servicePublishTitle: '发布服务',
	balanceLabel: '可提现余额(元)',
	pendingSettleLabel: '待结算(元)',
	totalIncomeLabel: '累计收入(元)',
	depositLabel: '保证金(元)',
	depositPaid: '已缴纳',
	depositUnpaid: '未缴纳',
	depositRequired: '应缴保证金',
	withdrawBtn: '提现',
	depositBtn: '缴纳保证金',
	depositRefundBtn: '申请退保',
	depositRefundPending: '退保审核中',
	depositRefundRejected: '退保已驳回',
	depositRefundRejectReason: '驳回原因',
	depositRefundRejectDefault: '平台未填写具体原因，如有疑问请联系客服',
	depositRefundSuccess: '退保申请已提交',
	depositRefundConfirm: '确认申请退还全部已缴保证金吗？需无未处理订单，审核通过后将退回可提现余额。',
	fundRecordsLink: '资金明细',
	withdrawRecordsLink: '提现明细',
	withdrawAmountLabel: '提现金额',
	withdrawMethodLabel: '提现方式',
	withdrawAccountLabel: '收款账户',
	withdrawAccountBindTitle: '收款方式',
	withdrawAccountBindLink: '设置收款方式',
	withdrawAccountNotBound: '未设置，点击前往配置',
	withdrawAllBtn: '全部提现',
	withdrawAmountPlaceholder: '请输入提现金额',
	depositAmountLabel: '缴纳金额',
	depositPayTip: '点击确认后将选择微信、支付宝或余额完成支付',
	depositTip: '保证金用于保障交易安全，关店可申请退还',
	depositPaidLabel: '已缴金额',
	depositNeedPayLabel: '待缴金额',
	depositRefundSectionTitle: '退还保证金',
	depositRefundRule: '关店或退出平台时可申请退还全部已缴保证金，需无未处理订单，审核通过后退回可提现余额。',
	depositRefundBlockedTip: '存在未处理订单，暂不可申请退保',
	depositStatusPaid: '已缴足',
	depositStatusUnpaid: '待缴足',
	depositStatusRefunding: '退保审核中',
	submitWithdraw: '确认提现',
	submitDeposit: '确认缴纳',
	withdrawConfirmTitle: '确认提现',
	withdrawConfirmContent: '确认提现',
	withdrawSuccess: '提现申请已提交',
	depositSuccess: '保证金缴纳成功',
	depositRequiredTip: '请先缴纳保证金后再操作',
	depositRequiredBanner: '保证金未缴足，当前仅可装修店铺',
	depositRequiredOpenTip: '缴足保证金后方可营业',
	statusNotOperating: '未营业',
	emptyFundRecords: '暂无资金明细',
	emptyWithdrawRecords: '暂无提现记录',
	emptyOrders: '暂无订单',
	emptyServices: '暂无服务',
	publishService: '发布服务',
	saveDraft: '保存草稿',
	saveAndPublish: '保存并上架',
	serviceName: '服务名称',
	servicePrice: '服务价格',
	serviceStock: '库存/名额',
	serviceDesc: '服务说明',
	serviceCover: '封面图片',
	tabAll: '全部',
	tabPending: '待处理',
	tabProcessing: '进行中',
	tabCompleted: '已完成',
	tabOnSale: '上架中',
	tabOffSale: '已下架',
	tabDraft: '草稿',
	edit: '编辑',
	publish: '上架',
	unpublish: '下架',
	delete: '删除',
	confirmDelete: '确认删除该服务？',
	deleteSuccess: '已删除',
	publishSuccess: '已上架',
	unpublishSuccess: '已下架',
	saveSuccess: '保存成功',
	orderNo: '订单编号',
	orderTime: '下单时间',
	orderCustomer: '下单用户',
	orderPhone: '联系电话',
	orderItems: '购买内容',
	orderAmount: '订单金额',
	orderRemark: '用户备注',
	orderStatus: '订单状态',
	confirmOrder: '确认订单',
	confirmShip: '确认发货',
	shipPopupTitle: '填写物流信息',
	shipCompanyLabel: '物流公司',
	shipCompanyPlaceholder: '如：顺丰速运',
	shipNoLabel: '物流单号',
	shipNoPlaceholder: '请输入物流单号',
	shipPopupCancel: '取消',
	shipPopupConfirm: '确认发货',
	shipPopupSubmitting: '提交中...',
	shipCompanyRequired: '请填写物流公司',
	shipNoRequired: '请填写物流单号',
	goShip: '去发货',
	confirmComplete: '完成订单',
	confirmCheckin: '确认入住',
	confirmVerify: '核销',
	verifyTitle: '订单核销',
	verifyScan: '扫码核销',
	verifyInputPlaceholder: '输入订单号',
	verifySearch: '查询订单',
	verifyEmpty: '暂无待核销订单',
	verifyPendingTitle: '待核销',
	verifyAll: '查看全部',
	verifySuccess: '核销成功',
	verifyConfirm: '确认核销该订单？',
	confirmTour: '确认出团',
	confirmReservation: '确认预约',
	confirmArrival: '确认到店',
	handleRefund: '处理退款',
	orderHandled: '操作成功',
	currencySymbol: '¥',
	loadFail: '加载失败，请稍后重试',
	networkError: '网络异常，请稍后重试',
	deleteNotSupported: '暂不支持删除，请先下架'
};

export const MERCHANT_CENTER_UI = {
	centerTitle: '商户中心',
	infoTitle: '商户信息',
	statusOpen: '营业中',
	statusClosed: '休息中',
	heroMessage: '消息提醒',
	heroWallet: '资金管理',
	heroOpen: '营业状态',
	heroDecor: '门店装修',
	panelService: '服务',
	panelOrder: '订单',
	panelRevenue: '营收',
	panelAll: '全部',
	panelOnSale: '在售',
	panelOnSaleReservation: '开放预约',
	panelPublishService: '发布服务',
	panelManageService: '管理服务',
	panelAllOrders: '全部订单',
	panelPending: '待处理',
	panelProcessing: '服务中',
	panelClosed: '已关闭',
	panelRevenueDetail: '营收明细',
	panelVerify: '待核销',
	panelPendingShip: '待发货',
	panelShipAll: '全部待发货',
	panelToday: '今日',
	panelMonth: '当月',
	panelTotal: '总计',
	panelQueue: '现场排队',
	panelQueuePreview: '用户端展示',
	panelQueueEnabled: '当前是否排队',
	panelQueueCount: '前面排队',
	panelQueueUnit: '桌',
	panelQueueUpdate: '更新排队状态',
	panelQueueSaving: '保存中...',
	panelQueueHint: '修改后自动保存，仅供参考，以到店实际情况为准',
	panelQueueRequireOpen: '请先开启营业',
	panelQueueNoQueue: '无需排队',
	panelQueueSaveSuccess: '排队状态已更新',
	currencySymbol: '¥'
};

/** 金额格式化 */
export function formatMoney(value, prefix = MERCHANT_BUSINESS_UI.currencySymbol) {
	const num = Number(value) || 0;
	return `${prefix}${num}`;
}

/** 手机号脱敏 */
export function maskPhone(phone) {
	const text = String(phone || '');
	if (text.length < 7) return text;
	return `${text.slice(0, 3)}****${text.slice(-4)}`;
}

/** 商户资料附加门店类型样式（对齐 dashboard.merchant） */
export function enrichMerchantProfile(profile) {
	if (!profile) return null;
	const theme = getMerchantStoreTheme(profile.storeType);
	const typeMeta = getStoreTypeMeta(profile.storeType);
	return {
		...profile,
		typeLabel: resolveStoreTypeLabel(profile.storeType, profile.typeLabel || typeMeta.label),
		typeColor: theme.primary || typeMeta.color,
		typeBgColor: theme.primaryLight || typeMeta.bgColor,
		typeIcon: theme.icon || typeMeta.icon,
		statusLabel: profile.statusLabel
			|| (profile.depositStatus === 'unpaid' ? MERCHANT_BUSINESS_UI.statusNotOperating
				: (profile.isOpen !== false ? '营业中' : '休息中')),
		canOperate: isMerchantDepositPaid(profile)
	};
}

/** 订单主操作按钮（action 对齐 POST /app/merchant/orders/{id}/action） */
export function getOrderPrimaryAction(order) {
	if (!order) return null;
	const s = order.status;
	const orderType = resolveOrderType(order);

	if (s === 'pending_confirm') {
		if (orderType === ORDER_TYPE.HOTEL || orderType === ORDER_TYPE.ROUTE) {
			return { label: MERCHANT_BUSINESS_UI.confirmVerify, action: 'verify' };
		}
		if (orderType === ORDER_TYPE.RESTAURANT) {
			return { label: MERCHANT_BUSINESS_UI.confirmReservation, action: 'confirm' };
		}
		if (isGoodsFlow(orderType)) {
			return { label: MERCHANT_BUSINESS_UI.confirmShip, action: 'ship' };
		}
		return { label: MERCHANT_BUSINESS_UI.confirmOrder, action: 'confirm' };
	}
	if (s === 'pending_ship') {
		return { label: MERCHANT_BUSINESS_UI.confirmShip, action: 'ship' };
	}
	if (s === 'shipped') {
		return { label: MERCHANT_BUSINESS_UI.confirmComplete, action: 'complete' };
	}
	if (s === 'pending_checkin' || s === 'on_tour') {
		if (orderType === ORDER_TYPE.ROUTE || orderType === ORDER_TYPE.HOTEL) {
			return { label: MERCHANT_BUSINESS_UI.confirmVerify, action: 'verify' };
		}
	}
	if ((s === 'pending_arrival' || s === 'preparing') && orderType === ORDER_TYPE.RESTAURANT) {
		return { label: MERCHANT_BUSINESS_UI.confirmArrival, action: 'complete' };
	}
	if (s === 'refunding') {
		return { label: MERCHANT_BUSINESS_UI.handleRefund, action: 'refund_agree' };
	}
	return null;
}

/** 是否已缴足保证金 */
export function isMerchantDepositPaid(source) {
	if (!source) return false;
	if (source.depositStatus === 'paid') return true;
	if (source.canOperate === true) return true;
	const required = Number(source.depositRequired ?? 0);
	const paid = Number(source.depositPaid ?? 0);
	return required > 0 ? paid >= required : paid > 0;
}

/** 已缴足保证金门控（需先通过入驻审核） */
export async function requireMerchantDepositPaid({ redirect = true } = {}) {
	if (!(await requireMerchantApproved())) return false;
	try {
		const res = await fetchMerchantWalletSummary();
		if (res.code === 0 && isMerchantDepositPaid(res.data)) {
			return true;
		}
		uni.showToast({ title: MERCHANT_BUSINESS_UI.depositRequiredTip, icon: 'none' });
		if (redirect) {
			setTimeout(() => {
				uni.navigateTo({ url: '/merchant-portal/center/wallet-deposit' });
			}, 400);
		}
		return false;
	} catch (e) {
		uni.showToast({ title: MERCHANT_BUSINESS_UI.networkError, icon: 'none' });
		return false;
	}
}

/** 已审核商户门控（App JWT + status=approved） */
export async function requireMerchantApproved(fallbackUrl = '/merchant-portal/onboarding/apply') {
	if (!requireMerchantLogin()) return false;
	try {
		const res = await fetchMerchantStatus();
		if (res.code === 0 && res.data?.status === MERCHANT_STATUS.APPROVED) {
			return true;
		}
		const status = res.data?.status;
		const url = status === MERCHANT_STATUS.PENDING
			? '/merchant-portal/onboarding/result'
			: fallbackUrl;
		uni.showToast({ title: '请先完成商户入驻', icon: 'none' });
		setTimeout(() => openPage(url), 400);
		return false;
	} catch (e) {
		uni.showToast({ title: MERCHANT_BUSINESS_UI.networkError, icon: 'none' });
		return false;
	}
}
