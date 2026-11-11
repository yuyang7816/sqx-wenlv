/** 地图服务 UI 文案 */
export const MAP_UI = {
	title: '地图服务',
	searchPlaceholder: '搜索景点、美食、停车场…',
	emptyText: '当前分类暂无相关地点',
	densityOn: '已开启人流密度图层',
	densityOff: '已关闭人流密度图层',
	locating: '正在定位…',
	locateFail: '定位失败，请检查权限',
	navigate: '导航前往',
	viewDetail: '查看详情',
	distanceSuffix: 'km',
	densityLabel: '人流密度',
	locateLabel: '我的位置',
	spacesLabel: '余位',
	totalLabel: '总车位'
};

/** 地图 POI 分类（顶部 Tab） */
export const MAP_CATEGORIES = [
	{ key: 'navigation', label: '地图导航', color: '#00BCD4', apiType: 'scenic' },
	{ key: 'food', label: '美食地图', color: '#FF6B8A', apiType: 'food' },
	{ key: 'parking', label: '停车通行', color: '#FFB800', apiType: 'parking' }
];

/** 荣昌区地图默认中心 */
export const MAP_DEFAULT_CENTER = {
	latitude: 29.405,
	longitude: 105.591
};

/** 入口 type 别名 → 分类 key */
const TYPE_ALIAS = {
	navigation: 'navigation',
	scenic: 'navigation',
	map: 'navigation',
	hotel: 'navigation',
	food: 'food',
	'food-map': 'food',
	goose: 'food',
	restaurant: 'food',
	parking: 'parking',
	transport: 'parking'
};

export function resolveMapCategory(type) {
	const key = TYPE_ALIAS[String(type || '').trim()] || 'navigation';
	return MAP_CATEGORIES.some((item) => item.key === key) ? key : 'navigation';
}

export function getMapCategoryMeta(categoryKey) {
	return MAP_CATEGORIES.find((item) => item.key === categoryKey) || MAP_CATEGORIES[0];
}

/** Tab 分类 key → 服务端 type 参数 */
export function categoryKeyToApiType(categoryKey) {
	return getMapCategoryMeta(categoryKey).apiType;
}

/** 距离（公里）格式化为展示文案 */
export function formatMapDistance(distanceKm) {
	if (distanceKm == null || Number.isNaN(Number(distanceKm))) {
		return '';
	}
	const km = Number(distanceKm);
	if (km < 1) {
		return `${Math.max(1, Math.round(km * 1000))}m`;
	}
	return `${km.toFixed(1)}km`;
}

/** Tab 分类 → 腾讯地图搜索关键字 */
export function categoryKeyToSearchKeyword(categoryKey) {
	return getCategorySearchConfig(categoryKey).keyword;
}

/**
 * Tab 分类 → 腾讯地点搜索参数
 * filter 使用官方分类，减少关键词模糊匹配带来的错点
 */
export function getCategorySearchConfig(categoryKey) {
	const configs = {
		navigation: {
			keyword: '景点',
			filter: 'category=旅游景点',
			mode: 'search',
			nameHint: /景点|景区|公园|博物馆|古迹|风景|旅游/
		},
		food: {
			keyword: '美食',
			// 用 search + 分类过滤；explore 容易返回无关热点
			filter: 'category=美食',
			mode: 'search',
			nameHint: /美食|餐|馆|火锅|面|小吃|咖啡|茶|酒楼|饭店|餐厅|快餐|烧烤|煲|粉|饺子|馄饨|串|食堂|料理/
		},
		parking: {
			keyword: '停车场',
			filter: 'category=停车场',
			mode: 'search',
			nameHint: /停车|车库|车位/
		}
	};
	return configs[categoryKey] || configs.navigation;
}

/** 按当前 Tab 严格过滤；不符分类直接丢弃（不回退原列表） */
export function filterPoisByCategory(list, categoryKey) {
	const config = getCategorySearchConfig(categoryKey);
	const hint = config.nameHint;
	if (!hint || !Array.isArray(list)) {
		return [];
	}
	return list.filter((item) => {
		const text = `${item.tag || ''}${item.name || ''}`;
		// 停车：必须命中停车相关，排除住宅/行政地名等
		if (categoryKey === 'parking') {
			return /停车|车库|车位/.test(text) && !/住宅|小区|行政区|地名地址/.test(item.tag || '');
		}
		if (categoryKey === 'food') {
			return hint.test(text) && !/住宅|小区|行政区|地名地址|停车场/.test(item.tag || '');
		}
		return hint.test(text);
	});
}

/** 服务端 MapPoiVo → 页面展示结构 */
export function mapPoiVoToDisplay(record) {
	const latitude = record.latitude != null ? Number(record.latitude) : null;
	const longitude = record.longitude != null ? Number(record.longitude) : null;
	let detailPath = '';
	if (record.sourceType === 'scenic' && record.sourceId) {
		detailPath = `/mobile-design/scenic/detail?id=${record.sourceId}`;
	} else if (record.sourceType === 'merchant' && record.sourceId) {
		detailPath = `/mobile-design/goose/detail?id=${record.sourceId}`;
	}
	return {
		id: record.id,
		name: record.name || '',
		address: record.address || '',
		latitude,
		longitude,
		distance: formatMapDistance(record.distance),
		tag: record.tag || '',
		detailPath,
		spaces: record.spaces,
		total: record.totalSpaces,
		fee: record.feeDesc || '',
		coverImage: record.coverImage || '',
		sourceType: record.sourceType || ''
	};
}

/** 将经纬度映射到 H5 占位图百分比坐标 */
export function projectPoiToPercent(poi, center = MAP_DEFAULT_CENTER) {
	const latSpan = 0.028;
	const lngSpan = 0.034;
	const left = 50 + ((poi.longitude - center.longitude) / lngSpan) * 42;
	const top = 50 - ((poi.latitude - center.latitude) / latSpan) * 42;
	return {
		left: `${Math.min(92, Math.max(8, left))}%`,
		top: `${Math.min(88, Math.max(12, top))}%`
	};
}

export function buildMapMarkers(pois, activeId = '') {
	return pois
		.filter((poi) => poi.latitude != null && poi.longitude != null)
		.map((poi, index) => ({
			id: index + 1,
			poiId: poi.id,
			latitude: poi.latitude,
			longitude: poi.longitude,
			title: poi.name,
			width: 28,
			height: 36,
			iconPath: '/static/logo.png',
			callout: {
				content: poi.name,
				display: activeId === poi.id ? 'ALWAYS' : 'BYCLICK',
				padding: 8,
				borderRadius: 6,
				fontSize: 12
			}
		}));
}
