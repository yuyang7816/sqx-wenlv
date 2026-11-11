/**
 * 综合搜索 API（路径对齐 AppSearchController）
 */
import { get } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/search';

/**
 * 综合搜索（公开接口，keywordCode 必填，禁止传中文）
 * @param {Object} params
 * @param {string} params.keywordCode 搜索关键词编码（ASCII）
 * @param {string} [params.lat] 纬度
 * @param {string} [params.lng] 经度
 */
export function fetchSearch(params = {}) {
	return get(PREFIX, appendLocationParams({
		keywordCode: params.keywordCode
	}, params));
}
