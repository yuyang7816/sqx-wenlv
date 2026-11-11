/**
 * 收货地址 API（路径对齐 AppAddressController）
 */
import { get, postJson } from './request.js';

const PREFIX = '/app/address';

/** 地址列表（需 App JWT） */
export function fetchAddressList() {
	return get(`${PREFIX}/list`);
}

/** 保存地址（新增或编辑，需 App JWT） */
export function saveAddress(payload) {
	return postJson(`${PREFIX}/save`, payload);
}

/** 删除地址（需 App JWT） */
export function deleteAddress(id) {
	return postJson(`${PREFIX}/delete`, { id });
}

/** 映射地址项到页面字段 */
export function mapAddressItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		phone: record.phone || '',
		region: record.region || '',
		detail: record.detail || '',
		isDefault: !!record.isDefault
	};
}
