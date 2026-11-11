<template>
	<view v-if="order" class="rd-page" :class="{ 'rd-page--with-footer': merchantActions.length }" :style="pageStyle">
		<!-- 顶部状态 -->
		<view class="rd-hero" :style="{ background: theme.heroGradient }">
			<view class="rd-hero__orb rd-hero__orb--1" />
			<view class="rd-hero__orb rd-hero__orb--2" />
			<view class="rd-hero__content">
				<view class="rd-hero__top">
					<text class="rd-hero__status">{{ order.statusLabel }}</text>
					<view class="rd-hero__pill">
						<text>{{ order.orderTypeLabel }}</text>
					</view>
				</view>
				<text v-if="statusHint" class="rd-hero__hint">{{ statusHint }}</text>
				<view class="rd-hero__no-row">
					<text class="rd-hero__no">{{ moui.labelBookingNo }} {{ order.orderNo }}</text>
					<text class="rd-hero__copy" @click="copyOrderNo">{{ moui.copyOrderNo }}</text>
				</view>
			</view>
		</view>

		<view class="rd-stack">
			<!-- 预约时间主卡 -->
			<view class="rd-reserve">
				<view v-if="hasReservation" class="rd-reserve__date" :style="{ background: theme.primaryLight }">
					<text class="rd-reserve__day" :style="{ color: theme.primary }">{{ resDay }}</text>
					<text v-if="resTime" class="rd-reserve__time" :style="{ color: theme.primary }">{{ resTime }}</text>
					<text class="rd-reserve__sub">{{ resDateSub }}</text>
				</view>
				<view v-else class="rd-reserve__date rd-reserve__date--placeholder" :style="{ background: theme.primaryLight }">
					<u-icon name="calendar-fill" :color="theme.primary" :size="28" />
					<text class="rd-reserve__sub">待安排到店时间</text>
				</view>
				<view class="rd-reserve__body">
					<view class="rd-reserve__title-row">
						<text class="rd-reserve__name">{{ order.customerName || '预约客人' }}</text>
						<text v-if="order.guestCount" class="rd-reserve__guest">{{ order.guestCount }}人</text>
					</view>
					<view v-if="dishLabel" class="rd-reserve__dish">
						<u-icon name="bag-fill" :color="theme.primary" :size="14" />
						<text>{{ dishLabel }}</text>
					</view>
					<view v-if="metaChips.length" class="rd-reserve__chips">
						<view v-for="chip in metaChips" :key="chip.key" class="rd-reserve__chip">
							<u-icon :name="chip.icon" :color="theme.primary" :size="14" />
							<text>{{ chip.text }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 进度时间轴 -->
			<view v-if="showTimeline" class="rd-timeline">
				<view v-for="(step, index) in timelineSteps" :key="step.key" class="rd-timeline__item"
					:class="`rd-timeline__item--${step.state}`">
					<view class="rd-timeline__track">
						<view class="rd-timeline__dot" :style="timelineDotStyle(step)" />
						<view v-if="index < timelineSteps.length - 1" class="rd-timeline__line"
							:class="{ 'rd-timeline__line--done': step.state === 'done' }" />
					</view>
					<text class="rd-timeline__label">{{ step.label }}</text>
				</view>
			</view>

			<!-- 统一信息区 -->
			<view class="rd-sheet">
				<view class="rd-section">
					<text class="rd-section__title">{{ moui.labelBookingInfo }}</text>
					<view class="rd-rows">
						<view v-if="order.reservationDate" class="rd-row">
							<text class="rd-row__label">预约日期</text>
							<text class="rd-row__value">{{ reservationDateDisplay }}</text>
						</view>
						<view v-if="resTime" class="rd-row">
							<text class="rd-row__label">到店时间</text>
							<text class="rd-row__value">{{ resTime }}</text>
						</view>
						<view v-if="order.guestCount" class="rd-row">
							<text class="rd-row__label">{{ moui.labelGuestCount }}</text>
							<text class="rd-row__value">{{ order.guestCount }}{{ moui.guestSuffix }}</text>
						</view>
						<view v-if="dishLabel" class="rd-row">
							<text class="rd-row__label">预约菜品</text>
							<text class="rd-row__value">{{ dishLabel }}</text>
						</view>
						<view v-if="storeName" class="rd-row">
							<text class="rd-row__label">门店</text>
							<text class="rd-row__value">{{ storeName }}</text>
						</view>
					</view>
				</view>

				<view class="rd-section-divider" />

				<view class="rd-section">
					<text class="rd-section__title">{{ moui.labelContact }}</text>
					<view class="rd-phone-bar">
						<view class="rd-phone-bar__info">
							<text class="rd-phone-bar__name">{{ order.customerName || '--' }}</text>
							<text class="rd-phone-bar__num">{{ displayPhone }}</text>
						</view>
						<view v-if="order.customerPhone" class="rd-phone-bar__btn" :style="{ background: theme.primary }"
							@click="callPhone">
							<u-icon name="phone-fill" color="#FFF" :size="16" />
							<text>{{ moui.callPhone }}</text>
						</view>
					</view>
				</view>

				<view class="rd-section-divider" />

				<view class="rd-section">
					<text class="rd-section__title">{{ moui.labelOrderInfo }}</text>
					<view class="rd-rows">
						<view class="rd-row">
							<text class="rd-row__label">{{ moui.labelBookingType }}</text>
							<text class="rd-row__value">{{ order.orderTypeLabel }}</text>
						</view>
						<view class="rd-row">
							<text class="rd-row__label">{{ moui.labelBookingTime }}</text>
							<text class="rd-row__value">{{ order.createdAt || '--' }}</text>
						</view>
						<view v-if="order.paidAt" class="rd-row">
							<text class="rd-row__label">{{ moui.labelPayTime }}</text>
							<text class="rd-row__value">{{ order.paidAt }}</text>
						</view>
						<view v-if="orderAmount > 0" class="rd-row rd-row--amount">
							<text class="rd-row__label">{{ moui.labelPayAmount }}</text>
							<text class="rd-row__value rd-row__value--amount" :style="{ color: theme.primary }">
								{{ formatMoney(orderAmount) }}
							</text>
						</view>
						<view v-else class="rd-row">
							<text class="rd-row__label">{{ moui.labelPayAmount }}</text>
							<text class="rd-row__value rd-row__value--free">{{ moui.labelFreeBooking }}</text>
						</view>
					</view>
				</view>

				<template v-if="order.userRemark">
					<view class="rd-section-divider" />
					<view class="rd-section">
						<text class="rd-section__title">{{ moui.labelRemark }}</text>
						<view class="rd-remark">
							<text>{{ order.userRemark }}</text>
						</view>
					</view>
				</template>
			</view>
			<view v-if="merchantActions.length" class="rd-stack__safe" />
		</view>

		<!-- 底部操作 -->
		<view v-if="merchantActions.length" class="rd-footer">
			<view v-for="action in merchantActions" :key="action.key" class="rd-footer__btn"
				:style="{ background: theme.primary }" :class="{ 'rd-footer__btn--loading': submitting }"
				@click="onAction(action.key)">
				<u-loading-icon v-if="submitting" mode="circle" color="#FFF" :size="18" />
				<text v-else>{{ action.label }}</text>
			</view>
		</view>
	</view>

	<view v-else-if="loading" class="rd-loading">
		<u-loading-icon mode="circle" :color="theme.primary" />
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		formatMoney,
		maskPhone,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		MERCHANT_ORDER_UI,
		formatRestaurantArrivalTime,
		formatRestaurantResDay,
		formatRestaurantResMonthWeek,
		mapRestaurantMerchantDetailOrder,
		getRestaurantStatusHint,
		buildRestaurantTimeline,
		parseRestaurantWeekday
	} from '@/common/merchant-order-ui.js';
	import {
		getMerchantOrderActions,
		runMerchantOrderAction
	} from '@/common/order-actions.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrderDetail
	} from '@/common/api/merchant.js';

	const PACKAGE_STORE_TYPE = 'restaurant';

	const {
		theme,
		pageStyle,
		businessUi,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const ui = businessUi;
	const moui = MERCHANT_ORDER_UI;
	const orderId = ref('');
	const order = ref(null);
	const loading = ref(false);
	const submitting = ref(false);

	const merchantActions = computed(() => getMerchantOrderActions(order.value));
	const statusHint = computed(() => getRestaurantStatusHint(order.value));
	const timelineSteps = computed(() => buildRestaurantTimeline(order.value));
	const showTimeline = computed(() => {
		const status = order.value?.status;
		return status && status !== 'cancelled' && status !== 'refunding';
	});
	const dishLabel = computed(() => order.value?.dishLabel || '');
	const storeName = computed(() => order.value?.storeName || '');
	const hasReservation = computed(() => Boolean(order.value?.reservationDate));
	const orderAmount = computed(() => Number(order.value?.amount) || 0);

	const displayPhone = computed(() => {
		const phone = order.value?.customerPhone;
		return phone ? maskPhone(String(phone)) : '--';
	});

	const resDay = computed(() => formatRestaurantResDay(order.value?.reservationDate));
	const resTime = computed(() => formatRestaurantArrivalTime(order.value?.reservationTime));
	const resDateSub = computed(() => formatRestaurantResMonthWeek(order.value?.reservationDate));

	const reservationDateDisplay = computed(() => {
		const dateStr = order.value?.reservationDate;
		if (!dateStr) return '';
		const week = parseRestaurantWeekday(dateStr);
		return week ? `${dateStr}（${week}）` : dateStr;
	});

	const metaChips = computed(() => {
		if (!order.value) return [];
		const rows = [];
		if (order.value.customerPhone) {
			rows.push({
				key: 'phone',
				icon: 'phone-fill',
				text: maskPhone(order.value.customerPhone)
			});
		}
		if (order.value.highlightLine && !order.value.reservationDate) {
			rows.push({
				key: 'reserve',
				icon: 'calendar-fill',
				text: order.value.highlightLine.replace(/^预约\s*/, '')
			});
		}
		return rows;
	});

	function timelineDotStyle(step) {
		if (step.state === 'done' || step.state === 'active') {
			return { background: theme.value.primary, borderColor: theme.value.primary };
		}
		if (step.state === 'cancelled') {
			return { background: '#FF4D4F', borderColor: '#FF4D4F' };
		}
		return { background: '#FFFFFF', borderColor: '#D8DCE3' };
	}

	async function reload() {
		if (!orderId.value) return;
		loading.value = true;
		try {
			const res = await fetchMerchantOrderDetail(orderId.value);
			if (res.code === 0 && res.data) {
				order.value = mapRestaurantMerchantDetailOrder(res.data);
			} else {
				order.value = null;
				uni.showToast({
					title: res.msg || ui.value.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			order.value = null;
			uni.showToast({
				title: ui.value.networkError,
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function copyOrderNo() {
		if (!order.value?.orderNo) return;
		uni.setClipboardData({
			data: order.value.orderNo,
			success: () => uni.showToast({
				title: moui.copyBookingSuccess,
				icon: 'success'
			})
		});
	}

	function callPhone() {
		const phone = order.value?.customerPhone;
		if (!phone) return;
		uni.makePhoneCall({
			phoneNumber: String(phone)
		});
	}

	async function onAction(actionKey) {
		if (submitting.value) return;
		submitting.value = true;
		try {
			await runMerchantOrderAction(actionKey, order.value, {
				onChanged: async (data) => {
					if (data) {
						order.value = mapRestaurantMerchantDetailOrder(data);
					} else {
						await reload();
					}
				}
			});
		} finally {
			submitting.value = false;
		}
	}

	onLoad(async (options) => {
		orderId.value = options?.id || '';
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		uni.setNavigationBarTitle({
			title: ui.value.orderDetailTitle || '预约详情'
		});
		applyNavBarColor();
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		applyNavBarColor();
		uni.setNavigationBarTitle({
			title: ui.value.orderDetailTitle || '预约详情'
		});
		await reload();
	});
</script>

<style lang="scss" scoped>
	.rd-page {
		min-height: 100vh;
		background: #F0F2F5;
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
		box-sizing: border-box;

		&--with-footer {
			padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
		}
	}

	.rd-loading {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #F0F2F5;
	}

	.rd-hero {
		position: relative;
		z-index: 1;
		overflow: hidden;
		padding: 32rpx var(--ry-page-padding-x) 36rpx;

		&__orb {
			position: absolute;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.12);

			&--1 {
				width: 240rpx;
				height: 240rpx;
				top: -80rpx;
				right: -40rpx;
			}

			&--2 {
				width: 120rpx;
				height: 120rpx;
				bottom: 20rpx;
				left: -30rpx;
			}
		}

		&__content {
			position: relative;
			z-index: 1;
		}

		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__status {
			font-size: 44rpx;
			font-weight: 800;
			color: #FFFFFF;
			line-height: 1.2;
		}

		&__pill {
			flex-shrink: 0;
			padding: 0 18rpx;
			min-height: 44rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.2);
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 22rpx;
				font-weight: 600;
				color: rgba(255, 255, 255, 0.95);
			}
		}

		&__hint {
			display: block;
			margin-top: 12rpx;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.88);
			line-height: 1.5;
		}

		&__no-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-top: 20rpx;
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.78);
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			color: #FFFFFF;
			padding: 6rpx 16rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.18);
		}
	}

	.rd-stack {
		position: relative;
		z-index: 2;
		margin-top: 16rpx;
		padding: 0 var(--ry-page-padding-x);
		display: flex;
		flex-direction: column;
		gap: 20rpx;

		&__safe {
			height: 32rpx;
			flex-shrink: 0;
		}
	}

	.rd-reserve {
		display: flex;
		gap: 20rpx;
		padding: 24rpx;
		background: #FFFFFF;
		border-radius: 24rpx;
		box-shadow: 0 4rpx 24rpx rgba(15, 23, 42, 0.06);

		&__date {
			flex-shrink: 0;
			width: 120rpx;
			padding: 16rpx 12rpx;
			border-radius: 16rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 4rpx;

			&--placeholder {
				width: 120rpx;
				gap: 8rpx;
			}
		}

		&__day {
			font-size: 48rpx;
			font-weight: 800;
			line-height: 1;
		}

		&__time {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1.2;
		}

		&__sub {
			font-size: 20rpx;
			color: #6B6B78;
			text-align: center;
			line-height: 1.35;
		}

		&__body {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 12rpx;
		}

		&__title-row {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 32rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.35;
		}

		&__guest {
			flex-shrink: 0;
			padding: 4rpx 14rpx;
			border-radius: 999rpx;
			background: #F2F3F7;
			font-size: 22rpx;
			font-weight: 600;
			color: #6B6B78;
		}

		&__dish {
			display: flex;
			align-items: center;
			gap: 8rpx;
			font-size: 26rpx;
			color: #4A4A56;
			line-height: 1.4;
		}

		&__chips {
			display: flex;
			flex-direction: column;
			align-items: flex-start;
			gap: 10rpx;
		}

		&__chip {
			display: flex;
			align-items: center;
			gap: 6rpx;
			padding: 8rpx 14rpx;
			border-radius: 999rpx;
			background: #F8F9FB;
			font-size: 22rpx;
			color: #4A4A56;
			line-height: 1.4;
		}
	}

	.rd-timeline {
		display: flex;
		padding: 24rpx 20rpx;
		background: #FFFFFF;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);

		&__item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 10rpx;
			min-width: 0;

			&--cancelled .rd-timeline__label {
				color: #FF4D4F;
			}

			&--pending .rd-timeline__label {
				color: #C0C0C8;
			}
		}

		&__track {
			position: relative;
			width: 100%;
			height: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__dot {
			width: 20rpx;
			height: 20rpx;
			border-radius: 50%;
			border: 3rpx solid #D8DCE3;
			background: #FFFFFF;
			z-index: 1;
		}

		&__line {
			position: absolute;
			left: 50%;
			right: -50%;
			top: 50%;
			height: 3rpx;
			margin-top: -1rpx;
			background: #E8EAED;

			&--done {
				background: #FF8533;
			}
		}

		&__label {
			font-size: 20rpx;
			color: #1A1A22;
			text-align: center;
			line-height: 1.3;
			padding: 0 4rpx;
		}
	}

	.rd-sheet {
		background: #FFFFFF;
		border-radius: 20rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);
	}

	.rd-section {
		padding: 28rpx;

		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A22;
			margin-bottom: 20rpx;
			line-height: 1.3;
		}
	}

	.rd-section-divider {
		height: 1rpx;
		margin: 0 28rpx;
		background: #F2F3F5;
	}

	.rd-rows {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.rd-row {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 24rpx;
		min-width: 0;

		&--amount {
			padding-top: 8rpx;
			align-items: center;
		}

		&__label {
			flex-shrink: 0;
			font-size: 26rpx;
			color: #9A9AA6;
			line-height: 1.5;
		}

		&__value {
			flex: 1;
			min-width: 0;
			font-size: 26rpx;
			color: #1A1A22;
			line-height: 1.5;
			text-align: right;
			word-break: break-all;

			&--amount {
				font-size: 36rpx;
				font-weight: 800;
				line-height: 1.2;
			}

			&--free {
				color: #2EAF6A;
				font-weight: 600;
			}
		}
	}

	.rd-phone-bar {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #F8F9FB;

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: row;
			align-items: center;
			gap: 16rpx;
		}

		&__name {
			flex-shrink: 0;
			font-size: 28rpx;
			font-weight: 600;
			color: #1A1A22;
			line-height: 1.3;
		}

		&__num {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #6B6B78;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__btn {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 8rpx;
			padding: 14rpx 22rpx;
			border-radius: 999rpx;

			text {
				font-size: 24rpx;
				font-weight: 600;
				color: #FFFFFF;
			}

			&:active {
				opacity: 0.88;
			}
		}
	}

	.rd-remark {
		padding: 20rpx;
		border-radius: 16rpx;
		background: #FFF8F0;
		border: 1rpx solid #FFE8D6;

		text {
			font-size: 26rpx;
			color: #4A4A56;
			line-height: 1.65;
		}
	}

	.rd-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: #FFFFFF;
		border-top: 1rpx solid #F0F0F0;
		box-sizing: border-box;

		&__btn {
			flex: 1;
			height: 96rpx;
			border-radius: 48rpx;
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 30rpx;
				font-weight: 700;
				color: #FFFFFF;
			}

			&--loading {
				opacity: 0.85;
			}

			&:active {
				opacity: 0.9;
			}
		}
	}
</style>
