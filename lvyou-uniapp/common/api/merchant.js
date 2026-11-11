/**
 * 商户入驻 API（路径对齐 AppMerchantController）
 */
import config from '../config.js';
import { normalizePublicUrl } from '../url.js';
import { get, postJson } from './request.js';
import { hasToken } from './auth.js';
import { MERCHANT_UI } from '../merchant-onboarding.js';
import { goLogin } from '../navigation.js';

const PREFIX = '/app/merchant';
const PAGE_SIZE = 10;

/** 查询入驻审核状态（需 App JWT） */
export function fetchMerchantStatus() {
	return get(`${PREFIX}/status`);
}

/** 可选入驻类型（含保证金，无需登录） */
export function fetchMerchantStoreTypes() {
	return get(`${PREFIX}/store-types`);
}

/** 提交入驻申请（需 App JWT） */
export function submitMerchantApply(payload) {
	return postJson(`${PREFIX}/apply`, payload);
}

/** 上传证照图片，路径对齐 FileController /file/upload */
export function uploadMerchantFile(filePath) {
	return new Promise((resolve, reject) => {
		let token = '';
		try {
			token = uni.getStorageSync('token') || '';
		} catch (e) { /* ignore */ }
		uni.uploadFile({
			url: `${config.APIHOST}/file/upload`,
			filePath,
			name: 'file',
			header: token ? { token } : {},
			success: (res) => {
				try {
					const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
					if (body && body.code === 0 && body.data) {
						resolve(body);
						return;
					}
					reject(new Error((body && body.msg) || MERCHANT_UI.uploadFail));
				} catch (e) {
					reject(e);
				}
			},
			fail: (err) => reject(err)
		});
	});
}

/** 解析上传结果 URL */
export function resolveUploadUrl(uploadRes) {
	const data = uploadRes && uploadRes.data;
	if (!data) return '';
	return normalizePublicUrl(data.path || data.relativePath || data.url || '');
}

/** 是否为已上传的服务端路径（非本地临时文件） */
export function isUploadedUrl(path) {
	const text = String(path || '').trim();
	if (!text) return false;
	if (text.startsWith('wxfile://') || text.startsWith('file://') || text.startsWith('blob:')) return false;
	if (text.includes('_doc/') || text.includes('_www/')) return false;
	if (/^https?:\/\/tmp/i.test(text)) return false;
	return text.startsWith('http') || text.startsWith('/');
}

/** 本地临时路径则上传，已是服务端 URL 则直接返回 */
export async function ensureUploadedUrl(filePath) {
	if (!filePath) return '';
	if (isUploadedUrl(filePath)) return filePath;
	const uploadRes = await uploadMerchantFile(filePath);
	const url = resolveUploadUrl(uploadRes);
	if (!url) throw new Error(MERCHANT_UI.uploadFail);
	return url;
}

/** 根据 status 接口数据生成「我的」页商户入口 */
export function buildMerchantEntry(statusVo) {
	const status = (statusVo && statusVo.status) || 'none';
	if (status === 'approved') {
		return {
			id: 'merchant-center',
			title: MERCHANT_UI.entryCenter,
			icon: 'home-fill',
			bgColor: '#EAF2FF',
			iconColor: '#2979FF',
			path: '/merchant-portal/center/index',
			badge: 0
		};
	}
	if (status === 'pending') {
		return {
			id: 'merchant-pending',
			title: MERCHANT_UI.entryPending,
			icon: 'clock-fill',
			bgColor: '#FFF8E6',
			iconColor: '#FFB800',
			path: '/merchant-portal/onboarding/result'
		};
	}
	return {
		id: 'merchant-apply',
		title: MERCHANT_UI.entryApply,
		icon: 'plus-circle-fill',
		bgColor: '#FFF3EB',
		iconColor: '#FF6600',
		path: '/merchant-portal/onboarding/apply'
	};
}

/** 商户页登录门控（App JWT） */
export function requireMerchantLogin() {
	if (!hasToken()) {
		uni.showToast({ title: MERCHANT_UI.loginTip, icon: 'none' });
		goLogin({ delay: 400 });
		return false;
	}
	return true;
}

/** 商户中心概览 GET /app/merchant/dashboard */
export function fetchMerchantDashboard() {
	return get(`${PREFIX}/dashboard`);
}

/** 待处理订单数 GET /app/merchant/orders/pending-count */
export function fetchMerchantPendingOrderCount() {
	return get(`${PREFIX}/orders/pending-count`);
}

/** 订单列表（分页）GET /app/merchant/orders */
export function fetchMerchantOrders(params) {
	return get(`${PREFIX}/orders`, params);
}

/** 订单详情 GET /app/merchant/orders/{id} */
export function fetchMerchantOrderDetail(id) {
	return get(`${PREFIX}/orders/${id}`);
}

/** 订单操作 POST /app/merchant/orders/{id}/action */
export function postMerchantOrderAction(id, payload) {
	return postJson(`${PREFIX}/orders/${id}/action`, payload);
}

/** 查询商品订单物流 POST /app/merchant/orders/{id}/logistics */
export function fetchMerchantOrderLogistics(id) {
	return postJson(`${PREFIX}/orders/${id}/logistics`, {});
}

/** 商户资料 GET /app/merchant/profile */
export function fetchMerchantProfile() {
	return get(`${PREFIX}/profile`);
}

/** 更新商户资料 POST /app/merchant/profile */
export function updateMerchantProfile(payload) {
	return postJson(`${PREFIX}/profile`, payload);
}

/** 更新餐饮现场排队状态（仅餐饮商户） */
export function updateMerchantQueueStatus(payload) {
	return postJson(`${PREFIX}/queue-status`, payload);
}

/** 首页推荐 Tab 选项 GET /app/merchant/home-tabs */
export function fetchMerchantHomeTabs() {
	return get(`${PREFIX}/home-tabs`);
}

/** 服务列表 GET /app/merchant/services */
export function fetchMerchantServices(tab) {
	const params = tab && tab !== 'all' ? { tab } : {};
	return get(`${PREFIX}/services`, params);
}

/** 服务详情 GET /app/merchant/services/{id} */
export function fetchMerchantServiceDetail(id) {
	return get(`${PREFIX}/services/${id}`);
}

/** 保存服务 POST /app/merchant/services/save */
export function saveMerchantService(payload) {
	return postJson(`${PREFIX}/services/save`, payload);
}

/** 服务上下架 POST /app/merchant/services/{id}/publish */
export function publishMerchantService(id, status) {
	return postJson(`${PREFIX}/services/${id}/publish`, { status });
}

/** 删除服务 POST /app/merchant/services/{id}/delete */
export function deleteMerchantService(id) {
	return postJson(`${PREFIX}/services/${id}/delete`, {});
}

export { fetchMessageUnreadCount } from './messages.js';

/** 商户钱包概览 GET /app/merchant/wallet/summary */
export function fetchMerchantWalletSummary() {
	return get(`${PREFIX}/wallet/summary`);
}

/** 创建保证金支付单 POST /app/merchant/wallet/deposit/order */
export function createMerchantDepositPayOrder(payload) {
	return postJson(`${PREFIX}/wallet/deposit/order`, payload || {});
}

/** 余额缴纳保证金 POST /app/merchant/wallet/deposit/pay-balance */
export function payMerchantDepositByBalance(payload) {
	return postJson(`${PREFIX}/wallet/deposit/pay-balance`, payload || {});
}

/** 申请退还保证金 POST /app/merchant/wallet/deposit/refund/apply */
export function applyMerchantDepositRefund(payload) {
	return postJson(`${PREFIX}/wallet/deposit/refund/apply`, payload || {});
}

/** 申请提现 POST /app/merchant/wallet/withdraw */
export function postMerchantWithdraw(payload) {
	return postJson(`${PREFIX}/wallet/withdraw`, payload);
}

/** 资金明细分页 GET /app/merchant/wallet/fund-records */
export function fetchMerchantFundRecords(params) {
	return get(`${PREFIX}/wallet/fund-records`, params);
}

/** 提现记录分页 GET /app/merchant/wallet/withdraw-records */
export function fetchMerchantWithdrawRecords(params) {
	return get(`${PREFIX}/wallet/withdraw-records`, params);
}

/** 门店装修 GET /app/merchant/store-decor */
export function fetchMerchantStoreDecor() {
	return get(`${PREFIX}/store-decor`);
}

/** 保存门店装修 POST /app/merchant/store-decor */
export function saveMerchantStoreDecor(payload) {
	return postJson(`${PREFIX}/store-decor`, payload);
}

/** 客房标准列表 GET /app/merchant/room-standards */
export function fetchMerchantRoomStandards() {
	return get(`${PREFIX}/room-standards`);
}

/** 客房标准详情 GET /app/merchant/room-standards/{id} */
export function fetchMerchantRoomStandardDetail(id) {
	return get(`${PREFIX}/room-standards/${id}`);
}

/** 保存客房标准 POST /app/merchant/room-standards/save */
export function saveMerchantRoomStandard(payload) {
	return postJson(`${PREFIX}/room-standards/save`, payload);
}

/** 删除客房标准 POST /app/merchant/room-standards/{id}/delete */
export function deleteMerchantRoomStandard(id) {
	return postJson(`${PREFIX}/room-standards/${id}/delete`, {});
}

export { PAGE_SIZE as MERCHANT_WALLET_PAGE_SIZE };
