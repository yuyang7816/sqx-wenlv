/**
 * 将相对路径或 OSS key 解析为可访问的完整 URL
 */
const DEFAULT_CDN = '';
const MEDIA_URL_RE = /https?:\/\/[^\s"'\\\],]+/g;

export function resolvePublicUrl(url) {
	const trimmed = String(url || '').trim().replace(/\\/g, '/');
	if (!trimmed) {
		return '';
	}
	if (/^https?:\/\//i.test(trimmed)) {
		return trimmed;
	}
	const path = trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
	return `${DEFAULT_CDN}${path}`;
}

/** 从可能被逗号拆坏的 JSON 字符串里取出真实图片地址 */
export function normalizeMediaList(list) {
	const result = [];
	const source = Array.isArray(list) ? list : (list ? [list] : []);
	source.forEach((item) => {
		const text = String(item || '').trim();
		if (!text) return;
		const matches = text.match(MEDIA_URL_RE);
		if (matches && matches.length) {
			matches.forEach((url) => {
				const resolved = resolvePublicUrl(url);
				if (resolved && !result.includes(resolved)) result.push(resolved);
			});
			return;
		}
		const resolved = resolvePublicUrl(text.replace(/^[[\]"']+|[[\]"']+$/g, ''));
		if (resolved && !result.includes(resolved)) result.push(resolved);
	});
	return result;
}

export function normalizeMediaUrl(raw) {
	const list = normalizeMediaList(raw ? [raw] : []);
	return list[0] || '';
}
