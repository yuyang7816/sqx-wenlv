/**
 * 商户入驻页静态文案、门店类型与表单校验（非 mock 数据）
 */
import { hasToken } from '@/common/api/auth.js';
import { getUserInfo } from '@/common/api/auth.js';
import { resolvePublicUrl } from '@/common/media-url.js';

export const MERCHANT_STATUS = {
	NONE: 'none',
	PENDING: 'pending',
	APPROVED: 'approved',
	REJECTED: 'rejected'
};

export const STORE_TYPES = [
	{ key: 'hotel', label: '酒店', icon: 'home-fill', color: '#2979FF', bgColor: '#EAF2FF' },
	{ key: 'restaurant', label: '餐饮', icon: 'coupon-fill', color: '#FF6600', bgColor: '#FFF3EB', desc: '仅支持预约，不支持在线购买' },
	{ key: 'travel', label: '旅游', icon: 'map-fill', color: '#2EAF6A', bgColor: '#E8F8EF' },
	{ key: 'culture', label: '非遗文创', icon: 'gift-fill', color: '#B45D2D', bgColor: '#F5EFEB' }
];

const STORE_TYPE_VISUAL = {
	hotel: { icon: 'home-fill', color: '#2979FF', bgColor: '#EAF2FF' },
	restaurant: { icon: 'coupon-fill', color: '#FF6600', bgColor: '#FFF3EB', desc: '仅支持预约，不支持在线购买' },
	travel: { icon: 'map-fill', color: '#2EAF6A', bgColor: '#E8F8EF' },
	culture: { icon: 'gift-fill', color: '#B45D2D', bgColor: '#F5EFEB' }
};

const DEFAULT_STORE_TYPE_VISUAL = {
	icon: 'bag-fill',
	color: '#666666',
	bgColor: '#F5F5F5'
};

let cachedStoreTypes = STORE_TYPES.map((item) => ({ ...item }));

/** 从后台字典/接口构建可选入驻类型（含保证金） */
export function mapStoreTypesFromDictItems(dictItems) {
	if (!Array.isArray(dictItems) || !dictItems.length) {
		return STORE_TYPES.map((item) => ({ ...item }));
	}
	return dictItems.map((item, index) => {
		const visual = STORE_TYPE_VISUAL[item.code] || DEFAULT_STORE_TYPE_VISUAL;
		return {
			key: item.code,
			label: resolveStoreTypeLabel(item.code, item.label || item.name || item.code),
			depositAmount: item.depositAmount != null ? Number(item.depositAmount) : null,
			sortOrder: item.sort ?? index,
			...visual
		};
	});
}

/** 合并后台类型并更新本地缓存 */
export function mergeStoreTypesFromDict(dictItems) {
	const mapped = mapStoreTypesFromDictItems(dictItems);
	cachedStoreTypes = mapped;
	return mapped;
}

export function setCachedStoreTypes(items) {
	cachedStoreTypes = Array.isArray(items) ? items : [];
}

export function getCachedStoreTypes() {
	return cachedStoreTypes;
}

const STORE_TYPE_ALIASES = { goods: 'culture' };

export const QUALIFICATIONS_BY_TYPE = {
	hotel: {
		title: '酒店入驻资质',
		required: [
			{ key: 'license', label: '营业执照', desc: '已在上方上传', required: true },
			{ key: 'legalId', label: '法人身份证', desc: '已在上方上传', required: true },
			{ key: 'storeFront', label: '门店门头照', desc: '含店招完整外观', required: true },
			{ key: 'specialPermit', label: '特种行业许可证', desc: '如有请上传', required: false },
			{ key: 'healthPermit', label: '卫生许可证', desc: '如有请上传', required: false }
		]
	},
	restaurant: {
		title: '餐饮入驻资质',
		note: '餐饮商户仅开放预约服务，不支持在线购买下单',
		required: [
			{ key: 'license', label: '营业执照', desc: '已在上方上传', required: true },
			{ key: 'legalId', label: '法人身份证', desc: '已在上方上传', required: true },
			{ key: 'storeFront', label: '门店门头照', desc: '含店招完整外观', required: true },
			{ key: 'foodPermit', label: '食品经营许可证', desc: '如有请上传', required: false },
			{ key: 'diningEnv', label: '就餐环境照片', desc: '可上传多张，展示就餐区域', required: false, multiple: true, minCount: 0, maxCount: 6 }
		]
	},
	culture: {
		title: '非遗文创入驻资质',
		required: [
			{ key: 'license', label: '营业执照', desc: '已在上方上传', required: true },
			{ key: 'legalId', label: '法人身份证', desc: '已在上方上传', required: true },
			{ key: 'storeFront', label: '门店/网店展示照', desc: '实体门店或线上店铺', required: true },
			{ key: 'productSample', label: '产品样品照片', desc: '可上传多张，展示主营产品', required: false, multiple: true, minCount: 0, maxCount: 9 }
		]
	},
	travel: {
		title: '旅游入驻资质',
		required: [
			{ key: 'license', label: '营业执照', desc: '已在上方上传', required: true },
			{ key: 'legalId', label: '法人身份证', desc: '已在上方上传', required: true },
			{ key: 'officePhoto', label: '经营场所照片', desc: '可上传多张，展示门店或办公环境', required: true, multiple: true, minCount: 1, maxCount: 6 },
			{ key: 'travelPermit', label: '旅行社经营许可证', desc: '如有请上传', required: false }
		]
	}
};

export const MERCHANT_UI = {
	entryApply: '商户入驻',
	entryCenter: '商户中心',
	entryPending: '入驻审核中',
	applyTitle: '商户入驻',
	resultTitle: '提交结果',
	contactSection: '联系人信息',
	contactName: '商户联系人',
	contactPhone: '联系方式',
	storeSection: '门店信息',
	storeName: '门店名称',
	storeRegion: '所在地区',
	storeAddressDetail: '详细地址',
	storeAddress: '门店地址',
	storeLocation: '门店位置',
	pickOnMap: '点击选择门店位置',
	repickOnMap: '重新选择门店位置',
	locationPlaceholder: '点击在地图上选择门店位置',
	locationRequired: '请选择门店位置',
	storeHours: '营业时间',
	storeType: '门店类型',
	shopCategory: '好店分类',
	shopCategoryRequired: '请选择好店分类',
	shopCategoryHint: '与平台「非遗好店」Tab 分类一致，审核通过后店铺将归入该分类',
	shopCategoryPlaceholder: '请选择好店分类',
	licenseSection: '营业执照',
	licenseNo: '统一社会信用代码',
	licenseImage: '营业执照照片',
	legalSection: '法人信息',
	legalName: '法人姓名',
	legalIdNo: '法人身份证号',
	legalIdFront: '身份证人像面',
	legalIdBack: '身份证国徽面',
	qualSection: '行业资质',
	submitApply: '提交入驻申请',
	loginTip: '请先登录',
	contactNameRequired: '请填写商户联系人',
	contactPhoneRequired: '请填写联系方式',
	storeNameRequired: '请填写门店名称',
	storeRegionRequired: '请选择所在地区',
	storeAddressDetailRequired: '请填写详细地址',
	storeTypeRequired: '请选择门店类型',
	licenseNoRequired: '请填写统一社会信用代码',
	licenseImageRequired: '请上传营业执照',
	legalNameRequired: '请填写法人姓名',
	legalIdNoRequired: '请填写法人身份证号',
	legalIdFrontRequired: '请上传身份证人像面',
	legalIdBackRequired: '请上传身份证国徽面',
	qualRequired: '请上传',
	submitSuccess: '入驻申请已提交',
	submitFail: '提交失败，请稍后重试',
	uploadFail: '图片上传失败',
	pendingDesc: '您的入驻申请已提交，平台将在 1-3 个工作日内完成审核，请耐心等待。',
	approvedDesc: '恭喜，您的商户入驻已通过审核，可进入商户中心经营管理。',
	rejectedDesc: '很抱歉，您的入驻申请未通过审核，请根据驳回原因修改资料后重新提交。',
	rejectedReasonTitle: '驳回原因',
	rejectedBannerTitle: '审核未通过',
	rejectedBannerHint: '已保留您上次提交的资料，请修改后重新提交',
	backMy: '返回我的',
	goCenter: '进入商户中心',
	reapply: '修改资料并重新提交',
	resubmitApply: '重新提交申请',
	viewQualification: '查看资质要求',
	uploadImage: '上传图片'
};

export function normalizeStoreType(key) {
	return STORE_TYPE_ALIASES[key] || key;
}

/** 字典/接口返回的中文名乱码（常见于 sys_dict.name 编码错误） */
export function isGarbledLabel(text) {
	const value = String(text || '').trim();
	if (!value) return true;
	return /^[?？]+$/.test(value);
}

/** 常见繁体字形，用于识别错误编码的字典名称 */
const TRADITIONAL_HINT = /[禮飲臺國際資訊網絡聯繫設開關顯選確認刪編輯創發評論點擊載獲處響驗註錄頭暱稱關絲動態歷訂單優會員權幫隱對於為與無這間價觀預約詳細圖視頻轉贊復還來說現內從後進過見實應該將帶讓變當雖難樣類標籤識語檔導專業數據調試執務環機構組織維護檢測帳戶瀏覽頁碼總條麵須備僅陸陽陰額餘]/;

export function containsTraditionalHint(text) {
	const value = String(text || '').trim();
	return !!value && TRADITIONAL_HINT.test(value);
}

function fallbackStoreTypeLabel(code) {
	const normalized = normalizeStoreType(code);
	const preset = STORE_TYPES.find((item) => item.key === normalized);
	return preset?.label || normalized || '';
}

/** 优先使用接口 label，乱码或繁体时回退本地预设 */
export function resolveStoreTypeLabel(storeType, labelFromApi) {
	if (!isGarbledLabel(labelFromApi) && !containsTraditionalHint(labelFromApi)) {
		return String(labelFromApi || '').trim() || fallbackStoreTypeLabel(storeType);
	}
	return fallbackStoreTypeLabel(storeType);
}

export function getStoreTypeMeta(key) {
	const normalized = normalizeStoreType(key);
	const fromCache = cachedStoreTypes.find((item) => item.key === normalized);
	const preset = STORE_TYPES.find((item) => item.key === normalized);
	if (fromCache) {
		return {
			...fromCache,
			label: resolveStoreTypeLabel(normalized, fromCache.label)
		};
	}
	if (preset) return { ...preset };
	return cachedStoreTypes[0] || STORE_TYPES[0];
}

export function formatDepositAmount(amount) {
	const num = Number(amount);
	if (!Number.isFinite(num)) return '--';
	return num.toFixed(2);
}

export function getQualificationsByType(typeKey) {
	const normalized = normalizeStoreType(typeKey);
	return QUALIFICATIONS_BY_TYPE[normalized] || QUALIFICATIONS_BY_TYPE.hotel;
}

export function isQualMultiItem(item) {
	return Boolean(item?.multiple);
}

export function getQualMaxCount(item) {
	if (!item) return 1;
	return isQualMultiItem(item) ? Number(item.maxCount || 9) : 1;
}

export function getQualMinCount(item) {
	if (!item) return 1;
	if (!isQualMultiItem(item)) return item.required ? 1 : 0;
	return Number(item.minCount ?? (item.required ? 1 : 0));
}

/** 解析资质图片 URL 列表（兼容单 URL、JSON 数组字符串、数组） */
export function parseQualImageUrls(raw) {
	if (!raw) return [];
	if (Array.isArray(raw)) return raw.map((url) => String(url || '').trim()).filter(Boolean);
	const text = String(raw).trim();
	if (!text) return [];
	if (text.startsWith('[')) {
		try {
			const parsed = JSON.parse(text);
			return Array.isArray(parsed) ? parsed.map((url) => String(url || '').trim()).filter(Boolean) : [text];
		} catch (e) {
			return [text];
		}
	}
	return [text];
}

export function getQualImageList(qualImages, key) {
	return parseQualImageUrls(qualImages?.[key]);
}

export function findQualItem(storeType, key) {
	return getQualificationsByType(storeType).required.find((item) => item.key === key) || null;
}

export function isQualItemFilled(qualImages, item) {
	const count = getQualImageList(qualImages, item.key).length;
	const minCount = getQualMinCount(item);
	if (item.required) return count >= minCount;
	return count >= minCount;
}

/** 表单回填：多图项转为数组，单图项转为字符串 */
export function normalizeQualImagesForForm(qualImages, storeType) {
	const result = {};
	Object.entries(qualImages || {}).forEach(([key, value]) => {
		const item = findQualItem(storeType, key);
		const urls = parseQualImageUrls(value).map((url) => resolvePublicUrl(url)).filter(Boolean);
		if (!urls.length) return;
		result[key] = item && isQualMultiItem(item) ? urls : urls[0];
	});
	return result;
}

/** 提交接口：多图项序列化为 JSON 数组字符串 */
export function serializeQualImagesForSubmit(qualImages, storeType) {
	const result = {};
	Object.entries(qualImages || {}).forEach(([key, value]) => {
		const item = findQualItem(storeType, key);
		const urls = parseQualImageUrls(value).filter(Boolean);
		if (!urls.length) return;
		if (item && isQualMultiItem(item)) {
			result[key] = JSON.stringify(urls);
		} else {
			result[key] = urls[0];
		}
	});
	return result;
}

export function formatStoreAddress(form) {
	const region = String(form?.storeRegion || '').trim();
	const detail = String(form?.storeAddressDetail || '').trim();
	if (region || detail) return `${region}${detail}`;
	return String(form?.storeAddress || '').trim();
}

export function getEmptyApplyForm() {
	return {
		contactName: '',
		contactPhone: '',
		storeName: '',
		storeRegion: '',
		storeAddressDetail: '',
		storeAddress: '',
		latitude: null,
		longitude: null,
		storeHours: '09:00-21:00',
		storeType: '',
		shopCategory: '',
		shopCategoryLabel: '',
		licenseNo: '',
		licenseImage: '',
		legalName: '',
		legalIdNo: '',
		legalIdFront: '',
		legalIdBack: '',
		qualImages: {}
	};
}

export function validateApplyForm(form) {
	if (!String(form.contactName || '').trim()) return { ok: false, message: MERCHANT_UI.contactNameRequired };
	if (!String(form.contactPhone || '').trim()) return { ok: false, message: MERCHANT_UI.contactPhoneRequired };
	if (!String(form.storeName || '').trim()) return { ok: false, message: MERCHANT_UI.storeNameRequired };
	if (!String(form.storeRegion || '').trim()) return { ok: false, message: MERCHANT_UI.storeRegionRequired };
	if (!String(form.storeAddressDetail || '').trim()) return { ok: false, message: MERCHANT_UI.storeAddressDetailRequired };
	if (form.latitude == null || form.longitude == null) {
		return { ok: false, message: MERCHANT_UI.locationRequired };
	}
	if (!form.storeType) return { ok: false, message: MERCHANT_UI.storeTypeRequired };
	if (form.storeType === 'culture' && !form.shopCategory) {
		return { ok: false, message: MERCHANT_UI.shopCategoryRequired };
	}
	if (!String(form.licenseNo || '').trim()) return { ok: false, message: MERCHANT_UI.licenseNoRequired };
	if (!form.licenseImage) return { ok: false, message: MERCHANT_UI.licenseImageRequired };
	if (!String(form.legalName || '').trim()) return { ok: false, message: MERCHANT_UI.legalNameRequired };
	if (!String(form.legalIdNo || '').trim()) return { ok: false, message: MERCHANT_UI.legalIdNoRequired };
	if (!form.legalIdFront) return { ok: false, message: MERCHANT_UI.legalIdFrontRequired };
	if (!form.legalIdBack) return { ok: false, message: MERCHANT_UI.legalIdBackRequired };

	const quals = getQualificationsByType(form.storeType).required.filter((item) => item.required);
	for (const item of quals) {
		if (['license', 'legalId'].includes(item.key)) continue;
		if (!isQualItemFilled(form.qualImages, item)) {
			return { ok: false, message: `${MERCHANT_UI.qualRequired}${item.label}` };
		}
	}
	return { ok: true };
}

/** 将入驻状态接口返回的数据回填到申请表单（驳回后修改重提） */
export function mapStatusToApplyForm(statusData) {
	const form = getEmptyApplyForm();
	if (!statusData) return form;

	form.storeName = statusData.storeName || '';
	form.storeType = normalizeStoreType(statusData.storeType || '');

	const detail = statusData.applyDetail || {};
	form.shopCategory = detail.shopCategory || '';
	form.shopCategoryLabel = detail.shopCategoryLabel || '';
	form.contactName = detail.contactName || '';
	form.contactPhone = detail.contactPhone || '';
	form.storeRegion = detail.storeRegion || '';
	form.storeAddressDetail = detail.storeAddressDetail || '';
	form.storeAddress = detail.storeAddress || formatStoreAddress({
		storeRegion: form.storeRegion,
		storeAddressDetail: form.storeAddressDetail
	});
	form.latitude = detail.latitude ?? null;
	form.longitude = detail.longitude ?? null;
	form.storeHours = detail.storeHours || form.storeHours;
	form.licenseNo = detail.licenseNo || '';
	form.licenseImage = resolvePublicUrl(detail.licenseImage);
	form.legalName = detail.legalName || '';
	form.legalIdNo = detail.legalIdNo || '';
	form.legalIdFront = resolvePublicUrl(detail.legalIdFront);
	form.legalIdBack = resolvePublicUrl(detail.legalIdBack);
	form.qualImages = normalizeQualImagesForForm(detail.qualImages || {}, form.storeType);
	return form;
}

/** 从登录用户信息预填联系人（需已登录） */
export async function prefillContactFromUser(form) {
	if (!hasToken()) return form;
	const next = { ...form };
	try {
		const res = await getUserInfo();
		if (res.code === 0 && res.data) {
			const user = res.data;
			next.contactName = next.contactName || user.nickname || user.userName || user.name || '';
			next.contactPhone = next.contactPhone || user.phone || user.phoneNo || '';
		}
	} catch (e) { /* ignore */ }
	if (!next.contactPhone) {
		try {
			next.contactPhone = uni.getStorageSync('phone') || '';
		} catch (e) { /* ignore */ }
	}
	return next;
}
