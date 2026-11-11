<template>
	<view v-if="order" class="mod-page" :style="pageStyle">
		<view class="mod-status" :class="`mod-status--${order.status}`">
			<view class="mod-status__main">
				<text class="mod-status__label">{{ order.statusLabel }}</text>
				<text class="mod-status__type">{{ order.orderTypeLabel }}</text>
			</view>
			<view class="mod-status__no-row">
				<text class="mod-status__no">{{ moui.labelOrderNo }} {{ order.orderNo }}</text>
				<text class="mod-status__copy" @click="copyOrderNo">{{ moui.copyOrderNo }}</text>
			</view>
		</view>

		<view class="mod-body">
			<!-- 商品 / 预订摘要 -->
			<view class="mod-card">
				<view class="mod-product">
					<image :src="order.cover" mode="aspectFill" class="mod-product__cover ry-placeholder-img" />
					<view class="mod-product__info">
						<view class="mod-product__title-row">
							<text class="mod-product__name">{{ order.mainName }}</text>
							<text v-if="order.titleTag" class="mod-product__tag">{{ order.titleTag }}</text>
						</view>
						<text v-if="order.subSpec" class="mod-product__spec">{{ order.subSpec }}</text>
						<text v-if="order.highlightLine" class="mod-product__highlight">{{ order.highlightLine }}</text>
					</view>
				</view>
				<view v-if="bookingRows.length" class="mod-card__divider" />
				<view v-if="bookingRows.length" class="mod-kv-list">
					<view v-for="row in bookingRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 预订人 -->
			<view v-if="showContactCard" class="mod-card">
				<text class="mod-card__title">{{ moui.labelContact }}</text>
				<view class="mod-contact">
					<view class="mod-contact__info">
						<view class="mod-contact__name">{{ order.customerName || '--' }}</view>
						<view class="mod-contact__phone">{{ displayPhone }}</view>
					</view>
					<view v-if="order.customerPhone" class="mod-contact__btn" :style="{ background: theme.primary }"
						@click="callPhone">
						<u-icon name="phone-fill" color="#FFF" :size="14" />
						<text>{{ moui.callPhone }}</text>
					</view>
				</view>
			</view>

			<!-- 核销码（酒店待核销） -->
			<view v-if="showVerifyBlock" class="mod-card mod-card--verify">
				<text class="mod-card__title">{{ moui.labelVerify }}</text>
				<text class="mod-verify__code">{{ order.verifyCode }}</text>
				<text class="mod-verify__hint">{{ moui.labelPendingVerify }}</text>
			</view>

			<!-- 订单信息 -->
			<view class="mod-card">
				<text class="mod-card__title">{{ moui.labelOrderInfo }}</text>
				<view class="mod-kv-list">
					<view v-for="row in metaRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value">{{ row.value }}</text>
					</view>
					<view v-if="order.verified" class="mod-kv">
						<text class="mod-kv__label">{{ moui.labelVerifyStatus }}</text>
						<text class="mod-kv__value mod-kv__value--success">{{ moui.labelVerified }}</text>
					</view>
				</view>
			</view>

			<!-- 费用明细 -->
			<view v-if="feeRows.length" class="mod-card">
				<text class="mod-card__title">{{ moui.labelFee }}</text>
				<view class="mod-kv-list">
					<view v-for="row in feeRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value" :class="{
								'mod-kv__value--discount': row.tone === 'discount',
								'mod-kv__value--total': row.tone === 'total'
							}">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 用户备注 -->
			<view v-if="order.userRemark" class="mod-card">
				<text class="mod-card__title">{{ moui.labelRemark }}</text>
				<text class="mod-remark">{{ order.userRemark }}</text>
			</view>
		</view>

		<view v-if="primaryAction" class="mod-footer">
			<view class="mod-footer__inner">
				<u-button :text="primaryAction.label" block :color="theme.primary" :custom-style="btnStyle"
					:loading="submitting" @click="onAction" />
			</view>
		</view>
	</view>
	<view v-else-if="loading" class="mod-loading">
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
		MERCHANT_BUSINESS_UI,
		getOrderPrimaryAction,
		maskPhone,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		MERCHANT_ORDER_UI,
		mapMerchantOrder,
		buildBookingRows,
		buildMetaRows,
		buildFeeRows,
		showVerifyCard
	} from '@/common/merchant-order-ui.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrderDetail,
		postMerchantOrderAction
	} from '@/common/api/merchant.js';

	const PACKAGE_STORE_TYPE = 'hotel';
const {
		theme,
		pageStyle,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const ui = MERCHANT_BUSINESS_UI;
	const moui = MERCHANT_ORDER_UI;
	const orderId = ref('');
	const order = ref(null);
	const loading = ref(false);
	const submitting = ref(false);
	const btnStyle = {
		height: '96rpx',
		borderRadius: '48rpx',
		fontWeight: '700',
		fontSize: '30rpx'
	};

	const primaryAction = computed(() => (order.value ? getOrderPrimaryAction(order.value) : null));
	const bookingRows = computed(() => buildBookingRows(order.value));
	const metaRows = computed(() => buildMetaRows(order.value));
	const feeRows = computed(() => buildFeeRows(order.value));
	const showVerifyBlock = computed(() => showVerifyCard(order.value));
	const showContactCard = computed(() => Boolean(order.value?.customerName || order.value?.customerPhone));
	const displayPhone = computed(() => {
		const phone = order.value?.customerPhone;
		if (!phone) return '--';
		return maskPhone(phone);
	});

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
					title: res.msg || ui.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			order.value = null;
			uni.showToast({
				title: ui.networkError,
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
				title: moui.copySuccess,
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
				title: ui.orderHandled,
				icon: 'success'
			});
		} catch (e) {
			uni.showToast({
				title: ui.networkError,
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
			title: ui.orderDetailTitle
		});
		applyNavBarColor();
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
		applyNavBarColor();
		await reload();
	});
</script>

<style lang="scss" scoped>
	.mod-page {
		min-height: 100vh;
		background: #F4F6FA;
		padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
	}

	.mod-loading {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #F4F6FA;
	}

	.mod-status {
		margin: 16rpx var(--ry-page-padding-x) 0;
		padding: 28rpx 28rpx 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #5B9BF5, #2979FF);
		color: #FFF;

		&--completed {
			background: linear-gradient(135deg, #3BCB7E, #2EAF6A);
		}

		&--cancelled,
		&--refunding {
			background: linear-gradient(135deg, #FF7B7B, #FF4D4F);
		}

		&--pending_confirm,
		&--pending_use,
		&--pending_checkin {
			background: linear-gradient(135deg, #FF9A4D, #FF6600);
		}

		&__main {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__label {
			font-size: 36rpx;
			font-weight: 800;
			line-height: 1.3;
		}

		&__type {
			font-size: 22rpx;
			padding: 6rpx 14rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.18);
		}

		&__no-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-top: 16rpx;
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.82);
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			color: #FFF;
			padding: 4rpx 12rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.16);
		}
	}

	.mod-body {
		padding: 16rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mod-card {
		background: #FFF;
		border-radius: 24rpx;
		padding: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);

		&--verify {
			background: linear-gradient(180deg, #FFF8F0 0%, #FFFFFF 100%);
			border: 1rpx solid #FFE8D6;
		}

		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A22;
			margin-bottom: 20rpx;
			line-height: 1.3;
		}

		&__divider {
			height: 1rpx;
			margin: 20rpx 0;
			background: #F0F1F5;
		}
	}

	.mod-product {
		display: flex;
		gap: 20rpx;

		&__cover {
			width: 128rpx;
			height: 128rpx;
			border-radius: 16rpx;
			flex-shrink: 0;
			background: #F0F0F0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__title-row {
			display: flex;
			align-items: flex-start;
			gap: 10rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.45;
		}

		&__tag {
			flex-shrink: 0;
			padding: 2rpx 10rpx;
			border-radius: 8rpx;
			background: #F2F3F7;
			font-size: 20rpx;
			color: #8A8A96;
		}

		&__spec {
			font-size: 24rpx;
			color: #9A9AA6;
			line-height: 1.4;
		}

		&__highlight {
			font-size: 24rpx;
			color: #2979FF;
			font-weight: 600;
			line-height: 1.4;
		}
	}

	.mod-kv-list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mod-kv {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 24rpx;

		&__label {
			flex-shrink: 0;
			width: 140rpx;
			font-size: 24rpx;
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

			&--discount {
				color: #FF6600;
			}

			&--total {
				color: #FF6600;
				font-size: 32rpx;
				font-weight: 800;
			}

			&--success {
				color: #2EAF6A;
				font-weight: 600;
			}
		}
	}

	.mod-contact {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;

		&__info {
			// flex: 1;
			min-width: 0;
			display: flex;
			// flex-direction: column;
			// gap: 8rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: #1A1A22;
		}

		&__phone {
			font-size: 26rpx;
			color: #36363d;
			margin-left: 20rpx;
		}

		&__btn {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 8rpx;
			padding: 12rpx 24rpx;
			border-radius: 999rpx;
			font-size: 24rpx;
			font-weight: 600;
			color: #FFF;
		}
	}

	.mod-verify {
		&__code {
			display: block;
			font-size: 48rpx;
			font-weight: 800;
			color: #FF6600;
			letter-spacing: 6rpx;
			line-height: 1.2;
			margin-bottom: 8rpx;
		}

		&__hint {
			font-size: 22rpx;
			color: #9A9AA6;
		}
	}

	.mod-remark {
		display: block;
		font-size: 26rpx;
		color: #4A4A56;
		line-height: 1.65;
	}

	.mod-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.96);
		backdrop-filter: blur(12rpx);
		box-shadow: 0 -8rpx 32rpx rgba(15, 23, 42, 0.08);
	}
</style>
