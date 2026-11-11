/**
 * 订单页 UI 文案与展示辅助（无 mock 数据）
 */
import { PLACEHOLDER_LOGO } from './theme.js';
import { openTab, PAGES } from './navigation.js';
import {
	ORDER_TYPE,
	USER_TAB_TYPE,
	resolveOrderType,
	orderTypeToUserTab,
	orderTypeLabel,
	isHotelFlow,
	isGoodsFlow,
	isRestaurantFlow
} from './order-type.js';

export const ORDER_CATEGORY_HOTEL = USER_TAB_TYPE.HOTEL;
export const ORDER_CATEGORY_GOODS = USER_TAB_TYPE.GOODS;
export const ORDER_CATEGORY_ROUTE = USER_TAB_TYPE.ROUTE;
export const ORDER_CATEGORY_RESTAURANT = ORDER_TYPE.RESTAURANT;
export {
	ORDER_TYPE,
	USER_TAB_TYPE,
	resolveOrderType,
	orderTypeToUserTab,
	orderTypeLabel,
	isHotelFlow,
	isRestaurantFlow
};
export const ORDER_LIST_PATH = PAGES.ORDER_LIST;
export const ORDER_TAB_CATEGORY_KEY = 'ORDER_TAB_CATEGORY';

/** 跳转订单 Tab 并可选指定分类 */
export function goOrderList(category = '') {
	try {
		if (category) {
			uni.setStorageSync(ORDER_TAB_CATEGORY_KEY, category);
		} else {
			uni.removeStorageSync(ORDER_TAB_CATEGORY_KEY);
		}
	} catch (e) {
		/* ignore */
	}
	openTab(ORDER_LIST_PATH);
}

/** 读取并清除 Tab 跳转时写入的分类 */
export function consumeOrderTabCategory() {
	try {
		const category = uni.getStorageSync(ORDER_TAB_CATEGORY_KEY);
		uni.removeStorageSync(ORDER_TAB_CATEGORY_KEY);
		return category || '';
	} catch (e) {
		return '';
	}
}

export const ORDER_UI = {
	navTitle: '我的订单',
	categoryGoods: '好物订单',
	categoryHotel: '酒店订单',
	categoryRoute: '线路预约',
	hotelTabs: ['全部', '待入住', '已完成', '已取消', '已退款'],
	goodsTabs: ['全部', '待发货', '待收货', '已完成'],
	routeTabs: ['全部', '待支付', '待出行', '已完成', '已取消', '已退款'],
	emptyGoods: '暂无好物订单',
	emptyHotel: '暂无酒店订单',
	emptyRoute: '暂无线路预约',
	goHome: '去首页',
	goShop: '去选购',
	goHotel: '去预定',
	goRoute: '去预约线路',
	btnViewRouteDetail: '查看详情',
	labelAmount: '实付',
	labelAmountRoute: '实付',
	labelBookingNo: '预约编号',
	sectionBookingInfo: '预约信息',
	btnCancelBooking: '取消预约',
	cancelBookingConfirm: '确定要取消该预约吗？',
	cancelBookingSuccess: '预约已取消',
	labelQty: 'x',
	labelStay: '入住',
	labelRoom: '房型',
	labelAddress: '收货地址',
	labelPay: '支付方式',
	labelDate: '下单时间',
	labelOrderType: '订单类型',
	payMethodWechat: '微信支付',
	payMethodAlipay: '支付宝',
	payMethodBalance: '余额支付',
	labelShop: '商家',
	labelRegion: '所属地区',
	labelGuest: '入住人',
	labelReceiver: '收货人',
	labelNights: '入住晚数',
	nightSuffix: '晚',
	orderNo: '订单编号',
	sectionHotel: '酒店预订',
	sectionGoods: '好物信息',
	sectionInfo: '订单信息',
	shopName: '非遗好物',
	statusPendingShip: '待发货',
	statusPendingReceive: '待收货',
	statusCompleted: '已完成',
	statusCancelled: '已取消',
	statusPendingStay: '待入住',
	statusCheckedIn: '已入住',
	statusRefunded: '已退款',
	statusPendingUse: '待使用',
	typeGoods: '好物订单',
	typeHotel: '酒店订单',
	typeRoute: '线路预约',
	statusPendingTrip: '待出行',
	statusPendingConfirm: '待确认',
	labelTripDate: '出行日期',
	labelGuestCount: '出行人数',
	guestCountSuffix: '人',
	labelRemark: '备注',
	labelDuration: '行程时长',
	labelSpots: '途经景点',
	sectionRoute: '线路信息',
	sectionTrip: '出行信息',
	sectionGuestRoute: '出行人信息',
	sectionCancelPolicy: '退改规则',
	labelIdCard: '身份证',
	labelBooker: '预约人',
	labelCompanion: '同行人',
	labelFeeRoute: '线路费用',
	labelFeeTotalRoute: '应付合计',
	labelMeetPoint: '集合点',
	labelContactPerson: '联系人',
	labelRouteStore: '线路门店',
	btnRouteStore: '查看门店',
	routeTipPending: '请按出行日期准时集合，出行当日出示核销码',
	routeTipConfirm: '支付成功，请按出行日期准时集合',
	routeTipDone: '感谢您的预约，欢迎再次选择我们',
	currencySymbol: '¥',
	detailTitle: '订单详情',
	detailTitleRoute: '预约详情',
	checkIn: '入住',
	checkOut: '离店',
	sectionGuest: '入住人信息',
	sectionMerchant: '商家信息',
	sectionBooking: '预订信息',
	sectionAddress: '收货信息',
	sectionFee: '费用明细',
	labelFeeRoom: '房费',
	labelFeeGoods: '商品金额',
	labelFeeShip: '运费',
	shipFree: '免运费',
	labelFeeTotal: '订单总额',
	labelDiscount: '优惠抵扣',
		labelVerifyStatus: '核销状态',
		labelVerifyTime: '核销时间',
		labelVerifyCode: '核销码',
		verifyCodeTip: '到店办理入住时，向商户出示此核销码或二维码',
		routeVerifyCodeTip: '出行当日向旅行社出示此核销码或二维码',
		btnVerifyCode: '核销码',
		btnCopyVerifyCode: '复制核销码',
		copyVerifyCodeSuccess: '核销码已复制',
		statusPendingVerify: '待核销',
	statusVerified: '已核销',
	labelOrderTime: '下单时间',
	labelPhone: '联系电话',
	payPaid: '已支付',
	hotelTipPending: '请按预约时间到店，出示核销码办理入住',
	hotelTipDone: '感谢您的预订，欢迎再次光临',
	goodsTipPending: '商品正在准备发货，请耐心等待',
	goodsTipReceive: '商品已发货，请注意查收',
	sectionLogistics: '物流信息',
	labelLogisticsCompany: '物流公司',
	labelLogisticsNo: '物流单号',
	btnCopyLogisticsNo: '复制单号',
	copyLogisticsNoSuccess: '物流单号已复制',
	btnViewLogistics: '查看物流',
	logisticsPageTitle: '物流详情',
	logisticsCurrentStatus: '当前状态',
	logisticsTrackTitle: '物流轨迹',
	logisticsEmptyTrack: '暂无物流轨迹',
	logisticsReceiver: '收货人',
	logisticsAddress: '收货地址',
	logisticsLoadFail: '物流信息加载失败',
	btnContact: '联系客服',
	btnNavigate: '导航',
	btnStoreGuide: '到店指引',
	btnRoomDetail: '详情',
	btnCopyOrderNo: '复制',
	copyOrderNoSuccess: '订单号已复制',
	btnViewMap: '查看地图',
	btnCallMerchant: '联系商家',
	btnCancel: '取消订单',
	btnPay: '去支付',
	btnRefund: '申请退款',
	btnConfirmReceive: '确认收货',
	btnRebookHotel: '再次预订',
	btnReview: '评价订单',
	reviewTitle: '发表评价',
	reviewScoreLabel: '评分',
	reviewContentLabel: '评价内容',
	reviewPlaceholder: '分享您的真实体验，帮助其他用户选择',
	reviewImagesLabel: '上传图片',
	reviewImagesTip: '最多6张，选填',
	reviewImagesAdd: '添加图片',
	reviewImagesUploading: '上传中...',
	reviewImagesUploadFail: '图片上传失败',
	reviewImagesLimit: '最多上传6张图片',
	reviewSubmit: '提交评价',
	reviewSuccess: '评价成功',
	reviewFail: '评价提交失败',
	reviewNotAllowed: '当前订单不可评价',
	reviewDuplicate: '该订单已评价',
	reviewAll: '查看全部',
	reviewSection: '用户评价',
	reviewEmpty: '暂无评价',
	refundConfirm: '确定申请退款吗？提交后请等待商家处理。',
	refundPopupTitle: '退款说明',
	refundPopupCalcTitle: '本次退款计算',
	refundPopupCancel: '我再想想',
	refundPopupConfirm: '确认申请退款',
	refundPopupSubmitting: '提交中...',
	refundSuccess: '退款申请已提交',
	refundFail: '退款申请失败',
	confirmReceiveContent: '确认已收到商品吗？',
	confirmReceiveSuccess: '已确认收货',
	actionFail: '操作失败',
	loginTip: '请先登录查看订单',
	cancelConfirm: '确定要取消该订单吗？',
	cancelSuccess: '订单已取消',
	cancelFail: '取消失败',
	loadFail: '加载失败'
};

/** 推断 C 端 Tab 分类（优先 userTabType / orderType） */
export function inferOrderCategory(record) {
	if (record?.userTabType) return record.userTabType;
	const orderType = resolveOrderType(record);
	if (isRestaurantFlow(orderType)) return ORDER_CATEGORY_RESTAURANT;
	const userTab = orderTypeToUserTab(orderType);
	if (userTab) return userTab;
	if (record?.checkIn || record?.checkOut || record?.roomName || record?.nights) {
		return ORDER_CATEGORY_HOTEL;
	}
	if (record?.tripDate || record?.routeName) return ORDER_CATEGORY_ROUTE;
	return ORDER_CATEGORY_GOODS;
}

/** 酒店订单是否处于待入住（可展示核销码） */
export function isHotelPendingCheckin(record) {
	if (!record || !isHotelFlow(resolveOrderType(record))) return false;
	if (record.verified) return false;
	const code = record.statusCode || '';
	if (code === 'pending_use' || code === 'pending_confirm') return true;
	const status = record.status || '';
	return status === ORDER_UI.statusPendingStay || status === '待确认' || status === '待使用';
}

/** 线路订单是否待核销（可展示核销码） */
export function isRoutePendingVerify(record) {
	if (!record || inferOrderCategory(record) !== ORDER_CATEGORY_ROUTE) return false;
	if (record.verified) return false;
	const code = record.statusCode || '';
	return code === 'pending_use';
}

/** 好物订单商品金额（不含运费） */
export function resolveGoodsItemsAmount(record) {
	if (!record) return 0;
	const items = Array.isArray(record.items) ? record.items : [];
	if (items.length) {
		const goods = items.reduce((sum, item) => {
			const lineTotal = Number(item.totalPrice);
			if (Number.isFinite(lineTotal) && lineTotal > 0) return sum + lineTotal;
			const qty = Number(item.quantity ?? item.qty ?? 1) || 1;
			const unit = Number(item.unitPrice ?? item.price ?? 0) || 0;
			return sum + unit * qty;
		}, 0);
		return Math.round(goods * 100) / 100;
	}
	const pay = Number(record.amount) || 0;
	const discount = Number(record.discountAmount) || 0;
	const shipping = record.shippingFee != null ? Math.max(0, Number(record.shippingFee) || 0) : 0;
	return Math.max(0, Math.round((pay + discount - shipping) * 100) / 100);
}

/** 好物订单运费（总额 - 商品金额） */
export function resolveOrderShippingFee(record) {
	if (!record || !isGoodsFlow(resolveOrderType(record))) return 0;
	const total = Number(record.totalAmount) || 0;
	const goods = resolveGoodsItemsAmount(record);
	if (total > 0 && goods >= 0) {
		const fee = total - goods;
		return fee > 0 ? Math.round(fee * 100) / 100 : 0;
	}
	return 0;
}

/** 用户端是否可查看物流（商家发货后才展示） */
export function canViewOrderLogistics(record) {
	if (!record || inferOrderCategory(record) !== ORDER_CATEGORY_GOODS) return false;
	const code = record.statusCode || '';
	if (['pending_pay', 'pending_use', 'pending_confirm', 'cancelled', 'refunded', 'refunding'].includes(code)) {
		return false;
	}
	if (['pending_receive', 'completed', 'shipped'].includes(code)) return true;
	return Boolean(record.logisticsInfo || extractLogisticsInfo(record.remark));
}

/** 商户端是否可查看物流（发货后或已填写运单） */
export function canMerchantViewOrderLogistics(order) {
	if (!order || !isGoodsFlow(resolveOrderType(order))) return false;
	const status = order.status || '';
	if (['cancelled', 'refunding'].includes(status)) return false;
	if (['shipped', 'pending_receive', 'completed'].includes(status)) return true;
	return Boolean(order.logisticsInfo || extractLogisticsInfo(order.remark));
}

/** 跳转物流详情页 */
export function openOrderLogistics(record, { scope = 'user' } = {}) {
	if (!record?.id) return;
	const queryScope = scope === 'merchant' ? '&scope=merchant' : '';
	uni.navigateTo({
		url: `/pages/order/logistics?id=${record.id}${queryScope}`
	});
}

/** 从备注中解析物流信息（商户发货时写入） */
export function extractLogisticsInfo(remark) {
	const text = String(remark || '');
	const marker = '物流信息：';
	const idx = text.indexOf(marker);
	if (idx < 0) return null;
	let segment = text.slice(idx + marker.length);
	const semicolon = segment.indexOf('；');
	if (semicolon >= 0) segment = segment.slice(0, semicolon);
	segment = segment.trim();
	if (!segment) return null;
	const parts = segment.split(/\s+/);
	if (parts.length >= 2) {
		return {
			company: parts.slice(0, -1).join(' '),
			trackingNo: parts[parts.length - 1],
			raw: segment
		};
	}
	return {
		company: '',
		trackingNo: segment,
		raw: segment
	};
}

/** 过滤备注中的系统追加字段，保留用户填写内容 */
export function extractUserOrderRemark(remark) {
	const text = String(remark || '').trim();
	if (!text) return '';
	const markers = ['物流信息：', '取消原因：', '预约时间：'];
	let cleaned = text;
	markers.forEach((marker) => {
		const idx = cleaned.indexOf(marker);
		if (idx >= 0) cleaned = cleaned.slice(0, idx).trim();
	});
	return cleaned.replace(/；+$/g, '').trim();
}

/** 用户端展示用订单状态（酒店无「待确认」，好物无「待确认」） */
export function resolveOrderDisplayStatus(record) {
	const category = inferOrderCategory(record);
	const status = record?.status || '';
	const code = record?.statusCode || '';
	if (category === ORDER_CATEGORY_GOODS) {
		if (code === 'pending_confirm' || status === '待确认') return ORDER_UI.statusPendingShip;
		return status;
	}
	if (category !== ORDER_CATEGORY_HOTEL) return status;
	if (record?.verified || status === ORDER_UI.statusCheckedIn) return ORDER_UI.statusCheckedIn;
	if (code === 'completed') return ORDER_UI.statusCheckedIn;
	if (code === 'pending_use' || code === 'pending_confirm') return ORDER_UI.statusPendingStay;
	if (status === '待确认' || status === '待使用') return ORDER_UI.statusPendingStay;
	return status;
}

function normalizeGoodsOrderFields(mapped) {
	if (!isGoodsFlow(resolveOrderType(mapped))) return mapped;
	const userTab = ORDER_CATEGORY_GOODS;
	mapped.userTabType = mapped.userTabType || userTab;
	mapped.orderCategory = userTab;
	if (mapped.statusCode === 'pending_confirm' || mapped.status === '待确认') {
		mapped.statusCode = 'pending_use';
		mapped.status = ORDER_UI.statusPendingShip;
	}
	mapped.logisticsInfo = extractLogisticsInfo(mapped.remark);
	mapped.userRemark = extractUserOrderRemark(mapped.remark);
	return mapped;
}

function normalizeHotelOrderFields(mapped) {
	if (!isHotelFlow(resolveOrderType(mapped))) return mapped;
	const userTab = ORDER_CATEGORY_HOTEL;
	mapped.orderType = mapped.orderType || ORDER_TYPE.HOTEL;
	mapped.userTabType = mapped.userTabType || userTab;
	mapped.orderCategory = userTab;
	mapped.orderTypeLabel = mapped.orderTypeLabel || orderTypeLabel(mapped.orderType);
	if (mapped.verified || mapped.status === ORDER_UI.statusCheckedIn || mapped.statusCode === 'completed') {
		mapped.statusCode = 'completed';
		mapped.status = ORDER_UI.statusCheckedIn;
		return mapped;
	}
	if (
		mapped.statusCode === 'pending_confirm'
		|| mapped.status === '待确认'
		|| mapped.status === '待使用'
		|| (mapped.statusCode === 'pending_use' && mapped.status !== ORDER_UI.statusPendingStay)
	) {
		mapped.statusCode = 'pending_use';
		mapped.status = ORDER_UI.statusPendingStay;
	}
	return mapped;
}

/** 列表 Tab 文案 → 服务端 status 参数 */
export function resolveListStatusParam(category, tabIndex) {
	const tabs = category === ORDER_CATEGORY_GOODS
		? ORDER_UI.goodsTabs
		: category === ORDER_CATEGORY_ROUTE
			? ORDER_UI.routeTabs
			: ORDER_UI.hotelTabs;
	const tab = tabs[tabIndex] || tabs[0];
	if (tab === '全部') return undefined;
	const statusMap = {
		待支付: 'pending_pay',
		待入住: 'pending_use',
		待确认: 'pending_confirm',
		待出行: 'pending_use',
		待发货: 'pending_use',
		待收货: 'pending_receive',
		已完成: 'completed',
		已取消: 'cancelled',
		已退款: 'refunded'
	};
	return statusMap[tab];
}

/** 映射服务端订单 VO 为页面展示结构 */
export function mapOrderVo(vo) {
	if (!vo) return {};
	const orderType = resolveOrderType(vo);
	const userTabType = vo.userTabType || orderTypeToUserTab(orderType);
	return normalizeGoodsOrderFields(normalizeHotelOrderFields({
		...vo,
		id: vo.id != null ? String(vo.id) : '',
		routeId: vo.routeId != null ? String(vo.routeId) : '',
		orderNo: vo.orderNo || '',
		orderType,
		orderTypeLabel: vo.orderTypeLabel || orderTypeLabel(orderType),
		userTabType,
		orderCategory: userTabType || inferOrderCategory(vo),
		amount: vo.amount != null ? Number(vo.amount) : 0,
		goodsCover: vo.goodsCover || vo.items?.[0]?.cover || PLACEHOLDER_LOGO,
		merchantCover: vo.merchantCover || '',
		hotelCover: vo.hotelCover || '',
		status: vo.status || '',
		statusCode: vo.statusCode || '',
		statusColor: vo.statusColor || '',
		canCancelOrder: Boolean(vo.canCancelOrder),
		canApplyRefund: Boolean(vo.canApplyRefund),
		canConfirmReceive: Boolean(vo.canConfirmReceive),
		canReview: Boolean(vo.canReview),
		reviewed: Boolean(vo.reviewed),
		verified: Boolean(vo.verified),
		verifiedAt: vo.verifiedAt || '',
		discountAmount: vo.discountAmount != null ? Number(vo.discountAmount) : 0,
		totalAmount: vo.totalAmount != null ? Number(vo.totalAmount) : Number(vo.amount ?? 0),
		shippingFee: vo.shippingFee != null ? Number(vo.shippingFee) : undefined,
		verifyCode: vo.verifyCode || resolveOrderVerifyCode(vo)
	}));
}

/** 展示用运费（优先后端字段，否则按总额推算） */
export function resolveDisplayShippingFee(record) {
	if (!record) return 0;
	if (record.shippingFee != null && Number.isFinite(Number(record.shippingFee))) {
		return Math.max(0, Number(record.shippingFee));
	}
	return resolveOrderShippingFee(record);
}

/** 解析订单核销码（优先后端字段，否则从订单号兜底） */
export function resolveOrderVerifyCode(record) {
	if (!record) return '';
	if (record.verifyCode) return String(record.verifyCode);
	const orderNo = String(record.orderNo || '');
	const digits = orderNo.replace(/\D/g, '');
	if (digits.length >= 6) return digits.slice(-6);
	if (digits.length > 0) return digits.padStart(6, '0');
	return '';
}

export function formatOrderAmount(amount) {
	const value = Number(amount);
	if (!value) return `${ORDER_UI.currencySymbol}0`;
	return `${ORDER_UI.currencySymbol}${value}`;
}

/** 支付方式码 → 展示文案 */
export function resolvePayMethodLabel(payMethod) {
	if (!payMethod) return '';
	const key = String(payMethod).trim().toLowerCase();
	const map = {
		wx: ORDER_UI.payMethodWechat,
		wechat: ORDER_UI.payMethodWechat,
		ali: ORDER_UI.payMethodAlipay,
		alipay: ORDER_UI.payMethodAlipay,
		wallet: ORDER_UI.payMethodBalance
	};
	return map[key] || payMethod;
}

export function formatHotelStayLine(record) {
	if (record.checkIn && record.checkOut) {
		return `${record.checkIn} 至 ${record.checkOut}`;
	}
	return record.date || '';
}

export function formatRouteTripLine(record) {
	const parts = [];
	if (record.tripDate) parts.push(`${ORDER_UI.labelTripDate} ${record.tripDate}`);
	if (record.guestCount) parts.push(`${record.guestCount}人`);
	return parts.join(' · ') || record.date || '';
}
