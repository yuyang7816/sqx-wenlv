/** 非遗名录级别 Tab（世界级 → 区县级，与管理后台 levelCode 对齐） */
export const HERITAGE_CRAFT_LEVEL_TABS = [
	{ key: 'world', label: '世界级' },
	{ key: 'national', label: '国家级' },
	{ key: 'provincial', label: '省级' },
	{ key: 'municipal', label: '市级' },
	{ key: 'county', label: '区县级' }
];

/** 根据级别短文案返回卡片样式类名 */
export function resolveCraftLevelClass(level) {
	if (level === '世界级') return 'catalog-card__level--world';
	if (level === '国家级') return 'catalog-card__level--national';
	if (level === '省级') return 'catalog-card__level--provincial';
	if (level === '市级') return 'catalog-card__level--municipal';
	if (level === '区县级') return 'catalog-card__level--county';
	return 'catalog-card__level--craft';
}
