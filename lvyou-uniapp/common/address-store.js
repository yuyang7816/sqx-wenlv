/**
 * 收货地址选中态本地存储（非 mock，仅记录下单页所选 addressId）
 */

export const SELECTED_ADDRESS_ID_KEY = 'heritage_selected_address_id';

export function getSelectedAddressId() {
	try {
		return uni.getStorageSync(SELECTED_ADDRESS_ID_KEY) || '';
	} catch (e) {
		return '';
	}
}

export function setSelectedAddressId(id) {
	if (id == null || id === '') {
		uni.removeStorageSync(SELECTED_ADDRESS_ID_KEY);
		return;
	}
	uni.setStorageSync(SELECTED_ADDRESS_ID_KEY, String(id));
}

export function formatAddress(address) {
	if (!address) return '';
	return `${address.region || ''}${address.detail || ''}`;
}

/** 从地址列表解析当前选中项（优先本地选中，其次默认，再次首条） */
export function resolveSelectedAddress(list) {
	if (!Array.isArray(list) || !list.length) return null;
	const selectedId = getSelectedAddressId();
	if (selectedId) {
		const found = list.find((item) => String(item.id) === String(selectedId));
		if (found) return found;
	}
	return list.find((item) => item.isDefault) || list[0] || null;
}
