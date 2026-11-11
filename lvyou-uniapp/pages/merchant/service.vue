<template>
	<view class="merchant-service-page">
		<view v-if="pageLoading" class="ms-state">
			<u-loading-icon mode="circle" :color="accentColor" />
			<text class="ms-state__text">{{ ui.loadingText }}</text>
		</view>
		<view v-else-if="loadError || !merchant.id" class="ms-state">
			<u-empty mode="list" :text="loadError || ui.emptyFallback" />
		</view>
		<template v-else>
		<view class="ms-hero" :style="heroStyle">
			<view class="ms-hero__bg" />
			<view class="ms-hero__content">
				<view class="ms-hero__badge">
					<u-icon name="checkmark-circle-fill" color="#FFFFFF" :size="14" />
					<text class="ms-hero__badge-text">{{ ui.badge }}</text>
				</view>
				<text class="ms-hero__type">{{ meta.tag }}</text>
				<text class="ms-hero__name">{{ merchant.name }}</text>
				<view class="ms-hero__addr">
					<u-icon name="map-fill" color="rgba(255,255,255,0.9)" :size="14" />
					<text class="ms-hero__addr-text">{{ merchant.address }}</text>
				</view>
			</view>
		</view>

		<view class="ms-body">
			<view class="ms-profile ry-card">
				<view class="ms-profile__icon" :style="{ backgroundColor: accentLight }">
					<u-icon :name="meta.icon" :color="accentColor" :size="28" />
				</view>
				<view class="ms-profile__main">
					<view class="ms-profile__tags">
						<text class="ms-profile__tag" :style="{ color: accentColor, backgroundColor: accentLight }">{{ merchant.type }}</text>
						<text class="ms-profile__tag ms-profile__tag--muted">{{ merchant.service }}</text>
					</view>
					<text class="ms-profile__region">{{ destination.name }} · {{ ui.regionSuffix }}</text>
				</view>
			</view>

			<view class="ms-steps ry-card">
				<text class="ms-section-title">{{ ui.sectionFlow }}</text>
				<view class="ms-steps__row">
					<view v-for="(step, index) in flowSteps" :key="step.label" class="ms-steps__item">
						<view class="ms-steps__node" :class="{ 'ms-steps__node--active': index === 0 }" :style="index === 0 ? { backgroundColor: accentColor } : {}">
							<text class="ms-steps__num">{{ index + 1 }}</text>
						</view>
						<text class="ms-steps__label">{{ step.label }}</text>
						<text class="ms-steps__desc">{{ step.desc }}</text>
						<view v-if="index < flowSteps.length - 1" class="ms-steps__line" />
					</view>
				</view>
			</view>

			<view class="ms-scope ry-card">
				<text class="ms-section-title">{{ ui.sectionScope }}</text>
				<view class="ms-scope__grid">
					<view v-for="item in meta.highlights" :key="item" class="ms-scope__chip">
						<u-icon name="checkmark" :color="accentColor" :size="12" />
						<text class="ms-scope__chip-text">{{ item }}</text>
					</view>
				</view>
				<view class="ms-scope__notice">
					<u-icon name="info-circle" :color="accentColor" :size="15" />
					<text class="ms-scope__notice-text">{{ ui.scopeNotice }}</text>
				</view>
			</view>

			<view class="ms-form ry-card">
				<text class="ms-section-title">{{ ui.sectionForm }}</text>

				<text class="ms-form__label">{{ ui.labelIntent }}</text>
				<view class="ms-intent">
					<view
						v-for="item in intentOptions"
						:key="item"
						class="ms-intent__item"
						:class="{ 'ms-intent__item--active': form.intent === item }"
						:style="form.intent === item ? intentActiveStyle : {}"
						@click="form.intent = item"
					>
						<text class="ms-intent__text" :style="form.intent === item ? { color: accentColor } : {}">{{ item }}</text>
					</view>
				</view>

				<view class="ms-field">
					<text class="ms-field__label">{{ ui.labelContact }}</text>
					<view class="ms-field__wrap">
						<u-input v-model="form.contactName" :placeholder="ui.placeholderContact" border="none" :custom-style="inputStyle" />
					</view>
				</view>

				<view class="ms-field">
					<text class="ms-field__label">{{ ui.labelPhone }}</text>
					<view class="ms-field__wrap">
						<u-input v-model="form.phone" type="number" maxlength="11" :placeholder="ui.placeholderPhone" border="none" :custom-style="inputStyle" />
					</view>
				</view>

				<view v-if="isHotel" class="ms-field" @click="openDatePicker">
					<text class="ms-field__label">{{ ui.labelCheckInDate }}</text>
					<view class="ms-field__picker">
						<text class="ms-field__picker-text" :class="{ 'ms-field__picker-text--placeholder': !form.date }">{{ form.date || ui.placeholderDate }}</text>
						<u-icon name="calendar" :size="16" :color="accentColor" />
					</view>
				</view>

				<view v-if="isHotel" class="ms-field ms-field--inline">
					<text class="ms-field__label">{{ ui.labelGuestCount }}</text>
					<u-number-box v-model="form.guestCount" :min="1" :max="10" :color="accentColor" />
				</view>

				<view class="ms-field ms-field--textarea">
					<text class="ms-field__label">{{ ui.labelRemark }}</text>
					<view class="ms-field__wrap ms-field__wrap--textarea">
						<u-textarea v-model="form.remark" :placeholder="ui.placeholderRemark" :maxlength="200" count border="none" />
					</view>
				</view>
			</view>

			<view class="ms-trust">
				<view class="ms-trust__item">
					<u-icon name="lock-fill" color="#2EAF6A" :size="16" />
					<text>{{ ui.trustPrivacy }}</text>
				</view>
				<view class="ms-trust__item">
					<u-icon name="clock-fill" color="#2979FF" :size="16" />
					<text>{{ ui.trustResponse }}</text>
				</view>
			</view>
		</view>

		<view class="ms-bar">
			<view class="ms-bar__info">
				<text class="ms-bar__label">{{ ui.barLabel }}</text>
				<text class="ms-bar__merchant">{{ merchant.name }}</text>
			</view>
			<u-button :text="ui.submitBtn" :color="accentColor" :custom-style="submitBtnStyle" :loading="submitting" :disabled="submitting" @click="submit" />
		</view>
		</template>

		<u-datetime-picker
			v-if="isHotel"
			v-model:show="showDatePicker"
			v-model="datePickerValue"
			mode="date"
			:min-date="minDate"
			:confirm-color="accentColor"
			@confirm="onDateConfirm"
		/>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { goOrderList } from '@/common/order-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchCurrentDestination, fetchCurrentMerchants } from '@/common/api/destination.js';
import { submitServiceRecord } from '@/common/api/service-record.js';
import {
	MERCHANT_SERVICE_UI,
	MERCHANT_FLOW_STEPS,
	getMerchantMeta,
	isHotelMerchant
} from '@/common/merchant-service-ui.js';

const ui = MERCHANT_SERVICE_UI;
const flowSteps = MERCHANT_FLOW_STEPS;
const inputStyle = { padding: '0', backgroundColor: 'transparent' };
const submitBtnStyle = { width: '110px', height: '44px', borderRadius: '22px', fontSize: '15px', fontWeight: '600', margin: '0' };

const emptyMerchant = () => ({ id: '', name: '', address: '', type: '', service: '', typeKey: '' });

function readStoredPhone() {
	try {
		return uni.getStorageSync('phone') || '';
	} catch (e) {
		return '';
	}
}

const pageLoading = ref(false);
const submitting = ref(false);
const loadError = ref('');
const routeMerchantId = ref('');
const destination = ref({ id: '', name: '' });
const merchant = ref(emptyMerchant());
const showDatePicker = ref(false);
const datePickerValue = ref(Date.now());
const minDate = Date.now();

const form = ref({
	intent: '',
	contactName: '',
	phone: readStoredPhone(),
	date: '',
	guestCount: 1,
	remark: ''
});

const meta = computed(() => getMerchantMeta(merchant.value));
const accentColor = computed(() => getModuleColor(meta.value.moduleKey));
const accentLight = computed(() => `${accentColor.value}18`);
const isHotel = computed(() => isHotelMerchant(merchant.value));
const intentOptions = computed(() => meta.value.intents);
const intentActiveStyle = computed(() => ({
	borderColor: accentColor.value,
	backgroundColor: `${accentColor.value}12`
}));
const heroStyle = computed(() => ({
	background: `linear-gradient(145deg, ${accentColor.value} 0%, ${accentColor.value}CC 55%, ${accentColor.value}88 100%)`
}));

onLoad((options) => {
	routeMerchantId.value = options?.merchantId || '';
	uni.setNavigationBarTitle({ title: ui.navTitle });
});

async function loadPageData() {
	if (!hasToken()) {
		uni.showToast({ title: ui.loginTip, icon: 'none' });
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/my/login', fail: () => {} });
		}, 400);
		return;
	}
	pageLoading.value = true;
	loadError.value = '';
	try {
		const [destRes, merchantsRes] = await Promise.all([
			fetchCurrentDestination(),
			fetchCurrentMerchants()
		]);
		if (destRes.code === 0 && destRes.data) {
			destination.value = destRes.data;
		}
		if (merchantsRes.code !== 0) {
			loadError.value = merchantsRes.msg || ui.loadMerchantsFail;
			merchant.value = emptyMerchant();
			return;
		}
		const list = Array.isArray(merchantsRes.data) ? merchantsRes.data : [];
		const found = routeMerchantId.value
			? list.find((item) => item.id === routeMerchantId.value)
			: list[0];
		if (!found) {
			loadError.value = routeMerchantId.value ? ui.merchantNotFound : ui.noMerchantAvailable;
			merchant.value = emptyMerchant();
			return;
		}
		merchant.value = found;
		if (!form.value.intent) {
			form.value.intent = getMerchantMeta(found).intents[0];
		}
	} catch (e) {
		loadError.value = ui.networkError;
		merchant.value = emptyMerchant();
	} finally {
		pageLoading.value = false;
	}
}

onShow(() => {
	loadPageData();
});

function openDatePicker() {
	showDatePicker.value = true;
}

function formatDate(timestamp) {
	const d = new Date(timestamp);
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function onDateConfirm(e) {
	form.value.date = formatDate(e.value);
}

async function submit() {
	if (!merchant.value.id) {
		uni.showToast({ title: loadError.value || ui.merchantUnavailable, icon: 'none' });
		return;
	}
	if (!form.value.contactName.trim()) {
		uni.showToast({ title: ui.toastContact, icon: 'none' });
		return;
	}
	if (!/^1\d{10}$/.test(form.value.phone)) {
		uni.showToast({ title: ui.toastPhone, icon: 'none' });
		return;
	}
	if (isHotel.value && !form.value.date) {
		uni.showToast({ title: ui.toastDate, icon: 'none' });
		return;
	}
	if (submitting.value) return;

	submitting.value = true;
	try {
		uni.showLoading({ title: ui.submittingText, mask: true });
		const payload = {
			merchantId: merchant.value.id,
			contactName: form.value.contactName.trim(),
			phone: form.value.phone,
			intent: form.value.intent,
			remark: form.value.remark.trim() || undefined
		};
		if (isHotel.value) {
			payload.date = form.value.date;
			payload.guestCount = form.value.guestCount;
		}
		const res = await submitServiceRecord(payload);
		uni.hideLoading();
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || ui.submitFail, icon: 'none' });
			return;
		}
		uni.showToast({ title: ui.toastSuccess, icon: 'success' });
		setTimeout(() => goOrderList(), 450);
	} catch (e) {
		uni.hideLoading();
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}
</script>

<style lang="scss" scoped>
.merchant-service-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.ms-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 24rpx;
	min-height: 60vh;
	padding: 48rpx var(--ry-page-padding-x);
}

.ms-state__text {
	font-size: 26rpx;
	color: var(--ry-text-hint);
}

.ms-hero {
	position: relative;
	padding: 32rpx var(--ry-page-padding-x) 88rpx;
	overflow: hidden;
}

.ms-hero__bg {
	position: absolute;
	inset: 0;
	opacity: 0.15;
	background-image: radial-gradient(circle at 85% 20%, #ffffff 0%, transparent 45%);
	pointer-events: none;
}

.ms-hero__content {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.ms-hero__badge {
	display: inline-flex;
	align-items: center;
	align-self: flex-start;
	gap: 8rpx;
	padding: 8rpx 20rpx;
	border-radius: var(--ry-radius-pill);
	background: rgba(255, 255, 255, 0.22);
	backdrop-filter: blur(8px);
}

.ms-hero__badge-text {
	font-size: 22rpx;
	color: #ffffff;
	font-weight: 500;
}

.ms-hero__type {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.85);
	letter-spacing: 2rpx;
}

.ms-hero__name {
	font-size: 44rpx;
	font-weight: 700;
	color: #ffffff;
	line-height: 1.35;
	max-width: 92%;
}

.ms-hero__addr {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-top: 4rpx;
}

.ms-hero__addr-text {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.9);
	line-height: 1.4;
}

.ms-body {
	margin-top: -56rpx;
	padding: 0 var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: var(--ry-card-gap, 20rpx);
}

.ms-section-title {
	display: block;
	font-size: 30rpx;
	font-weight: 600;
	color: var(--ry-text-primary);
	margin-bottom: 24rpx;
}

.ms-profile {
	display: flex;
	align-items: center;
	gap: 24rpx;
	padding: 28rpx;
}

.ms-profile__icon {
	flex-shrink: 0;
	width: 96rpx;
	height: 96rpx;
	border-radius: var(--ry-radius-lg);
	display: flex;
	align-items: center;
	justify-content: center;
}

.ms-profile__main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.ms-profile__tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.ms-profile__tag {
	padding: 6rpx 16rpx;
	border-radius: var(--ry-radius-pill);
	font-size: 22rpx;
	font-weight: 600;
}

.ms-profile__tag--muted {
	color: var(--ry-text-secondary);
	background: #f5f6fa;
	font-weight: 500;
}

.ms-profile__region {
	font-size: 24rpx;
	color: var(--ry-text-hint);
}

.ms-steps {
	padding: 28rpx;
}

.ms-steps__row {
	display: flex;
	justify-content: space-between;
	position: relative;
}

.ms-steps__item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	position: relative;
	text-align: center;
	padding: 0 8rpx;
}

.ms-steps__node {
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	background: #e8eaf0;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 12rpx;
	z-index: 1;
}

.ms-steps__node--active .ms-steps__num {
	color: #ffffff;
}

.ms-steps__num {
	font-size: 24rpx;
	font-weight: 700;
	color: var(--ry-text-hint);
}

.ms-steps__label {
	font-size: 26rpx;
	font-weight: 600;
	color: var(--ry-text-primary);
	margin-bottom: 4rpx;
}

.ms-steps__desc {
	font-size: 22rpx;
	color: var(--ry-text-hint);
}

.ms-steps__line {
	position: absolute;
	top: 24rpx;
	left: calc(50% + 28rpx);
	width: calc(100% - 56rpx);
	height: 2rpx;
	background: #e8eaf0;
	z-index: 0;
}

.ms-scope {
	padding: 28rpx;
}

.ms-scope__grid {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-bottom: 24rpx;
}

.ms-scope__chip {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 14rpx 22rpx;
	border-radius: var(--ry-radius-pill);
	background: #f8f9fc;
}

.ms-scope__chip-text {
	font-size: 24rpx;
	color: var(--ry-text-secondary);
}

.ms-scope__notice {
	display: flex;
	gap: 12rpx;
	padding: 20rpx;
	border-radius: var(--ry-radius-md);
	background: #f8f9fc;
}

.ms-scope__notice-text {
	flex: 1;
	font-size: 23rpx;
	color: var(--ry-text-secondary);
	line-height: 1.65;
}

.ms-form {
	padding: 28rpx;
}

.ms-form__label {
	display: block;
	font-size: 26rpx;
	color: var(--ry-text-secondary);
	margin-bottom: 16rpx;
}

.ms-intent {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-bottom: 28rpx;
}

.ms-intent__item {
	padding: 16rpx 28rpx;
	border-radius: var(--ry-radius-pill);
	border: 2rpx solid #e8eaf0;
	background: #ffffff;
}

.ms-intent__item--active {
	border-width: 2rpx;
}

.ms-intent__text {
	font-size: 26rpx;
	color: var(--ry-text-secondary);
	font-weight: 500;
}

.ms-field {
	margin-bottom: 24rpx;
}

.ms-field--inline {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.ms-field--inline .ms-field__label {
	margin-bottom: 0;
}

.ms-field__label {
	display: block;
	font-size: 26rpx;
	color: var(--ry-text-secondary);
	margin-bottom: 16rpx;
}

.ms-field__wrap {
	padding: 20rpx 24rpx;
	background: #f8f9fc;
	border-radius: var(--ry-radius-md);
	min-height: 88rpx;
	display: flex;
	align-items: center;
	box-sizing: border-box;
}

.ms-field__wrap--textarea {
	min-height: 160rpx;
	align-items: flex-start;
	padding: 16rpx 24rpx;
}

.ms-field__picker {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 24rpx;
	background: #f8f9fc;
	border-radius: var(--ry-radius-md);
	min-height: 88rpx;
	box-sizing: border-box;
}

.ms-field__picker-text {
	font-size: 28rpx;
	color: var(--ry-text-primary);
}

.ms-field__picker-text--placeholder {
	color: var(--ry-text-hint);
}

.ms-field--textarea {
	margin-bottom: 0;
}

.ms-trust {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 8rpx 8rpx 16rpx;
}

.ms-trust__item {
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-size: 23rpx;
	color: var(--ry-text-hint);
}

.ms-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 24rpx;
	min-height: 112rpx;
	padding: 20rpx var(--ry-page-padding-x);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background: #ffffff;
	border-top: 1rpx solid #f0f0f0;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
	box-sizing: border-box;
}

.ms-bar__info {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.ms-bar__label {
	font-size: 22rpx;
	color: var(--ry-text-hint);
}

.ms-bar__merchant {
	font-size: 26rpx;
	font-weight: 600;
	color: var(--ry-text-primary);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
</style>
