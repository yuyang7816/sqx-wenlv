/**
 * 生成各业态独立 service-edit：酒店保留房型逻辑，其余去掉酒店依赖
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const SRC = path.join(ROOT, 'merchant-portal', 'center', 'service-edit.vue');
const src = fs.readFileSync(SRC, 'utf8');

const STYLE = src.slice(src.indexOf('<style'));

function removeUntil(text, startMark, endMark) {
	const s = text.indexOf(startMark);
	const e = text.indexOf(endMark, s);
	if (s < 0 || e < 0) throw new Error(`markers not found: ${startMark} / ${endMark}`);
	return text.slice(0, s) + text.slice(e);
}

const DESC = {
	restaurant: '用户端招牌菜品列表展示此介绍',
	travel: '用户端线路详情页「线路介绍」展示',
	culture: '用户端商品详情页标题下方展示',
	hotel: '用户端房型卡片展示简介（可选）'
};
const COVER = {
	restaurant: '用户端招牌菜品列表封面图',
	travel: '用户端线路详情页顶部封面',
	culture: '用户端商品详情页封面与轮播首图',
	hotel: '用户端酒店详情「房型推荐」卡片封面'
};
const HOME_TAB = {
	hotel: 'hotel',
	travel: 'route',
	restaurant: 'activity',
	culture: 'heritage'
};

function buildNonHotel(type) {
	let t = src;
	// template: remove hotel blocks
	t = removeUntil(t, '\n\t\t<!-- 客房标准（酒店） -->', '\n\t\t<!-- 扩展字段 -->');

	// script head through coverHint
	const scriptStart = t.indexOf('<script setup>');
	const styleStart = t.indexOf('<style');
	const script = t.slice(scriptStart, styleStart);

	const newScript = `<script setup>
import { computed, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	SERVICE_STATUS,
	requireMerchantDepositPaid
} from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { getServiceExtraFields, getServiceDescLabel } from '@/common/merchant-service-fields.js';
import {
	fetchMerchantServiceDetail,
	saveMerchantService,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

const PACKAGE_STORE_TYPE = '${type}';
function resolveHomeTabKey() {
	return '${HOME_TAB[type]}';
}

const { theme, pageStyle, businessUi, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const extraFields = computed(() => getServiceExtraFields(PACKAGE_STORE_TYPE));
const descLabel = computed(() => getServiceDescLabel(PACKAGE_STORE_TYPE));
const descHint = computed(() => '${DESC[type]}');
const coverHint = computed(() => '${COVER[type]}');
const ui = businessUi;
const serviceId = ref('');
const submitting = ref(false);
const formInitialized = ref(false);
const emptyForm = () => ({
	id: '',
	name: '',
	price: '',
	stock: '',
	spec: '',
	meetPoint: '',
	spots: '',
	goodsSpecs: '',
	detail: '',
	monthlySales: '',
	desc: '',
	cover: '',
	homeTabKey: ''
});

const form = reactive(emptyForm());
const btnPrimary = { flex: 1, height: '48px', borderRadius: '24px', fontWeight: '600' };
const btnPlain = { flex: 1, height: '48px', borderRadius: '24px' };

async function loadForm() {
	if (!serviceId.value) {
		Object.assign(form, emptyForm());
		form.homeTabKey = resolveHomeTabKey();
		return;
	}
	try {
		const res = await fetchMerchantServiceDetail(serviceId.value);
		if (res.code === 0 && res.data) {
			const service = res.data;
			Object.assign(form, {
				id: service.id,
				name: service.name || '',
				price: String(service.price ?? ''),
				stock: String(service.stock ?? ''),
				desc: service.desc || '',
				spec: service.spec || '',
				meetPoint: service.meetPoint || '',
				spots: service.spots || '',
				goodsSpecs: service.goodsSpecs || '',
				detail: service.detail || '',
				monthlySales: String(service.monthlySales ?? ''),
				cover: service.cover || '',
				homeTabKey: service.homeTabKey || ''
			});
		}
	} catch (e) {
		uni.showToast({ title: ui.value.networkError, icon: 'none' });
	}
}

function pickCover() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => { form.cover = res.tempFilePaths[0]; }
	});
}

async function doSave(status) {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: \`请填写\${ui.value.serviceName}\`, icon: 'none' });
		return;
	}
	const homeTabKey = String(form.homeTabKey || resolveHomeTabKey()).trim();
	submitting.value = true;
	try {
		let coverUrl = form.cover;
		if (coverUrl) {
			coverUrl = await ensureUploadedUrl(coverUrl);
		}
		const payload = {
			id: form.id || undefined,
			name: String(form.name).trim(),
			price: Number(form.price) || 0,
			stock: Number(form.stock) || 0,
			desc: String(form.desc || '').trim(),
			spec: String(form.spec || '').trim() || undefined,
			meetPoint: String(form.meetPoint || '').trim() || undefined,
			spots: String(form.spots || '').trim() || undefined,
			goodsSpecs: String(form.goodsSpecs || '').trim() || undefined,
			detail: String(form.detail || '').trim() || undefined,
			monthlySales: Number(form.monthlySales) || 0,
			cover: coverUrl,
			homeTabKey: homeTabKey || undefined,
			status
		};
		const res = await saveMerchantService(payload);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: ui.value.saveSuccess, icon: 'success' });
		setTimeout(() => uni.navigateBack(), 500);
	} catch (e) {
		uni.showToast({ title: e.message || ui.value.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

function onSaveDraft() {
	doSave(SERVICE_STATUS.DRAFT);
}

function onPublish() {
	doSave(SERVICE_STATUS.ON_SALE);
}

onLoad(async (options) => {
	serviceId.value = options?.id || '';
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	const title = serviceId.value ? ui.value.serviceEditTitle : ui.value.servicePublishTitle;
	uni.setNavigationBarTitle({ title });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	applyNavBarColor();
	if (!formInitialized.value) {
		await loadForm();
		formInitialized.value = true;
	}
});
</script>

`;

	const template = t.slice(0, scriptStart);
	return template + newScript + STYLE;
}

function buildHotel() {
	let t = src;
	t = t.replace(
		'url="/merchant-portal/center/room-standards"',
		'url="/merchant-hotel/room-standards"'
	);
	t = t.replace(
		"import HotelRoomFields from '@/merchant-portal/center/components/hotel-room-fields.vue';",
		"import HotelRoomFields from '@/merchant-hotel/components/hotel-room-fields.vue';"
	);
	t = t.replace(
		'const { theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor } = useMerchantTheme();',
		`const PACKAGE_STORE_TYPE = 'hotel';
const { theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();`
	);
	t = t.replace(
		`onLoad(async (options) => {
	serviceId.value = options?.id || '';
	await initTheme();`,
		`onLoad(async (options) => {
	serviceId.value = options?.id || '';
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();`
	);
	t = t.replace(
		`onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	await initTheme();`,
		`onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();`
	);
	return t;
}

for (const type of ['restaurant', 'travel', 'culture']) {
	const out = path.join(ROOT, `merchant-${type}`, 'service-edit.vue');
	fs.writeFileSync(out, buildNonHotel(type), 'utf8');
	console.log('wrote', type);
}

fs.writeFileSync(path.join(ROOT, 'merchant-hotel', 'service-edit.vue'), buildHotel(), 'utf8');
console.log('wrote hotel');
