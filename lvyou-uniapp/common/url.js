import { resolvePublicUrl } from './media-url.js';

/** 修正上传/接口返回的图片地址为可访问的完整 URL */
export function normalizePublicUrl(url) {
	return resolvePublicUrl(url);
}

export { resolvePublicUrl };
