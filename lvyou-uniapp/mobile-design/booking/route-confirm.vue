<template>
	<view class="rc-page">
		<view class="rc-ambient" />

		<scroll-view scroll-y class="rc-scroll" :show-scrollbar="false">
			<view class="rc-inner">
				<!-- 线路概要 -->
				<view class="rc-summary ry-card">
					<view class="rc-route">
						<image class="rc-route__cover ry-placeholder-img" :src="product.cover" mode="aspectFill" />
						<view class="rc-route__main">
							<text class="rc-route__name">{{ product.title }}</text>
							<text v-if="product.desc" class="rc-route__desc">{{ product.desc }}</text>
							<view class="rc-route__foot">
								<view class="rc-route__price">
									<text class="rc-route__symbol">¥</text>
									<text class="rc-route__amount">{{ product.unitPrice }}</text>
									<text class="rc-route__suffix">起</text>
								</view>
								<text v-if="product.duration" class="rc-route__duration">{{ product.duration }}</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 预约信息（出行 + 联系人一体） -->
				<view class="rc-panel">
					<view class="rc-panel__head">
						<text class="rc-panel__title">{{ ui.sectionForm }}</text>
						<text class="rc-panel__sub">填写出行与联系信息</text>
					</view>

					<view class="rc-section">
						<view class="rc-section__label">
							<text class="rc-section__step">01</text>
							<text class="rc-section__name">出行安排</text>
						</view>
						<view class="rc-form">
							<view class="rc-form__row rc-form__row--link" @click="openDatePicker">
								<text class="rc-form__label">{{ ui.labelTravelDate }}</text>
								<view class="rc-form__value">
									<text v-if="form.date" class="rc-form__text">{{ dateDisplay }}</text>
									<text v-else class="rc-form__placeholder">{{ ui.placeholderTravelDate }}</text>
									<u-icon name="arrow-right" color="#C7C7CC" :size="14" />
								</view>
							</view>
							<view class="rc-form__row">
								<text class="rc-form__label">{{ ui.labelGuestCountRoute }}</text>
								<view class="rc-form__value rc-form__value--stepper">
									<view class="rc-stepper">
										<view class="rc-stepper__btn" :class="{ 'rc-stepper__btn--off': form.guestCount <= 1 }"
											@click="changeGuestCount(-1)">
											<u-icon name="minus" :size="12" color="#666666" />
										</view>
										<text class="rc-stepper__num">{{ form.guestCount }}</text>
										<view class="rc-stepper__btn rc-stepper__btn--plus"
											:class="{ 'rc-stepper__btn--off': form.guestCount >= 10 }" @click="changeGuestCount(1)">
											<u-icon name="plus" :size="12" color="#FFFFFF" />
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>

					<view class="rc-section">
						<view class="rc-section__label">
							<text class="rc-section__step">02</text>
							<text class="rc-section__name">{{ ui.sectionGuests }}</text>
						</view>
						<view v-for="(guest, index) in guestList" :key="index" class="rc-guest">
							<text class="rc-guest__title">{{ routeGuestRoleLabel(index) }}</text>
							<view class="rc-form">
								<view class="rc-form__row">
									<text class="rc-form__label">{{ ui.labelName }}</text>
									<view class="rc-form__input">
										<u-input v-model="guest.name" :placeholder="ui.guestNamePlaceholder" border="none"
											input-align="right" color="#1A1A1A" :placeholder-style="placeholderStyle"
											:custom-style="inputStyle" />
									</view>
								</view>
								<view class="rc-form__row">
									<text class="rc-form__label">身份证号</text>
									<view class="rc-form__input">
										<u-input v-model="guest.idCard" placeholder="请输入18位身份证号" maxlength="18" border="none"
											input-align="right" color="#1A1A1A" :placeholder-style="placeholderStyle"
											:custom-style="inputStyle" />
									</view>
								</view>
								<view v-if="index === 0" class="rc-form__row">
									<text class="rc-form__label">{{ ui.labelPhone }}</text>
									<view class="rc-form__input">
										<u-input v-model="guest.phone" type="number" maxlength="11" :placeholder="ui.placeholderPhone"
											border="none" input-align="right" color="#1A1A1A" :placeholder-style="placeholderStyle"
											:custom-style="inputStyle" />
									</view>
								</view>
							</view>
						</view>
					</view>

					<view class="rc-section">
						<view class="rc-section__label">
							<text class="rc-section__step">03</text>
							<text class="rc-section__name">{{ ui.labelRemark }}</text>
						</view>
						<view class="rc-form">
							<view class="rc-form__row rc-form__row--area">
								<view class="rc-form__textarea">
									<u-textarea v-model="form.remark" :placeholder="ui.placeholderRemark" :maxlength="200"
										:count="false" border="none" :placeholder-style="remarkPlaceholderStyle"
										:custom-style="textareaStyle" />
								</view>
							</view>
						</view>
					</view>
				</view>

				<view v-if="subtotalAmount > 0" class="rc-coupon" @click="openCouponSelect">
					<view class="rc-coupon__left">
						<u-icon name="coupon-fill" :color="accentColor" :size="16" />
						<text class="rc-coupon__label">{{ ui.labelCoupon }}</text>
					</view>
					<view class="rc-coupon__right">
						<text v-if="selectedCoupon" class="rc-coupon__discount">- {{ formatMoney(couponDiscount) }}</text>
						<text v-else-if="availableCouponCount" class="rc-coupon__hint">{{ availableCouponCount }}{{ ui.couponSelectHint }}</text>
						<text v-else class="rc-coupon__none">{{ ui.couponNone }}</text>
						<u-icon name="arrow-right" :size="14" color="#C7C7CC" />
					</view>
				</view>

				<!-- 费用明细 -->
				<view class="rc-bill">
					<view class="rc-bill__row">
						<text class="rc-bill__label">{{ ui.routeFeeLine }}</text>
						<text class="rc-bill__value">¥{{ product.unitPrice }} × {{ form.guestCount }}人</text>
					</view>
					<view v-if="couponDiscount > 0" class="rc-bill__row">
						<text class="rc-bill__label">{{ ui.labelDiscount }}</text>
						<text class="rc-bill__value rc-bill__value--off">- {{ formatMoney(couponDiscount) }}</text>
					</view>
					<view class="rc-bill__divider" />
					<view class="rc-bill__row rc-bill__row--total">
						<text class="rc-bill__label">{{ ui.totalLabel }}</text>
						<view class="rc-bill__total">
							<text class="rc-bill__symbol">¥</text>
							<text class="rc-bill__amount">{{ totalPriceText }}</text>
						</view>
					</view>
				</view>

				<!-- 预约须知 -->
				<view class="rc-note">
					<view class="rc-note__head">
						<u-icon name="info-circle-fill" :color="accentColor" :size="14" />
						<text class="rc-note__title">{{ ui.sectionPolicyRoute }}</text>
					</view>
					<view class="rc-note__list">
						<text v-for="(item, index) in policyList" :key="index" class="rc-note__item">{{ item }}</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="rc-bar">
			<view class="rc-bar__glass" />
			<view class="rc-bar__inner">
				<view class="rc-bar__info">
					<text class="rc-bar__label">{{ ui.totalLabel }}</text>
					<view class="rc-bar__price">
						<text class="rc-bar__symbol">¥</text>
						<text class="rc-bar__amount">{{ totalPriceText }}</text>
					</view>
				</view>
				<view class="rc-bar__btn" :class="{ 'rc-bar__btn--disabled': creatingOrder }" @click="onSubmit">
					<text class="rc-bar__btn-text">{{ submitBtnText }}</text>
					<u-icon name="arrow-right" color="#FFFFFF" :size="14" />
				</view>
			</view>
		</view>

		<u-datetime-picker class="rc-date-picker" v-model:show="showDatePicker" v-model="datePickerValue" mode="date"
			:min-date="minDate" :round="20" :confirm-color="accentColor" @confirm="onDateConfirm"
			@cancel="showDatePicker = false" />
		<GlobalPayHost />
	</view>
</template>

<script setup>
	import { ref, computed, watch } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
	import { normalizeMediaList, normalizeMediaUrl } from '@/common/media-url.js';
	import { fetchRouteDetail } from '@/common/api/route.js';
	import { COUPON_SCOPE_ROUTE } from '@/common/coupon-util.js';
	import { useBookingPay, ORDER_CATEGORY_ROUTE } from '@/common/booking-pay.js';
	import GlobalPayHost from '@/components/global-pay-host.vue';
	import {
		BOOKING_UI,
		createDefaultSimpleForm,
		createDefaultRouteGuestList,
		syncRouteGuestList,
		routeGuestRoleLabel,
		formatDateStr,
		getWeekdayLabel
	} from '@/common/booking-ui.js';
	import { validateRouteGuestList, normalizeIdCard } from '@/common/id-card.js';

	const ui = BOOKING_UI;
	const accentColor = getModuleColor('route');
	const inputStyle = {
		padding: '0',
		backgroundColor: 'transparent',
		fontSize: '28rpx',
		height: '24px',
		color: '#1A1A1A',
		textAlign: 'right'
	};
	const textareaStyle = {
		padding: '0',
		backgroundColor: 'transparent',
		fontSize: '14px',
		width: '100%',
		color: '#1A1A1A',
		lineHeight: '1.5',
		minHeight: '56px'
	};
	const placeholderStyle = 'color: #C7C7CC; text-align: right';
	const remarkPlaceholderStyle = 'color: #C7C7CC';
	const minDate = Date.now();

	const product = ref({
		id: '',
		title: '',
		unitPrice: 0,
		duration: '',
		cover: PLACEHOLDER_LOGO,
		desc: '',
		policySummary: ''
	});
	const form = ref(createDefaultSimpleForm());
	const guestList = ref(createDefaultRouteGuestList(form.value.guestCount, form.value.contactName, form.value.phone));
	const showDatePicker = ref(false);
	const datePickerValue = ref(Date.now());

	const couponScope = computed(() => COUPON_SCOPE_ROUTE);
	const orderCategory = computed(() => ORDER_CATEGORY_ROUTE);
	const subtotalAmount = computed(
		() => (Number(product.value.unitPrice) || 0) * (Number(form.value.guestCount) || 1)
	);
	const dateDisplay = computed(() => {
		if (!form.value.date) return '';
		return `${form.value.date} ${getWeekdayLabel(form.value.date)}`;
	});
	const policyList = computed(() => {
		if (product.value.policySummary) {
			return String(product.value.policySummary).split('\n').filter(Boolean);
		}
		return [ui.policyRouteDate, ui.policyRouteId, ui.policyRouteCancel, ui.policyRouteContact];
	});
	const submitBtnText = computed(() => (subtotalAmount.value > 0 ? ui.submitPay : ui.submitBook));
	const totalPriceText = computed(() => formatMoney(totalAmount.value));

	const {
		creatingOrder,
		selectedCoupon,
		availableCouponCount,
		couponDiscount,
		totalAmount,
		openCouponSelect,
		onSubmit,
		onBookingShow,
		formatMoney
	} = useBookingPay({
		couponScope,
		subtotalAmount,
		orderCategory,
		validateForm: validateRouteForm,
		buildCreateOrderPayload,
		accentColor,
		priceColor: '#FF6600'
	});

	watch(() => form.value.guestCount, (count) => {
		guestList.value = syncRouteGuestList(
			guestList.value,
			count,
			guestList.value[0]?.name || form.value.contactName,
			guestList.value[0]?.phone || form.value.phone
		);
	});

	function changeGuestCount(delta) {
		const next = form.value.guestCount + delta;
		if (next < 1 || next > 10) return;
		form.value.guestCount = next;
	}

	function openDatePicker() {
		showDatePicker.value = true;
	}

	function onDateConfirm(e) {
		form.value.date = formatDateStr(e.value);
		showDatePicker.value = false;
	}

	function validateRouteForm() {
		if (!form.value.date) {
			uni.showToast({ title: ui.toastTravelDate, icon: 'none' });
			return false;
		}
		const guestError = validateRouteGuestList(guestList.value, form.value.guestCount);
		if (guestError) {
			uni.showToast({ title: guestError, icon: 'none' });
			return false;
		}
		if (!/^1\d{10}$/.test(String(guestList.value[0]?.phone || '').trim())) {
			uni.showToast({ title: ui.toastPhone, icon: 'none' });
			return false;
		}
		return true;
	}

	function buildCreateOrderPayload() {
		const booker = guestList.value[0] || {};
		const payload = {
			category: 'route',
			guestCount: form.value.guestCount,
			contactName: booker.name,
			contactPhone: booker.phone,
			remark: form.value.remark,
			unitPrice: product.value.unitPrice,
			routeId: Number(product.value.id),
			travelDate: form.value.date,
			guestList: guestList.value.map((guest, index) => ({
				role: index === 0 ? 'booker' : 'companion',
				name: String(guest.name || '').trim(),
				idCard: normalizeIdCard(guest.idCard),
				phone: index === 0 ? String(guest.phone || '').trim() : (guest.phone ? String(guest.phone).trim() : undefined)
			}))
		};
		if (selectedCoupon.value?.id) {
			payload.couponId = Number(selectedCoupon.value.id);
		}
		return payload;
	}

	function loadProduct(options) {
		product.value = {
			id: String(options?.id || ''),
			title: options?.title ? decodeURIComponent(options.title) : '',
			unitPrice: Number(options?.price || 0),
			duration: options?.duration ? decodeURIComponent(options.duration) : '',
			cover: normalizeMediaUrl(options?.cover ? decodeURIComponent(options.cover) : '') || PLACEHOLDER_LOGO,
			desc: options?.desc ? decodeURIComponent(options.desc) : '',
			policySummary: ''
		};
		uni.setNavigationBarTitle({ title: ui.navTitleRoute });
	}

	async function loadRouteDetail(id) {
		if (!id) return;
		try {
			const res = await fetchRouteDetail(id);
			if (res.code !== 0 || !res.data) return;
			const data = res.data;
			const banners = normalizeMediaList(data.banners);
			const cover = normalizeMediaUrl(data.cover) || banners[0] || PLACEHOLDER_LOGO;
			const price = data.price != null ? Number(data.price) : Number(data.priceFrom) || 0;
			const dayCount = Number(data.dayCount) || 0;
			const duration = data.duration || data.days || (dayCount > 0 ? `${dayCount}天` : '');
			product.value = {
				...product.value,
				title: data.name || product.value.title,
				unitPrice: price || product.value.unitPrice,
				duration: duration || product.value.duration,
				cover,
				desc: data.desc || data.intro || product.value.desc,
				policySummary: data.cancelPolicySummary || data.policySummary || ''
			};
		} catch (e) {
			// 保留 URL 参数中的展示数据
		}
	}

	onLoad(async (options) => {
		loadProduct(options);
		await loadRouteDetail(product.value.id);
	});

	onShow(() => {
		onBookingShow();
	});
</script>

<style lang="scss" scoped>
	.rc-page {
		min-height: 100vh;
		background: #f5f6fa;
		position: relative;
	}

	.rc-ambient {
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		height: 360rpx;
		background: linear-gradient(165deg, #fff0e6 0%, #fff7f2 45%, #f5f6fa 100%);
		pointer-events: none;
	}

	.rc-scroll {
		height: 100vh;
		box-sizing: border-box;
	}

	.rc-inner {
		position: relative;
		z-index: 1;
		padding: 20rpx var(--ry-page-padding-x) calc(168rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	/* ── 线路概要 ── */
	.rc-summary {
		padding: 24rpx;

		&__hint {
			display: block;
			margin-top: 16rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid #f2f2f2;
			font-size: 22rpx;
			color: #bbbbbb;
			text-align: right;
		}
	}

	.rc-route {
		display: flex;
		gap: 20rpx;

		&__cover {
			width: 180rpx;
			height: 160rpx;
			border-radius: 14rpx;
			background: #f0f0f0;
			flex-shrink: 0;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			padding: 4rpx 0;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.35;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			line-clamp: 2;
			overflow: hidden;
		}

		&__desc {
			margin-top: 8rpx;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__foot {
			margin-top: 10rpx;
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;
		}

		&__symbol {
			font-size: 22rpx;
			font-weight: 600;
			color: #ff6600;
		}

		&__amount {
			font-size: 32rpx;
			font-weight: 800;
			color: #ff6600;
			line-height: 1;
		}

		&__suffix {
			margin-left: 2rpx;
			font-size: 20rpx;
			color: #999999;
		}

		&__duration {
			font-size: 22rpx;
			color: #999999;
			flex-shrink: 0;
		}
	}

	/* ── 预约面板 ── */
	.rc-panel {
		padding: 28rpx 24rpx 8rpx;
		border-radius: 24rpx;
		background: #ffffff;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);

		&__head {
			margin-bottom: 8rpx;
			padding-bottom: 20rpx;
			border-bottom: 1rpx solid #f2f2f2;
		}

		&__title {
			display: block;
			font-size: 30rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.3;
		}

		&__sub {
			display: block;
			margin-top: 6rpx;
			font-size: 22rpx;
			color: #999999;
		}
	}

	.rc-section {
		padding: 24rpx 0;

		&+& {
			border-top: 1rpx solid #f5f5f5;
		}

		&__label {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-bottom: 20rpx;
		}

		&__step {
			font-size: 22rpx;
			font-weight: 700;
			color: #ff6600;
			letter-spacing: 1rpx;
		}

		&__name {
			font-size: 26rpx;
			font-weight: 600;
			color: #333333;
		}
	}

	.rc-stepper {
		display: flex;
		align-items: center;
		gap: 14rpx;

		&__btn {
			width: 48rpx;
			height: 48rpx;
			border-radius: 50%;
			background: #ffffff;
			border: 1rpx solid #e8ecf2;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;

			&--plus {
				background: linear-gradient(135deg, #ff8a3d, #ff6600);
				border-color: transparent;
				box-shadow: 0 4rpx 12rpx rgba(255, 102, 0, 0.25);
			}

			&--off {
				opacity: 0.35;
			}
		}

		&__num {
			min-width: 32rpx;
			text-align: center;
			font-size: 30rpx;
			font-weight: 700;
			color: #1a1a1a;
		}
	}

	/* ── 联系人表单 ── */
	.rc-form {
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		&__row {
			display: flex;
			align-items: center;
			gap: 20rpx;
			min-height: 88rpx;
			padding: 16rpx 20rpx;
			border-radius: 16rpx;
			background: #f7f9fc;
			box-sizing: border-box;

			&--area {
				align-items: flex-start;
				min-height: 120rpx;
				padding-top: 18rpx;
			}
		}

		&__label {
			flex-shrink: 0;
			width: 120rpx;
			font-size: 26rpx;
			color: #999999;
		}

		&__value {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 8rpx;

			&--stepper {
				justify-content: flex-end;
			}
		}

		&__text {
			font-size: 28rpx;
			color: #1a1a1a;
			text-align: right;
		}

		&__placeholder {
			font-size: 28rpx;
			color: #c7c7cc;
			text-align: right;
		}

		&__row--link:active {
			opacity: 0.85;
		}

		&__input {
			flex: 1;
			min-width: 0;
		}

		&__textarea {
			flex: 1;
			min-width: 0;
		}
	}

	.rc-form__row:not(.rc-form__row--area) :deep(.u-input) {
		flex: 1;
		min-width: 0;
	}

	.rc-form__row:not(.rc-form__row--area) :deep(.uni-input-input),
	.rc-form__row:not(.rc-form__row--area) :deep(.uni-input-placeholder),
	.rc-form__row:not(.rc-form__row--area) :deep(.u-input__content__field-wrapper__field) {
		text-align: right !important;
	}

	.rc-form__row--area :deep(.u-textarea),
	.rc-form__row--area :deep(.u-textarea__count) {
		background-color: transparent !important;
	}

	.rc-form__row--area :deep(.u-textarea__field),
	.rc-form__row--area :deep(.u-textarea textarea),
	.rc-form__row--area :deep(textarea) {
		color: #1a1a1a !important;
		text-align: left !important;
	}

	/* ── 费用明细 ── */
	.rc-bill {
		padding: 24rpx 28rpx;
		border-radius: 20rpx;
		background: #ffffff;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);

		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding: 8rpx 0;

			&--total {
				padding-top: 4rpx;
			}
		}

		&__label {
			font-size: 26rpx;
			color: #666666;
		}

		&__value {
			font-size: 26rpx;
			color: #333333;
		}

		&__divider {
			height: 1rpx;
			margin: 12rpx 0;
			background: #f2f2f2;
		}

		&__total {
			display: flex;
			align-items: baseline;
		}

		&__symbol {
			font-size: 24rpx;
			font-weight: 600;
			color: #ff6600;
		}

		&__amount {
			font-size: 36rpx;
			font-weight: 800;
			color: #ff6600;
			line-height: 1;
		}

		&__tip {
			display: block;
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #bbbbbb;
			line-height: 1.4;
		}
	}

	/* ── 预约须知 ── */
	.rc-note {
		padding: 24rpx 28rpx;
		border-radius: 20rpx;
		background: #fff;
		// border: 1rpx solid #f0f0f0;

		&__head {
			display: flex;
			align-items: center;
			gap: 8rpx;
			margin-bottom: 14rpx;
		}

		&__title {
			font-size: 24rpx;
			font-weight: 600;
			color: #666666;
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: 10rpx;
		}

		&__item {
			position: relative;
			padding-left: 18rpx;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.55;

			&::before {
				content: '';
				position: absolute;
				left: 0;
				top: 14rpx;
				width: 6rpx;
				height: 6rpx;
				border-radius: 50%;
				background: #cccccc;
			}
		}
	}

	/* ── 底部栏 ── */
	.rc-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		padding: 14rpx var(--ry-page-padding-x) calc(14rpx + env(safe-area-inset-bottom));

		&__glass {
			position: absolute;
			inset: 0;
			background: rgba(255, 255, 255, 0.96);
			backdrop-filter: blur(20px);
			box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.05);
		}

		&__inner {
			position: relative;
			z-index: 1;
			display: flex;
			align-items: center;
			gap: 20rpx;
		}

		&__info {
			flex: 1;
			min-width: 0;
		}

		&__label {
			display: block;
			font-size: 20rpx;
			color: #999999;
		}

		&__price {
			display: flex;
			align-items: baseline;
			margin-top: 2rpx;
		}

		&__symbol {
			font-size: 24rpx;
			font-weight: 600;
			color: #ff6600;
		}

		&__amount {
			font-size: 42rpx;
			font-weight: 800;
			color: #ff6600;
			line-height: 1;
		}

		&__btn {
			flex-shrink: 0;
			min-width: 260rpx;
			height: 88rpx;
			padding: 0 36rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #ff8a3d 0%, #ff6600 100%);
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			box-shadow: 0 10rpx 28rpx rgba(255, 102, 0, 0.32);

			&:active {
				opacity: 0.92;
			}

			&--disabled {
				opacity: 0.55;
			}
		}

		&__btn-text {
			font-size: 30rpx;
			font-weight: 600;
			color: #ffffff;
			letter-spacing: 1rpx;
		}
	}

	.rc-guest {
		margin-bottom: 24rpx;

		&__title {
			display: block;
			font-size: 26rpx;
			font-weight: 600;
			color: #666666;
			margin-bottom: 12rpx;
		}
	}

	.rc-coupon {
		margin: 24rpx 0;
		padding: 28rpx 32rpx;
		background: #ffffff;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;

		&__left {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__label {
			font-size: 28rpx;
			color: #1a1a1a;
		}

		&__right {
			display: flex;
			align-items: center;
			gap: 8rpx;
		}

		&__discount {
			font-size: 28rpx;
			color: #ff6600;
			font-weight: 600;
		}

		&__hint {
			font-size: 26rpx;
			color: #ff6600;
		}

		&__none {
			font-size: 26rpx;
			color: #c7c7cc;
		}
	}

	.rc-bill__value--off {
		color: #ff6600;
	}

	.rc-date-picker {
		:deep(.u-popup__content) {
			background-color: #ffffff !important;
		}

		:deep(.u-picker),
		:deep(.u-toolbar),
		:deep(.u-picker__view) {
			background-color: #ffffff;
		}

		:deep(.u-picker__view__column__item) {
			color: rgba(0, 0, 0, 0.25) !important;
		}

		:deep(.u-picker__view__column__item--selected) {
			color: #1a1a1a !important;
		}
	}
</style>
