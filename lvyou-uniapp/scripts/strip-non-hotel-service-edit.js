/**
 * 非酒店业态的 service-edit 去掉酒店房型逻辑与组件依赖
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');

const NON_HOTEL = ['restaurant', 'travel', 'culture'];

for (const type of NON_HOTEL) {
	const file = path.join(ROOT, `merchant-${type}`, 'service-edit.vue');
	let text = fs.readFileSync(file, 'utf8');

	// 去掉模板中客房标准 / 继承提示 / HotelRoomFields
	text = text.replace(
		/\n\t\t<!-- 客房标准（酒店） -->[\s\S]*?<HotelRoomFields[\s\S]*?\/>\n/,
		'\n'
	);

	// 去掉酒店相关 import
	text = text.replace(
		/import \{\n\temptyRoomDetail,[\s\S]*?\} from '@\/common\/merchant-hotel-room\.js';\n/,
		''
	);
	text = text.replace(
		/import HotelRoomFields from '@\/merchant-hotel\/components\/hotel-room-fields\.vue';\n/,
		''
	);
	text = text.replace(
		/,\n\tfetchMerchantRoomStandards/,
		''
	);

	// 简化 resolveHomeTabKey：固定本包类型
	text = text.replace(
		/\/\*\* 门店类型 → 首页推荐 Tab（与后端 MerchantHomeTabSupport 一致） \*\/\nfunction resolveHomeTabKey\(storeType\) \{\n\tconst key = String\(storeType \|\| ''\)\.toLowerCase\(\);\n\tif \(key === 'hotel'\) return 'hotel';\n\tif \(key === 'travel'\) return 'route';\n\tif \(key === 'restaurant' \|\| key === 'culture' \|\| key === 'goose'\) return 'activity';\n\treturn 'activity';\n\}\n\n/,
		`const PACKAGE_STORE_TYPE = '${type}';\nfunction resolveHomeTabKey() {\n\tif (PACKAGE_STORE_TYPE === 'travel') return 'route';\n\treturn 'activity';\n}\n\n`
	);

	// 用固定类型替代 storeType 驱动的文案
	text = text.replace(
		/const \{ theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor \} = useMerchantTheme\(\);\nconst extraFields = computed\(\(\) => getServiceExtraFields\(storeType\.value\)\);\nconst descLabel = computed\(\(\) => getServiceDescLabel\(storeType\.value\)\);\nconst descHint = computed\(\(\) => \{\n\tconst map = \{\n\t\thotel: '用户端房型卡片展示简介（可选）',\n\t\trestaurant: '用户端招牌菜品列表展示此介绍',\n\t\ttravel: '用户端线路详情页「线路介绍」展示',\n\t\tculture: '用户端商品详情页标题下方展示'\n\t\};\n\treturn map\[storeType\.value\] \|\| '用户端详情页展示';\n\}\);\nconst coverHint = computed\(\(\) => \{\n\tconst map = \{\n\t\thotel: '用户端酒店详情「房型推荐」卡片封面',\n\t\trestaurant: '用户端招牌菜品列表封面图',\n\t\ttravel: '用户端线路详情页顶部封面',\n\t\tculture: '用户端商品详情页封面与轮播首图'\n\t\};\n\treturn map\[storeType\.value\] \|\| '用户端详情页封面';\n\}\);/,
		`const { theme, pageStyle, businessUi, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();\nconst extraFields = computed(() => getServiceExtraFields(PACKAGE_STORE_TYPE));\nconst descLabel = computed(() => getServiceDescLabel(PACKAGE_STORE_TYPE));\nconst DESC_HINT = {\n\trestaurant: '用户端招牌菜品列表展示此介绍',\n\ttravel: '用户端线路详情页「线路介绍」展示',\n\tculture: '用户端商品详情页标题下方展示'\n};\nconst COVER_HINT = {\n\trestaurant: '用户端招牌菜品列表封面图',\n\ttravel: '用户端线路详情页顶部封面',\n\tculture: '用户端商品详情页封面与轮播首图'\n};\nconst descHint = computed(() => DESC_HINT[PACKAGE_STORE_TYPE] || '用户端详情页展示');\nconst coverHint = computed(() => COVER_HINT[PACKAGE_STORE_TYPE] || '用户端详情页封面');`
	);

	// 删除房型相关响应式与函数
	text = text.replace(/\nconst roomDetail = ref\(emptyRoomDetail\(\)\);[\s\S]*?const selectedRoomStandardLabel = computed\(\(\) => selectedStandard\.value\?\.name \|\| ''\);\n/, '\n');
	text = text.replace(/\nasync function loadRoomStandards\(\) \{[\s\S]*?\n\}\n\nfunction onRoomStandardChange\(event\) \{[\s\S]*?\n\}\n\nfunction onInheritStandardChange\(enabled\) \{[\s\S]*?\n\}\n/, '\n');

	// 简化 loadForm
	text = text.replace(
		/async function loadForm\(\) \{\n\tif \(!serviceId\.value\) \{\n\t\tObject\.assign\(form, emptyForm\(\)\);\n\t\tform\.homeTabKey = resolveHomeTabKey\(storeType\.value\);\n\t\troomDetail\.value = emptyRoomOverride\(\);\n\t\troomStandardId\.value = '';\n\t\tinheritStandard\.value = false;\n\t\treturn;\n\t\}/,
		`async function loadForm() {\n\tif (!serviceId.value) {\n\t\tObject.assign(form, emptyForm());\n\t\tform.homeTabKey = resolveHomeTabKey();\n\t\treturn;\n\t}`
	);
	text = text.replace(
		/\t\t\troomStandardId\.value = service\.roomStandardId \? String\(service\.roomStandardId\) : '';\n\t\t\tinheritStandard\.value = Boolean\(service\.inheritStandard\);\n\t\t\tconst linkedStandard = roomStandards\.value\.find\(\n\t\t\t\t\(item\) => String\(item\.id\) === String\(service\.roomStandardId\)\n\t\t\t\);\n\t\t\tif \(inheritStandard\.value && linkedStandard\) \{\n\t\t\t\troomDetail\.value = normalizeRoomOverride\(service\.roomDetail \|\| \{\}\);\n\t\t\t\} else \{\n\t\t\t\troomDetail\.value = normalizeRoomDetail\(service\.roomDetail \|\| \{\}\);\n\t\t\t\}\n/,
		''
	);

	// 简化 doSave
	text = text.replace(
		/const homeTabKey = String\(form\.homeTabKey \|\| resolveHomeTabKey\(storeType\.value\)\)\.trim\(\);/,
		'const homeTabKey = String(form.homeTabKey || resolveHomeTabKey()).trim();'
	);
	text = text.replace(
		/\t\tlet roomDetailPayload;\n\t\tif \(storeType\.value === 'hotel'\) \{\n\t\t\tconst detail = normalizeRoomDetail\(\n\t\t\t\tinheritStandard\.value && selectedStandard\.value\n\t\t\t\t\t\? extractRoomOverridesFromStandard\(displayRoomDetail\.value, selectedStandard\.value\)\n\t\t\t\t\t: displayRoomDetail\.value\n\t\t\t\);\n\t\t\tconst uploadedGallery = \[\];\n\t\t\tfor \(const path of detail\.gallery \|\| \[\]\) \{\n\t\t\t\tuploadedGallery\.push\(await ensureUploadedUrl\(path\)\);\n\t\t\t\}\n\t\t\tdetail\.gallery = uploadedGallery;\n\t\t\tif \(!coverUrl && uploadedGallery\.length\) \{\n\t\t\t\tcoverUrl = uploadedGallery\[0\];\n\t\t\t\}\n\t\t\troomDetailPayload = buildRoomDetailPayload\(detail, \{\n\t\t\t\tinheritStandard: inheritStandard\.value && !!roomStandardId\.value,\n\t\t\t\tstandard: inheritStandard\.value \? selectedStandard\.value : null\n\t\t\t\}\);\n\t\t\}\n/,
		''
	);
	text = text.replace(
		/\tspec: storeType\.value === 'hotel' \? undefined : \(String\(form\.spec \|\| ''\)\.trim\(\) \|\| undefined\),/,
		'\t\t\tspec: String(form.spec || \'\').trim() || undefined,'
	);
	text = text.replace(
		/\t\t\troomDetail: roomDetailPayload,\n\t\t\troomStandardId: roomStandardId\.value \? Number\(roomStandardId\.value\) : undefined,\n\t\t\tinheritStandard: inheritStandard\.value && !!roomStandardId\.value,\n/,
		''
	);

	// onShow 去掉 loadRoomStandards
	text = text.replace(
		/\tawait loadRoomStandards\(\);\n\tif \(!formInitialized\.value\) \{/,
		'\tif (!formInitialized.value) {'
	);

	// onLoad/onShow 锁定类型
	text = text.replace(
		/onLoad\(async \(options\) => \{\n\tserviceId\.value = options\?\.id \|\| '';\n\tawait initTheme\(\);/,
		`onLoad(async (options) => {\n\tserviceId.value = options?.id || '';\n\tapplyStoreType(PACKAGE_STORE_TYPE);\n\tawait initTheme();`
	);
	text = text.replace(
		/onShow\(async \(\) => \{\n\tif \(!\(await requireMerchantDepositPaid\(\)\)\) return;\n\tawait initTheme\(\);/,
		`onShow(async () => {\n\tif (!(await requireMerchantDepositPaid())) return;\n\tapplyStoreType(PACKAGE_STORE_TYPE);\n\tawait initTheme();`
	);

	// 去掉酒店继承提示样式可保留无妨
	fs.writeFileSync(file, text, 'utf8');
	console.log('stripped hotel logic:', type);
}

// 酒店包：固定类型 + 组件路径
{
	const file = path.join(ROOT, 'merchant-hotel', 'service-edit.vue');
	let text = fs.readFileSync(file, 'utf8');
	text = text.replace(
		/\/\*\* 门店类型 → 首页推荐 Tab（与后端 MerchantHomeTabSupport 一致） \*\/\nfunction resolveHomeTabKey\(storeType\) \{\n\tconst key = String\(storeType \|\| ''\)\.toLowerCase\(\);\n\tif \(key === 'hotel'\) return 'hotel';\n\tif \(key === 'travel'\) return 'route';\n\tif \(key === 'restaurant' \|\| key === 'culture' \|\| key === 'goose'\) return 'activity';\n\treturn 'activity';\n\}\n\nconst \{ theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor \} = useMerchantTheme\(\);/,
		`const PACKAGE_STORE_TYPE = 'hotel';\nfunction resolveHomeTabKey() {\n\treturn 'hotel';\n}\n\nconst { theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();`
	);
	text = text.replace(/resolveHomeTabKey\(storeType\.value\)/g, 'resolveHomeTabKey()');
	text = text.replace(
		/onLoad\(async \(options\) => \{\n\tserviceId\.value = options\?\.id \|\| '';\n\tawait initTheme\(\);/,
		`onLoad(async (options) => {\n\tserviceId.value = options?.id || '';\n\tapplyStoreType(PACKAGE_STORE_TYPE);\n\tawait initTheme();`
	);
	text = text.replace(
		/onShow\(async \(\) => \{\n\tif \(!\(await requireMerchantDepositPaid\(\)\)\) return;\n\tawait initTheme\(\);/,
		`onShow(async () => {\n\tif (!(await requireMerchantDepositPaid())) return;\n\tapplyStoreType(PACKAGE_STORE_TYPE);\n\tawait initTheme();`
	);
	// 模板中直接用 hotel，去掉 v-if storeType
	text = text.replace(/v-if="storeType === 'hotel'"/g, '');
	text = text.replace(/v-if="storeType === 'hotel' && inheritStandard && selectedStandard"/g, 'v-if="inheritStandard && selectedStandard"');
	fs.writeFileSync(file, text, 'utf8');
	console.log('locked hotel service-edit');
}
