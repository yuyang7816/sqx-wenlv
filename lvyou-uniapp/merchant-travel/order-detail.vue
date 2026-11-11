<template>
	<view v-if="order" class="od-page" :class="{ 'od-page--with-footer': primaryAction || secondaryAction }" :style="pageStyle">
		<!-- 顶部状态区 -->
		<view class="od-hero" :style="{ background: theme.heroGradient }">
			<view class="od-hero__orb od-hero__orb--1" />
			<view class="od-hero__orb od-hero__orb--2" />
			<view class="od-hero__content">
				<view class="od-hero__top">
					<text class="od-hero__status">{{ order.statusLabel }}</text>
					<view class="od-hero__pill">
						<text>{{ order.orderTypeLabel }}</text>
					</view>
				</view>
				<view class="od-hero__no-row">
					<text class="od-hero__no">{{ moui.labelBookingNo }} {{ order.orderNo }}</text>
					<text class="od-hero__copy" @click="copyOrderNo">{{ moui.copyOrderNo }}</text>
				</view>
			</view>
		</view>

		<view class="od-stack">
			<!-- 线路卡片 -->
			<view class="od-route">
				<image :src="order.cover" mode="aspectFill" class="od-route__banner ry-placeholder-img" />
				<view class="od-route__mask" />
				<view class="od-route__body">
					<view v-if="routeSpec" class="od-route__tag"
						:style="{ background: theme.primaryLight, color: theme.primary }">
						<text>{{ routeSpec }}</text>
					</view>
					<text class="od-route__name">{{ order.mainName }}</text>
				</view>
			</view>

			<!-- 统一信息区 -->
			<view class="od-sheet">
				<view class="od-section">
					<text class="od-section__title">{{ moui.labelTripInfo }}</text>
					<view class="od-rows">
						<view v-if="order.tourDate" class="od-row">
							<text class="od-row__label">{{ moui.labelTripDate }}</text>
							<text class="od-row__value">{{ tourDateDisplay }}</text>
						</view>
						<view v-if="order.guestCount" class="od-row">
							<text class="od-row__label">{{ moui.labelGuestCount }}</text>
							<text class="od-row__value">{{ order.guestCount }}{{ moui.guestSuffix }}</text>
						</view>
						<view v-if="order.meetPoint" class="od-row">
							<text class="od-row__label">{{ moui.labelMeetPoint }}</text>
							<text class="od-row__value">{{ order.meetPoint }}</text>
						</view>
						<view v-if="routeRefPrice" class="od-row">
							<text class="od-row__label">{{ moui.labelRefPrice }}</text>
							<text class="od-row__value od-row__value--price" :style="{ color: theme.primary }">{{ routeRefPrice }}</text>
						</view>
					</view>
				</view>

				<view v-if="guestSummary" class="od-section-divider" />

				<view v-if="guestSummary" class="od-section">
					<view class="od-section__head">
						<text class="od-section__title">出行人</text>
						<view v-if="guestSummary.hasDetail" class="od-section__link" @click.stop="openGuestPage">
							<text :style="{ color: theme.primary }">查看全部</text>
							<u-icon name="arrow-right" :color="theme.primary" :size="14" />
						</view>
					</view>
					<view class="od-rows">
						<view class="od-row">
							<text class="od-row__label">预约人</text>
							<text class="od-row__value">{{ guestSummary.bookerName }}</text>
						</view>
						<view v-if="guestSummary.companionCount > 0" class="od-row">
							<text class="od-row__label">同行人</text>
							<text class="od-row__value">{{ guestSummary.companionCount }}人</text>
						</view>
					</view>
					<view v-if="order.customerPhone" class="od-phone-bar">
						<view class="od-phone-bar__info">
							<text class="od-phone-bar__name">{{ order.customerName || guestSummary.bookerName }}</text>
							<text class="od-phone-bar__num">{{ displayPhone }}</text>
						</view>
						<view class="od-phone-bar__btn" :style="{ background: theme.primary }" @click.stop="callPhone">
							<u-icon name="phone-fill" color="#FFF" :size="16" />
							<text>{{ moui.callPhone }}</text>
						</view>
					</view>
				</view>

				<view class="od-section-divider" />

				<view class="od-section">
					<text class="od-section__title">{{ moui.labelOrderInfo }}</text>
					<view class="od-rows">
						<view class="od-row">
							<text class="od-row__label">{{ moui.labelBookingTime }}</text>
							<text class="od-row__value">{{ order.createdAt || '--' }}</text>
						</view>
						<view v-if="refTotalAmount > 0" class="od-row od-row--amount">
							<text class="od-row__label">{{ moui.labelPayAmount }}</text>
							<text class="od-row__value od-row__value--amount" :style="{ color: theme.primary }">
								{{ formatMoney(refTotalAmount) }}
							</text>
						</view>
						<view v-if="order.userRemark" class="od-row od-row--remark">
							<text class="od-row__label">{{ moui.labelRemark }}</text>
							<text class="od-row__value od-row__value--remark">{{ order.userRemark }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部操作 -->
		<view v-if="primaryAction || secondaryAction" class="od-footer">
			<view v-if="secondaryAction" class="od-footer__btn od-footer__btn--ghost"
				:style="{ borderColor: theme.primary, color: theme.primary }"
				:class="{ 'od-footer__btn--loading': submitting }" @click="onSecondaryAction">
				<text>{{ secondaryAction.label }}</text>
			</view>
			<view v-if="primaryAction" class="od-footer__btn" :style="{ background: theme.primary }"
				:class="{ 'od-footer__btn--loading': submitting }" @click="onAction">
				<u-loading-icon v-if="submitting" mode="circle" color="#FFF" :size="18" />
				<text v-else>{{ primaryAction.label }}</text>
			</view>
		</view>
	</view>

	<view v-else-if="loading" class="od-loading">
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
		getOrderPrimaryAction,
		maskPhone,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		MERCHANT_ORDER_UI,
		mapMerchantOrder,
		formatRouteRefPrice
	} from '@/common/merchant-order-ui.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrderDetail,
		postMerchantOrderAction
	} from '@/common/api/merchant.js';

	const PACKAGE_STORE_TYPE = 'travel';
	const WEEK_LABELS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

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

	const primaryAction = computed(() => (order.value ? getOrderPrimaryAction(order.value) : null));
	const secondaryAction = computed(() => {
		if (!order.value || order.value.status === 'refunding') return null;
		if (order.value.verified || order.value.status === 'completed') return null;
		if (['pending_checkin', 'on_tour', 'pending_use', 'pending_confirm'].includes(order.value.status)) {
			return { label: '天气原因全额退', action: 'refund_weather' };
		}
		return null;
	});
	const guestSummary = computed(() => {
		const list = order.value?.guestList;
		if (Array.isArray(list) && list.length) {
			const booker = list.find((item) => item.role === 'booker') || list[0];
			return {
				bookerName: booker?.name || order.value?.customerName || '--',
				companionCount: Math.max(0, list.length - 1),
				hasDetail: list.length > 0
			};
		}
		if (order.value?.customerName) {
			return {
				bookerName: order.value.customerName,
				companionCount: 0,
				hasDetail: false
			};
		}
		return null;
	});
	const routeSpec = computed(() => order.value?.items?.[0]?.spec || order.value?.subSpec || '');
	const routeRefPrice = computed(() => formatRouteRefPrice(order.value));

	const tourDateDisplay = computed(() => {
		const parsed = parseTourDate(order.value?.tourDate);
		if (!order.value?.tourDate) return '';
		if (!parsed.week) return order.value.tourDate;
		return `${order.value.tourDate}（${parsed.week}）`;
	});

	const displayPhone = computed(() => {
		const phone = order.value?.customerPhone;
		return phone ? maskPhone(String(phone)) : '';
	});

	const refTotalAmount = computed(() => {
		if (!order.value) return 0;
		return Number(order.value.totalAmount) > 0 ?
			Number(order.value.totalAmount) :
			Number(order.value.amount) || 0;
	});

	function parseTourDate(dateStr) {
		if (!dateStr) return {
			day: '--',
			month: '',
			week: ''
		};
		const parts = String(dateStr).split('-');
		if (parts.length < 3) return {
			day: dateStr,
			month: '',
			week: ''
		};
		const date = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
		return {
			day: Number(parts[2]),
			month: Number(parts[1]),
			week: WEEK_LABELS[date.getDay()] || ''
		};
	}

	async function reload() {
		if (!orderId.value) return;
		loading.value = true;
		try {
			const res = await fetchMerchantOrderDetail(orderId.value);
			if (res.code === 0 && res.data) {
				order.value = mapMerchantOrder(res.data);
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

	function openGuestPage() {
		if (!orderId.value) return;
		uni.navigateTo({ url: `/merchant-travel/order-guests?id=${orderId.value}` });
	}

	async function onSecondaryAction() {
		if (!secondaryAction.value || submitting.value) return;
		uni.showModal({
			title: '天气原因全额退款',
			content: '确认为用户发起全额退款吗？退款将原路返回用户账户。',
			success: async (res) => {
				if (!res.confirm) return;
				submitting.value = true;
				try {
					const actionRes = await postMerchantOrderAction(orderId.value, { action: secondaryAction.value.action });
					if (actionRes.code === 0) {
						uni.showToast({ title: '已发起全额退款', icon: 'success' });
						await reload();
					} else {
						uni.showToast({ title: actionRes.msg || '操作失败', icon: 'none' });
					}
				} catch (e) {
					uni.showToast({ title: ui.value.networkError, icon: 'none' });
				} finally {
					submitting.value = false;
				}
			}
		});
	}

	async function onAction() {
		if (!primaryAction.value || submitting.value) return;
		submitting.value = true;
		try {
			const res = await postMerchantOrderAction(orderId.value, {
				action: primaryAction.value.action
			});
			if (res.code !== 0) {
				uni.showToast({
					title: res.msg || '操作失败',
					icon: 'none'
				});
				return;
			}
			if (res.data) {
				order.value = mapMerchantOrder(res.data);
			} else {
				await reload();
			}
			uni.showToast({
				title: ui.value.orderHandled,
				icon: 'success'
			});
		} catch (e) {
			uni.showToast({
				title: ui.value.networkError,
				icon: 'none'
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
			title: ui.value.orderDetailTitle
		});
		applyNavBarColor();
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		applyNavBarColor();
		uni.setNavigationBarTitle({
			title: ui.value.orderDetailTitle
		});
		await reload();
	});
</script>

<style lang="scss" scoped>
	.od-page {
		min-height: 100vh;
		background: #F0F2F5;
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
		box-sizing: border-box;

		&--with-footer {
			padding-bottom: calc(152rpx + env(safe-area-inset-bottom));
		}
	}

	.od-loading {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #F0F2F5;
	}

	/* ── 顶部 Hero ── */
	.od-hero {
		position: relative;
		overflow: hidden;
		padding: 32rpx var(--ry-page-padding-x) 72rpx;

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
			letter-spacing: 1rpx;
		}

		&__pill {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			min-height: 44rpx;
			padding: 0 18rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.2);
			backdrop-filter: blur(4rpx);
			box-sizing: border-box;

			text {
				font-size: 22rpx;
				font-weight: 600;
				color: rgba(255, 255, 255, 0.95);
				line-height: 44rpx;
			}
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
			line-height: 1.4;
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

	/* ── 内容栈（叠在 Hero 上） ── */
	.od-stack {
		margin-top: -44rpx;
		padding: 0 var(--ry-page-padding-x);
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	/* ── 线路大图卡 ── */
	.od-route {
		position: relative;
		border-radius: 24rpx;
		overflow: hidden;

		&__banner {
			width: 100%;
			height: 320rpx;
			display: block;
			background: #D8DCE3;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 200rpx;
			background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.72) 100%);
			pointer-events: none;
		}

		&__body {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			padding: 24rpx 28rpx 28rpx;
		}

		&__tag {
			display: inline-flex;
			padding: 6rpx 16rpx;
			border-radius: 8rpx;
			margin-bottom: 12rpx;

			text {
				font-size: 22rpx;
				font-weight: 600;
				line-height: 1.2;
			}
		}

		&__name {
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
			font-size: 30rpx;
			font-weight: 700;
			color: #FFFFFF;
			line-height: 1.45;
			text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.25);
		}
	}

	/* ── 统一信息卡 ── */
	.od-sheet {
		background: #FFFFFF;
		border-radius: 20rpx;
		overflow: hidden;
	}

	.od-section {
		padding: 28rpx;

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 20rpx;

			.od-section__title {
				margin-bottom: 0;
			}
		}

		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.3;
			margin-bottom: 20rpx;
		}

		&__link {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 4rpx;
			font-size: 24rpx;
			font-weight: 500;

			&:active {
				opacity: 0.75;
			}
		}
	}

	.od-section-divider {
		height: 1rpx;
		margin: 0 28rpx;
		background: #F2F3F5;
	}

	.od-rows {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.od-row {
		display: flex;
		align-items: flex-start;
		gap: 24rpx;
		min-width: 0;

		&--amount {
			padding-top: 8rpx;
		}

		&--remark {
			align-items: flex-start;
		}

		&__label {
			width: 148rpx;
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
			word-break: break-all;

			&--price {
				font-weight: 700;
			}

			&--amount {
				font-size: 36rpx;
				font-weight: 800;
				line-height: 1.2;
			}

			&--remark {
				color: #4A4A56;
			}
		}
	}

	.od-phone-bar {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-top: 20rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #F8F9FB;

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 4rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: #1A1A22;
			line-height: 1.3;
		}

		&__num {
			font-size: 24rpx;
			color: #6B6B78;
			line-height: 1.3;
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
				line-height: 1;
			}

			&:active {
				opacity: 0.88;
			}
		}
	}

	/* ── 底部操作 ── */
	.od-footer {
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
				line-height: 1;
			}

			&:active {
				opacity: 0.9;
			}

			&--ghost {
				background: #FFFFFF;
				border-width: 2rpx;
				border-style: solid;

				text {
					font-weight: 600;
					color: inherit;
				}
			}

			&--loading {
				opacity: 0.85;
			}
		}
	}
</style>
