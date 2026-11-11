<template>
	<view class="feedback-page ry-page">
		<view class="feedback-body">
			<view class="feedback-hero ry-card">
				<view class="feedback-hero__icon">
					<u-icon name="edit-pen-fill" color="#FF6600" :size="36" />
				</view>
				<text class="feedback-hero__title">{{ ui.heroTitle }}</text>
				<text class="feedback-hero__desc">{{ ui.heroDesc }}</text>
			</view>

			<view class="feedback-section ry-card">
				<view class="feedback-section__head">
					<text class="feedback-section__title">{{ ui.sectionType }}</text>
				</view>
				<view class="feedback-field feedback-field--picker" @click="openTypePopup">
					<view class="feedback-type-picker">
						<view v-if="selectedTypeOption" class="feedback-type-picker__left">
							<view class="feedback-type-picker__icon" :style="{ backgroundColor: selectedTypeOption.bgColor }">
								<u-icon :name="selectedTypeOption.icon" :color="selectedTypeOption.iconColor" :size="18" />
							</view>
							<text class="feedback-type-picker__text">{{ form.type }}</text>
						</view>
						<text v-else class="feedback-type-picker__placeholder">{{ ui.typePlaceholder }}</text>
						<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
					</view>
				</view>
			</view>

			<view class="feedback-section ry-card">
				<view class="feedback-section__head">
					<text class="feedback-section__title">{{ ui.sectionContent }}</text>
					<text class="feedback-section__count">{{ contentLength }}/500</text>
				</view>
				<view class="feedback-textarea-wrap">
					<textarea
						v-model="form.content"
						class="feedback-textarea"
						:maxlength="500"
						:placeholder="ui.contentPlaceholder"
						placeholder-class="feedback-textarea__placeholder"
						auto-height
					/>
				</view>
			</view>

			<view class="feedback-section ry-card">
				<view class="feedback-section__head">
					<text class="feedback-section__title">{{ ui.sectionImages }}</text>
					<text class="feedback-section__hint">{{ ui.imagesTip }}</text>
				</view>
				<view class="feedback-images">
					<view
						v-for="(img, index) in form.images"
						:key="`${img}-${index}`"
						class="feedback-images__item"
					>
						<image class="feedback-images__img" :src="img" mode="aspectFill" @click="previewImage(index)" />
						<view class="feedback-images__del" @click.stop="removeImage(index)">
							<u-icon name="close" color="#FFFFFF" :size="12" />
						</view>
					</view>
					<view v-if="form.images.length < maxImages" class="feedback-images__add" @click="chooseImage">
						<u-icon name="camera-fill" color="#CCCCCC" :size="28" />
						<text class="feedback-images__add-text">{{ ui.imagesAdd }}</text>
					</view>
				</view>
			</view>

			<view class="feedback-section ry-card">
				<view class="feedback-section__head">
					<text class="feedback-section__title">{{ ui.sectionContact }}</text>
				</view>
				<view class="feedback-field">
					<text class="feedback-field__label">手机号</text>
					<view class="feedback-field__input-wrap">
						<input
							v-model="form.phone"
							class="feedback-field__input"
							type="number"
							maxlength="11"
							:placeholder="ui.phonePlaceholder"
							placeholder-class="feedback-field__placeholder"
						/>
					</view>
				</view>
			</view>
		</view>

		<view class="feedback-bar">
			<text class="feedback-bar__tip">{{ ui.footerTip }}</text>
			<view
				class="feedback-bar__btn"
				:class="{ 'feedback-bar__btn--disabled': submitting }"
				@click="onSubmit"
			>
				<text class="feedback-bar__btn-text">{{ ui.submitBtn }}</text>
			</view>
		</view>

		<u-popup
			:show="showTypePopup"
			mode="bottom"
			:round="32"
			bg-color="transparent"
			:close-on-click-overlay="true"
			@close="closeTypePopup"
		>
			<view class="feedback-type-popup">
				<view class="feedback-type-popup__header">
					<text class="feedback-type-popup__title">{{ ui.typePopupTitle }}</text>
					<view class="feedback-type-popup__close" @click="closeTypePopup">
						<u-icon name="close" :size="18" color="#999" />
					</view>
				</view>
				<view class="feedback-type-popup__list">
					<view
						v-for="item in typeOptions"
						:key="item.name"
						class="feedback-type-popup__item"
						:class="{ 'feedback-type-popup__item--active': form.type === item.name }"
						@click="selectType(item.name)"
					>
						<view class="feedback-type-popup__icon" :style="{ backgroundColor: item.bgColor }">
							<u-icon :name="item.icon" :color="item.iconColor" :size="20" />
						</view>
						<text class="feedback-type-popup__text">{{ item.name }}</text>
						<view
							class="feedback-type-popup__radio"
							:class="{ 'feedback-type-popup__radio--checked': form.type === item.name }"
						>
							<u-icon v-if="form.type === item.name" name="checkmark" :size="12" color="#fff" />
						</view>
					</view>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { FEEDBACK_UI, FEEDBACK_TYPE_OPTIONS } from '@/common/feedback-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchUserProfile } from '@/common/api/user.js';
import {
	submitFeedback,
	uploadFeedbackFile,
	resolveUploadUrl,
	isLocalTempPath
} from '@/common/api/feedback.js';

const ui = FEEDBACK_UI;
const typeOptions = FEEDBACK_TYPE_OPTIONS;
const maxImages = 3;
const submitting = ref(false);
const uploading = ref(false);
const showTypePopup = ref(false);

const form = ref({
	type: typeOptions[0].name,
	content: '',
	images: [],
	phone: ''
});

const contentLength = computed(() => form.value.content.length);
const selectedTypeOption = computed(() => typeOptions.find((item) => item.name === form.value.type));

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadContactPhone() {
	if (!hasToken()) return;
	try {
		const cachedPhone = uni.getStorageSync('phone');
		if (/^1\d{10}$/.test(String(cachedPhone || ''))) {
			form.value.phone = cachedPhone;
			return;
		}
	} catch (e) { /* ignore */ }
	try {
		const res = await fetchUserProfile();
		if (res.code === 0 && res.data) {
			const phone = res.data.phone || res.data.phoneNo || '';
			if (/^1\d{10}$/.test(String(phone))) {
				form.value.phone = phone;
			}
		}
	} catch (e) { /* 预填失败不阻断提交 */ }
}

onLoad(() => {
	if (!requireLogin()) return;
});

onShow(() => {
	if (!requireLogin()) return;
	loadContactPhone();
});

function openTypePopup() {
	showTypePopup.value = true;
}

function closeTypePopup() {
	showTypePopup.value = false;
}

function selectType(name) {
	form.value.type = name;
	closeTypePopup();
}

function chooseImage() {
	if (uploading.value) return;
	const remain = maxImages - form.value.images.length;
	if (remain <= 0) {
		uni.showToast({ title: ui.toastImageLimit, icon: 'none' });
		return;
	}
	uni.chooseImage({
		count: remain,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const paths = (res.tempFilePaths || []).slice(0, remain);
			if (!paths.length) return;
			uploading.value = true;
			uni.showLoading({ title: '上传中...', mask: true });
			try {
				const uploaded = [];
				for (const path of paths) {
					const uploadRes = await uploadFeedbackFile(path);
					const url = resolveUploadUrl(uploadRes);
					if (!url) {
						uni.showToast({ title: ui.toastUploadFail, icon: 'none' });
						return;
					}
					uploaded.push(url);
				}
				form.value.images = form.value.images.concat(uploaded).slice(0, maxImages);
			} catch (e) {
				uni.showToast({ title: ui.toastUploadFail, icon: 'none' });
			} finally {
				uploading.value = false;
				uni.hideLoading();
			}
		}
	});
}

function removeImage(index) {
	form.value.images.splice(index, 1);
}

function previewImage(index) {
	uni.previewImage({
		current: form.value.images[index],
		urls: form.value.images
	});
}

async function ensureImagesUploaded() {
	const urls = [];
	for (const img of form.value.images) {
		if (isLocalTempPath(img)) {
			const uploadRes = await uploadFeedbackFile(img);
			const url = resolveUploadUrl(uploadRes);
			if (!url) throw new Error(ui.toastUploadFail);
			urls.push(url);
		} else {
			urls.push(img);
		}
	}
	return urls;
}

async function onSubmit() {
	if (submitting.value || uploading.value) return;
	if (!requireLogin()) return;

	const content = form.value.content.trim();
	if (!content) {
		uni.showToast({ title: ui.toastContent, icon: 'none' });
		return;
	}
	if (content.length < 10) {
		uni.showToast({ title: ui.toastContentMin, icon: 'none' });
		return;
	}
	if (!form.value.type) {
		uni.showToast({ title: ui.toastType, icon: 'none' });
		return;
	}
	if (!/^1\d{10}$/.test(form.value.phone)) {
		uni.showToast({ title: ui.toastPhone, icon: 'none' });
		return;
	}

	submitting.value = true;
	uni.showLoading({ title: '提交中...', mask: true });
	try {
		const images = await ensureImagesUploaded();
		const res = await submitFeedback({
			type: form.value.type,
			content,
			images,
			phone: form.value.phone
		});
		uni.hideLoading();
		if (res.code === 0) {
			uni.showToast({ title: ui.toastSuccess, icon: 'success' });
			setTimeout(() => uni.navigateBack(), 1200);
			return;
		}
		uni.showToast({ title: res.msg || ui.toastSubmitFail, icon: 'none' });
	} catch (e) {
		uni.hideLoading();
		uni.showToast({ title: e.message || ui.toastNetworkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}
</script>

<style lang="scss" scoped>
	.feedback-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.feedback-body {
		padding: 24rpx var(--ry-page-padding-x) calc(220rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.feedback-hero {
		padding: 36rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
		background: linear-gradient(135deg, #fff6ee, #ffffff);
	}

	.feedback-hero__icon {
		width: 88rpx;
		height: 88rpx;
		border-radius: 24rpx;
		background-color: #fff3eb;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.feedback-hero__title {
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	.feedback-hero__desc {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		text-align: center;
		line-height: 1.5;
	}

	.feedback-section {
		padding: 28rpx 24rpx;
	}

	.feedback-section__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.feedback-section__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	.feedback-section__count,
	.feedback-section__hint {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.feedback-field--picker {
		margin-bottom: 0;
	}

	.feedback-type-picker {
		background-color: #f8f9fc;
		border-radius: var(--ry-radius-pill);
		min-height: 88rpx;
		padding: 0 28rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;

		&:active {
			opacity: 0.85;
		}
	}

	.feedback-type-picker__left {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex: 1;
		min-width: 0;
	}

	.feedback-type-picker__icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.feedback-type-picker__text {
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	.feedback-type-picker__placeholder {
		font-size: 28rpx;
		color: #cccccc;
		flex: 1;
	}

	.feedback-type-popup {
		background-color: #ffffff;
		border-radius: 32rpx 32rpx 0 0;
		padding: 28rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
	}

	.feedback-type-popup__header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 24rpx;
	}

	.feedback-type-popup__title {
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	.feedback-type-popup__close {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.feedback-type-popup__list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.feedback-type-popup__item {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 24rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #f8f9fc;
		border: 2rpx solid transparent;

		&--active {
			background-color: #fff3eb;
			border-color: #ff6600;
		}

		&:active {
			opacity: 0.85;
		}
	}

	.feedback-type-popup__icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.feedback-type-popup__text {
		flex: 1;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	.feedback-type-popup__item--active .feedback-type-popup__text {
		color: #ff6600;
		font-weight: 500;
	}

	.feedback-type-popup__radio {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #dddddd;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;

		&--checked {
			border-color: #ff6600;
			background-color: #ff6600;
		}
	}

	.feedback-textarea-wrap {
		background-color: #f8f9fc;
		border-radius: var(--ry-radius-lg);
		padding: 20rpx 24rpx;
		min-height: 240rpx;
	}

	.feedback-textarea {
		width: 100%;
		min-height: 200rpx;
		font-size: 28rpx;
		color: var(--ry-text-primary);
		line-height: 1.6;
	}

	.feedback-textarea__placeholder {
		color: #cccccc;
		font-size: 28rpx;
	}

	.feedback-images {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.feedback-images__item,
	.feedback-images__add {
		width: 160rpx;
		height: 160rpx;
		border-radius: var(--ry-radius-md);
		overflow: hidden;
		position: relative;
	}

	.feedback-images__img {
		width: 100%;
		height: 100%;
	}

	.feedback-images__del {
		position: absolute;
		top: 8rpx;
		right: 8rpx;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background-color: rgba(0, 0, 0, 0.45);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.feedback-images__add {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		background-color: #f8f9fc;
		border: 2rpx dashed #dddddd;
	}

	.feedback-images__add-text {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	.feedback-field {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
		margin-bottom: 20rpx;

		&:last-child {
			margin-bottom: 0;
		}
	}

	.feedback-field__label {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
	}

	.feedback-field__input-wrap {
		background-color: #f8f9fc;
		border-radius: var(--ry-radius-pill);
		min-height: 88rpx;
		padding: 0 28rpx;
		display: flex;
		align-items: center;
	}

	.feedback-field__input {
		flex: 1;
		height: 88rpx;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	.feedback-field__placeholder {
		color: #cccccc;
		font-size: 28rpx;
	}

	.feedback-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 10;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background-color: #ffffff;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.feedback-bar__tip {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		text-align: center;
		line-height: 1.5;
	}

	.feedback-bar__btn {
		height: 88rpx;
		border-radius: 44rpx;
		background: linear-gradient(135deg, #ff8a3d, #ff6600);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 28rpx rgba(255, 102, 0, 0.24);

		&:active {
			opacity: 0.9;
		}

		&--disabled {
			opacity: 0.65;
		}
	}

	.feedback-bar__btn-text {
		font-size: 32rpx;
		font-weight: 600;
		color: #ffffff;
	}
</style>
