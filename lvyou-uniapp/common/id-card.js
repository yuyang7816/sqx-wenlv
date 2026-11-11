/**
 * 身份证号与出行人姓名校验（线路预约）
 */

export function normalizeIdCard(value) {
	return String(value || '').trim().toUpperCase();
}

/** 18 位身份证格式（含 X） */
export function isValidIdCard(value) {
	const id = normalizeIdCard(value);
	return /^\d{17}[\dX]$/.test(id);
}

/** 姓名：2-20 字符，中文/字母/间隔号 */
export function isValidGuestName(name) {
	const value = String(name || '').trim();
	if (value.length < 2 || value.length > 20) return false;
	return /^[\u4e00-\u9fa5a-zA-Z·]+$/.test(value);
}

/**
 * 校验线路出行人列表
 * @returns {string} 错误提示，通过则返回空字符串
 */
export function validateRouteGuestList(guestList, guestCount) {
	if (!Array.isArray(guestList) || guestList.length !== guestCount) {
		return '请填写全部出行人信息';
	}
	const idSet = new Set();
	for (let i = 0; i < guestList.length; i++) {
		const guest = guestList[i] || {};
		const label = i === 0 ? '预约人' : `同行人${i}`;
		if (!isValidGuestName(guest.name)) {
			return `请填写${label}真实姓名（2-20字）`;
		}
		const idCard = normalizeIdCard(guest.idCard);
		if (!isValidIdCard(idCard)) {
			return `请填写${label}有效身份证号`;
		}
		if (idSet.has(idCard)) {
			return '同一订单内身份证号不可重复';
		}
		idSet.add(idCard);
		if (i === 0 && guest.phone && !/^1\d{10}$/.test(String(guest.phone).trim())) {
			return '请填写预约人有效手机号';
		}
	}
	return '';
}
