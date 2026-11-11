/** 非遗名录级别（世界级 → 区县级，与后端 levelCode 对齐） */
export const HERITAGE_CRAFT_LEVEL_OPTIONS = [
  { label: '世界级（联合国教科文组织人类非物质文化遗产）', value: 'world', short: '世界级' },
  { label: '国家级', value: 'national', short: '国家级' },
  { label: '省级', value: 'provincial', short: '省级' },
  { label: '市级', value: 'municipal', short: '市级' },
  { label: '区县级', value: 'county', short: '区县级' },
]

export function resolveCraftLevelLabel(levelCode, levelLabel) {
  if (levelLabel) return levelLabel
  return HERITAGE_CRAFT_LEVEL_OPTIONS.find((item) => item.value === levelCode)?.label || ''
}
