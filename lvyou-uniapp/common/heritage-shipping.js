/** 非遗好店运费配置解析与计算 */

export function defaultShopShipping() {
	return {
		freeShipping: true,
		fixedFee: 0,
		freeAmountThreshold: 0,
		shippingNote: ''
	};
}

export function normalizeShopShipping(raw) {
	const defaults = defaultShopShipping();
	if (!raw || typeof raw !== 'object') {
		return { ...defaults };
	}
	const freeShipping = raw.freeShipping !== false;
	const fixedFee = normalizeMoney(raw.fixedFee);
	const freeAmountThreshold = normalizeMoney(raw.freeAmountThreshold);
	return {
		freeShipping: freeShipping || fixedFee <= 0,
		fixedFee: freeShipping ? 0 : fixedFee,
		freeAmountThreshold,
		shippingNote: String(raw.shippingNote || '').trim()
	};
}

export function buildShopShippingPayload(form) {
	const normalized = normalizeShopShipping({
		freeShipping: form?.freeShipping !== false,
		fixedFee: form?.fixedFee,
		freeAmountThreshold: form?.freeAmountThreshold,
		shippingNote: form?.shippingNote
	});
	if (normalized.freeShipping) {
		return {
			freeShipping: true,
			fixedFee: 0,
			freeAmountThreshold: normalizeMoney(form?.freeAmountThreshold),
			shippingNote: normalized.shippingNote
		};
	}
	return normalized;
}

export function calcHeritageShippingFee(shipping, goodsAmount) {
	const config = normalizeShopShipping(shipping);
	const amount = normalizeMoney(goodsAmount);
	if (config.freeShipping) {
		return 0;
	}
	if (config.freeAmountThreshold > 0 && amount >= config.freeAmountThreshold) {
		return 0;
	}
	return config.fixedFee;
}

function normalizeMoney(value) {
	const num = Number(value);
	if (!Number.isFinite(num) || num < 0) {
		return 0;
	}
	return Math.round(num * 100) / 100;
}
