/**
 * 目的地 API（路径对齐 AppDestinationController）
 */
import { get, postJson } from './request.js';
import { buildLocationListQuery } from '../location-params.js';

const PREFIX = '/app/destination';

/** 已上线目的地列表（按 sort 升序） */
export function fetchDestinationList() {
	return get(`${PREFIX}/list`);
}

/** 按当前位置解析目的地 */
export async function fetchCurrentDestination() {
	const params = await buildLocationListQuery();
	return get(`${PREFIX}/current`, params);
}

/** 当前位置目的地下已审核商户列表（需 App JWT） */
export async function fetchCurrentMerchants() {
	const params = await buildLocationListQuery();
	return get(`${PREFIX}/current/merchants`, params);
}

/** 切换当前目的地（需 App JWT，destinationCode 为列表项 id 字段） */
export function switchDestination(destinationCode) {
	return postJson(`${PREFIX}/switch`, {
		destinationCode: String(destinationCode || '').trim()
	});
}
