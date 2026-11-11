/**
 * 门店装修页 UI 文案与展示辅助（非 mock 数据）
 */
export const STORE_DECOR_UI = {
	pageTitle: '门店详情装修',
	storeDecorTitle: '门店详情装修',
	hint: '填写内容将同步至用户端详情页；房型/商品请在「服务管理」发布，保存后自动展示在本店详情，无需在此手动关联',
	previewUserPage: '预览效果',
	saveBtn: '保存装修',
	saveSuccess: '装修已保存',
	previewTip: '即将打开用户端预览',
	sectionVisual: '视觉展示',
	sectionBasic: '基础信息',
	sectionContent: '内容详情',
	sectionBusiness: '营业信息',
	sectionShipping: '物流运费',
	sectionShippingDesc: '门店级运费规则，对本店全部文创商品生效',
	sectionPolicy: '酒店政策',
	sectionPolicyDesc: '预订与入住须知，用户端详情页「酒店政策」展示',
	sectionVisualDesc: '封面图用于列表展示，轮播图用于详情页顶部轮播',
	sectionBasicDesc: '标签、联系方式等核心信息',
	sectionContentDesc: '介绍文案与特色说明',
	sectionBusinessDesc: '分时段营业安排',
	uploadBanner: '轮播图',
	uploadCover: '封面图',
	uploadCoverTip: '用于列表卡片、分享缩略图，建议 16:9，1 张',
	uploadBannerTip: '用于详情页门店风采展示，建议 16:9，最多 5 张',
	uploadTip: '建议 16:9，最多 5 张',
	uploadCoverOnlyTip: '用于列表卡片展示，建议 16:9，1 张',
	coverTheme: '主题渐变色',
	tagPlaceholder: '多个标签用顿号分隔',
	tagInputPlaceholder: '如：非遗美食',
	addTag: '添加',
	addTagEmpty: '请输入标签',
	addTagDuplicate: '该标签已添加',
	tagHint: '逐项添加，保存后在用户端详情页展示',
	facilitiesPlaceholder: '如：免费WiFi',
	addFacility: '添加',
	addFacilityEmpty: '请输入设施名称',
	addFacilityDuplicate: '该设施已添加',
	facilitiesPresetLabel: '快捷添加',
	servicesPlaceholder: '文创销售、手作体验、团队预约',
	introPlaceholderHotel: '可选，酒店介绍补充说明',
	introPlaceholderRestaurant: '介绍门店历史、特色菜品、用餐环境',
	introPlaceholderTravel: '介绍门店特色、导游风格、服务承诺等',
	introPlaceholderCulture: '介绍品牌故事、体验项目、门店特色',
	addHour: '添加时段',
	addItinerary: '添加行程节点'
};

/** 用户端预览路径 */
export const USER_PREVIEW_PATH_BY_TYPE = {
	hotel: '/mobile-design/hotel/detail?id=preview',
	restaurant: '/mobile-design/goose/detail?id=preview',
	travel: '/mobile-design/route/store?id=preview',
	culture: '/mobile-design/heritage/shop-detail?id=preview'
};

export function getUserPreviewPath(typeKey) {
	return USER_PREVIEW_PATH_BY_TYPE[typeKey] || USER_PREVIEW_PATH_BY_TYPE.hotel;
}

export function parseCommaList(text) {
	return String(text || '')
		.split(/[,，、]/)
		.map((s) => s.trim())
		.filter(Boolean);
}

export function joinCommaList(list) {
	return (list || []).join('、');
}

/** 酒店设施名称 → 用户端展示图标 */
const FACILITY_ICON_RULES = [
	{ match: /wifi|无线/i, icon: 'wifi' },
	{ match: /停车/i, icon: 'car-fill' },
	{ match: /健身/i, icon: 'grid-fill' },
	{ match: /早/i, icon: 'gift-fill' },
	{ match: /洗衣/i, icon: 'bag-fill' },
	{ match: /泳池|游泳/i, icon: 'star-fill' }
];

export function resolveFacilityIcon(label) {
	const text = String(label || '');
	for (const rule of FACILITY_ICON_RULES) {
		if (rule.match.test(text)) {
			return rule.icon;
		}
	}
	return 'grid-fill';
}

/** 酒店设施快捷选项 */
export const HOTEL_FACILITY_PRESETS = [
	'免费WiFi',
	'免费停车',
	'健身房',
	'含早餐',
	'24小时前台',
	'行李寄存',
	'电梯',
	'洗衣服务',
	'游泳池'
];

export function normalizeFacilityItems(items) {
	return (items || [])
		.map((item, index) => {
			const label = String(typeof item === 'string' ? item : item?.label || '').trim();
			if (!label) return null;
			return {
				id: item?.id || `${Date.now()}-${index}`,
				label,
				icon: resolveFacilityIcon(label)
			};
		})
		.filter(Boolean);
}

export function buildFacilitiesPayload(list) {
	return normalizeFacilityItems(list).map((item, index) => ({
		id: index + 1,
		icon: resolveFacilityIcon(item.label),
		label: item.label
	}));
}

/** 各类型门店装修 ↔ 用户端详情字段说明 */
export const STORE_DECOR_FIELD_GUIDE = {
	hotel: '封面图、轮播图、星级、入住/退房时间、电话、设施、酒店政策 → 酒店详情页；房型请在「服务管理」发布',
	restaurant: '封面图、轮播图、角标、标签、介绍、营业时间 → 餐饮详情页；已预约数由系统根据预约记录自动统计；菜品请在「服务管理」发布',
	travel: '封面、时长、标签、集合点、景点、介绍、行程节点 → 线路详情页；线路请在「服务管理」发布',
	culture: '封面、标签、介绍、特色服务、运费规则 → 好店详情页；商品请在「服务管理」发布'
};

/** 门店装修 JSON 中已废弃的手动关联服务字段 */
export const LEGACY_STORE_DECOR_SERVICE_KEYS = ['linkedServices', 'roomTypes', 'serviceIds', 'linkedRoomIds'];

export const HOTEL_POLICY_DEFAULTS = {
	bookingNotice: '支付成功后订单生效，请按预约日期携带有效证件办理入住。',
	checkInNotice: '前台 24 小时服务，请于入住时在柜台办理登记。',
	depositPolicy: '不收取押金',
	breakfastInfo: ''
};

export function ensureHotelPolicyDefaults(policy) {
	const target = policy && typeof policy === 'object' ? policy : {};
	if (!String(target.bookingNotice || '').trim()) {
		target.bookingNotice = HOTEL_POLICY_DEFAULTS.bookingNotice;
	}
	if (!String(target.checkInNotice || '').trim()) {
		target.checkInNotice = HOTEL_POLICY_DEFAULTS.checkInNotice;
	}
	if (!String(target.depositPolicy || '').trim()) {
		target.depositPolicy = HOTEL_POLICY_DEFAULTS.depositPolicy;
	}
	if (target.breakfastInfo == null) {
		target.breakfastInfo = HOTEL_POLICY_DEFAULTS.breakfastInfo;
	}
	return target;
}

export const HOTEL_DECOR_DEFAULTS = {
	checkInTime: '14:00 后入住',
	checkOutTime: '12:00 前退房',
	starLabel: '四星级',
	starLevel: 4
};

export function stripLegacyStoreDecorServiceKeys(payload) {
	if (!payload || typeof payload !== 'object') {
		return payload;
	}
	const cleaned = { ...payload };
	LEGACY_STORE_DECOR_SERVICE_KEYS.forEach((key) => {
		delete cleaned[key];
	});
	return cleaned;
}

export function ensureHotelDecorDefaults(form) {
	if (!form || typeof form !== 'object') {
		return;
	}
	if (!String(form.checkInTime || '').trim()) {
		form.checkInTime = HOTEL_DECOR_DEFAULTS.checkInTime;
	}
	if (!String(form.checkOutTime || '').trim()) {
		form.checkOutTime = HOTEL_DECOR_DEFAULTS.checkOutTime;
	}
	if (!String(form.starLabel || '').trim()) {
		form.starLabel = HOTEL_DECOR_DEFAULTS.starLabel;
	}
	if (form.starLevel == null || form.starLevel === '') {
		form.starLevel = HOTEL_DECOR_DEFAULTS.starLevel;
	}
}

export function getStoreDecorProfileNote(typeKey) {
	const key = String(typeKey || 'hotel').toLowerCase();
	if (key === 'culture') {
		return '门店名称、地址、营业时间、联系电话来自入驻资料，保存后自动同步至用户端好店详情';
	}
	if (key === 'hotel') {
		return '门店名称、地址来自入驻资料；联系电话可在下方填写或使用入驻手机号';
	}
	return '门店名称、地址、联系电话来自入驻资料，与用户端详情页展示一致';
}
