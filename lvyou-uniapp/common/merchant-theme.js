/**
 * 商户中心：按 storeType 区分主题色与业务文案
 */
import { normalizeStoreType, getStoreTypeMeta } from '@/common/merchant-onboarding.js';

const STORAGE_KEY = 'ry_merchant_store_type';

/** 各门店类型主题（primary 为主色，与入驻页一致） */
const STORE_THEMES = {
	hotel: {
		primary: '#2979FF',
		primaryMid: '#5B9FFF',
		primaryLight: '#EAF2FF',
		heroGradient: 'linear-gradient(135deg, #2979FF 0%, #5B9FFF 100%)',
		walletGradient: 'linear-gradient(135deg, #2979FF 0%, #5B9FFF 55%, #7EB3FF 100%)'
	},
	restaurant: {
		primary: '#FF6600',
		primaryMid: '#FF8533',
		primaryLight: '#FFF3EB',
		heroGradient: 'linear-gradient(135deg, #FF6600 0%, #FF8533 100%)',
		walletGradient: 'linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%)'
	},
	travel: {
		primary: '#2EAF6A',
		primaryMid: '#45C87E',
		primaryLight: '#E8F8EF',
		heroGradient: 'linear-gradient(135deg, #2EAF6A 0%, #45C87E 100%)',
		walletGradient: 'linear-gradient(135deg, #2EAF6A 0%, #45C87E 55%, #7ED4A6 100%)'
	},
	culture: {
		primary: '#B45D2D',
		primaryMid: '#C97852',
		primaryLight: '#F5EFEB',
		heroGradient: 'linear-gradient(135deg, #5D4037 0%, #8D6E63 100%)',
		walletGradient: 'linear-gradient(135deg, #B45D2D 0%, #8D6E63 55%, #C97852 100%)'
	}
};

const DEFAULT_THEME = STORE_THEMES.hotel;

/** 各类型「在售商品」文案（面板标题、按钮等） */
const OFFERING_COPY = {
	hotel: {
		offering: '房间',
		offeringUnit: '间',
		panelService: '房间',
		panelPublishService: '发布房间',
		panelManageService: '管理房间',
		panelOnSale: '在售',
		servicesTitle: '房间管理',
		serviceEditTitle: '编辑房间',
		servicePublishTitle: '发布房间',
		publishService: '发布房间',
		emptyServices: '暂无房间，快去发布吧',
		serviceName: '房间名称',
		servicePrice: '房间价格',
		serviceStock: '可售间数',
		serviceDesc: '房间说明',
		serviceCover: '房间封面',
		saveAndPublish: '保存并上架'
	},
	restaurant: {
		offering: '菜品',
		offeringUnit: '道',
		panelService: '菜品',
		panelPublishService: '发布菜品',
		panelManageService: '管理菜品',
		panelOnSale: '开放预约',
		panelRevenue: '预约统计',
		panelToday: '今日预约',
		panelMonth: '本月预约',
		panelTotal: '累计预约',
		panelOrder: '预约',
		panelAllOrders: '全部预约',
		panelPending: '待确认',
		panelProcessing: '待到店',
		panelClosed: '已到店',
		servicesTitle: '菜品管理',
		serviceEditTitle: '编辑菜品',
		servicePublishTitle: '发布菜品',
		publishService: '发布菜品',
		emptyServices: '暂无菜品，快去发布吧',
		serviceName: '菜品名称',
		servicePrice: '参考价格',
		serviceStock: '可预约名额',
		serviceDesc: '菜品说明',
		serviceCover: '菜品封面',
		saveAndPublish: '保存并开放预约'
	},
	travel: {
		offering: '路线',
		offeringUnit: '条',
		panelService: '路线',
		panelPublishService: '发布路线',
		panelManageService: '管理路线',
		panelOnSale: '开放预约',
		panelOrder: '预约',
		panelRouteBooking: '待跟进预约',
		routeBookingAll: '全部预约',
		contactCustomer: '联系用户',
		confirmRouteBooking: '确认预约',
		panelAllOrders: '全部预约',
		ordersTitle: '预约管理',
		orderDetailTitle: '预约详情',
		emptyOrders: '暂无预约',
		servicesTitle: '路线管理',
		serviceEditTitle: '编辑路线',
		servicePublishTitle: '发布路线',
		publishService: '发布路线',
		emptyServices: '暂无路线，快去发布吧',
		serviceName: '路线名称',
		servicePrice: '价格（元/人）',
		serviceStock: '可预约名额',
		serviceDesc: '路线说明',
		serviceCover: '路线封面',
		saveAndPublish: '保存并开放预约'
	},
	culture: {
		offering: '好物',
		offeringUnit: '件',
		panelService: '好物',
		panelPublishService: '发布好物',
		panelManageService: '管理好物',
		panelOnSale: '在售',
		panelPending: '待发货',
		panelProcessing: '待收货',
		panelPendingShip: '待发货',
		panelShipAll: '全部待发货',
		servicesTitle: '好物管理',
		serviceEditTitle: '编辑好物',
		servicePublishTitle: '发布好物',
		publishService: '发布好物',
		emptyServices: '暂无好物，快去发布吧',
		serviceName: '好物名称',
		servicePrice: '好物价格',
		serviceStock: '库存数量',
		serviceDesc: '好物说明',
		serviceCover: '好物封面',
		saveAndPublish: '保存并上架'
	}
};

const DEFAULT_COPY = OFFERING_COPY.hotel;

export function cacheMerchantStoreType(storeType) {
	if (!storeType) return;
	try {
		uni.setStorageSync(STORAGE_KEY, normalizeStoreType(storeType));
	} catch (e) {
		// ignore
	}
}

export function getCachedMerchantStoreType() {
	try {
		return normalizeStoreType(uni.getStorageSync(STORAGE_KEY) || '') || '';
	} catch (e) {
		return '';
	}
}

export function getMerchantStoreTheme(storeType) {
	const key = normalizeStoreType(storeType) || 'hotel';
	const colors = STORE_THEMES[key] || DEFAULT_THEME;
	const typeMeta = getStoreTypeMeta(key);
	return {
		storeType: key,
		...colors,
		icon: typeMeta.icon,
		label: typeMeta.label,
		bgColor: typeMeta.bgColor,
		color: typeMeta.color
	};
}

export function getMerchantOfferingCopy(storeType) {
	const key = normalizeStoreType(storeType) || 'hotel';
	return OFFERING_COPY[key] || DEFAULT_COPY;
}

/** 生成页面 CSS 变量，供 scoped 样式 var(--mc-primary) 使用 */
export function buildMerchantThemeStyle(storeType) {
	const theme = getMerchantStoreTheme(storeType);
	return {
		'--mc-primary': theme.primary,
		'--mc-primary-mid': theme.primaryMid,
		'--mc-primary-light': theme.primaryLight,
		'--mc-hero-gradient': theme.heroGradient,
		'--mc-wallet-gradient': theme.walletGradient
	};
}

/** 合并商户中心首页文案 */
export function resolveMerchantCenterUi(storeType, baseUi = {}) {
	const copy = getMerchantOfferingCopy(storeType);
	const merged = {
		...baseUi,
		panelService: copy.panelService,
		panelPublishService: copy.panelPublishService,
		panelManageService: copy.panelManageService,
		panelOnSale: copy.panelOnSale,
		panelOnSaleReservation: copy.panelOnSale
	};
	if (copy.panelOrder) merged.panelOrder = copy.panelOrder;
	if (copy.panelAllOrders) merged.panelAllOrders = copy.panelAllOrders;
	if (copy.panelRevenue) merged.panelRevenue = copy.panelRevenue;
	if (copy.panelToday) merged.panelToday = copy.panelToday;
	if (copy.panelMonth) merged.panelMonth = copy.panelMonth;
	if (copy.panelTotal) merged.panelTotal = copy.panelTotal;
	if (copy.panelPending) merged.panelPending = copy.panelPending;
	if (copy.panelProcessing) merged.panelProcessing = copy.panelProcessing;
	if (copy.panelClosed) merged.panelClosed = copy.panelClosed;
	if (copy.panelRouteBooking) merged.panelRouteBooking = copy.panelRouteBooking;
	if (copy.routeBookingAll) merged.routeBookingAll = copy.routeBookingAll;
	if (copy.contactCustomer) merged.contactCustomer = copy.contactCustomer;
	if (copy.confirmRouteBooking) merged.confirmRouteBooking = copy.confirmRouteBooking;
	if (copy.panelPendingShip) merged.panelPendingShip = copy.panelPendingShip;
	if (copy.panelShipAll) merged.panelShipAll = copy.panelShipAll;
	return merged;
}

/** 合并商户业务页文案（服务/商品/房间管理） */
export function resolveMerchantBusinessUi(storeType, baseUi = {}) {
	const copy = getMerchantOfferingCopy(storeType);
	const merged = {
		...baseUi,
		servicesTitle: copy.servicesTitle,
		serviceEditTitle: copy.serviceEditTitle,
		servicePublishTitle: copy.servicePublishTitle,
		publishService: copy.publishService,
		emptyServices: copy.emptyServices,
		serviceName: copy.serviceName,
		servicePrice: copy.servicePrice,
		serviceStock: copy.serviceStock,
		serviceDesc: copy.serviceDesc,
		serviceCover: copy.serviceCover,
		saveAndPublish: copy.saveAndPublish
	};
	if (copy.ordersTitle) merged.ordersTitle = copy.ordersTitle;
	if (copy.orderDetailTitle) merged.orderDetailTitle = copy.orderDetailTitle;
	if (copy.emptyOrders) merged.emptyOrders = copy.emptyOrders;
	return merged;
}
