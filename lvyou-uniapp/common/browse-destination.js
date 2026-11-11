/**
 * 首页手动选择的浏览城市。未选择时首页仍按设备定位展示。
 */
const STORAGE_KEY = 'browse_destination';

export function readBrowseDestination() {
	try {
		const raw = uni.getStorageSync(STORAGE_KEY);
		if (!raw) return null;
		const data = typeof raw === 'string' ? JSON.parse(raw) : raw;
		if (!data || !data.id) return null;
		return {
			id: String(data.id),
			name: data.name || '',
			shortName: data.shortName || ''
		};
	} catch (e) {
		return null;
	}
}

export function saveBrowseDestination(item) {
	const id = String(item?.id || '').trim();
	if (!id) return;
	const payload = {
		id,
		name: item.name || '',
		shortName: item.shortName || ''
	};
	try {
		uni.setStorageSync(STORAGE_KEY, JSON.stringify(payload));
	} catch (e) {
		// ignore storage failures
	}
}

export function clearBrowseDestination() {
	try {
		uni.removeStorageSync(STORAGE_KEY);
	} catch (e) {
		// ignore storage failures
	}
}

export function browseDestinationLabel(item) {
	if (!item) return '';
	return String(item.shortName || item.name || '').replace(/[市区縣]$/, '').trim();
}
