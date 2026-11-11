/**
 * 商户端订单列表/详情展示辅助（对齐 C 端 order-ui 信息层级）
 */
import { PLACEHOLDER_LOGO } from './theme.js';
import {
	resolveOrderType,
	orderTypeLabel,
	isHotelFlow,
	isRouteFlow,
	isGoodsFlow,
	isRestaurantFlow,
	ORDER_TYPE
} from './order-type.js';
import { formatMoney, maskPhone } from './merchant-center.js';
import {
	extractLogisticsInfo,
	extractUserOrderRemark,
	resolvePayMethodLabel,
	resolveGoodsItemsAmount,
	resolveDisplayShippingFee
} from './order-ui.js';

export const MERCHANT_ORDER_UI = {
	typeHotel: '酒店预订',
	typeRoute: '线路预约',
	typeGoods: '好物订单',
	typeRestaurant: '餐饮预约',
	labelStay: '入住',
	labelCheckOut: '离店',
	labelTrip: '出行',
	labelTripDate: '出行日期',
	labelReserve: '预约',
	labelGuest: '预订人',
	labelContact: '联系人',
	labelPhone: '联系电话',
	labelRoomCount: '间数',
	labelGuestCount: '人数',
	labelVerify: '核销码',
	labelVerifyStatus: '核销状态',
	labelVerified: '已核销',
	labelPendingVerify: '待核销',
	labelAddress: '收货信息',
	labelReceiver: '收货人',
	labelMeetPoint: '集合点',
	labelExpectedArrival: '预计到店',
	labelOrderInfo: '订单信息',
	labelBookingInfo: '预约信息',
	labelOrderType: '订单类型',
	labelBookingType: '预约类型',
	labelOrderNo: '订单编号',
	labelBookingNo: '预约编号',
	labelOrderTime: '下单时间',
	labelBookingTime: '预约时间',
	labelPayTime: '支付时间',
	labelVerifyTime: '核销时间',
	labelFee: '费用明细',
	labelRefFee: '线路费用',
	labelFeeItem: '商品金额',
	labelShipping: '运费',
	shipFree: '免运费',
	labelDiscount: '优惠抵扣',
	labelPayAmount: '实付金额',
	labelRefAmount: '实付金额',
	labelRemark: '用户备注',
	labelBookingStatus: '预约状态',
	labelLogistics: '物流信息',
	labelLogisticsCompany: '物流公司',
	labelLogisticsNo: '物流单号',
	labelRouteInfo: '线路信息',
	labelTripInfo: '出行信息',
	labelRefPrice: '单价',
	labelFreeBooking: '免费预约',
	nightSuffix: '晚',
	guestSuffix: '人',
	copyOrderNo: '复制',
	copySuccess: '订单号已复制',
	copyBookingSuccess: '预约号已复制',
	callPhone: '拨打',
	freeTag: '预约'
};

/** 餐饮商户端列表：突出联系人、预约时间与人数，不展示菜品/封面 */
export function mapRestaurantMerchantListOrder(record) {
	const order = mapMerchantOrder(record);
	if (!order) return null;
	order.mainName = order.customerName || '预约客人';
	order.extraLine = order.guestCount ? `${order.guestCount}${MERCHANT_ORDER_UI.guestSuffix}` : '';
	order.hideAmount = Number(order.amount) <= 0;
	return order;
}

/** 从订单明细名解析门店名（格式：店名 · 菜品） */
function extractRestaurantStoreName(first) {
	const name = sanitizeRestaurantItemName(first?.name);
	if (!name) return '';
	const sep = name.indexOf(' · ');
	if (sep < 0) return name === '餐饮预约' ? '' : name;
	return name.slice(0, sep).trim();
}

const RESTAURANT_WEEK_LABELS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

/** 餐饮预约：解析 yyyy-MM-dd 对应星期 */
export function parseRestaurantWeekday(dateStr) {
	if (!dateStr) return '';
	const parts = String(dateStr).split('-');
	if (parts.length < 3) return '';
	const date = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
	if (Number.isNaN(date.getTime())) return '';
	return RESTAURANT_WEEK_LABELS[date.getDay()] || '';
}

/** 餐饮预约：日期块大号数字（日） */
export function formatRestaurantResDay(dateStr) {
	if (!dateStr) return '--';
	const parts = String(dateStr).split('-');
	return parts.length >= 3 ? Number(parts[2]) : dateStr;
}

/** 餐饮预约：到店时间（统一为 HH:mm） */
export function formatRestaurantArrivalTime(timeStr = '') {
	const raw = String(timeStr || '').trim();
	if (!raw) return '';
	if (raw.includes(' ')) {
		const clock = raw.split(' ').pop() || '';
		return clock.slice(0, 5);
	}
	return raw.slice(0, 5);
}

/** 餐饮预约：日期块副标题（月份 + 星期，不含时间） */
export function formatRestaurantResMonthWeek(dateStr) {
	if (!dateStr) return '';
	const parts = String(dateStr).split('-');
	if (parts.length < 2) return '';
	const month = `${Number(parts[1])}月`;
	const week = parseRestaurantWeekday(dateStr);
	return week ? `${month} ${week}` : month;
}

/** 餐饮商户端详情：补充预约菜品/门店摘要，供详情页展示 */
export function mapRestaurantMerchantDetailOrder(record) {
	const order = mapRestaurantMerchantListOrder(record);
	if (!order) return null;
	const first = order.items?.[0] || {};
	order.dishLabel = extractRestaurantDishLabel(first);
	order.storeName = extractRestaurantStoreName(first);
	order.subSpec = order.guestCount ? `${order.guestCount}${MERCHANT_ORDER_UI.guestSuffix}` : '';
	order.titleTag = '';
	return order;
}

/** 餐饮预约：当前状态提示文案 */
export function getRestaurantStatusHint(order) {
	if (!order) return '';
	const map = {
		pending_confirm: '用户已提交预约，请尽快确认是否接单',
		pending_arrival: '预约已确认，请等待用户按约定时间到店',
		preparing: '用户已到店，请安排就座并接待',
		completed: '本次预约已完成',
		cancelled: '预约已取消',
		refunding: '退款处理中，请关注进度'
	};
	return map[order.status] || '';
}

/** 餐饮预约：进度时间轴（用于详情页） */
export function buildRestaurantTimeline(order) {
	if (!order) return [];
	const status = order.status;
	const cancelled = status === 'cancelled' || status === 'refunding';
	const steps = [
		{ key: 'submit', label: '提交预约' },
		{ key: 'confirm', label: '商户确认' },
		{ key: 'arrival', label: '到店用餐' },
		{ key: 'done', label: '已到店' }
	];
	if (cancelled) {
		return steps.map((step, index) => ({
			...step,
			state: index === 0 ? 'done' : 'cancelled'
		}));
	}
	const statusIndex = {
		pending_confirm: 1,
		pending_arrival: 2,
		preparing: 2,
		completed: 4
	};
	const active = statusIndex[status] ?? 1;
	return steps.map((step, index) => {
		const stepNum = index + 1;
		if (status === 'completed') {
			return { ...step, state: 'done' };
		}
		if (stepNum < active) {
			return { ...step, state: 'done' };
		}
		if (stepNum === active) {
			return { ...step, state: 'active' };
		}
		return { ...step, state: 'pending' };
	});
}

export function mapMerchantOrder(record) {
	if (!record) return null;
	const orderType = resolveOrderType(record);
	const items = (record.items || []).map((item) => ({
		...item,
		cover: item.cover || PLACEHOLDER_LOGO,
		price: Number(item.price ?? 0),
		qty: item.qty ?? 1
	}));
	const first = items[0] || {};
	return {
		...record,
		orderType,
		orderTypeLabel: record.orderTypeLabel || orderTypeLabel(orderType),
		items,
		amount: Number(record.amount ?? 0),
		totalAmount: Number(record.totalAmount ?? record.amount ?? 0),
		discountAmount: Number(record.discountAmount ?? 0),
		shippingFee: record.shippingFee != null ? Number(record.shippingFee) : undefined,
		cover: first.cover || PLACEHOLDER_LOGO,
		mainName: resolveMainName(record, first, orderType),
		subSpec: resolveSubSpec(record, first, orderType),
		titleTag: resolveTitleTag(record, orderType),
		highlightLine: buildHighlightLine(record, orderType),
		extraLine: buildExtraLine(record, orderType),
		userRemark: extractUserRemark(record.remark),
		logisticsInfo: extractLogisticsInfo(record.remark),
		verified: Boolean(record.verified)
	};
}

/** 清理历史回填数据里误拼接的 " booking" 后缀 */
function sanitizeRestaurantItemName(name) {
	if (!name) return '';
	return String(name).replace(/\s+booking\s*$/i, '').trim();
}

/** 从订单明细名解析预约菜品（格式：店名 · 菜品） */
function extractRestaurantDishLabel(first) {
	const name = sanitizeRestaurantItemName(first?.name);
	if (!name) return '';
	const sep = name.indexOf(' · ');
	if (sep < 0) return '';
	const dish = name.slice(sep + 3).trim();
	return dish && dish !== '餐饮预约' ? dish : '';
}

function resolveMainName(record, first, orderType) {
	if (isHotelFlow(orderType)) return first.name || record.title || '酒店预订';
	if (isRouteFlow(orderType)) return first.name || record.title || '线路预约';
	if (isRestaurantFlow(orderType)) {
		return '餐饮预约';
	}
	return first.name || record.title || '好物订单';
}

function resolveSubSpec(record, first, orderType) {
	if (isHotelFlow(orderType)) return first.spec || '';
	if (isRouteFlow(orderType)) {
		const parts = [];
		if (record.guestCount) parts.push(`${record.guestCount}${MERCHANT_ORDER_UI.guestSuffix}`);
		return parts.join(' · ');
	}
	if (isRestaurantFlow(orderType)) {
		if (record.guestCount) return `${record.guestCount}${MERCHANT_ORDER_UI.guestSuffix}`;
		return first.spec || '';
	}
	return first.spec || '';
}

function resolveTitleTag(record, orderType) {
	if (isHotelFlow(orderType) && Number(record.roomCount) > 1) {
		return `${record.roomCount}${MERCHANT_ORDER_UI.labelRoomCount}`;
	}
	if (isGoodsFlow(orderType) && record.items?.length > 1) {
		return `共${record.items.length}件`;
	}
	const qty = record.items?.[0]?.qty;
	if (isGoodsFlow(orderType) && Number(qty) > 1) return `x${qty}`;
	return '';
}

function formatShortDate(dateStr) {
	if (!dateStr) return '';
	const parts = String(dateStr).split('-');
	if (parts.length < 3) return dateStr;
	return `${Number(parts[1])}/${Number(parts[2])}`;
}

export function buildHighlightLine(record, orderType = resolveOrderType(record)) {
	if (isHotelFlow(orderType) && record.checkIn) {
		const inDate = formatShortDate(record.checkIn);
		const outDate = formatShortDate(record.checkOut);
		const nights = record.nights ? `${record.nights}${MERCHANT_ORDER_UI.nightSuffix}` : '';
		const range = `${inDate} 至 ${outDate}`;
		return nights ? `${range} · ${nights}` : range;
	}
	if (isRouteFlow(orderType) && record.tourDate) {
		return `${MERCHANT_ORDER_UI.labelTrip} ${record.tourDate}`;
	}
	if (isRestaurantFlow(orderType) && record.reservationDate) {
		const time = record.reservationTime ? ` ${record.reservationTime}` : '';
		return `${MERCHANT_ORDER_UI.labelReserve} ${record.reservationDate}${time}`;
	}
	return '';
}

export function buildExtraLine(record, orderType = resolveOrderType(record)) {
	if (isHotelFlow(orderType)) {
		const guest = record.guestNames || record.customerName;
		return guest ? `${MERCHANT_ORDER_UI.labelGuest} ${guest}` : '';
	}
	if (isRouteFlow(orderType)) {
		return record.meetPoint || record.guestNames || record.customerName || '';
	}
	if (isRestaurantFlow(orderType)) {
		return '';
	}
	if (isGoodsFlow(orderType) && record.address) {
		const contact = record.receiverName || record.customerName || '';
		const phone = record.receiverPhone || record.customerPhone || '';
		return [contact, phone, record.address].filter(Boolean).join(' · ');
	}
	return record.customerName ? `${MERCHANT_ORDER_UI.labelContact} ${record.customerName}` : '';
}

/** 过滤商户端备注中的系统追加字段，保留用户填写内容 */
export function extractUserRemark(remark) {
	return extractUserOrderRemark(remark);
}

export function buildLogisticsRows(order) {
	if (!order) return [];
	const info = order.logisticsInfo || extractLogisticsInfo(order.remark);
	if (!info) return [];
	const rows = [];
	if (info.company) {
		rows.push({ label: MERCHANT_ORDER_UI.labelLogisticsCompany, value: info.company });
	}
	if (info.trackingNo) {
		rows.push({ label: MERCHANT_ORDER_UI.labelLogisticsNo, value: info.trackingNo });
	}
	return rows;
}

export function buildBookingRows(order) {
	if (!order) return [];
	const type = resolveOrderType(order);
	const rows = [];
	if (isHotelFlow(type)) {
		if (order.roomCount) rows.push({ label: MERCHANT_ORDER_UI.labelRoomCount, value: `${order.roomCount}间` });
		if (order.guestNames && order.guestNames !== order.customerName) {
			rows.push({ label: MERCHANT_ORDER_UI.labelGuest, value: order.guestNames });
		}
		if (order.expectedArrivalTime) {
			rows.push({ label: MERCHANT_ORDER_UI.labelExpectedArrival, value: order.expectedArrivalTime });
		}
		return rows;
	}
	if (isRouteFlow(type)) {
		if (order.guestCount) rows.push({ label: MERCHANT_ORDER_UI.labelGuestCount, value: `${order.guestCount}${MERCHANT_ORDER_UI.guestSuffix}` });
		if (order.guestNames && order.guestNames !== order.customerName) {
			rows.push({ label: MERCHANT_ORDER_UI.labelGuest, value: order.guestNames });
		}
		if (order.meetPoint) rows.push({ label: MERCHANT_ORDER_UI.labelMeetPoint, value: order.meetPoint });
		return rows;
	}
	if (isRestaurantFlow(type)) {
		if (order.guestCount) rows.push({ label: MERCHANT_ORDER_UI.labelGuestCount, value: `${order.guestCount}${MERCHANT_ORDER_UI.guestSuffix}` });
		return rows;
	}
	if (isGoodsFlow(type)) {
		const qty = order.items?.[0]?.qty;
		if (qty) rows.push({ label: '数量', value: String(qty) });
		if (order.receiverName || order.customerName) {
			rows.push({ label: MERCHANT_ORDER_UI.labelReceiver, value: order.receiverName || order.customerName });
		}
		const phone = order.receiverPhone || order.customerPhone;
		if (phone) {
			rows.push({ label: MERCHANT_ORDER_UI.labelPhone, value: maskPhone(phone) });
		}
		if (order.address) rows.push({ label: MERCHANT_ORDER_UI.labelAddress, value: order.address });
	}
	return rows;
}

/** 线路预约：出行信息行 */
export function buildRouteTripRows(order) {
	if (!order || !isRouteFlow(resolveOrderType(order))) return [];
	const rows = [];
	if (order.tourDate) rows.push({ label: MERCHANT_ORDER_UI.labelTripDate, value: order.tourDate });
	if (order.guestCount) {
		rows.push({
			label: MERCHANT_ORDER_UI.labelGuestCount,
			value: `${order.guestCount}${MERCHANT_ORDER_UI.guestSuffix}`
		});
	}
	const guests = order.guestNames || order.customerName;
	if (guests) rows.push({ label: MERCHANT_ORDER_UI.labelGuest, value: guests });
	if (order.meetPoint) rows.push({ label: MERCHANT_ORDER_UI.labelMeetPoint, value: order.meetPoint });
	return rows;
}

/** 线路预约：预约信息行 */
export function buildRouteMetaRows(order) {
	if (!order || !isRouteFlow(resolveOrderType(order))) return [];
	const rows = [
		{
			label: MERCHANT_ORDER_UI.labelBookingNo,
			value: order.orderNo || '--'
		},
		{
			label: MERCHANT_ORDER_UI.labelBookingType,
			value: order.orderTypeLabel || orderTypeLabel(order.orderType)
		},
		{
			label: MERCHANT_ORDER_UI.labelBookingTime,
			value: order.createdAt || '--'
		}
	];
	if (order.statusLabel) {
		rows.push({ label: MERCHANT_ORDER_UI.labelBookingStatus, value: order.statusLabel });
	}
	return rows;
}

export function formatRouteRefPrice(order) {
	if (!order) return '';
	const item = order.items?.[0];
	const price = Number(item?.price ?? order.totalAmount ?? order.amount ?? 0);
	if (price <= 0) return '';
	return `${formatMoney(price)}/人`;
}

export function buildMetaRows(order) {
	if (!order) return [];
	const isRoute = isRouteFlow(resolveOrderType(order));
	const rows = [
		{
			label: isRoute ? MERCHANT_ORDER_UI.labelBookingType : MERCHANT_ORDER_UI.labelOrderType,
			value: order.orderTypeLabel || orderTypeLabel(order.orderType)
		},
		{
			label: isRoute ? MERCHANT_ORDER_UI.labelBookingTime : MERCHANT_ORDER_UI.labelOrderTime,
			value: order.createdAt || '--'
		}
	];
	if (!isRoute && order.paidAt) rows.push({ label: MERCHANT_ORDER_UI.labelPayTime, value: order.paidAt });
	if (order.verifiedAt) rows.push({ label: MERCHANT_ORDER_UI.labelVerifyTime, value: order.verifiedAt });
	return rows;
}

export function buildFeeRows(order) {
	if (!order) return [];
	const orderType = resolveOrderType(order);
	const isRoute = isRouteFlow(orderType);
	const isGoods = isGoodsFlow(orderType);
	const amount = isRoute
		? (Number(order.totalAmount) > 0 ? Number(order.totalAmount) : Number(order.amount) || 0)
		: Number(order.amount) || 0;
	if (amount <= 0 && !isGoods) return [];
	const rows = [];
	if (isGoods) {
		rows.push({
			label: MERCHANT_ORDER_UI.labelFeeItem,
			value: formatMoney(resolveGoodsItemsAmount(order))
		});
		const shipping = resolveDisplayShippingFee(order);
		rows.push({
			label: MERCHANT_ORDER_UI.labelShipping,
			value: shipping > 0 ? formatMoney(shipping) : MERCHANT_ORDER_UI.shipFree
		});
	} else {
		const itemLine = order.items?.length
			? order.items.map((item) => `${item.name}${item.qty > 1 ? ` x${item.qty}` : ''}`).join('、')
			: order.mainName;
		rows.push({
			label: isRoute ? MERCHANT_ORDER_UI.labelRefFee : MERCHANT_ORDER_UI.labelFeeItem,
			value: itemLine
		});
	}
	if (!isRoute && Number(order.discountAmount) > 0) {
		rows.push({ label: MERCHANT_ORDER_UI.labelDiscount, value: `- ${formatMoney(order.discountAmount)}`, tone: 'discount' });
	}
	rows.push({
		label: isRoute ? MERCHANT_ORDER_UI.labelRefAmount : MERCHANT_ORDER_UI.labelPayAmount,
		value: formatMoney(amount),
		tone: 'total'
	});
	return rows;
}

const VERIFY_HIDDEN_STATUSES = new Set(['cancelled', 'refunding', 'completed']);

export function showVerifyCard(order) {
	if (!order) return false;
	const orderType = resolveOrderType(order);
	if (!isHotelFlow(orderType) && !isRouteFlow(orderType)) return false;
	if (VERIFY_HIDDEN_STATUSES.has(order.status)) return false;
	return Boolean(order.verifyCode) && !order.verified;
}

export function statusBadgeClass(status) {
	if (status === 'completed') return 'is-done';
	if (status === 'cancelled' || status === 'refunding') return 'is-cancel';
	if (status === 'pending_ship' || status === 'shipped' || status === 'pending_receive') return 'is-ship';
	if (status === 'pending_confirm' || status === 'pending_checkin' || status === 'pending_use'
		|| status === 'pending_arrival' || status === 'preparing') return 'is-pending';
	return 'is-default';
}

export function formatDisplayAmount(order) {
	if (!order) return MERCHANT_ORDER_UI.labelFreeBooking;
	const isRoute = isRouteFlow(resolveOrderType(order));
	const amount = isRoute
		? (Number(order.totalAmount) > 0 ? Number(order.totalAmount) : Number(order.amount) || 0)
		: Number(order.amount) || 0;
	if (amount <= 0) return MERCHANT_ORDER_UI.labelFreeBooking;
	return formatMoney(amount);
}

export { resolvePayMethodLabel, ORDER_TYPE };
