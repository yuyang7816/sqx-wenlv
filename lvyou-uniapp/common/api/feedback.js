/**
 * 意见反馈 API（路径对齐 AppFeedbackController / FileController）
 */
import config from '../config.js';
import { postJson } from './request.js';
import { resolveUploadUrl, isLocalTempPath } from './user.js';

const PREFIX = '/app/feedback';

/** 提交意见反馈（需 App JWT，对齐 @Login POST /app/feedback/submit） */
export function submitFeedback(payload) {
	return postJson(`${PREFIX}/submit`, payload);
}

/** 上传反馈截图，路径对齐 FileController POST /file/upload */
export function uploadFeedbackFile(filePath) {
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

export { resolveUploadUrl, isLocalTempPath };
