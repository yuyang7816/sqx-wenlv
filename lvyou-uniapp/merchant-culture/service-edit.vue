<template>
	<view class="mse-page" :style="pageStyle">
		<!-- 封面与轮播 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="camera-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">封面与轮播</text>
					<text class="mse-module__desc">封面用于列表展示；轮播图用于详情页顶部横向滑动</text>
				</view>
			</view>
			<view class="mse-cover" @click="pickCover">
				<image v-if="form.cover" :src="form.cover" mode="aspectFill" class="mse-cover__img" />
				<view v-else class="mse-cover__empty">
					<u-icon name="camera-fill" :color="theme.primary" :size="32" />
					<text>上传封面</text>
				</view>
			</view>
			<view class="mse-banner">
				<view class="mse-banner__head">
					<text class="mse-banner__label">轮播图</text>
					<text class="mse-banner__badge">{{ form.banners.length }}/{{ maxBanners }}</text>
				</view>
				<text class="mse-banner__hint">建议 1:1 或 4:3，可多张滑动预览；未上传时使用封面</text>
				<scroll-view scroll-x class="mse-banner__scroll" :show-scrollbar="false">
					<view class="mse-banner__row">
						<view v-if="form.banners.length < maxBanners" class="mse-banner__item mse-banner__item--add" @click="pickBanners">
							<u-icon name="plus-circle-fill" :color="theme.primary" :size="22" />
							<text>添加</text>
						</view>
						<view v-for="(img, index) in form.banners" :key="index" class="mse-banner__item">
							<image :src="img" mode="aspectFill" class="mse-banner__img" />
							<view class="mse-banner__idx">{{ index + 1 }}</view>
							<view class="mse-banner__del" @click="removeBanner(index)">
								<u-icon name="close" color="#FFF" :size="10" />
							</view>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 基本信息 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="edit-pen-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">基本信息</text>
					<text class="mse-module__desc">名称、价格与库存等核心信息</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>{{ ui.serviceName }}</text>
					<input v-model="form.name" class="mse-field__input" :placeholder="'请输入' + ui.serviceName" placeholder-class="mse-field__placeholder" />
				</view>
				<view v-if="specMode === SPEC_MODE_SINGLE" class="mse-field">
					<text class="mse-field__label">{{ ui.servicePrice }}</text>
					<view class="mse-field__input-wrap">
						<text class="mse-field__prefix">¥</text>
						<input v-model="form.price" class="mse-field__input mse-field__input--plain" type="digit" placeholder="请输入价格" placeholder-class="mse-field__placeholder" />
					</view>
				</view>
				<view v-if="specMode === SPEC_MODE_SINGLE" class="mse-field mse-field--last">
					<text class="mse-field__label">{{ ui.serviceStock }}</text>
					<input v-model="form.stock" class="mse-field__input" type="number" placeholder="请输入库存或名额" placeholder-class="mse-field__placeholder" />
				</view>
			</view>
		</view>

		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="grid-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">规格与价格</text>
					<text class="mse-module__desc">单规格填写统一价格；多规格按 SKU 分别定价</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-spec-mode">
					<view
						class="mse-spec-mode__item"
						:class="{ 'mse-spec-mode__item--active': specMode === SPEC_MODE_SINGLE }"
						@click="switchSpecMode(SPEC_MODE_SINGLE)"
					>
						<text>单规格</text>
					</view>
					<view
						class="mse-spec-mode__item"
						:class="{ 'mse-spec-mode__item--active': specMode === SPEC_MODE_MULTI }"
						@click="switchSpecMode(SPEC_MODE_MULTI)"
					>
						<text>多规格</text>
					</view>
				</view>
				<view v-if="specMode === SPEC_MODE_MULTI" class="mse-spec-list">
					<up-collapse
						class="mse-spec-collapse"
						ref="specCollapseRef"
						accordion
						:border="false"
						:value="activeSpecName"
						@change="onSpecCollapseChange"
					>
						<up-collapse-item
							v-for="(sku, index) in specSkus"
							:key="`spec-${index}`"
							:name="String(index)"
							:border="false"
							class="mse-spec-collapse-item"
						>
							<template #title>
								<view class="mse-spec-collapse-title">
									<text class="mse-spec-collapse-title__text">{{ collapseSpecTitle(sku, index) }}</text>
								</view>
							</template>
							<view class="mse-spec-panel">
								<view class="mse-field">
									<text class="mse-field__label"><text class="mse-field__req">*</text>规格名称</text>
									<input v-model="sku.name" class="mse-field__input" placeholder="如：标准款、礼盒装" placeholder-class="mse-field__placeholder" />
								</view>
								<view class="mse-spec-row__grid">
									<view class="mse-field mse-field--inline">
										<text class="mse-field__label"><text class="mse-field__req">*</text>价格</text>
										<view class="mse-field__input-wrap">
											<text class="mse-field__prefix">¥</text>
											<input v-model="sku.price" class="mse-field__input mse-field__input--plain" type="digit" placeholder="请输入价格" placeholder-class="mse-field__placeholder" />
										</view>
									</view>
									<view class="mse-field mse-field--inline">
										<text class="mse-field__label"><text class="mse-field__req">*</text>库存</text>
										<input v-model="sku.stock" class="mse-field__input" type="number" placeholder="请输入库存" placeholder-class="mse-field__placeholder" />
									</view>
								</view>
								<view v-if="specSkus.length > 1" class="mse-spec-panel__del" @click="removeSpecRow(index)">
									<u-icon name="trash" color="#FF4D4F" :size="14" />
									<text>删除此规格</text>
								</view>
							</view>
						</up-collapse-item>
					</up-collapse>
					<view class="mse-spec-add" @click="addSpecRow">
						<u-icon name="plus" :color="theme.primary" :size="16" />
						<text>添加规格</text>
					</view>
					<text class="mse-field__hint">展开单个规格编辑，避免规格过多时页面过长；列表展示最低售价</text>
				</view>
				<text v-else class="mse-field__hint">单规格商品请在上方填写统一价格与库存</text>
			</view>
		</view>

		<view v-if="extraFields.length" class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="list-dot" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">补充信息</text>
					<text class="mse-module__desc">按业态填写额外展示字段</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view v-for="(field, index) in extraFields" :key="field.key" class="mse-field" :class="{ 'mse-field--last': index === extraFields.length - 1 }">
					<text class="mse-field__label">{{ field.label }}</text>
					<textarea
						v-if="field.type === 'textarea'"
						v-model="form[field.key]"
						class="mse-field__textarea"
						:placeholder="field.placeholder"
						placeholder-class="mse-field__placeholder"
						maxlength="500"
					/>
					<input
						v-else
						v-model="form[field.key]"
						class="mse-field__input"
						:type="field.type === 'number' ? 'number' : 'text'"
						:placeholder="field.placeholder"
						placeholder-class="mse-field__placeholder"
					/>
					<text v-if="field.hint" class="mse-field__hint">{{ field.hint }}</text>
				</view>
			</view>
		</view>

		<!-- 简介 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="file-text" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">{{ descLabel }}</text>
					<text class="mse-module__desc">{{ descHint }}</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field mse-field--last">
					<textarea v-model="form.desc" class="mse-field__textarea" :placeholder="'请输入' + descLabel" placeholder-class="mse-field__placeholder" maxlength="500" />
				</view>
			</view>
		</view>

		<!-- 商品详情图 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="photo-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">商品详情图</text>
					<text class="mse-module__desc">用户端「商品详情」区块纵向拼接展示，与顶部轮播图分开；建议宽 750px 长图</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-detail-images__head">
					<text class="mse-detail-images__label">详情图</text>
					<text class="mse-detail-images__badge">{{ form.detailImages.length }}/{{ maxDetailImages }}</text>
				</view>
				<view class="mse-detail-images">
					<view v-for="(img, index) in form.detailImages" :key="index" class="mse-detail-images__item">
						<image :src="img" mode="widthFix" class="mse-detail-images__img" />
						<view class="mse-detail-images__idx">{{ index + 1 }}</view>
						<view class="mse-detail-images__del" @click="removeDetailImage(index)">
							<u-icon name="close" color="#FFF" :size="10" />
						</view>
					</view>
					<view v-if="form.detailImages.length < maxDetailImages" class="mse-detail-images__add" @click="pickDetailImages">
						<u-icon name="plus-circle-fill" :color="theme.primary" :size="22" />
						<text>添加详情图</text>
					</view>
				</view>
				<text class="mse-field__hint">按顺序拼接展示，可与文字详情组合使用</text>
			</view>
		</view>

		<view class="mse-footer">
			<u-button text="保存草稿" plain :color="theme.primary" :custom-style="btnPlain" :loading="submitting" @click="onSaveDraft" />
			<u-button :text="ui.saveAndPublish" :color="theme.primary" :custom-style="btnPrimary" :loading="submitting" @click="onPublish" />
		</view>
	</view>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	SERVICE_STATUS,
	requireMerchantDepositPaid
} from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { getServiceExtraFields, getServiceDescLabel } from '@/common/merchant-service-fields.js';
import {
	SPEC_MODE_SINGLE,
	SPEC_MODE_MULTI,
	createEmptySku,
	parseGoodsSpecs,
	aggregateSkus,
	serializeGoodsSpecs
} from '@/common/merchant-goods-spec.js';
import {
	fetchMerchantServiceDetail,
	saveMerchantService,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

const PACKAGE_STORE_TYPE = 'culture';
function resolveHomeTabKey() {
	return 'heritage';
}

const { theme, pageStyle, businessUi, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const extraFields = computed(() => getServiceExtraFields(PACKAGE_STORE_TYPE));
const descLabel = computed(() => getServiceDescLabel(PACKAGE_STORE_TYPE));
const descHint = computed(() => '用户端好物详情页标题下方展示');
const ui = businessUi;
const maxBanners = 6;
const maxDetailImages = 20;
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
	banners: [],
	detailImages: [],
	homeTabKey: ''
});

const form = reactive(emptyForm());
const specMode = ref(SPEC_MODE_SINGLE);
const specSkus = ref([createEmptySku()]);
const activeSpecName = ref('0');
const specCollapseRef = ref(null);
const btnPrimary = { flex: 1, height: '48px', borderRadius: '24px', fontWeight: '600' };
const btnPlain = { flex: 1, height: '48px', borderRadius: '24px' };

function applySpecState(rawGoodsSpecs, price, stock) {
	const parsed = parseGoodsSpecs(rawGoodsSpecs, price, stock);
	specMode.value = parsed.mode;
	if (parsed.mode === SPEC_MODE_MULTI && parsed.skus.length) {
		specSkus.value = parsed.skus.map((item) => ({
			name: item.name || '',
			price: String(item.price ?? ''),
			stock: String(item.stock ?? '')
		}));
		return;
	}
	specSkus.value = [createEmptySku()];
}

function collapseSpecTitle(sku, index) {
	const name = String(sku?.name || '').trim();
	if (name) {
		const price = String(sku?.price ?? '').trim();
		const stock = String(sku?.stock ?? '').trim();
		const suffix = price ? ` · ¥${price}` : '';
		const stockText = stock ? ` · 库存${stock}` : '';
		return `${name}${suffix}${stockText}`;
	}
	return `规格 ${index + 1} · 点击展开填写`;
}

function onSpecCollapseChange(activeNames) {
	if (Array.isArray(activeNames)) {
		const opened = activeNames.find((item) => item.status === 'open');
		activeSpecName.value = opened ? String(opened.name) : '';
		return;
	}
	if (activeNames != null && activeNames !== '') {
		activeSpecName.value = String(activeNames);
	}
}

function refreshSpecCollapse() {
	nextTick(() => {
		const collapse = specCollapseRef.value;
		if (collapse && typeof collapse.init === 'function') {
			collapse.init();
		}
	});
}

function switchSpecMode(mode) {
	if (specMode.value === mode) return;
	specMode.value = mode;
	if (mode === SPEC_MODE_MULTI && specSkus.value.length === 1 && !specSkus.value[0].name) {
		specSkus.value = [createEmptySku(), createEmptySku()];
	}
	activeSpecName.value = '0';
	refreshSpecCollapse();
}

function addSpecRow() {
	specSkus.value.push(createEmptySku());
	activeSpecName.value = String(specSkus.value.length - 1);
	refreshSpecCollapse();
}

function removeSpecRow(index) {
	if (specSkus.value.length <= 1) return;
	specSkus.value.splice(index, 1);
	activeSpecName.value = '0';
	refreshSpecCollapse();
}

function validateSpecForm() {
	if (specMode.value !== SPEC_MODE_MULTI) {
		return true;
	}
	const rows = specSkus.value.map((item) => ({
		name: String(item.name || '').trim(),
		price: Number(item.price),
		stock: Number(item.stock)
	})).filter((item) => item.name);
	if (rows.length < 2) {
		uni.showToast({ title: '多规格至少添加 2 个规格', icon: 'none' });
		return false;
	}
	const names = new Set();
	for (const row of rows) {
		if (!row.name) {
			uni.showToast({ title: '请填写规格名称', icon: 'none' });
			return false;
		}
		if (names.has(row.name)) {
			uni.showToast({ title: '规格名称不能重复', icon: 'none' });
			return false;
		}
		names.add(row.name);
		if (!(row.price >= 0)) {
			uni.showToast({ title: '请填写规格价格', icon: 'none' });
			return false;
		}
		if (!(row.stock >= 0)) {
			uni.showToast({ title: '请填写规格库存', icon: 'none' });
			return false;
		}
	}
	return true;
}

function buildSpecPayload() {
	if (specMode.value !== SPEC_MODE_MULTI) {
		return {
			price: Number(form.price) || 0,
			stock: Number(form.stock) || 0,
			goodsSpecs: undefined
		};
	}
	const skus = specSkus.value
		.map((item) => ({
			name: String(item.name || '').trim(),
			price: Number(item.price) || 0,
			stock: Number(item.stock) || 0
		}))
		.filter((item) => item.name);
	const { minPrice, totalStock } = aggregateSkus(skus);
	return {
		price: minPrice,
		stock: totalStock,
		goodsSpecs: serializeGoodsSpecs(SPEC_MODE_MULTI, skus)
	};
}

async function loadForm() {
	if (!serviceId.value) {
		Object.assign(form, emptyForm());
		form.homeTabKey = resolveHomeTabKey();
		specMode.value = SPEC_MODE_SINGLE;
		specSkus.value = [createEmptySku()];
		activeSpecName.value = '0';
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
				banners: Array.isArray(service.banners) ? service.banners.filter(Boolean) : [],
				detailImages: Array.isArray(service.detailImages) ? service.detailImages.filter(Boolean) : [],
				homeTabKey: service.homeTabKey || ''
			});
			applySpecState(service.goodsSpecs, service.price, service.stock);
			activeSpecName.value = '0';
			refreshSpecCollapse();
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

function pickBanners() {
	const remain = maxBanners - (form.banners?.length || 0);
	if (remain <= 0) return;
	uni.chooseImage({
		count: remain,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			form.banners.push(...(res.tempFilePaths || []));
		}
	});
}

function removeBanner(index) {
	form.banners.splice(index, 1);
}

function pickDetailImages() {
	const remain = maxDetailImages - (form.detailImages?.length || 0);
	if (remain <= 0) return;
	uni.chooseImage({
		count: remain,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			form.detailImages.push(...(res.tempFilePaths || []));
		}
	});
}

function removeDetailImage(index) {
	form.detailImages.splice(index, 1);
}

async function doSave(status) {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: `请填写${ui.value.serviceName}`, icon: 'none' });
		return;
	}
	if (!validateSpecForm()) {
		return;
	}
	const specPayload = buildSpecPayload();
	const homeTabKey = String(form.homeTabKey || resolveHomeTabKey()).trim();
	submitting.value = true;
	try {
		let coverUrl = form.cover;
		if (coverUrl) {
			coverUrl = await ensureUploadedUrl(coverUrl);
		}
		const banners = [];
		for (const img of form.banners || []) {
			banners.push(await ensureUploadedUrl(img));
		}
		const detailImages = [];
		for (const img of form.detailImages || []) {
			detailImages.push(await ensureUploadedUrl(img));
		}
		const payload = {
			id: form.id || undefined,
			name: String(form.name).trim(),
			price: specPayload.price,
			stock: specPayload.stock,
			desc: String(form.desc || '').trim(),
			spec: String(form.spec || '').trim() || undefined,
			meetPoint: String(form.meetPoint || '').trim() || undefined,
			spots: String(form.spots || '').trim() || undefined,
			goodsSpecs: specPayload.goodsSpecs,
			detail: String(form.detail || '').trim() || undefined,
			monthlySales: Number(form.monthlySales) || 0,
			cover: coverUrl,
			banners,
			detailImages,
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

<style lang="scss" scoped>
.mse-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 20rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.mse-module {
	padding: 28rpx;
	overflow: hidden;

	&__head {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		margin-bottom: 24rpx;
	}

	&__icon {
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__meta {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.35;
	}

	&__desc {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.55;
	}

	&__body {
		display: flex;
		flex-direction: column;
	}
}

.mse-inherit-tip {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
	padding: 20rpx 24rpx;
	font-size: 22rpx;
	line-height: 1.55;
	color: var(--ry-text-secondary);
	background: rgba(41, 121, 255, 0.06);
}

.mse-cover {
	width: 100%;
	height: 280rpx;
	border-radius: 16rpx;
	overflow: hidden;
	background: #F7F8FC;

	&__img { width: 100%; height: 100%; }

	&__empty {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}
}

.mse-field {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 24rpx;

	&--last { margin-bottom: 0; }

	&__label {
		font-size: 26rpx;
		color: var(--ry-text-primary);
		font-weight: 600;
	}

	&__req { color: #FF4D4F; margin-right: 4rpx; }

	&__input {
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);

		&--plain { flex: 1; padding-left: 0; background: transparent; height: auto; }
	}

	&__input-wrap {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
	}

	&__prefix {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__textarea {
		width: 100%;
		min-height: 160rpx;
		padding: 20rpx 24rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
		box-sizing: border-box;
	}

	&__placeholder { color: #B0B8C8; }

	&__picker {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__picker-placeholder {
		color: #B0B8C8;
	}

	&__hint {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__link {
		color: #2979FF;
	}
}

.mse-spec-mode {
	display: flex;
	gap: 16rpx;
	margin-bottom: 24rpx;

	&__item {
		flex: 1;
		height: 72rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		color: var(--ry-text-secondary);

		&--active {
			background: rgba(41, 121, 255, 0.1);
			color: var(--ry-text-primary);
			font-weight: 600;
		}
	}
}

.mse-spec-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.mse-spec-panel {
	padding: 8rpx 8rpx 4rpx;
	display: flex;
	flex-direction: column;

	&__del {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		margin-top: 8rpx;
		padding: 18rpx;
		border-radius: 12rpx;
		background: #FFF5F5;
		font-size: 24rpx;
		color: #FF4D4F;
	}
}

:deep(.mse-spec-collapse) {
	.u-collapse-item {
		margin-bottom: 16rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		overflow: hidden;
	}

	.u-cell,
	.u-cell__body {
		background: #F7F8FC !important;
	}

	.u-cell--clickable {
		background-color: #F7F8FC !important;
	}

	.u-cell:active,
	.u-cell--clickable:active {
		background-color: #EEF2F8 !important;
	}

	.u-cell__body {
		padding: 20rpx 24rpx !important;
	}
}

:deep(.mse-spec-collapse-item) {
	.u-cell__body {
		overflow: hidden;
	}

	.u-cell__body__content {
		flex: 1;
		min-width: 0;
		overflow: hidden;
	}

	.u-cell__title {
		flex: 1;
		min-width: 0;
		overflow: hidden;
		width: 0;
	}

	.u-cell__right-icon-wrap {
		flex-shrink: 0;
	}

	.u-cell__label,
	.u-cell__body__content > .u-cell__label {
		display: none !important;
		height: 0 !important;
		margin: 0 !important;
		padding: 0 !important;
	}
}

.mse-spec-collapse-title {
	width: 100%;
	min-width: 0;
	overflow: hidden;

	&__text {
		display: block;
		width: 100%;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

.mse-spec-row {
	padding: 20rpx;
	border-radius: 16rpx;
	background: #F7F8FC;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 12rpx;
	}

	&__title {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__remove {
		font-size: 24rpx;
		color: #FF4D4F;
	}

	&__grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 16rpx;
	}
}

.mse-field--inline {
	margin-bottom: 0;
}

.mse-spec-add {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 80rpx;
	border-radius: 16rpx;
	border: 1rpx dashed rgba(41, 121, 255, 0.35);
	font-size: 26rpx;
	color: #2979FF;
}

.mse-banner {
	margin-top: 24rpx;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8rpx;
	}

	&__label {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__badge {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__hint {
		display: block;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		margin-bottom: 16rpx;
	}

	&__scroll {
		width: 100%;
		white-space: nowrap;
	}

	&__row {
		display: inline-flex;
		gap: 16rpx;
		padding: 4rpx 0;
	}

	&__item {
		position: relative;
		flex-shrink: 0;
		width: 200rpx;
		height: 150rpx;
		border-radius: 16rpx;
		overflow: hidden;
		background: #F7F8FC;

		&--add {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			border: 3rpx dashed #D8DEE8;
			font-size: 22rpx;
			color: var(--ry-text-hint);
		}
	}

	&__img {
		width: 100%;
		height: 100%;
	}

	&__idx {
		position: absolute;
		bottom: 8rpx;
		left: 8rpx;
		padding: 2rpx 10rpx;
		border-radius: 8rpx;
		background: rgba(0, 0, 0, 0.45);
		font-size: 20rpx;
		color: #FFF;
		font-weight: 600;
	}

	&__del {
		position: absolute;
		top: 8rpx;
		right: 8rpx;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
	}
}

.mse-detail-images {
	display: flex;
	flex-direction: column;
	gap: 16rpx;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 16rpx;
	}

	&__label {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__badge {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__item {
		position: relative;
		width: 100%;
		border-radius: 16rpx;
		overflow: hidden;
		background: #F7F8FC;
	}

	&__img {
		display: block;
		width: 100%;
	}

	&__idx {
		position: absolute;
		bottom: 12rpx;
		left: 12rpx;
		padding: 2rpx 10rpx;
		border-radius: 8rpx;
		background: rgba(0, 0, 0, 0.45);
		font-size: 20rpx;
		color: #FFF;
		font-weight: 600;
	}

	&__del {
		position: absolute;
		top: 12rpx;
		right: 12rpx;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__add {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		min-height: 160rpx;
		border-radius: 16rpx;
		border: 3rpx dashed #D8DEE8;
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}

.mse-switch-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 24rpx;
	padding: 20rpx 24rpx;
	border-radius: 16rpx;
	background: #F7F8FC;

	&__text {
		flex: 1;
		min-width: 0;
	}
}

.mse-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
	display: flex;
	gap: 16rpx;
}
</style>
