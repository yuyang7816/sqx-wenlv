<template>
	<view class="hc-page">
		<view class="hc-ambient" />

		<scroll-view scroll-y class="hc-scroll" :show-scrollbar="false">
			<view class="hc-inner">
				<!-- 票券式房型摘要 -->
				<view class="hc-ticket">
					<view class="hc-ticket__shine" />
					<view class="hc-ticket__body">
						<view class="hc-ticket__row">
							<image class="hc-ticket__cover ry-placeholder-img" :src="product.cover" mode="aspectFill" />
							<view class="hc-ticket__info">
								<text class="hc-ticket__hotel">{{ product.title }}</text>
								<text class="hc-ticket__room">{{ displayRoomName }}</text>
							</view>
							<view class="hc-ticket__price-box">
								<text class="hc-ticket__price-tag">每晚</text>
								<view class="hc-ticket__price">
									<text class="hc-ticket__symbol">¥</text>
									<text class="hc-ticket__amount">{{ product.unitPrice }}</text>
								</view>
							</view>
						</view>
					</view>
					<view class="hc-ticket__tear">
						<view class="hc-ticket__notch hc-ticket__notch--l" />
						<view class="hc-ticket__line" />
						<view class="hc-ticket__notch hc-ticket__notch--r" />
					</view>
					<view class="hc-ticket__foot">
						<u-icon name="home-fill" :color="accentColor" :size="13" />
						<text class="hc-ticket__foot-text">{{ nights > 0 ? `预计预定 ${nights} 晚` : ui.stayTip }}</text>
					</view>
				</view>

				<!-- 预订日期 -->
				<view class="hc-card">
					<view class="hc-card__head" style="margin-bottom: 20rpx;">
						<view class="hc-card__icon">
							<u-icon name="calendar-fill" :color="accentColor" :size="15" />
						</view>
						<view class="hc-card__titles">
							<text class="hc-card__title">{{ ui.sectionStay }}</text>
							<text class="hc-card__sub">{{ ui.stayTip }}</text>
						</view>
					</view>
					<view class="hc-stay">
						<view class="hc-stay__cell" :class="{ 'hc-stay__cell--filled': hotelForm.checkIn }"
							@click="openCheckInPicker">
							<text class="hc-stay__label">{{ ui.checkIn }}</text>
							<template v-if="hotelForm.checkIn">
								<view class="hc-stay__date">
									<text class="hc-stay__month">{{ checkInParts.month }}</text>
									<text class="hc-stay__day">{{ checkInParts.day }}</text>
								</view>
								<text class="hc-stay__week">{{ checkInParts.weekday }}</text>
							</template>
							<text v-else class="hc-stay__empty">{{ ui.selectCheckIn }}</text>
						</view>

						<view class="hc-stay__bridge">
							<view class="hc-stay__bridge-line" />
							<view class="hc-stay__badge">
								<text>{{ nightsText }}</text>
							</view>
							<view class="hc-stay__bridge-line" />
						</view>

						<view class="hc-stay__cell" :class="{ 'hc-stay__cell--filled': hotelForm.checkOut }"
							@click="openCheckOutPicker">
							<text class="hc-stay__label">{{ ui.checkOut }}</text>
							<template v-if="hotelForm.checkOut">
								<view class="hc-stay__date">
									<text class="hc-stay__month">{{ checkOutParts.month }}</text>
									<text class="hc-stay__day">{{ checkOutParts.day }}</text>
								</view>
								<text class="hc-stay__week">{{ checkOutParts.weekday }}</text>
							</template>
							<text v-else class="hc-stay__empty">{{ ui.selectCheckOut }}</text>
						</view>
					</view>
				</view>

				<!-- 预订间数 -->
				<view class="hc-card">
					<view class="hc-card__head hc-card__head--row">
						<view class="hc-card__head-left">
							<view class="hc-card__icon">
								<u-icon name="home-fill" :color="accentColor" :size="15" />
							</view>
							<view class="hc-card__titles">
								<text class="hc-card__title">{{ ui.sectionRoomCount }}</text>
								<text class="hc-card__sub">{{ ui.roomCountTip }}</text>
							</view>
						</view>
						<view class="hc-counter">
							<view class="hc-counter__btn" :class="{ 'hc-counter__btn--off': hotelForm.roomCount <= 1 }"
								@click="changeRoomCount(-1)">
								<u-icon name="minus" :size="13" color="#666" />
							</view>
							<text class="hc-counter__num">{{ hotelForm.roomCount || 1 }}</text>
							<view class="hc-counter__btn hc-counter__btn--plus"
								:class="{ 'hc-counter__btn--off': hotelForm.roomCount >= 5 }" @click="changeRoomCount(1)">
								<u-icon name="plus" :size="13" color="#FFF" />
							</view>
						</view>
					</view>
				</view>

				<!-- 预订人 -->
				<view class="hc-card">
					<view class="hc-card__head hc-card__head--row" style="margin-bottom: 20rpx;">
						<view class="hc-card__head-left">
							<view class="hc-card__icon">
								<u-icon name="account-fill" :color="accentColor" :size="15" />
							</view>
							<view class="hc-card__titles">
								<text class="hc-card__title">{{ ui.sectionGuests }}</text>
								<text class="hc-card__sub">{{ ui.guestsTip }}</text>
							</view>
						</view>
					</view>
					<view class="hc-form">
						<view v-for="(_, index) in hotelForm.guestNames" :key="index" class="hc-form__item">
							<view class="hc-form__field">
								<text class="hc-form__label">{{ ui.labelName }}</text>
								<u-input v-model="hotelForm.guestNames[index]" :placeholder="ui.guestNamePlaceholder" border="none"
									color="#1A1A1A" :placeholder-style="placeholderStyle" :custom-style="inputStyle" />
							</view>
						</view>
						<view class="hc-form__item">
							<view class="hc-form__field">
								<text class="hc-form__label">{{ ui.labelPhone }}</text>
								<u-input v-model="hotelForm.phone" type="number" maxlength="11" :placeholder="ui.placeholderPhone"
									border="none" color="#1A1A1A" :placeholder-style="placeholderStyle" :custom-style="inputStyle" />
							</view>
						</view>
					</view>
				</view>
				<!-- 备注 -->
				<view class="hc-card hc-card--remark">
					<view class="hc-card__head">
						<view class="hc-card__icon">
							<u-icon name="edit-pen-fill" :color="accentColor" :size="15" />
						</view>
						<text class="hc-card__title">{{ ui.labelRemark }}</text>
					</view>
					<view class="hc-form__field hc-form__field--remark">
						<u-textarea v-model="hotelForm.remark" :placeholder="ui.placeholderRemark" :maxlength="200" :count="false"
							border="none" :placeholder-style="placeholderStyle" :custom-style="remarkTextareaStyle" />
					</view>
				</view>
				<!-- 优惠券 -->
				<view v-if="subtotalAmount > 0" class="hc-card hc-card--tap" @click="openCouponSelect">
					<view class="hc-coupon">
						<view class="hc-coupon__left">
							<view class="hc-coupon__icon">
								<u-icon name="coupon-fill" :color="accentColor" :size="16" />
							</view>
							<text class="hc-coupon__label">{{ ui.labelCoupon }}</text>
						</view>
						<view class="hc-coupon__right">
							<text v-if="selectedCoupon" class="hc-coupon__discount">- {{ formatMoney(couponDiscount) }}</text>
							<text v-else-if="availableCouponCount"
								class="hc-coupon__hint">{{ availableCouponCount }}{{ ui.couponSelectHint }}</text>
							<text v-else class="hc-coupon__none">{{ ui.couponNone }}</text>
							<u-icon name="arrow-right" :size="14" color="#C7C7CC" />
						</view>
					</view>
				</view>

				<!-- 费用明细 -->
				<view v-if="nights > 0" class="hc-card">
					<view class="hc-card__head">
						<view class="hc-card__icon">
							<u-icon name="rmb-circle-fill" :color="accentColor" :size="15" />
						</view>
						<text class="hc-card__title">{{ ui.priceDetail }}</text>
					</view>
					<view class="hc-bill">
						<view class="hc-bill__row">
							<text class="hc-bill__label">{{ breakdownLine }}</text>
							<text class="hc-bill__value">{{ breakdownAmount }}</text>
						</view>
						<view v-if="couponDiscount > 0" class="hc-bill__row">
							<text class="hc-bill__label">{{ ui.labelDiscount }}</text>
							<text class="hc-bill__value hc-bill__value--off">- {{ formatMoney(couponDiscount) }}</text>
						</view>
						<view class="hc-bill__sep" />
						<view class="hc-bill__row hc-bill__row--total">
							<text class="hc-bill__total-label">{{ ui.totalLabel }}</text>
							<text class="hc-bill__total">{{ totalPriceText }}</text>
						</view>
						<text class="hc-bill__note">{{ ui.totalHint }}</text>
					</view>
				</view>



				<!-- 预订须知 -->
				<view class="hc-card hc-card--policy">
					<view class="hc-card__head">
						<view class="hc-card__icon">
							<u-icon name="info-circle-fill" :color="accentColor" :size="15" />
						</view>
						<text class="hc-card__title">{{ ui.sectionPolicy }}</text>
					</view>
					<view class="hc-policy">
						<view v-for="(item, index) in policyList" :key="index" class="hc-policy__item">
							<view class="hc-policy__dot" />
							<text class="hc-policy__text">{{ item }}</text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 底部结算 -->
		<view class="hc-bar">
			<view class="hc-bar__glass" />
			<view class="hc-bar__inner">
				<view class="hc-bar__info">
					<text class="hc-bar__label">{{ ui.totalLabel }}</text>
					<view class="hc-bar__price">
						<text class="hc-bar__symbol">¥</text>
						<text class="hc-bar__amount">{{ totalAmountDisplay }}</text>
					</view>
				</view>
				<view class="hc-bar__btn" @click="onSubmit">
					<text class="hc-bar__btn-text">{{ submitBtnText }}</text>
					<u-icon name="arrow-right" color="#FFF" :size="14" />
				</view>
			</view>
		</view>

		<u-datetime-picker class="hc-date-picker" v-model:show="showCheckInPicker" v-model="checkInPickerValue" mode="date"
			:min-date="minDate" :round="20" :confirm-color="accentColor" :mask-style="datePickerMaskStyle"
			@confirm="onCheckInConfirm" @cancel="showCheckInPicker = false" />
		<u-datetime-picker class="hc-date-picker" v-model:show="showCheckOutPicker" v-model="checkOutPickerValue"
			mode="date" :min-date="checkOutMinDate" :round="20" :confirm-color="accentColor" :mask-style="datePickerMaskStyle"
			@confirm="onCheckOutConfirm" @cancel="showCheckOutPicker = false" />

		<GlobalPayHost />
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		watch
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		fetchHotelRoomDetail
	} from '@/common/api/hotel.js';
	import {
		COUPON_SCOPE_HOTEL
	} from '@/common/coupon-util.js';
	import {
		useBookingPay,
		ORDER_CATEGORY_HOTEL
	} from '@/common/booking-pay.js';
	import GlobalPayHost from '@/components/global-pay-host.vue';
	import {
		BOOKING_UI,
		createDefaultHotelForm,
		formatDateStr,
		calcNights,
		syncGuestNames,
		buildRoomSubtitle,
		formatMoney as formatMoneyText,
		getWeekdayLabel
	} from '@/common/booking-ui.js';

	const ui = {
		...BOOKING_UI,
		navTitleHotel: '确认预定',
		sectionStay: '预定日期',
		stayTip: '请选择预定与离店日期',
		checkIn: '预定',
		selectCheckIn: '选择预定',
		sectionGuests: '预定人信息',
		guestsTip: '姓名须与预定证件一致',
		toastCheckIn: '请选择预定日期',
		toastCheckoutBeforeCheckin: '离店日期须晚于预定日期',
		toastGuestName: '请填写所有预定人姓名',
		policyCheckIn: '14:00 后可到店办理，12:00 前退房',
		policyCancel: '预定前 1 天 18:00 前可免费取消',
		policyId: '预定人姓名须与有效证件一致'
	};
	const accentColor = getModuleColor('hotel');
	const inputStyle = {
		padding: '0',
		backgroundColor: 'transparent',
		fontSize: '15px',
		height: '22px',
		color: '#1A1A1A'
	};
	const remarkTextareaStyle = {
		padding: '0',
		backgroundColor: 'transparent',
		fontSize: '14px',
		width: '100%',
		color: '#1A1A1A',
		lineHeight: '1.5',
		minHeight: '56px'
	};
	const placeholderStyle = 'color: #C7C7CC';
	const datePickerMaskStyle =
		'background-image: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.65)), linear-gradient(0deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.65));';
	const minDate = Date.now();

	const product = ref({
		id: '',
		roomId: '',
		title: '',
		subtitle: '',
		roomName: '',
		cover: PLACEHOLDER_LOGO,
		unitPrice: 0
	});
	const hotelForm = ref(createDefaultHotelForm());
	const showCheckInPicker = ref(false);
	const showCheckOutPicker = ref(false);
	const checkInPickerValue = ref(Date.now());
	const checkOutPickerValue = ref(Date.now() + 86400000);

	const couponScope = computed(() => COUPON_SCOPE_HOTEL);
	const orderCategory = computed(() => ORDER_CATEGORY_HOTEL);
	const nights = computed(() => calcNights(hotelForm.value.checkIn, hotelForm.value.checkOut));
	const nightsText = computed(() => (nights.value > 0 ? `${nights.value}${ui.nightsSuffix}` : ui.nightsPending + ui
		.nightsSuffix));
	const checkOutMinDate = computed(() => {
		if (!hotelForm.value.checkIn) return minDate + 86400000;
		const d = new Date(hotelForm.value.checkIn.replace(/-/g, '/'));
		d.setDate(d.getDate() + 1);
		return d.getTime();
	});
	const roomName = computed(() => {
		const sub = product.value.subtitle || '';
		const sep = ui.roomSeparator.trim();
		if (!sub) return '';
		const idx = sub.indexOf(sep);
		return idx > -1 ? sub.slice(0, idx) : sub;
	});
	const displayRoomName = computed(() => product.value.roomName || roomName.value || '精选房型');
	const subtotalAmount = computed(() => {
		const price = Number(product.value.unitPrice) || 0;
		const rooms = hotelForm.value.roomCount || 1;
		return price * (nights.value || 0) * rooms;
	});
	const breakdownLine = computed(() => {
		const name = displayRoomName.value;
		if (nights.value <= 0) return name;
		const rooms = hotelForm.value.roomCount || 1;
		return `${name} x ${rooms}间 x ${nights.value}${ui.nightsSuffix}`;
	});
	const breakdownAmount = computed(() => formatMoneyText(product.value.unitPrice * nights.value));
	const totalAmountDisplay = computed(() => (nights.value <= 0 ? '--' : totalAmount.value));
	const submitBtnText = computed(() => (product.value.unitPrice === 0 ? ui.submitBook : ui.submitPay));
	const checkInParts = computed(() => parseDateParts(hotelForm.value.checkIn));
	const checkOutParts = computed(() => parseDateParts(hotelForm.value.checkOut));
	const policyList = computed(() => [ui.policyCheckIn, ui.policyCancel, ui.policyId, ui.policyContact]);

	const {
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
		validateForm: validateHotelForm,
		buildCreateOrderPayload,
		accentColor,
		priceColor: '#FF4757'
	});

	const totalPriceText = computed(() => formatMoney(totalAmount.value));

	watch(() => hotelForm.value.guestCount, (count) => {
		hotelForm.value.guestNames = syncGuestNames(hotelForm.value.guestNames, count, hotelForm.value.guestNames[0] ||
			'');
	});

	function parseDateParts(dateStr) {
		if (!dateStr) return {
			day: '',
			month: '',
			weekday: ''
		};
		const d = new Date(dateStr.replace(/-/g, '/'));
		if (Number.isNaN(d.getTime())) return {
			day: '',
			month: '',
			weekday: ''
		};
		return {
			day: String(d.getDate()),
			month: `${d.getMonth() + 1}月`,
			weekday: getWeekdayLabel(dateStr)
		};
	}

	function changeGuestCount(delta) {
		const next = hotelForm.value.guestCount + delta;
		if (next < 1 || next > 4) return;
		hotelForm.value.guestCount = next;
	}

	function changeRoomCount(delta) {
		const next = (hotelForm.value.roomCount || 1) + delta;
		if (next < 1 || next > 5) return;
		hotelForm.value.roomCount = next;
	}

	function openCheckInPicker() {
		showCheckInPicker.value = true;
	}

	function openCheckOutPicker() {
		if (!hotelForm.value.checkIn) {
			uni.showToast({
				title: ui.toastCheckIn,
				icon: 'none'
			});
			return;
		}
		showCheckOutPicker.value = true;
	}

	function onCheckInConfirm(e) {
		hotelForm.value.checkIn = formatDateStr(e.value);
		if (hotelForm.value.checkOut && calcNights(hotelForm.value.checkIn, hotelForm.value.checkOut) <= 0) {
			hotelForm.value.checkOut = '';
		}
		showCheckInPicker.value = false;
	}

	function onCheckOutConfirm(e) {
		hotelForm.value.checkOut = formatDateStr(e.value);
		showCheckOutPicker.value = false;
	}

	function validatePhone(phone) {
		if (!/^1\d{10}$/.test(phone)) {
			uni.showToast({
				title: ui.toastPhone,
				icon: 'none'
			});
			return false;
		}
		return true;
	}

	function validateHotelForm() {
		if (!product.value.roomId) {
			uni.showToast({
				title: '请先选择房型',
				icon: 'none'
			});
			return false;
		}
		if (!hotelForm.value.checkIn) {
			uni.showToast({
				title: ui.toastCheckIn,
				icon: 'none'
			});
			return false;
		}
		if (!hotelForm.value.checkOut) {
			uni.showToast({
				title: ui.toastCheckOut,
				icon: 'none'
			});
			return false;
		}
		if (nights.value <= 0) {
			uni.showToast({
				title: ui.toastCheckoutBeforeCheckin,
				icon: 'none'
			});
			return false;
		}
		if (hotelForm.value.guestNames.some((name) => !String(name).trim())) {
			uni.showToast({
				title: ui.toastGuestName,
				icon: 'none'
			});
			return false;
		}
		return validatePhone(hotelForm.value.phone);
	}

	function buildCreateOrderPayload() {
		const payload = {
			category: 'hotel',
			roomCount: hotelForm.value.roomCount || 1,
			expectedArrivalTime: hotelForm.value.expectedArrivalTime || '',
			guestCount: hotelForm.value.guestCount,
			guestNames: hotelForm.value.guestNames.filter((name) => String(name).trim()),
			contactName: hotelForm.value.guestNames[0] || '',
			contactPhone: hotelForm.value.phone,
			remark: hotelForm.value.remark,
			unitPrice: product.value.unitPrice,
			hotelId: Number(product.value.id),
			roomId: Number(product.value.roomId),
			checkInDate: hotelForm.value.checkIn,
			checkOutDate: hotelForm.value.checkOut
		};
		if (selectedCoupon.value?.id) payload.couponId = Number(selectedCoupon.value.id);
		return payload;
	}

	function resolveRoomCover(data) {
		const images = (data?.gallery || []).filter(Boolean);
		if (images.length) return images[0];
		if (data?.coverImage) return data.coverImage;
		return PLACEHOLDER_LOGO;
	}

	function applyRoomDetail(data) {
		if (!data) return;
		if (data.name) product.value.roomName = data.name;
		if (data.hotelName) product.value.title = data.hotelName;
		if (data.price != null) product.value.unitPrice = Number(data.price) || product.value.unitPrice;
		product.value.cover = resolveRoomCover(data);
		if (data.spec) {
			product.value.subtitle = buildRoomSubtitle(data.name || product.value.roomName, data.spec);
		}
	}

	async function loadRoomDetail() {
		const hotelId = product.value.id;
		const roomId = product.value.roomId;
		if (!hotelId || !roomId) return;
		try {
			const res = await fetchHotelRoomDetail(hotelId, roomId);
			if (res.code === 0 && res.data) {
				applyRoomDetail(res.data);
			}
		} catch (e) {
			/* 保留 URL 参数兜底 */
		}
	}

	function loadProduct(options) {
		const title = options?.title ? decodeURIComponent(options.title) : '';
		const room = options?.room ? decodeURIComponent(options.room) : '';
		const spec = options?.subtitle ? decodeURIComponent(options.subtitle) : '';
		product.value = {
			id: String(options?.id || ''),
			roomId: String(options?.roomId || ''),
			title,
			subtitle: buildRoomSubtitle(room, spec),
			roomName: room,
			cover: options?.cover ? decodeURIComponent(options.cover) : PLACEHOLDER_LOGO,
			unitPrice: Number(options?.price || 0)
		};
		uni.setNavigationBarTitle({
			title: ui.navTitleHotel
		});
		loadRoomDetail();
	}

	onLoad(loadProduct);
	onShow(onBookingShow);
</script>

<style lang="scss" scoped>
	.hc-page {
		min-height: 100vh;
		background: #EEF2F8;
		position: relative;
	}

	.hc-ambient {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		height: 520rpx;
		background: linear-gradient(165deg, #DCE8FF 0%, #EEF2F8 55%, transparent 100%);
		pointer-events: none;
		z-index: 0;
	}

	.hc-scroll {
		height: 100vh;
		position: relative;
		z-index: 1;
	}

	.hc-inner {
		padding: 24rpx var(--ry-page-padding-x) calc(180rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	/* ── 票券摘要 ── */
	.hc-ticket {
		position: relative;
		border-radius: 28rpx;
		background: #FFF;
		box-shadow: 0 16rpx 48rpx rgba(20, 55, 120, 0.10);
		overflow: hidden;

		&__shine {
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			height: 120rpx;
			background: linear-gradient(135deg, rgba(41, 121, 255, 0.08) 0%, transparent 60%);
			pointer-events: none;
		}

		&__body {
			padding: 36rpx 32rpx 28rpx;
		}

		&__row {
			display: flex;
			align-items: center;
			gap: 20rpx;
		}

		&__cover {
			flex-shrink: 0;
			width: 128rpx;
			height: 128rpx;
			border-radius: 16rpx;
			background: #F0F4FA;
		}

		&__info {
			flex: 1;
			min-width: 0;
			align-self: flex-start;
		}

		&__hotel {
			display: block;
			font-size: 22rpx;
			color: #8E8E93;
			letter-spacing: 1rpx;
			line-height: 1.4;
			margin-bottom: 10rpx;
		}

		&__room {
			display: block;
			font-size: 32rpx;
			font-weight: 700;
			color: #1A1A1A;
			line-height: 1.35;
			letter-spacing: -0.5rpx;
		}

		&__price-box {
			flex-shrink: 0;
			text-align: right;
			padding-top: 4rpx;
		}

		&__price-tag {
			display: block;
			font-size: 20rpx;
			color: #8E8E93;
			margin-bottom: 4rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;
			justify-content: flex-end;
			gap: 2rpx;
			color: #FF4757;
		}

		&__symbol {
			font-size: 24rpx;
			font-weight: 500;
		}

		&__amount {
			font-size: 44rpx;
			font-weight: 700;
			letter-spacing: -1rpx;
		}

		&__tear {
			display: flex;
			align-items: center;
			padding: 0 24rpx;
		}

		&__notch {
			width: 24rpx;
			height: 24rpx;
			border-radius: 50%;
			background: #EEF2F8;
			flex-shrink: 0;

			&--l {
				margin-left: -12rpx;
			}

			&--r {
				margin-right: -12rpx;
			}
		}

		&__line {
			flex: 1;
			height: 0;
			border-top: 2rpx dashed #E5EAF2;
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			padding: 20rpx 32rpx 28rpx;
			background: linear-gradient(180deg, #FAFBFD 0%, #F5F8FC 100%);
		}

		&__foot-text {
			font-size: 24rpx;
			color: #666;
		}
	}

	/* ── 卡片区块 ── */
	.hc-card {
		padding: 28rpx;
		border-radius: 24rpx;
		background: #FFF;
		box-shadow: 0 8rpx 32rpx rgba(20, 55, 120, 0.06);

		&--tap:active {
			opacity: 0.92;
			transform: scale(0.995);
		}

		&--policy {
			box-shadow: none;
			background: rgba(255, 255, 255, 0.72);
			border: 1rpx solid rgba(255, 255, 255, 0.9);
		}

		&__head {
			display: flex;
			align-items: center;
			gap: 14rpx;
			// margin-bottom: 24rpx;

			&--row {
				justify-content: space-between;
				// margin-bottom: 20rpx;
			}
		}

		&__head-left {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 14rpx;
		}

		&__icon {
			flex-shrink: 0;
			width: 52rpx;
			height: 52rpx;
			border-radius: 16rpx;
			background: linear-gradient(135deg, #EAF2FF, #F0F6FF);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__titles {
			flex: 1;
			min-width: 0;
		}

		&__title {
			display: block;
			font-size: 30rpx;
			font-weight: 500;
			color: #1A1A1A;
			line-height: 1.35;
		}

		&__sub {
			display: block;
			margin-top: 4rpx;
			font-size: 22rpx;
			color: #AEAEB2;
			line-height: 1.3;
		}

		&__head--row &__title,
		&__head:not(&__head--row)>&__title {
			margin-bottom: 0;
		}
	}

	/* ── 日期选择 ── */
	.hc-stay {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.hc-stay__cell {
		flex: 0 0 auto;
		width: 240rpx;
		padding: 16rpx 12rpx;
		border-radius: 16rpx;
		background: #F7F9FC;
		border: 2rpx solid transparent;
		box-sizing: border-box;
		transition: border-color 0.2s, background 0.2s;

		&:active {
			opacity: 0.88;
		}

		&--filled {
			background: #FFF;
			border-color: rgba(41, 121, 255, 0.25);
			box-shadow: 0 4rpx 16rpx rgba(41, 121, 255, 0.08);
		}
	}

	.hc-stay__label {
		display: block;
		font-size: 20rpx;
		color: #AEAEB2;
		letter-spacing: 2rpx;
		margin-bottom: 8rpx;
	}

	.hc-stay__date {
		display: flex;
		align-items: baseline;
		gap: 6rpx;
	}

	.hc-stay__month {
		font-size: 24rpx;
		color: #666;
		font-weight: 400;
	}

	.hc-stay__day {
		font-size: 40rpx;
		font-weight: 700;
		color: #1A1A1A;
		line-height: 1;
		letter-spacing: -1rpx;
	}

	.hc-stay__week {
		display: block;
		margin-top: 4rpx;
		font-size: 20rpx;
		color: #8E8E93;
	}

	.hc-stay__empty {
		font-size: 24rpx;
		color: #C7C7CC;
		padding-top: 8rpx;
	}

	.hc-stay__bridge {
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		width: 64rpx;
		gap: 0;
		padding: 0;
		align-self: center;
	}

	.hc-stay__bridge-line {
		width: 2rpx;
		height: 20rpx;
		background: linear-gradient(180deg, transparent, #D1D9E6, transparent);
	}

	.hc-stay__badge {
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 64rpx;
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #FF6600, #ffa238);
		box-shadow: 0 4rpx 12rpx rgba(41, 121, 255, 0.35);
		text-align: center;

		text {
			font-size: 20rpx;
			font-weight: 500;
			color: #FFF;
			white-space: nowrap;
			text-align: center;
			line-height: 1.2;
		}
	}

	/* ── 人数步进器 ── */
	.hc-counter {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex-shrink: 0;

		&__btn {
			width: 52rpx;
			height: 52rpx;
			border-radius: 50%;
			background: #F2F4F8;
			display: flex;
			align-items: center;
			justify-content: center;

			&--plus {
				background: linear-gradient(135deg, #FF6600, #ffa238);
				box-shadow: 0 4rpx 12rpx rgba(41, 121, 255, 0.3);
			}

			&--off {
				opacity: 0.3;
			}
		}

		&__num {
			min-width: 32rpx;
			text-align: center;
			font-size: 30rpx;
			font-weight: 500;
			color: #1A1A1A;
		}
	}

	/* ── 预订人表单 ── */
	.hc-form {
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__field {
			width: 100%;
			box-sizing: border-box;
			padding: 20rpx 24rpx;
			border-radius: 16rpx;
			background: #F7F9FC;
			// border: 1rpx solid #EEF2F8;
			display: flex;
			align-items: center;



			// :deep(.u-input__content__field-wrapper__field),
			// :deep(.uni-input-input),
			// :deep(.u-textarea__field),
			:deep(textarea) {
				color: #1A1A1A !important;
				font-size: 28rpx;
			}

			:deep(.uni-input-placeholder),
			:deep(.uni-input-input) {
				text-align: right;
				font-size: 28rpx;
			}

			&--remark {
				margin-top: 16rpx;
				min-height: 88rpx;
				padding: 16rpx 20rpx;
			}
		}

		&__label {
			display: block;
			font-size: 26rpx;
			color: #AEAEB2;
			// margin-bottom: 8rpx;
			letter-spacing: 1rpx;
		}
	}

	.hc-card--remark {
		.hc-card__head {
			margin-bottom: 0;
		}
	}

	/* ── 优惠券 ── */
	.hc-coupon {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;

		&__left {
			display: flex;
			align-items: center;
			gap: 14rpx;
		}

		&__icon {
			width: 48rpx;
			height: 48rpx;
			border-radius: 14rpx;
			background: linear-gradient(135deg, #FFF5F0, #FFEDE5);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__label {
			font-size: 28rpx;
			color: #1A1A1A;
		}

		&__right {
			display: flex;
			align-items: center;
			gap: 8rpx;
		}

		&__discount {
			font-size: 26rpx;
			color: #FF4757;
		}

		&__hint {
			font-size: 26rpx;
			color: #2979FF;
		}

		&__none {
			font-size: 26rpx;
			color: #C7C7CC;
		}
	}

	/* ── 账单 ── */
	.hc-bill {
		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 20rpx;
			padding: 12rpx 0;

			&--total {
				padding-top: 20rpx;
			}
		}

		&__label {
			flex: 1;
			font-size: 26rpx;
			color: #666;
			line-height: 1.4;
		}

		&__value {
			font-size: 26rpx;
			color: #333;
			flex-shrink: 0;

			&--off {
				color: #FF4757;
			}
		}

		&__sep {
			height: 1rpx;
			margin: 8rpx 0;
			background: linear-gradient(90deg, transparent, #E5EAF2, transparent);
		}

		&__total-label {
			font-size: 28rpx;
			color: #1A1A1A;
			font-weight: 500;
		}

		&__total {
			font-size: 36rpx;
			font-weight: 700;
			color: #FF4757;
			letter-spacing: -0.5rpx;
		}

		&__note {
			display: block;
			margin-top: 12rpx;
			font-size: 20rpx;
			color: #C7C7CC;
			line-height: 1.5;
		}
	}

	/* ── 须知 ── */
	.hc-policy {
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__item {
			display: flex;
			align-items: flex-start;
			gap: 14rpx;
		}

		&__dot {
			flex-shrink: 0;
			width: 8rpx;
			height: 8rpx;
			border-radius: 50%;
			margin-top: 12rpx;
			background: linear-gradient(135deg, #FF6600, #ffa238);
		}

		&__text {
			flex: 1;
			font-size: 24rpx;
			color: #8E8E93;
			line-height: 1.65;
		}
	}

	/* ── 底部栏 ── */
	.hc-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;

		&__glass {
			position: absolute;
			inset: 0;
			background: rgba(255, 255, 255, 0.88);
			backdrop-filter: blur(24px);
			border-top: 1rpx solid rgba(255, 255, 255, 0.8);
			box-shadow: 0 -8rpx 40rpx rgba(20, 55, 120, 0.08);
		}

		&__inner {
			position: relative;
			display: flex;
			align-items: center;
			gap: 24rpx;
			padding: 20rpx var(--ry-page-padding-x);
			padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		}

		&__info {
			flex: 1;
			min-width: 0;
		}

		&__label {
			font-size: 20rpx;
			color: #AEAEB2;
			letter-spacing: 1rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;
			gap: 2rpx;
			margin-top: 2rpx;
			color: #FF4757;
		}

		&__symbol {
			font-size: 26rpx;
			font-weight: 500;
		}

		&__amount {
			font-size: 48rpx;
			font-weight: 700;
			letter-spacing: -1rpx;
			line-height: 1.1;
		}

		&__btn {
			flex-shrink: 0;
			min-width: 260rpx;
			height: 92rpx;
			padding: 0 36rpx;
			border-radius: 46rpx;
			background: linear-gradient(135deg, #FF6600, #ffa238);
			box-shadow: 0 8rpx 28rpx rgba(255, 102, 0, 0.38);
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;

			&:active {
				opacity: 0.92;
				transform: scale(0.98);
			}
		}

		&__btn-text {
			font-size: 30rpx;
			font-weight: 400;
			color: #FFF;
			letter-spacing: 1rpx;
		}
	}

	/* 日期选择弹框：强制白色主题（覆盖 dark mode） */
	.hc-date-picker {
		:deep(.u-popup__content) {
			background-color: #fff !important;
		}

		:deep(.u-picker),
		:deep(.u-toolbar),
		:deep(.u-picker__view) {
			background-color: #fff;
		}

		:deep(.u-picker__view__column__item) {
			color: rgba(0, 0, 0, 0.25) !important;
		}

		:deep(.u-picker__view__column__item--selected) {
			color: #1a1a1a !important;
		}
	}
</style>
