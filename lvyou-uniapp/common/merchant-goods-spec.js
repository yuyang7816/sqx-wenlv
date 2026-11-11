/**
 * 文创好物规格：单规格 / 多规格 SKU
 */

export const SPEC_MODE_SINGLE = 'single';
export const SPEC_MODE_MULTI = 'multi';

export function createEmptySku() {
	return { name: '', price: '', stock: '' };
}

function splitLegacyNames(raw) {
	return String(raw || '')
		.split(/[,，、]/)
		.map((item) => item.trim())
		.filter(Boolean);
}

function normalizeSku(raw, fallbackPrice = 0, fallbackStock = 0) {
	if (!raw || typeof raw !== 'object') return null;
	const name = String(raw.name || '').trim();
	if (!name) return null;
	return {
		name,
		price: raw.price != null && raw.price !== '' ? Number(raw.price) : Number(fallbackPrice) || 0,
		stock: raw.stock != null && raw.stock !== '' ? Number(raw.stock) : Number(fallbackStock) || 0
	};
}

/** 解析商户端 goodsSpecs 字段 */
export function parseGoodsSpecs(raw, fallbackPrice = 0, fallbackStock = 0) {
	const price = Number(fallbackPrice) || 0;
	const stock = Number(fallbackStock) || 0;
	if (!raw) {
		return { mode: SPEC_MODE_SINGLE, skus: [] };
	}
	const text = String(raw).trim();
	if (!text) {
		return { mode: SPEC_MODE_SINGLE, skus: [] };
	}
	if (text.startsWith('{')) {
		try {
			const parsed = JSON.parse(text);
			const mode = parsed?.mode === SPEC_MODE_MULTI ? SPEC_MODE_MULTI : SPEC_MODE_SINGLE;
			const skus = (Array.isArray(parsed?.skus) ? parsed.skus : [])
				.map((item) => normalizeSku(item, price, stock))
				.filter(Boolean);
			if (mode === SPEC_MODE_MULTI && skus.length) {
				return { mode: SPEC_MODE_MULTI, skus };
			}
			if (skus.length === 1) {
				return { mode: SPEC_MODE_SINGLE, skus };
			}
		} catch (e) {
			// fall through
		}
	}
	const legacyNames = splitLegacyNames(text);
	if (legacyNames.length > 1) {
		return {
			mode: SPEC_MODE_MULTI,
			skus: legacyNames.map((name) => ({ name, price, stock }))
		};
	}
	if (legacyNames.length === 1) {
		return {
			mode: SPEC_MODE_SINGLE,
			skus: [{ name: legacyNames[0], price, stock }]
		};
	}
	return { mode: SPEC_MODE_SINGLE, skus: [] };
}

export function aggregateSkus(skus = []) {
	const list = Array.isArray(skus) ? skus : [];
	if (!list.length) {
		return { minPrice: 0, totalStock: 0 };
	}
	let minPrice = null;
	let totalStock = 0;
	list.forEach((item) => {
		const skuPrice = Number(item?.price) || 0;
		const skuStock = Number(item?.stock) || 0;
		if (minPrice == null || skuPrice < minPrice) {
			minPrice = skuPrice;
		}
		totalStock += skuStock;
	});
	return {
		minPrice: minPrice == null ? 0 : minPrice,
		totalStock
	};
}

export function serializeGoodsSpecs(mode, skus, fallbackPrice = 0, fallbackStock = 0) {
	const normalizedMode = mode === SPEC_MODE_MULTI ? SPEC_MODE_MULTI : SPEC_MODE_SINGLE;
	const normalizedSkus = (Array.isArray(skus) ? skus : [])
		.map((item) => normalizeSku(item, fallbackPrice, fallbackStock))
		.filter(Boolean);
	if (normalizedMode === SPEC_MODE_SINGLE) {
		return '';
	}
	return JSON.stringify({ mode: SPEC_MODE_MULTI, skus: normalizedSkus });
}

export function findSkuByName(skus, name) {
	const target = String(name || '').trim();
	if (!target) return null;
	return (Array.isArray(skus) ? skus : []).find((item) => String(item?.name || '').trim() === target) || null;
}

/** 用户端 specs 字段（可能是字符串数组或 SKU 对象数组） */
export function normalizeAppSkus(specs, fallbackPrice = 0, fallbackStock = 0) {
	if (!Array.isArray(specs) || !specs.length) {
		return { mode: SPEC_MODE_SINGLE, skus: [] };
	}
	if (typeof specs[0] === 'string') {
		if (specs.length === 1) {
			return {
				mode: SPEC_MODE_SINGLE,
				skus: [{ name: specs[0], price: fallbackPrice, stock: fallbackStock }]
			};
		}
		return {
			mode: SPEC_MODE_MULTI,
			skus: specs.map((name) => ({
				name: String(name),
				price: fallbackPrice,
				stock: fallbackStock
			}))
		};
	}
	const skus = specs
		.map((item) => normalizeSku(item, fallbackPrice, fallbackStock))
		.filter(Boolean);
	if (skus.length <= 1) {
		return { mode: SPEC_MODE_SINGLE, skus };
	}
	return { mode: SPEC_MODE_MULTI, skus };
}
