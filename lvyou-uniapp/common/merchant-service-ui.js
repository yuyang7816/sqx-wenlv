/**
 * 商户服务页文案与类型元数据（非 mock 数据）
 */

export const MERCHANT_SERVICE_UI = {
	navTitle: '商户服务',
	badge: '商户自营 · 平台同步',
	regionSuffix: '文旅商户',
	sectionFlow: '服务流程',
	sectionScope: '可咨询事项',
	scopeNotice: '商户自行管理预约、库存与履约。提交后平台记录服务意向并同步至商户，具体规则以商户说明为准。',
	sectionForm: '提交服务意向',
	labelIntent: '咨询类型',
	labelContact: '联系人',
	labelPhone: '手机号',
	labelCheckInDate: '期望入住日期',
	labelGuestCount: '入住人数',
	labelRemark: '补充说明',
	placeholderContact: '请输入联系人姓名',
	placeholderPhone: '请输入11位手机号',
	placeholderDate: '请选择日期',
	placeholderRemark: '选填，如房型偏好、到店时间等',
	trustPrivacy: '信息仅同步至对应商户',
	trustResponse: '商户通常在 2 小时内响应',
	barLabel: '提交至',
	submitBtn: '提交意向',
	toastContact: '请输入联系人',
	toastPhone: '请输入正确手机号',
	toastDate: '请选择期望入住日期',
	toastSuccess: '已提交，等待商户确认',
	loadingText: '加载中...',
	emptyFallback: '暂无商户服务信息',
	loginTip: '请先登录',
	loadMerchantsFail: '商户加载失败',
	merchantNotFound: '未找到该商户',
	noMerchantAvailable: '当前暂无可用商户',
	networkError: '网络异常，请稍后重试',
	merchantUnavailable: '商户信息不可用',
	submittingText: '提交中...',
	submitFail: '提交失败',
	statusPending: '商户确认中',
	statusProcessing: '商户处理中',
	sourceMerchant: '商户服务',
	serviceSuffix: '服务'
};

export const MERCHANT_FLOW_STEPS = [
	{ label: '提交意向', desc: '填写需求' },
	{ label: '商户确认', desc: '2h 内响应' },
	{ label: '完成履约', desc: '到店/入住' }
];

export const MERCHANT_SERVICE_META = {
	hotel: {
		moduleKey: 'hotel',
		icon: 'home-fill',
		tag: '酒店住宿',
		highlights: ['房型查询', '入住改期', '团队预订'],
		intents: ['预订咨询', '改期申请', '其他事项']
	},
	food: {
		moduleKey: 'goose',
		icon: 'gift-fill',
		tag: '餐饮到店',
		highlights: ['堂食预约', '包厢预留', '外带咨询'],
		intents: ['到店预约', '包厢咨询', '其他事项']
	},
	heritage: {
		moduleKey: 'heritage',
		icon: 'star-fill',
		tag: '非遗体验',
		highlights: ['手作体验', '团体定制', '文创选购'],
		intents: ['体验预约', '团体咨询', '其他事项']
	},
	default: {
		moduleKey: 'default',
		icon: 'shop-fill',
		tag: '商户服务',
		highlights: ['业务咨询', '预约服务', '售后沟通'],
		intents: ['服务咨询', '预约申请', '其他事项']
	}
};

export function getMerchantTypeKey(merchant) {
	const typeKey = merchant?.typeKey;
	if (typeKey) {
		if (typeKey === 'hotel') return 'hotel';
		if (typeKey === 'restaurant' || typeKey === 'goose') return 'food';
		if (typeKey === 'culture') return 'heritage';
		return 'default';
	}
	const id = String(merchant?.id || '');
	if (id.startsWith('hotel')) return 'hotel';
	if (id.startsWith('goose')) return 'food';
	if (id.startsWith('heritage')) return 'heritage';
	return 'default';
}

export function getMerchantMeta(merchant) {
	return MERCHANT_SERVICE_META[getMerchantTypeKey(merchant)] || MERCHANT_SERVICE_META.default;
}

export function isHotelMerchant(merchant) {
	return getMerchantTypeKey(merchant) === 'hotel';
}
