/**
 * 表单联系人预填（从本地登录摘要读取，无 mock）
 */

export function getStoredContactName() {
	try {
		return uni.getStorageSync('nickname') || '';
	} catch (e) {
		return '';
	}
}

export function getStoredPhone() {
	try {
		return uni.getStorageSync('phone') || '';
	} catch (e) {
		return '';
	}
}

export function getDefaultContactName() {
	return getStoredContactName();
}

export function getDefaultPhone() {
	return getStoredPhone();
}
