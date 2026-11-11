/**
 * 预订确认页 UI 文案与纯工具函数（无 mock 数据）
 */
import { getDefaultContactName, getDefaultPhone } from './user-prefill.js';

export const BOOKING_UI = {
	navTitle: '确认预订',
	currencySymbol: '\u00A5',
	freeLabel: '免费',
	roomSeparator: ' \u00B7 ',
	sectionForm: '预约信息',
	sectionStay: '入住日期',
	stayTip: '请选择入住与离店日期',
	checkIn: '入住',
	checkOut: '离店',
	nightsSuffix: '晚',
	nightsPending: '-',
	selectCheckIn: '选择入住',
	selectCheckOut: '选择离店',
	sectionGuests: '入住人信息',
	guestsTip: '姓名须与入住证件一致',
	sectionRoomCount: '预订间数',
	roomCountTip: '需多间请调整间数',
	labelRoomCount: '间数',
	guestCount: '入住人数',
	guestLabel: '入住人',
	labelName: '姓名',
	guestNamePlaceholder: '请输入真实姓名',
	sectionContact: '联系人',
	contactTip: '用于接收预订确认通知',
	labelContact: '联系人',
	labelPhone: '手机号',
	placeholderContact: '请输入联系人姓名',
	placeholderPhone: '请输入11位手机号',
	labelRemark: '备注',
	placeholderRemark: '选填，如延迟到店、特殊需求等',
	labelDate: '预约日期',
	placeholderDate: '请选择日期',
	labelGuestCountActivity: '参与人数',
	labelGuestCountRoute: '出行人数',
	labelGuestCountDefault: '出行人数',
	labelTravelDate: '出行日期',
	placeholderTravelDate: '请选择出行日期',
	sectionRouteSummary: '线路概要',
	labelRouteDuration: '行程时长',
	labelRouteSpots: '途经景点',
	sectionPolicyRoute: '预约须知',
	policyRouteDate: '请按所选出行日期准时到集合点，迟到可能影响行程',
	policyRouteId: '出行人请携带有效身份证件，姓名与证件一致',
	policyRouteCancel: '出行前 1 天 18:00 前可免费取消或改期',
	policyRouteContact: '如有特殊需求请在备注中说明，客服将尽快联系您',
	toastTravelDate: '请选择出行日期',
	routeFeeLine: '线路费用',
	priceDetail: '费用明细',
	perNight: '每晚',
	roomFee: '房费',
	serviceFee: '服务费',
	serviceFeeFree: '免服务费',
	totalLabel: '合计',
	totalHint: '含税费，到店无隐形消费',
	submitBook: '确认预约',
	submitPay: '确认支付',
	stepDate: '选日期',
	stepGuest: '填入住人',
	stepPay: '去支付',
	sectionPolicy: '预订须知',
	policyCheckIn: '14:00 后可办理入住，12:00 前退房',
	policyCancel: '入住前 1 天 18:00 前可免费取消',
	policyId: '入住人姓名须与有效证件一致',
	policyContact: '如有特殊需求请在备注中说明',
	roomChange: '更换房型',
	toastContact: '请输入联系人',
	toastPhone: '请输入正确手机号',
	toastCheckIn: '请选择入住日期',
	toastCheckOut: '请选择离店日期',
	toastCheckoutBeforeCheckin: '离店日期须晚于入住日期',
	toastGuestName: '请填写所有入住人姓名',
	toastDate: '请选择日期',
	typeLabels: {
		hotel: '酒店住宿',
		scenic: '景区门票',
		route: '旅游线路',
		activity: '精彩活动'
	},
	serviceTypes: {
		hotel: '酒店服务',
		route: '线路预约',
		activity: '活动报名',
		scenic: '景区预约'
	},
	priceUnit: {
		hotel: '/晚',
		default: '/人'
	},
	statusPending: '待使用',
	sourcePlatform: '平台服务',
	payTitle: '选择支付方式',
	payAmountLabel: '支付金额',
	payConfirm: '立即支付',
	paySuccess: '支付成功',
	payProcessing: '支付处理中...',
	paySelectMethod: '请选择支付方式',
	payBalanceInsufficient: '余额不足，请更换支付方式',
	payMethodBalance: '余额支付',
	payMethodAlipay: '支付宝',
	payMethodWechat: '微信支付',
	payBalanceDesc: '可用余额',
	payRecommend: '推荐',
	labelCoupon: '优惠券',
	labelDiscount: '优惠抵扣',
	couponSelectHint: '张可用',
	couponNone: '暂无可用',
	couponGoSelect: '去选择',
	loginTip: '请先登录',
	createOrderFail: '创建订单失败',
	payPopupUnavailable: '支付窗口未就绪，请前往订单完成支付',
	orderCreatedGoPay: '订单已创建，请完成支付',
	payConfirmTitle: '确认支付',
	payConfirmContent: '是否确认支付',
	unsupportedType: '当前类型暂不支持在线下单',
	orderCreating: '提交订单中...',
	navTitleHotel: '确认预订',
	navTitleRoute: '确认预约'
};

export function createDefaultHotelForm() {
	const today = new Date();
	today.setHours(0, 0, 0, 0);
	const tomorrow = new Date(today);
	tomorrow.setDate(tomorrow.getDate() + 1);
	return {
		checkIn: formatDateStr(today.getTime()),
		checkOut: formatDateStr(tomorrow.getTime()),
		roomCount: 1,
		expectedArrivalTime: '18:00 前',
		guestCount: 1,
		guestNames: [getDefaultContactName()],
		contactName: getDefaultContactName(),
		phone: getDefaultPhone(),
		remark: ''
	};
}

export function createDefaultSimpleForm() {
	return {
		date: '',
		guestCount: 2,
		contactName: getDefaultContactName(),
		phone: getDefaultPhone(),
		remark: ''
	};
}

export function createDefaultRouteGuestList(count, fallbackName = '', fallbackPhone = '') {
	const list = [];
	for (let i = 0; i < count; i++) {
		list.push({
			role: i === 0 ? 'booker' : 'companion',
			name: i === 0 ? fallbackName : '',
			idCard: '',
			phone: i === 0 ? fallbackPhone : ''
		});
	}
	return list;
}

export function syncRouteGuestList(guestList, count, fallbackName = '', fallbackPhone = '') {
	const next = (guestList || []).slice(0, count).map((guest, index) => ({
		role: index === 0 ? 'booker' : 'companion',
		name: guest?.name || (index === 0 ? fallbackName : ''),
		idCard: guest?.idCard || '',
		phone: index === 0 ? (guest?.phone || fallbackPhone) : (guest?.phone || '')
	}));
	while (next.length < count) {
		const index = next.length;
		next.push({
			role: index === 0 ? 'booker' : 'companion',
			name: index === 0 ? fallbackName : '',
			idCard: '',
			phone: index === 0 ? fallbackPhone : ''
		});
	}
	return next;
}

export function routeGuestRoleLabel(index) {
	return index === 0 ? '预约人' : `同行人${index}`;
}

export function formatDateStr(timestamp) {
	const d = new Date(timestamp);
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function parseDateStr(str) {
	if (!str) return null;
	const d = new Date(str.replace(/-/g, '/'));
	return Number.isNaN(d.getTime()) ? null : d;
}

export function calcNights(checkIn, checkOut) {
	const inD = parseDateStr(checkIn);
	const outD = parseDateStr(checkOut);
	if (!inD || !outD) return 0;
	const diff = Math.round((outD.getTime() - inD.getTime()) / 86400000);
	return diff > 0 ? diff : 0;
}

export function syncGuestNames(guestNames, count, fallbackName = '') {
	const next = guestNames.slice(0, count);
	while (next.length < count) {
		next.push(next.length === 0 ? fallbackName : '');
	}
	return next;
}

export function buildRoomSubtitle(room, spec) {
	if (!room) return spec || '';
	if (!spec || spec === room) return room;
	return `${room}${BOOKING_UI.roomSeparator}${spec}`;
}

export function buildStaySummary(checkIn, checkOut, nights) {
	if (!checkIn || !checkOut) return checkIn || '';
	return `${checkIn} 至 ${checkOut}${BOOKING_UI.roomSeparator}${nights}${BOOKING_UI.nightsSuffix}`;
}

export function buildGuestSummary(guestNames) {
	return (guestNames || []).filter(Boolean).join('\u3001');
}

export function formatMoney(amount) {
	return `${BOOKING_UI.currencySymbol}${amount}`;
}

export function formatUnitPrice(unitPrice, priceUnit) {
	if (!unitPrice) return BOOKING_UI.freeLabel;
	return `${BOOKING_UI.currencySymbol}${unitPrice}${priceUnit || ''}`;
}

export function formatBreakdownLine(subtitle, nights, unitPrice) {
	return `${subtitle} x ${nights}${BOOKING_UI.nightsSuffix}`;
}

export const WEEKDAY_LABELS = ['\u5468\u65e5', '\u5468\u4e00', '\u5468\u4e8c', '\u5468\u4e09', '\u5468\u56db', '\u5468\u4e94', '\u5468\u516d'];

export function getWeekdayLabel(dateStr) {
	if (!dateStr) return '';
	const d = new Date(dateStr.replace(/-/g, '/'));
	if (Number.isNaN(d.getTime())) return '';
	return WEEKDAY_LABELS[d.getDay()] || '';
}

export function formatDisplayDate(dateStr) {
	if (!dateStr) return '';
	const d = new Date(dateStr.replace(/-/g, '/'));
	if (Number.isNaN(d.getTime())) return dateStr;
	return `${d.getMonth() + 1}\u6708${d.getDate()}\u65e5`;
}

export const PAYMENT_OPTIONS = [
	{ key: 'balance', labelKey: 'payMethodBalance', icon: 'rmb-circle-fill', color: '#FF6600' },
	{ key: 'alipay', labelKey: 'payMethodAlipay', icon: 'zhifubao-circle-fill', color: '#1677FF', recommend: true },
	{ key: 'wechat', labelKey: 'payMethodWechat', icon: 'weixin-circle-fill', color: '#07C160' }
];

export function getPaymentLabel(option) {
	return BOOKING_UI[option.labelKey] || option.key;
}
