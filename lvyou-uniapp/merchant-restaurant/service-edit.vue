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
					<text class="mse-module__desc">封面用于列表展示；轮播图用于菜品详情顶部展示</text>
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
				<text class="mse-banner__hint">建议 16:9，可多张滑动预览</text>
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
					<text class="mse-module__desc">名称与参考价格等核心信息</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>{{ ui.serviceName }}</text>
					<input v-model="form.name" class="mse-field__input" :placeholder="'请输入' + ui.serviceName" placeholder-class="mse-field__placeholder" />
				</view>
				<view class="mse-field mse-field--last">
					<text class="mse-field__label">{{ ui.servicePrice }}</text>
					<view class="mse-field__input-wrap">
						<text class="mse-field__prefix">¥</text>
						<input v-model="form.price" class="mse-field__input mse-field__input--plain" type="digit" placeholder="0 表示免费预约" placeholder-class="mse-field__placeholder" />
					</view>
				</view>
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

		<view class="mse-footer">
			<u-button text="保存草稿" plain :color="theme.primary" :custom-style="btnPlain" :loading="submitting" @click="onSaveDraft" />
			<u-button :text="ui.saveAndPublish" :color="theme.primary" :custom-style="btnPrimary" :loading="submitting" @click="onPublish" />
		</view>
	</view>
</template>

<script setup>
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

const PACKAGE_STORE_TYPE = 'restaurant';
function resolveHomeTabKey() {
	return 'activity';
}

const { theme, pageStyle, businessUi, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const extraFields = computed(() => getServiceExtraFields(PACKAGE_STORE_TYPE));
const descLabel = computed(() => getServiceDescLabel(PACKAGE_STORE_TYPE));
const descHint = computed(() => '用户端招牌菜品列表展示此介绍');
const ui = businessUi;
const maxBanners = 6;
const serviceId = ref('');
const submitting = ref(false);
const formInitialized = ref(false);
const emptyForm = () => ({
	id: '',
	name: '',
	price: '',
	spec: '',
	meetPoint: '',
	spots: '',
	goodsSpecs: '',
	detail: '',
	monthlySales: '',
	desc: '',
	cover: '',
	banners: [],
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
				desc: service.desc || '',
				spec: service.spec || '',
				meetPoint: service.meetPoint || '',
				spots: service.spots || '',
				goodsSpecs: service.goodsSpecs || '',
				detail: service.detail || '',
				monthlySales: String(service.monthlySales ?? ''),
				cover: service.cover || '',
				banners: Array.isArray(service.banners) ? service.banners.filter(Boolean) : [],
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

async function doSave(status) {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: `请填写${ui.value.serviceName}`, icon: 'none' });
		return;
	}
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
		const payload = {
			id: form.id || undefined,
			name: String(form.name).trim(),
			price: Number(form.price) || 0,
			stock: 0,
			desc: String(form.desc || '').trim(),
			spec: String(form.spec || '').trim() || undefined,
			meetPoint: String(form.meetPoint || '').trim() || undefined,
			spots: String(form.spots || '').trim() || undefined,
			goodsSpecs: String(form.goodsSpecs || '').trim() || undefined,
			detail: String(form.detail || '').trim() || undefined,
			monthlySales: Number(form.monthlySales) || 0,
			cover: coverUrl,
			banners,
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
