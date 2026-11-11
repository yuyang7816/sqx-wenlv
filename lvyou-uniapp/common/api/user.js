/**
 * 用户资料 API（路径对齐 AppUserController / FileController）
 */
import config from '../config.js';
import { normalizePublicUrl } from '../url.js';
import { get, postJson } from './request.js';

const PREFIX = '/app/base/user';

/** 获取当前登录用户资料（需 App JWT） */
export function fetchUserProfile() {
	return get(`${PREFIX}/info`);
}

/** 更新用户资料（需 App JWT，字段对齐 UpdateUserBO） */
export function updateUserProfile(payload) {
	return postJson(`${PREFIX}/update`, payload);
}

/** 提交账号注销申请（需 App JWT） */
export function submitCancelAccount(reason = '') {
	return postJson('/app/base/cancelAccount/submit', { reason });
}

/** 上传头像等文件，路径对齐 FileController POST /file/upload */
export function uploadUserFile(filePath) {
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

/** 解析上传接口返回的文件 URL */
export function resolveUploadUrl(uploadRes) {
	const data = uploadRes && uploadRes.data;
	if (!data) return '';
	return normalizePublicUrl(data.path || data.relativePath || data.url || '');
}

export { normalizePublicUrl };

/** 是否为本地临时路径（尚未上传） */
export function isLocalTempPath(path) {
	const text = String(path || '').trim();
	if (!text) return false;
	if (text.startsWith('http://') || text.startsWith('https://')) return false;
	if (text.startsWith('/')) return false;
	return true;
}
