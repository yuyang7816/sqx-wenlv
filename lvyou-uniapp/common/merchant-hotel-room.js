/**
 * 商户酒店房型扩展字段
 */
import { resolveFacilityIcon, HOTEL_FACILITY_PRESETS, normalizeFacilityItems } from '@/common/merchant-store-decor.js';

export const HOTEL_ROOM_UI = {
	galleryTitle: '房型相册',
	galleryHint: '建议上传 3–9 张实景图，用户端房型详情页轮播展示',
	galleryAdd: '添加图片',
	paramsTitle: '房型参数',
	bedTitle: '床型信息',
	petPolicyTitle: '宠物政策',
	petPolicyDesc: '部分酒店提供宠物友好房型，可按房型单独设置',
	petPolicyLabel: '宠物政策',
	petPolicyPlaceholder: '请选择',
	facilitiesTitle: '房间设施',
	facilitiesFromStandardLabel: '来自客房标准',
	facilitiesOwnLabel: '本房型新增',
	tagsTitle: '卖点标签',
	tagsHint: '如：近景区、延迟退房，用户端房型标题旁展示',
	tagsFromStandardLabel: '来自客房标准',
	tagsOwnLabel: '本房型新增',
	addTag: '添加',
	addTagEmpty: '请输入标签',
	addTagDuplicate: '该标签已添加',
	windowLabel: '窗户',
	smokingLabel: '吸烟政策',
	areaLabel: '面积 (㎡)',
	floorLabel: '楼层',
	occupancyLabel: '可住人数',
	bedTypeLabel: '床型',
	bedCountLabel: '床数量',
	bedSizeLabel: '床宽 (米)',
	extraBedLabel: '加床政策',
	extraBedPlaceholder: '如：不可加床'
};

export const HOTEL_BED_TYPE_OPTIONS = ['大床', '双床', '单人床', '特大床'];
export const HOTEL_WINDOW_OPTIONS = ['有窗', '无窗', '部分有窗'];
export const HOTEL_SMOKING_OPTIONS = ['禁烟', '可吸烟', '部分禁烟'];
export const HOTEL_PET_POLICY_OPTIONS = ['不可携带宠物', '可携带宠物', '宠物友好'];

export const HOTEL_ROOM_TAG_PRESETS = ['免费停车', '近景区', '延迟退房', '安心消毒'];

export function emptyRoomDetail() {
	return {
		gallery: [],
		area: '',
		floor: '',
		occupancy: '',
		bedType: '',
		bedCount: '',
		bedSize: '',
		extraBedPolicy: '',
		petPolicy: '',
		windowType: '',
		smokingPolicy: '',
		facilities: [],
		tags: []
	};
}

/** 是否为未填写过的完整房型数据（emptyRoomDetail 初始态，不应视为覆盖标准） */
export function isUninitializedFullDetail(raw) {
	if (!raw || typeof raw !== 'object') return true;
	if (raw.inheritStandard) return false;
	const hasGallery = Array.isArray(raw.gallery) && raw.gallery.length > 0;
	const hasFacilities = Array.isArray(raw.facilities) && raw.facilities.length > 0;
	const hasTags = Array.isArray(raw.tags) && raw.tags.length > 0;
	const hasScalars = Boolean(
		raw.area || raw.floor || raw.bedType || raw.bedCount || raw.bedSize
			|| raw.extraBedPolicy || raw.occupancy || raw.petPolicy
			|| raw.windowType || raw.smokingPolicy
	);
	const hasExcluded = Boolean(
		(Array.isArray(raw.excludedTags) && raw.excludedTags.length)
		|| (Array.isArray(raw.excludedFacilities) && raw.excludedFacilities.length)
	);
	return !hasGallery && !hasFacilities && !hasTags && !hasScalars && !hasExcluded;
}

/** 继承标准模式下的房型差异项（未覆盖字段保持 undefined，避免与标准默认值混淆） */
export function emptyRoomOverride() {
	return {
		gallery: [],
		area: '',
		floor: '',
		occupancy: '',
		bedType: '',
		bedCount: '',
		bedSize: '',
		extraBedPolicy: '',
		windowType: '',
		smokingPolicy: '',
		petPolicy: '',
		facilities: [],
		tags: [],
		excludedTags: [],
		excludedFacilities: []
	};
}

export function normalizeRoomOverride(raw) {
	const base = emptyRoomOverride();
	if (!raw || typeof raw !== 'object') {
		return base;
	}
	return {
		...base,
		gallery: Array.isArray(raw.gallery) ? [...raw.gallery].filter(Boolean) : [],
		area: String(raw.area ?? '').trim(),
		floor: String(raw.floor ?? '').trim(),
		occupancy: raw.occupancy == null || raw.occupancy === '' ? '' : String(raw.occupancy),
		bedType: String(raw.bedType ?? '').trim(),
		bedCount: raw.bedCount == null || raw.bedCount === '' ? '' : String(raw.bedCount),
		bedSize: String(raw.bedSize ?? '').trim(),
		extraBedPolicy: String(raw.extraBedPolicy ?? '').trim(),
		windowType: String(raw.windowType ?? '').trim(),
		smokingPolicy: String(raw.smokingPolicy ?? '').trim(),
		petPolicy: String(raw.petPolicy ?? '').trim(),
		facilities: normalizeFacilityItems(raw.facilities || []),
		tags: Array.isArray(raw.tags) ? raw.tags.map((t) => String(t).trim()).filter(Boolean) : [],
		excludedTags: Array.isArray(raw.excludedTags)
			? raw.excludedTags.map((t) => String(t).trim()).filter(Boolean)
			: [],
		excludedFacilities: Array.isArray(raw.excludedFacilities)
			? raw.excludedFacilities.map((t) => String(t).trim()).filter(Boolean)
			: []
	};
}

function mergeFacilityLists(base = [], extra = []) {
	const seen = new Set();
	const result = [];
	for (const item of [...base, ...extra]) {
		const label = String(item?.label || '').trim();
		if (!label || seen.has(label)) continue;
		seen.add(label);
		result.push({
			id: item.id ?? `${result.length + 1}`,
			label,
			icon: item.icon || resolveFacilityIcon(label)
		});
	}
	return result;
}

export function normalizeRoomDetail(raw) {
	const base = emptyRoomDetail();
	if (!raw || typeof raw !== 'object') {
		return base;
	}
	return {
		...base,
		gallery: Array.isArray(raw.gallery) ? [...raw.gallery].filter(Boolean) : [],
		area: String(raw.area ?? '').trim(),
		floor: String(raw.floor ?? '').trim(),
		occupancy: raw.occupancy == null || raw.occupancy === '' ? '' : String(raw.occupancy),
		bedType: String(raw.bedType ?? '').trim(),
		bedCount: raw.bedCount == null || raw.bedCount === '' ? '' : String(raw.bedCount),
		bedSize: String(raw.bedSize ?? '').trim(),
		extraBedPolicy: String(raw.extraBedPolicy ?? '').trim(),
		petPolicy: String(raw.petPolicy ?? '').trim(),
		windowType: String(raw.windowType ?? '').trim(),
		smokingPolicy: String(raw.smokingPolicy ?? '').trim(),
		facilities: normalizeFacilityItems(raw.facilities || []),
		tags: Array.isArray(raw.tags) ? raw.tags.map((t) => String(t).trim()).filter(Boolean) : []
	};
}

export function emptyRoomStandard() {
	return {
		id: '',
		name: '',
		floorHint: '',
		gallery: [],
		windowType: '',
		smokingPolicy: '',
		petPolicy: '',
		facilities: [],
		tags: []
	};
}

export function standardToRoomDetail(standard) {
	if (!standard || typeof standard !== 'object') {
		return emptyRoomDetail();
	}
	const defaults = standard.defaults || {};
	return normalizeRoomDetail({
		gallery: standard.gallery || [],
		floor: standard.floorHint || '',
		windowType: defaults.windowType || standard.windowType || '',
		smokingPolicy: defaults.smokingPolicy || standard.smokingPolicy || '',
		petPolicy: defaults.petPolicy || standard.petPolicy || '',
		facilities: standard.facilities || [],
		tags: standard.tags || []
	});
}

function mergeStringLists(base = [], extra = []) {
	const seen = new Set();
	const result = [];
	for (const item of [...base, ...extra]) {
		const normalized = String(item || '').trim();
		if (normalized && !seen.has(normalized)) {
			seen.add(normalized);
			result.push(normalized);
		}
	}
	return result;
}

function filterExcludedStringList(items = [], excluded = []) {
	const excludedSet = new Set(
		(Array.isArray(excluded) ? excluded : [])
			.map((item) => String(item || '').trim())
			.filter(Boolean)
	);
	if (!excludedSet.size) {
		return items;
	}
	return items.filter((item) => !excludedSet.has(String(item || '').trim()));
}

function filterExcludedFacilities(items = [], excluded = []) {
	const excludedSet = new Set(
		(Array.isArray(excluded) ? excluded : [])
			.map((item) => String(item || '').trim())
			.filter(Boolean)
	);
	if (!excludedSet.size) {
		return items;
	}
	return items.filter((item) => !excludedSet.has(String(item?.label || '').trim()));
}

/** 获取客房标准中的标签列表 */
export function getStandardTags(standard) {
	return standardToRoomDetail(standard).tags;
}

/** 获取客房标准中的设施列表 */
export function getStandardFacilities(standard) {
	return standardToRoomDetail(standard).facilities;
}

/** 将合并后的标签拆分为「继承自标准」与「本房型新增」 */
export function splitTagsByStandard(allTags, standardTags = []) {
	const stdSet = new Set(
		(Array.isArray(standardTags) ? standardTags : [])
			.map((item) => String(item || '').trim())
			.filter(Boolean)
	);
	const fromStandard = [];
	const own = [];
	for (const tag of Array.isArray(allTags) ? allTags : []) {
		const normalized = String(tag || '').trim();
		if (!normalized) continue;
		if (stdSet.has(normalized)) {
			fromStandard.push(normalized);
		} else {
			own.push(normalized);
		}
	}
	return { fromStandard, own };
}

/** 将合并后的设施拆分为「继承自标准」与「本房型新增」 */
export function splitFacilitiesByStandard(allFacilities, standardFacilities = []) {
	const stdSet = new Set(
		(Array.isArray(standardFacilities) ? standardFacilities : [])
			.map((item) => String(item?.label || item || '').trim())
			.filter(Boolean)
	);
	const fromStandard = [];
	const own = [];
	for (const item of Array.isArray(allFacilities) ? allFacilities : []) {
		const label = String(item?.label || '').trim();
		if (!label) continue;
		if (stdSet.has(label)) {
			fromStandard.push(item);
		} else {
			own.push(item);
		}
	}
	return { fromStandard, own };
}

/** 将客房标准与房型差异项合并为编辑页展示数据 */
export function mergeRoomDetailWithStandard(roomDetail, standard) {
	const std = standardToRoomDetail(standard);
	const room = normalizeRoomOverride(roomDetail);
	return normalizeRoomDetail({
		gallery: mergeStringLists(std.gallery, room.gallery),
		tags: filterExcludedStringList(mergeStringLists(std.tags, room.tags), room.excludedTags),
		facilities: filterExcludedFacilities(
			mergeFacilityLists(std.facilities, room.facilities),
			room.excludedFacilities
		),
		floor: room.floor || std.floor,
		windowType: room.windowType || std.windowType,
		smokingPolicy: room.smokingPolicy || std.smokingPolicy,
		petPolicy: room.petPolicy || std.petPolicy,
		area: room.area,
		occupancy: room.occupancy,
		bedType: room.bedType,
		bedCount: room.bedCount,
		bedSize: room.bedSize,
		extraBedPolicy: room.extraBedPolicy
	});
}

/** 从合并展示数据中提取继承模式下的房型差异项（与后端 normalizeRoomOverrideDetail 对齐） */
export function extractRoomOverridesFromStandard(effectiveDetail, standard) {
	const std = standardToRoomDetail(standard);
	const effective = normalizeRoomDetail(effectiveDetail);
	const stdGallery = new Set(std.gallery);
	const stdTags = new Set(std.tags);
	const stdFacilityLabels = new Set(std.facilities.map((item) => item.label));
	const effectiveTagSet = new Set(effective.tags);
	const effectiveFacilityLabels = new Set(effective.facilities.map((item) => item.label));
	const override = {
		gallery: effective.gallery.filter((item) => !stdGallery.has(item)),
		tags: effective.tags.filter((item) => !stdTags.has(item)),
		facilities: effective.facilities.filter((item) => !stdFacilityLabels.has(item.label)),
		excludedTags: std.tags.filter((item) => !effectiveTagSet.has(item)),
		excludedFacilities: std.facilities
			.map((item) => item.label)
			.filter((label) => !effectiveFacilityLabels.has(label)),
		area: effective.area,
		floor: effective.floor,
		occupancy: effective.occupancy,
		bedType: effective.bedType,
		bedCount: effective.bedCount,
		bedSize: effective.bedSize,
		extraBedPolicy: effective.extraBedPolicy,
		windowType: effective.windowType && effective.windowType !== std.windowType ? effective.windowType : '',
		smokingPolicy: effective.smokingPolicy && effective.smokingPolicy !== std.smokingPolicy ? effective.smokingPolicy : '',
		petPolicy: effective.petPolicy && effective.petPolicy !== std.petPolicy ? effective.petPolicy : ''
	};
	return normalizeRoomOverride(override);
}

export function buildRoomStandardPayload(form) {
	const detail = normalizeRoomDetail(form);
	return {
		id: form.id || undefined,
		name: String(form.name || '').trim(),
		floorHint: String(detail.floor || form.floorHint || '').trim() || undefined,
		gallery: detail.gallery,
		tags: detail.tags,
		facilities: detail.facilities.map((item, index) => ({
			id: index + 1,
			icon: resolveFacilityIcon(item.label),
			label: item.label
		})),
		defaults: {
			windowType: detail.windowType,
			smokingPolicy: detail.smokingPolicy,
			petPolicy: detail.petPolicy
		},
		sort: form.sort == null || form.sort === '' ? 0 : Number(form.sort),
		status: form.status == null || form.status === '' ? 1 : Number(form.status)
	};
}

export function buildRoomDetailPayload(roomDetail, options = {}) {
	const normalized = options.inheritStandard
		? normalizeRoomOverride(roomDetail)
		: normalizeRoomDetail(roomDetail);
	const payload = {
		inheritStandard: Boolean(options.inheritStandard),
		gallery: normalized.gallery,
		area: normalized.area,
		floor: normalized.floor,
		occupancy: normalized.occupancy ? Number(normalized.occupancy) : null,
		bedType: normalized.bedType,
		bedCount: normalized.bedCount ? Number(normalized.bedCount) : null,
		bedSize: normalized.bedSize,
		extraBedPolicy: normalized.extraBedPolicy,
		windowType: normalized.windowType,
		smokingPolicy: normalized.smokingPolicy,
		petPolicy: normalized.petPolicy,
		facilities: normalized.facilities.map((item, index) => ({
			id: index + 1,
			icon: resolveFacilityIcon(item.label),
			label: item.label
		})),
		tags: normalized.tags,
		excludedTags: normalized.excludedTags,
		excludedFacilities: normalized.excludedFacilities
	};
	if (options.inheritStandard) {
		if (!payload.windowType) delete payload.windowType;
		if (!payload.smokingPolicy) delete payload.smokingPolicy;
		if (!payload.petPolicy) delete payload.petPolicy;
		if (!payload.excludedTags?.length) delete payload.excludedTags;
		if (!payload.excludedFacilities?.length) delete payload.excludedFacilities;
	} else {
		delete payload.excludedTags;
		delete payload.excludedFacilities;
	}
	return payload;
}

export { HOTEL_FACILITY_PRESETS, resolveFacilityIcon };
