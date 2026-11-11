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
					<text class="mse-module__desc">封面必填；轮播图展示在用户端详情页顶部</text>
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
					<text class="mse-module__desc">标题、价格（元/人）、城市与行程天数</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>线路标题</text>
					<input v-model="form.name" class="mse-field__input" placeholder="请输入线路标题" placeholder-class="mse-field__placeholder" />
				</view>
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>价格（元/人）</text>
					<view class="mse-field__input-wrap">
						<text class="mse-field__prefix">¥</text>
						<input v-model="form.price" class="mse-field__input mse-field__input--plain" type="digit" placeholder="请输入售价，用户端可在线支付" placeholder-class="mse-field__placeholder" />
					</view>
				</view>
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>旅行城市</text>
					<input v-model="form.travelCity" class="mse-field__input" placeholder="如：荣昌、重庆" placeholder-class="mse-field__placeholder" />
				</view>
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>行程天数</text>
					<input v-model="form.dayCount" class="mse-field__input" type="number" placeholder="如：1、2、3" placeholder-class="mse-field__placeholder" @blur="syncItineraryDaysByCount" />
				</view>
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>游览景点</text>
					<input v-model="form.spots" class="mse-field__input" placeholder="景点A、景点B" placeholder-class="mse-field__placeholder" />
					<text class="mse-field__hint">多个景点用顿号分隔</text>
				</view>
				<view class="mse-field mse-field--last">
					<text class="mse-field__label">集合地点</text>
					<input v-model="form.meetPoint" class="mse-field__input" placeholder="如：荣昌区游客中心" placeholder-class="mse-field__placeholder" />
				</view>
			</view>
		</view>

		<!-- 线路介绍 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="file-text" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">线路介绍</text>
					<text class="mse-module__desc">用户端线路详情页「线路介绍」展示</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field mse-field--last">
					<textarea v-model="form.desc" class="mse-field__textarea" placeholder="请输入线路介绍" placeholder-class="mse-field__placeholder" maxlength="500" />
				</view>
			</view>
		</view>

		<!-- 每日行程 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="list-dot" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">每日行程</text>
					<text class="mse-module__desc">按天折叠填写，展开一天编辑，避免页面过长</text>
				</view>
			</view>
			<view class="mse-module__body">
				<up-collapse
					class="mse-day-collapse"
					ref="itineraryCollapseRef"
					accordion
					:border="false"
					:value="activeDayName"
					@change="onItineraryCollapseChange"
				>
					<up-collapse-item
						v-for="(day, dayIndex) in form.itineraryDays"
						:key="`day-${day.dayNo || dayIndex}`"
						:name="String(dayIndex)"
						:border="false"
						class="mse-day-collapse-item"
					>
						<template #title>
							<view class="mse-day-collapse-title">
								<text class="mse-day-collapse-title__text">{{ collapseDayTitle(day, dayIndex) }}</text>
							</view>
						</template>
						<view class="mse-day-panel">
							<view class="mse-field">
								<text class="mse-field__label"><text class="mse-field__req">*</text>当天标题</text>
								<input v-model="day.title" class="mse-field__input" placeholder="如：集合、景点游览" placeholder-class="mse-field__placeholder" />
							</view>
							<view class="mse-field">
								<text class="mse-field__label"><text class="mse-field__req">*</text>详细内容</text>
								<textarea v-model="day.content" class="mse-field__textarea" placeholder="请填写当天行程说明" placeholder-class="mse-field__placeholder" maxlength="800" />
							</view>
							<view class="mse-field">
								<text class="mse-field__label">行程图片</text>
								<scroll-view scroll-x class="mse-images-scroll" :show-scrollbar="false">
									<view class="mse-images">
										<view v-if="(day.images || []).length < 6" class="mse-images__add" @click="pickDayImages(dayIndex)">
											<u-icon name="plus-circle-fill" :color="theme.primary" :size="22" />
											<text>添加</text>
										</view>
										<view v-for="(img, imgIndex) in day.images" :key="imgIndex" class="mse-images__item">
											<image :src="img" mode="aspectFill" class="mse-images__img" />
											<view class="mse-images__del" @click="removeDayImage(dayIndex, imgIndex)">
												<u-icon name="close" color="#FFF" :size="10" />
											</view>
										</view>
									</view>
								</scroll-view>
							</view>
							<view v-if="form.itineraryDays.length > 1" class="mse-day-panel__del" @click="removeDay(dayIndex)">
								<u-icon name="trash" color="#FF4D4F" :size="14" />
								<text>删除这一天</text>
							</view>
						</view>
					</up-collapse-item>
				</up-collapse>
				<view class="mse-add-day" @click="addDay">
					<u-icon name="plus-circle-fill" :color="theme.primary" :size="16" />
					<text>添加一天行程</text>
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
import { nextTick, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	SERVICE_STATUS,
	requireMerchantDepositPaid
} from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	fetchMerchantServiceDetail,
	saveMerchantService,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

const PACKAGE_STORE_TYPE = 'travel';
function resolveHomeTabKey() {
	return 'route';
}

const { theme, pageStyle, businessUi, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const ui = businessUi;
const serviceId = ref('');
const submitting = ref(false);
const formInitialized = ref(false);
const activeDayName = ref('0');
const itineraryCollapseRef = ref(null);
const maxBanners = 6;

function emptyDay(dayNo = 1) {
	return {
		dayNo,
		dayLabel: `第${dayNo}天`,
		title: '',
		content: '',
		images: []
	};
}

const emptyForm = () => ({
	id: '',
	name: '',
	price: '',
	stock: '',
	travelCity: '',
	dayCount: '1',
	meetPoint: '',
	spots: '',
	desc: '',
	cover: '',
	banners: [],
	homeTabKey: '',
	itineraryDays: [emptyDay(1)]
});

const form = reactive(emptyForm());
const btnPrimary = { flex: 1, height: '48px', borderRadius: '24px', fontWeight: '600' };
const btnPlain = { flex: 1, height: '48px', borderRadius: '24px' };

function normalizeItineraryDays(list, dayCount) {
	const source = Array.isArray(list) ? list : [];
	const count = Math.max(1, Number(dayCount) || source.length || 1);
	const days = [];
	for (let i = 0; i < count; i += 1) {
		const raw = source[i] || {};
		const images = Array.isArray(raw.images)
			? raw.images.filter(Boolean)
			: (raw.cover ? [raw.cover] : []);
		days.push({
			dayNo: i + 1,
			dayLabel: raw.dayLabel || `第${i + 1}天`,
			title: raw.title || raw.name || '',
			content: raw.content || raw.desc || '',
			images
		});
	}
	return days;
}

function syncItineraryDaysByCount() {
	const count = Math.max(1, Number(form.dayCount) || 1);
	form.dayCount = String(count);
	form.itineraryDays = normalizeItineraryDays(form.itineraryDays, count);
	activeDayName.value = '0';
	refreshItineraryCollapse();
}

function collapseDayTitle(day, dayIndex) {
	const dayLabel = day.dayLabel || `第${dayIndex + 1}天`;
	const summary = collapseDayLabel(day);
	return summary ? `${dayLabel} · ${summary}` : dayLabel;
}

function collapseDayLabel(day) {
	const title = String(day?.title || '').trim();
	if (title) return title;
	const content = String(day?.content || '').trim();
	if (content) return content.slice(0, 20) + (content.length > 20 ? '…' : '');
	return '点击展开填写当天行程';
}

function onItineraryCollapseChange(activeNames) {
	if (Array.isArray(activeNames)) {
		const opened = activeNames.find((item) => item.status === 'open');
		activeDayName.value = opened ? String(opened.name) : '';
		return;
	}
	if (activeNames != null && activeNames !== '') {
		activeDayName.value = String(activeNames);
	}
}

function refreshItineraryCollapse() {
	nextTick(() => {
		const collapse = itineraryCollapseRef.value;
		if (collapse && typeof collapse.init === 'function') {
			collapse.init();
		}
	});
}

function addDay() {
	const next = form.itineraryDays.length + 1;
	form.itineraryDays.push(emptyDay(next));
	form.dayCount = String(form.itineraryDays.length);
	activeDayName.value = String(form.itineraryDays.length - 1);
	refreshItineraryCollapse();
}

function removeDay(index) {
	if (form.itineraryDays.length <= 1) return;
	form.itineraryDays.splice(index, 1);
	form.itineraryDays.forEach((day, i) => {
		day.dayNo = i + 1;
		day.dayLabel = `第${i + 1}天`;
	});
	form.dayCount = String(form.itineraryDays.length);
	activeDayName.value = '0';
	refreshItineraryCollapse();
}

function pickDayImages(dayIndex) {
	const day = form.itineraryDays[dayIndex];
	const remain = 6 - (day.images?.length || 0);
	if (remain <= 0) return;
	uni.chooseImage({
		count: remain,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const paths = res.tempFilePaths || [];
			// 最新上传的排在最前
			day.images.unshift(...paths.reverse());
			refreshItineraryCollapse();
		}
	});
}

function removeDayImage(dayIndex, imgIndex) {
	form.itineraryDays[dayIndex].images.splice(imgIndex, 1);
	refreshItineraryCollapse();
}

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
			const dayCount = service.dayCount || (Array.isArray(service.itineraryDays) ? service.itineraryDays.length : 1) || 1;
			Object.assign(form, {
				id: service.id,
				name: service.name || '',
				price: String(service.price ?? ''),
				stock: String(service.stock ?? ''),
				desc: service.desc || '',
				travelCity: service.travelCity || '',
				dayCount: String(dayCount),
				meetPoint: service.meetPoint || '',
				spots: service.spots || '',
				cover: service.cover || '',
				banners: Array.isArray(service.banners) ? service.banners.filter(Boolean) : [],
				homeTabKey: service.homeTabKey || '',
				itineraryDays: normalizeItineraryDays(service.itineraryDays, dayCount)
			});
			activeDayName.value = '0';
			refreshItineraryCollapse();
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
			const paths = res.tempFilePaths || [];
			// 最新上传的紧挨添加按钮（数组最前）
			form.banners.unshift(...paths.reverse());
		}
	});
}

function removeBanner(index) {
	form.banners.splice(index, 1);
}

function validatePublishForm() {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: '请填写线路标题', icon: 'none' });
		return false;
	}
	if (form.price === '' || form.price == null) {
		uni.showToast({ title: '请填写价格（元/人）', icon: 'none' });
		return false;
	}
	if (!String(form.travelCity || '').trim()) {
		uni.showToast({ title: '请填写旅行城市', icon: 'none' });
		return false;
	}
	if (!(Number(form.dayCount) > 0)) {
		uni.showToast({ title: '请填写行程天数', icon: 'none' });
		return false;
	}
	if (!String(form.spots || '').trim()) {
		uni.showToast({ title: '请填写游览景点', icon: 'none' });
		return false;
	}
	for (const day of form.itineraryDays) {
		if (!String(day.title || '').trim() || !String(day.content || '').trim()) {
			uni.showToast({ title: '请完善每日行程标题与内容', icon: 'none' });
			return false;
		}
	}
	return true;
}

async function doSave(status) {
	if (status === SERVICE_STATUS.ON_SALE && !validatePublishForm()) {
		return;
	}
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: '请填写线路标题', icon: 'none' });
		return;
	}
	const homeTabKey = String(form.homeTabKey || resolveHomeTabKey()).trim();
	const dayCount = Math.max(1, Number(form.dayCount) || form.itineraryDays.length || 1);
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
		const itineraryDays = [];
		for (let i = 0; i < form.itineraryDays.length; i += 1) {
			const day = form.itineraryDays[i];
			const images = [];
			for (const img of day.images || []) {
				images.push(await ensureUploadedUrl(img));
			}
			itineraryDays.push({
				dayNo: i + 1,
				dayLabel: `第${i + 1}天`,
				title: String(day.title || '').trim(),
				content: String(day.content || '').trim(),
				images
			});
		}
		const payload = {
			id: form.id || undefined,
			name: String(form.name).trim(),
			price: Number(form.price) || 0,
			stock: Number(form.stock) || 0,
			desc: String(form.desc || '').trim(),
			spec: `${dayCount}天`,
			travelCity: String(form.travelCity || '').trim() || undefined,
			dayCount,
			meetPoint: String(form.meetPoint || '').trim() || undefined,
			spots: String(form.spots || '').trim() || undefined,
			itineraryDays,
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
		top: 10rpx;
		right: 10rpx;
		z-index: 3;
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

	&__hint {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}
}

.mse-day-panel {
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

.mse-images-scroll {
	width: 100%;
	white-space: nowrap;
}

.mse-images {
	display: inline-flex;
	flex-wrap: nowrap;
	gap: 16rpx;
	padding: 4rpx 0;

	&__item,
	&__add {
		flex-shrink: 0;
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		overflow: hidden;
		position: relative;
		background: #F0F2F7;
	}

	&__img {
		width: 100%;
		height: 100%;
	}

	&__del {
		position: absolute;
		top: 8rpx;
		right: 8rpx;
		width: 32rpx;
		height: 32rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.45);
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
		font-size: 22rpx;
		color: var(--ry-text-hint);
		border: 1rpx dashed #D0D5DD;
	}
}

:deep(.mse-day-collapse) {
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

:deep(.mse-day-collapse-item) {
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

.mse-day-collapse-title {
	width: 100%;
	min-width: 0;
	overflow: hidden;

	&__text {
		display: block;
		width: 100%;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

.mse-add-day {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	margin-top: 16rpx;
	padding: 20rpx;
	border-radius: 16rpx;
	background: #F7F8FC;
	font-size: 26rpx;
	color: var(--ry-text-primary);
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
