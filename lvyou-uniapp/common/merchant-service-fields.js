/**
 * 商户服务编辑页：按入驻类型对齐用户端展示字段
 */

export const SERVICE_EXTRA_FIELDS = {
	hotel: [],
	restaurant: [
		{
			key: 'monthlySales',
			label: '月售数量',
			placeholder: '0',
			required: false,
			type: 'number',
			hint: '用户端招牌菜品将展示月售数量'
		}
	],
	travel: [
		{
			key: 'travelCity',
			label: '旅行城市',
			placeholder: '如：荣昌、重庆',
			required: true,
			type: 'text',
			hint: '用户端线路详情页将展示旅行城市'
		},
		{
			key: 'dayCount',
			label: '行程天数',
			placeholder: '如：1、2、3',
			required: true,
			type: 'number',
			hint: '填写数字天数，用户端将展示为「N天」'
		},
		{
			key: 'meetPoint',
			label: '集合地点',
			placeholder: '如：荣昌区游客中心',
			required: false,
			type: 'text',
			hint: '用户端线路详情页将展示集合地点'
		},
		{
			key: 'spots',
			label: '游览景点',
			placeholder: '景点A、景点B',
			required: true,
			type: 'text',
			hint: '多个景点用顿号分隔，用户端将展示为「景点A · 景点B」'
		}
	],
	culture: [
		{
			key: 'detail',
			label: '好物详情',
			placeholder: '请输入好物详细介绍',
			required: false,
			type: 'textarea',
			hint: '用户端好物详情页「好物详情」区块展示'
		}
	]
};

/** 服务说明字段标签（按入驻类型） */
export const SERVICE_DESC_LABEL = {
	hotel: '房型说明',
	restaurant: '菜品介绍',
	travel: '线路介绍',
	culture: '好物简介'
};

export function getServiceExtraFields(storeType) {
	const key = String(storeType || 'hotel').toLowerCase();
	return SERVICE_EXTRA_FIELDS[key] || SERVICE_EXTRA_FIELDS.hotel;
}

export function getServiceDescLabel(storeType) {
	const key = String(storeType || 'hotel').toLowerCase();
	return SERVICE_DESC_LABEL[key] || '服务说明';
}
