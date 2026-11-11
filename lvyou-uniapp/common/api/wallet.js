/**
 * 用户钱包 API（路径对齐 AppWalletController）
 */
import config from '../config.js';
import { normalizePublicUrl } from '../url.js';
import { get, postJson } from './request.js';

const PREFIX = '/app/wallet';
const PAGE_SIZE = 10;

/** 钱包概览（需 App JWT） */
export function fetchWalletSummary() {
	return get(`${PREFIX}/summary`);
}

/** 资金明细分页（filter: all/income/expense） */
export function fetchWalletTransactions(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	};
	if (params.filter && params.filter !== 'all') {
		data.filter = params.filter;
	}
	return get(`${PREFIX}/transactions`, data);
}

/** 提现记录分页 */
export function fetchWalletWithdrawRecords(params = {}) {
	return get(`${PREFIX}/withdraw-records`, {
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	});
}

/** 创建充值单 */
export function createWalletRecharge(amount) {
	return postJson(`${PREFIX}/recharge`, { amount });
}

/** 申请提现 */
export function createWalletWithdraw(payload) {
	return postJson(`${PREFIX}/withdraw`, payload);
}

/** 上传文件（微信收款码等），路径对齐 FileController */
export function uploadWalletFile(filePath) {
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
					reject(new Error((body && body.msg) || '上传失败'));
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
	return normalizePublicUrl(data.path || data.relativePath || '');
}

export { PAGE_SIZE as WALLET_PAGE_SIZE };
