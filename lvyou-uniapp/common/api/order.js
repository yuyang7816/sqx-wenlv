/**
 * 订单 API（路径对齐 AppOrderController）
 */
import { get, postJson } from './request.js';

const PREFIX = '/app/order';

/** 接口是否成功（兼容 code 为字符串） */
export function isApiSuccess(res) {
	return res != null && Number(res.code) === 0;
}

/** 解析创建订单返回体 */
export function resolveCreateOrderResult(res) {
	if (!isApiSuccess(res)) return null;
	const data = res.data;
	if (!data || typeof data !== 'object') return null;
	const orderNo = data.orderNo || data.order_no || '';
	const orderId = data.orderId ?? data.order_id ?? data.id ?? null;
	const payAmount = Number(data.payAmount ?? data.pay_amount ?? 0) || 0;
	if (!orderNo && orderId == null) return null;
	return {
		orderId,
		orderNo,
		payAmount,
		statusCode: data.statusCode || data.status_code || '',
		status: data.status || '',
		orderType: data.orderType || data.order_type || ''
	};
}

/** 创建酒店/线路订单 */
export function createOrder(payload) {
	return postJson(`${PREFIX}/create`, payload);
}

/** 订单列表（分页） */
export function fetchOrderList(params = {}) {
	return get(`${PREFIX}/list`, {
		page: params.page || 1,
		limit: params.limit || 10,
		category: params.category,
		status: params.status,
		orderNo: params.orderNo
	});
}

/** 订单详情 */
export function fetchOrderDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 线路退款预览（比例/分账） */
export function fetchRefundPreview(id) {
	return get(`${PREFIX}/${id}/refund-preview`);
}

/** 取消订单（仅待支付） */
export function cancelOrder(id) {
	return postJson(`${PREFIX}/${id}/cancel`, {});
}

/** 订单操作：complete 确认收货 / refund 申请退款 */
export function postOrderAction(id, payload) {
	return postJson(`${PREFIX}/${id}/action`, payload);
}

/** 创建商品订单（需 App JWT） */
export function createGoodsOrder(payload) {
	return postJson(`${PREFIX}/goods/create`, payload);
}

/** 查询商品订单物流 */
export function fetchOrderLogistics(id) {
	return postJson(`${PREFIX}/logistics`, { id });
}
