<template>
	<view class="olg-page">
		<view v-if="loading" class="olg-loading">
			<u-loading-icon mode="circle" color="#B45D2D" />
		</view>

		<view v-else-if="detail" class="olg-body">
			<view class="olg-hero">
				<text class="olg-hero__status">{{ detail.currentStatus || ui.logisticsEmptyTrack }}</text>
				<text v-if="detail.orderNo" class="olg-hero__no">{{ ui.orderNo }} {{ detail.orderNo }}</text>
			</view>

			<view class="olg-card">
				<view v-if="detail.logisticsCompany" class="olg-kv">
					<text class="olg-kv__label">{{ ui.labelLogisticsCompany }}</text>
					<text class="olg-kv__value">{{ detail.logisticsCompany }}</text>
				</view>
				<view v-if="detail.logisticsNo" class="olg-kv olg-kv--no">
					<text class="olg-kv__label">{{ ui.labelLogisticsNo }}</text>
					<view class="olg-kv__no">
						<text class="olg-kv__value">{{ detail.logisticsNo }}</text>
						<text class="olg-kv__copy" @click="copyTrackingNo">{{ ui.btnCopyLogisticsNo }}</text>
					</view>
				</view>
				<view v-if="receiverLine" class="olg-kv">
					<text class="olg-kv__label">{{ ui.logisticsReceiver }}</text>
					<text class="olg-kv__value">{{ receiverLine }}</text>
				</view>
				<view v-if="detail.fullAddress" class="olg-kv">
					<text class="olg-kv__label">{{ ui.logisticsAddress }}</text>
					<text class="olg-kv__value">{{ detail.fullAddress }}</text>
				</view>
			</view>

			<view v-if="detail.queryTip" class="olg-tip">
				<u-icon name="info-circle" color="#B45D2D" :size="14" />
				<text>{{ detail.queryTip }}</text>
			</view>

			<view class="olg-card">
				<text class="olg-card__title">{{ ui.logisticsTrackTitle }}</text>
				<view v-if="tracks.length" class="olg-track">
					<view v-for="(track, index) in tracks" :key="`${track.time}-${index}`" class="olg-track__item"
						:class="{ 'olg-track__item--active': index === 0 }">
						<view class="olg-track__dot" />
						<view class="olg-track__content">
							<text class="olg-track__context">{{ track.context }}</text>
							<text v-if="track.time" class="olg-track__time">{{ track.time }}</text>
						</view>
					</view>
				</view>
				<view v-else class="olg-empty">
					<text>{{ ui.logisticsEmptyTrack }}</text>
				</view>
			</view>
		</view>

		<view v-else class="olg-empty-page">
			<u-empty mode="data" :text="ui.logisticsLoadFail" />
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { hasToken } from '@/common/api/auth.js';
	import { fetchOrderLogistics } from '@/common/api/order.js';
	import { fetchMerchantOrderLogistics } from '@/common/api/merchant.js';
	import { ORDER_UI } from '@/common/order-ui.js';

	const ui = ORDER_UI;
	const orderId = ref('');
	const scope = ref('user');
	const loading = ref(false);
	const detail = ref(null);

	const tracks = computed(() => (
		Array.isArray(detail.value?.tracks) ? detail.value.tracks : []
	));

	const receiverLine = computed(() => {
		const name = String(detail.value?.receiverName || '').trim();
		const phone = String(detail.value?.receiverPhone || '').trim();
		return [name, phone].filter(Boolean).join(' ');
	});

	async function loadLogistics() {
		if (!orderId.value) return;
		loading.value = true;
		try {
			const res = scope.value === 'merchant'
				? await fetchMerchantOrderLogistics(orderId.value)
				: await fetchOrderLogistics(orderId.value);
			if (res.code === 0 && res.data) {
				detail.value = res.data;
			} else {
				detail.value = null;
				uni.showToast({ title: res.msg || ui.logisticsLoadFail, icon: 'none' });
			}
		} catch (e) {
			detail.value = null;
			uni.showToast({ title: ui.logisticsLoadFail, icon: 'none' });
		} finally {
			loading.value = false;
		}
	}

	function copyTrackingNo() {
		const no = detail.value?.logisticsNo;
		if (!no) return;
		uni.setClipboardData({
			data: String(no),
			success: () => {
				uni.showToast({ title: ui.copyLogisticsNoSuccess, icon: 'none' });
			}
		});
	}

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({ title: ui.loginTip, icon: 'none' });
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/my/login', fail: () => {} });
		}, 400);
		return false;
	}

	onLoad((options) => {
		orderId.value = options?.id ? String(options.id) : '';
		scope.value = String(options?.scope || 'user') === 'merchant' ? 'merchant' : 'user';
		uni.setNavigationBarTitle({ title: ui.logisticsPageTitle });
	});

	onShow(async () => {
		if (!requireLogin()) return;
		await loadLogistics();
	});
</script>

<style lang="scss" scoped>
	.olg-page {
		min-height: 100vh;
		background: #f8f5f2;
	}

	.olg-loading,
	.olg-empty-page {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.olg-body {
		padding: 0 var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	}

	.olg-hero {
		padding: 40rpx 0 28rpx;
		display: flex;
		flex-direction: column;
		gap: 10rpx;

		&__status {
			font-size: 34rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.4;
		}

		&__no {
			font-size: 22rpx;
			color: #999;
		}
	}

	.olg-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx;
		margin-bottom: 16rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);

		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			margin-bottom: 20rpx;
		}
	}

	.olg-kv {
		display: flex;
		justify-content: space-between;
		gap: 24rpx;
		padding: 12rpx 0;

		&--no {
			align-items: center;
		}

		&__label {
			flex-shrink: 0;
			width: 140rpx;
			font-size: 26rpx;
			color: #999;
		}

		&__value {
			flex: 1;
			font-size: 26rpx;
			color: #333;
			text-align: right;
			line-height: 1.5;
			word-break: break-all;
		}

		&__no {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 12rpx;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			color: #b45d2d;
			padding: 4rpx 12rpx;
			border-radius: 999rpx;
			background: #fff6f0;
		}
	}

	.olg-tip {
		display: flex;
		align-items: flex-start;
		gap: 10rpx;
		padding: 18rpx 20rpx;
		margin-bottom: 16rpx;
		border-radius: 16rpx;
		background: #fff6f0;

		text {
			flex: 1;
			font-size: 24rpx;
			color: #8d6e63;
			line-height: 1.6;
		}
	}

	.olg-track {
		display: flex;
		flex-direction: column;
		gap: 0;

		&__item {
			display: flex;
			gap: 20rpx;
			position: relative;
			padding-bottom: 28rpx;

			&:not(:last-child)::before {
				content: '';
				position: absolute;
				left: 11rpx;
				top: 28rpx;
				bottom: 0;
				width: 2rpx;
				background: #eceff3;
			}

			&--active {
				.olg-track__dot {
					background: #b45d2d;
					box-shadow: 0 0 0 6rpx rgba(180, 93, 45, 0.15);
				}

				.olg-track__context {
					color: #1a1a1a;
					font-weight: 600;
				}
			}
		}

		&__dot {
			width: 24rpx;
			height: 24rpx;
			margin-top: 6rpx;
			border-radius: 50%;
			background: #d8dce3;
			flex-shrink: 0;
		}

		&__content {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__context {
			font-size: 26rpx;
			color: #666;
			line-height: 1.5;
		}

		&__time {
			font-size: 22rpx;
			color: #aaa;
		}
	}

	.olg-empty {
		padding: 24rpx 0 8rpx;
		text-align: center;

		text {
			font-size: 24rpx;
			color: #999;
		}
	}
</style>
