/**
 * 搜索关键词配置（展示文案仅用于 UI，请求服务端须传 code）
 */

export const SEARCH_HOT_KEYWORDS = [
	{ code: 'wanling_guzhen', label: '万灵古镇', badge: '热' },
	{ code: 'rongchang_lue', label: '卤鹅', badge: '新' },
	{ code: 'hotel_stay', label: '酒店住宿' },
	{ code: 'heritage_experience', label: '非遗体验' },
	{ code: 'antao_town', label: '安陶小镇' },
	{ code: 'bus_route', label: '公交线路' },
	{ code: 'guchang_town', label: '古昌镇' },
	{ code: 'rongchang_museum', label: '博物馆' }
];

export const SEARCH_EXPLORE_ITEMS = [
	{ key: 'scenic', name: '热门景区', desc: '山水人文', icon: 'photo-fill', color: '#2EAF6A', iconBg: 'rgba(46,175,106,0.15)', bg: 'linear-gradient(135deg, #F0FAF4 0%, #E8F5EE 100%)', code: 'scenic' },
	{ key: 'hotel', name: '品质酒店', desc: '舒适住宿', icon: 'home-fill', color: '#2979FF', iconBg: 'rgba(41,121,255,0.12)', bg: 'linear-gradient(135deg, #F0F5FF 0%, #E8F0FF 100%)', code: 'hotel' },
	{ key: 'route', name: '旅游线路', desc: '精选路线', icon: 'map-fill', color: '#FF6600', iconBg: 'rgba(255,102,0,0.12)', bg: 'linear-gradient(135deg, #FFF8F0 0%, #FFF3E8 100%)', code: 'route' },
	{ key: 'heritage', name: '非遗文创', desc: '匠心传承', icon: 'gift-fill', color: '#5D4037', iconBg: 'rgba(93,64,55,0.12)', bg: 'linear-gradient(135deg, #FAF6F4 0%, #F5EFEB 100%)', code: 'heritage' }
];

const KEYWORD_CODE_PATTERN = /^[a-z][a-z0-9_-]*$/;

/** 将用户输入或展示文案解析为 keywordCode（禁止向服务端传中文） */
export function resolveKeywordCode(input) {
	const trimmed = (input || '').trim();
	if (!trimmed) {
		return null;
	}
	const lower = trimmed.toLowerCase();
	if (KEYWORD_CODE_PATTERN.test(lower)) {
		return lower;
	}
	const hot = SEARCH_HOT_KEYWORDS.find(item => item.label === trimmed);
	if (hot) {
		return hot.code;
	}
	const explore = SEARCH_EXPLORE_ITEMS.find(item => item.name === trimmed || item.label === trimmed);
	if (explore) {
		return explore.code;
	}
	return null;
}

/** 根据 keywordCode 取展示文案 */
export function labelOfKeywordCode(code) {
	if (!code) {
		return '';
	}
	const normalized = code.trim().toLowerCase();
	const hot = SEARCH_HOT_KEYWORDS.find(item => item.code === normalized);
	if (hot) {
		return hot.label;
	}
	const explore = SEARCH_EXPLORE_ITEMS.find(item => item.code === normalized);
	if (explore) {
		return explore.name;
	}
	return code;
}
