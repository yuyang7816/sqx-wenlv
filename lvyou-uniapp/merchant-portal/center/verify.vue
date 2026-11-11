<template>
	<view class="mv-page" :style="pageStyle">
		<view class="mv-search ry-card">
			<view class="mv-search__bar">
				<u-icon name="search" color="#B0B0B8" :size="18" />
				<input v-model="orderNo" class="mv-search__input" :placeholder="ui.verifyInputPlaceholder"
					placeholder-class="mv-search__placeholder" confirm-type="search" @confirm="loadOrder" />
				<text class="mv-search__action" :style="{ color: theme.primary }" @click="loadOrder">
					{{ ui.verifySearch }}
				</text>
			</view>
		</view>

		<view v-if="order" class="mv-result">
			<text class="mv-result__label">查询结果</text>
			<view class="mv-card ry-card"  @click="goOrderDetail">
				<view class="mv-card__head">
					<view class="mv-card__head-left">
						<text class="mv-card__type">{{ order.orderTypeLabel || '订单' }}</text>
						<text class="mv-card__no">{{ order.orderNo }}</text>
					</view>
					<text class="mv-card__status" :class="`mv-card__status--${statusBadgeClass(order.status)}`">
						{{ order.statusLabel }}
					</text>
				</view>
				<view class="mv-card__body">
					<view v-if="order.tourDate" class="mv-card__date" :style="{ background: theme.primaryLight }">
						<text class="mv-card__date-day" :style="{ color: theme.primary }">{{ tourDay(order.tourDate) }}</text>
						<text class="mv-card__date-sub">{{ tourMonth(order.tourDate) }}</text>
					</view>
					<view class="mv-card__info">
						<view class="mv-card__title-row">
							<text class="mv-card__title">{{ order.mainName }}</text>
							<text v-if="order.titleTag" class="mv-card__tag">{{ order.titleTag }}</text>
						</view>
						<text v-if="order.highlightLine" class="mv-card__highlight">{{ order.highlightLine }}</text>
						<text v-if="order.extraLine" class="mv-card__extra">{{ order.extraLine }}</text>
						<view v-if="guestLine(order)" class="mv-card__guest">
							<text>{{ guestLine(order) }}</text>
							<view v-if="order.customerPhone" class="mv-card__call" @click.stop="callPhone(order)">
								<u-icon name="phone-fill" :color="theme.primary" :size="16" />
							</view>
						</view>
					</view>
				</view>
				<view class="mv-card__foot" >
					<text class="mv-card__amount">{{ formatDisplayAmount(order) }}</text>
					<view v-if="canVerify" class="mv-card__action" :style="{ background: theme.primary }"
						@click.stop="onVerify(order)">
						<text>{{ ui.confirmVerify }}</text>
					</view>
					<view v-else class="mv-card__tip">
						<text>{{ order.verified ? '该订单已核销' : '当前状态不可核销' }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-else-if="searched && !loading" class="mv-empty">
			<u-empty mode="order" text="未找到订单" />
		</view>

		<view v-if="!order" class="mv-pending">
			<view class="mv-pending__head">
				<text class="mv-pending__title">{{ ui.verifyPendingTitle }}</text>
				<text v-if="pendingOrders.length" class="mv-pending__count">{{ pendingOrders.length }} 单</text>
			</view>
			<view v-if="pendingOrders.length" class="mv-pending-list">
				<view v-for="item in pendingOrders" :key="item.id" class="mv-card ry-card"  @click="goPendingDetail(item)">
					<view class="mv-card__head">
						<view class="mv-card__head-left">
							<text class="mv-card__type">{{ item.orderTypeLabel || '订单' }}</text>
							<text class="mv-card__no">{{ item.orderNo }}</text>
						</view>
						<text class="mv-card__status" :class="`mv-card__status--${statusBadgeClass(item.status)}`">
							{{ item.statusLabel }}
						</text>
					</view>
					<view class="mv-card__body">
						<view v-if="item.tourDate" class="mv-card__date" :style="{ background: theme.primaryLight }">
							<text class="mv-card__date-day" :style="{ color: theme.primary }">{{ tourDay(item.tourDate) }}</text>
							<text class="mv-card__date-sub">{{ tourMonth(item.tourDate) }}</text>
						</view>
						<view class="mv-card__info">
							<view class="mv-card__title-row">
								<text class="mv-card__title">{{ item.mainName }}</text>
								<text v-if="item.titleTag" class="mv-card__tag">{{ item.titleTag }}</text>
							</view>
							<!-- <text v-if="item.highlightLine" class="mv-card__highlight">{{ item.highlightLine }}</text> -->
							<text v-if="item.extraLine" class="mv-card__extra">{{ item.extraLine }}</text>
							<view v-if="guestLine(item)" class="mv-card__guest">
								<text>{{ guestLine(item) }}</text>
								<view v-if="item.customerPhone" class="mv-card__call" @click.stop="callPhone(item)">
									<u-icon name="phone-fill" :color="theme.primary" :size="16" />
								</view>
							</view>
						</view>
					</view>
					<view class="mv-card__foot" >
						<text class="mv-card__amount">{{ formatDisplayAmount(item) }}</text>
						<view v-if="canVerifyOrder(item)" class="mv-card__action" :style="{ background: theme.primary }"
							@click.stop="onVerify(item)">
							<text>{{ ui.confirmVerify }}</text>
						</view>
					</view>
				</view>
			</view>
			<view v-else-if="!listLoading" class="mv-empty">
				<u-empty mode="order" :text="ui.verifyEmpty" />
			</view>
			<view v-if="listLoading && pendingOrders.length === 0" class="mv-loading">
				<u-loading-icon mode="circle" :color="theme.primary" />
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		MERCHANT_BUSINESS_UI,
		maskPhone,
		requireMerchantDepositPaid,
		getOrderPrimaryAction
	} from '@/common/merchant-center.js';
	import {
		mapMerchantOrder,
		formatDisplayAmount,
		statusBadgeClass
	} from '@/common/merchant-order-ui.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrderDetail,
		fetchMerchantOrders,
		postMerchantOrderAction
	} from '@/common/api/merchant.js';
	import {
		getMerchantBizPath
	} from '@/common/merchant-biz-routes.js';

	const PAGE_SIZE = 20;

	const {
		theme,
		pageStyle,
		storeType,
		initTheme,
		applyNavBarColor
	} = useMerchantTheme();
	const ui = MERCHANT_BUSINESS_UI;
	const orderNo = ref('');
	const order = ref(null);
	const loading = ref(false);
	const searched = ref(false);
	const submitting = ref(false);
	const pendingOrders = ref([]);
	const listLoading = ref(false);
	const listPage = ref(1);
	const listHasMore = ref(true);

	const canVerify = computed(() => canVerifyOrder(order.value));

	function canVerifyOrder(record) {
		if (!record || record.verified) return false;
		return getOrderPrimaryAction(record)?.action === 'verify';
	}

	function guestLine(record) {
		if (!record) return '';
		const name = record.guestNames || record.customerName || '';
		const phone = record.customerPhone ? maskPhone(record.customerPhone) : '';
		if (name && phone) return `${name} · ${phone}`;
		return name || phone || '';
	}

	function tourDay(dateStr) {
		if (!dateStr) return '';
		const parts = String(dateStr).split('-');
		return parts.length >= 3 ? parts[2] : '';
	}

	function tourMonth(dateStr) {
		if (!dateStr) return '';
		const parts = String(dateStr).split('-');
		return parts.length >= 2 ? `${Number(parts[1])}月` : '';
	}

	function readRouteOrderNo() {
		const pages = getCurrentPages();
		const param = pages[pages.length - 1]?.options?.orderNo;
		return param ? decodeURIComponent(param) : '';
	}

	function goOrderDetail() {
		if (!order.value?.id) return;
		uni.navigateTo({
			url: getMerchantBizPath(storeType.value || 'hotel', 'order-detail', {
				id: order.value.id
			})
		});
	}

	function goPendingDetail(item) {
		if (!item?.id) return;
		uni.navigateTo({
			url: getMerchantBizPath(storeType.value || 'hotel', 'order-detail', {
				id: item.id
			})
		});
	}

	function mapOrderRecord(record) {
		return mapMerchantOrder(record);
	}

	async function loadPendingOrders(reset = false) {
		if (listLoading.value) return;
		if (!reset && !listHasMore.value) return;
		if (reset) {
			listPage.value = 1;
			listHasMore.value = true;
		}
		listLoading.value = true;
		try {
			const res = await fetchMerchantOrders({
				tab: 'verify',
				page: listPage.value,
				limit: PAGE_SIZE
			});
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapOrderRecord).filter(Boolean);
				pendingOrders.value = reset ? records : [...pendingOrders.value, ...records];
				const total = Number(res.data.total ?? 0);
				listHasMore.value = records.length >= PAGE_SIZE && pendingOrders.value.length < total;
			} else if (reset) {
				pendingOrders.value = [];
			}
		} catch (e) {
			if (reset) pendingOrders.value = [];
		} finally {
			listLoading.value = false;
		}
	}

	function callPhone(record) {
		const phone = record?.customerPhone;
		if (!phone) return;
		uni.makePhoneCall({
			phoneNumber: String(phone)
		});
	}

	async function loadOrder() {
		const key = String(orderNo.value || '').trim();
		if (!key) {
			uni.showToast({
				title: ui.verifyInputPlaceholder,
				icon: 'none'
			});
			return;
		}
		loading.value = true;
		searched.value = true;
		try {
			const res = await fetchMerchantOrderDetail(key);
			if (res.code === 0 && res.data) {
				order.value = mapOrderRecord(res.data);
			} else {
				order.value = null;
				uni.showToast({
					title: res.msg || '订单不存在',
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

	function onVerify(targetOrder) {
		const target = targetOrder || order.value;
		if (!target || !canVerifyOrder(target) || submitting.value) return;
		uni.showModal({
			title: ui.confirmVerify,
			content: ui.verifyConfirm,
			success: async (modalRes) => {
				if (!modalRes.confirm) return;
				submitting.value = true;
				try {
					const res = await postMerchantOrderAction(target.id, {
						action: 'verify'
					});
					if (res.code !== 0) {
						uni.showToast({
							title: res.msg || '核销失败',
							icon: 'none'
						});
						return;
					}
					if (res.data) {
						const nextOrder = mapOrderRecord(res.data);
						if (order.value?.id === target.id) {
							order.value = nextOrder;
						}
					}
					uni.showToast({
						title: ui.verifySuccess,
						icon: 'success'
					});
					await loadPendingOrders(true);
				} catch (e) {
					uni.showToast({
						title: ui.networkError,
						icon: 'none'
					});
				} finally {
					submitting.value = false;
				}
			}
		});
	}

	onLoad(async (options) => {
		await initTheme();
		uni.setNavigationBarTitle({
			title: ui.verifyTitle
		});
		applyNavBarColor();
		if (options?.orderNo) {
			orderNo.value = decodeURIComponent(options.orderNo);
		}
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		const fromRoute = readRouteOrderNo();
		if (fromRoute && fromRoute !== orderNo.value) {
			orderNo.value = fromRoute;
			order.value = null;
			searched.value = false;
		}
		if (orderNo.value && !order.value) {
			loadOrder();
		}
		if (!order.value) {
			await loadPendingOrders(true);
		}
	});

	onPullDownRefresh(async () => {
		if (order.value) {
			await loadOrder();
		} else {
			await loadPendingOrders(true);
		}
		uni.stopPullDownRefresh();
	});

	onReachBottom(async () => {
		if (order.value || listLoading.value || !listHasMore.value) return;
		listPage.value += 1;
		await loadPendingOrders(false);
	});
</script>

<style lang="scss" scoped>
	.ry-card {
		box-shadow: none;
	}

	.mv-page {
		min-height: 100vh;
		background: var(--ry-page-bg);
		padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	}

	.mv-search {
		padding: 20rpx 24rpx;

		&__bar {
			display: flex;
			align-items: center;
			gap: 12rpx;
			padding: 0 20rpx;
			height: 80rpx;
			border-radius: 999rpx;
			background: #fafafa;
			border: 1rpx solid #eee;
		}

		&__input {
			flex: 1;
			font-size: 28rpx;
			color: var(--ry-text-primary);
		}

		&__placeholder {
			color: #ccc;
		}

		&__action {
			flex-shrink: 0;
			font-size: 28rpx;
			font-weight: 600;
			padding-left: 8rpx;

			&:active {
				opacity: 0.75;
			}
		}
	}

	.mv-result {
		margin-top: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__label {
			padding: 0 4rpx;
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}
	}

	.mv-card {
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;

		&:active {
			opacity: 0.92;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__head-left {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__type {
			flex-shrink: 0;
			font-size: 22rpx;
			color: var(--ry-text-hint);
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: var(--ry-text-hint);
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__status {
			flex-shrink: 0;
			font-size: 22rpx;
			padding: 4rpx 14rpx;
			border-radius: 999rpx;

			&--is-pending {
				background: var(--mc-primary-light, #eaf2ff);
				color: var(--mc-primary, #2979ff);
			}

			&--is-done {
				background: #e8f8ef;
				color: #2eaf6a;
			}

			&--is-cancel {
				background: #fff0f0;
				color: #ff4d4f;
			}

			&--is-ship,
			&--is-default {
				background: #f2f3f7;
				color: #8a8a96;
			}
		}

		&__body {
			display: flex;
			align-items: flex-start;
			gap: 16rpx;
		}

		&__date {
			flex-shrink: 0;
			width: 140rpx;
			padding: 38rpx 0;
			border-radius: 14rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 2rpx;
		}

		&__date-day {
			font-size: 32rpx;
			font-weight: 800;
			line-height: 1.1;
		}

		&__date-sub {
			font-size: 20rpx;
			color: #8a8a96;
			line-height: 1.2;
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

		&__title {
			flex: 1;
			min-width: 0;
			font-size: 28rpx;
			font-weight: 700;
			color: var(--ry-text-primary);
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__tag {
			flex-shrink: 0;
			padding: 2rpx 10rpx;
			border-radius: 8rpx;
			background: #f2f3f7;
			font-size: 20rpx;
			color: #8a8a96;
		}

		&__highlight {
			font-size: 24rpx;
			color: var(--mc-primary, #2979ff);
			line-height: 1.4;
		}

		&__extra {
			font-size: 24rpx;
			color: var(--ry-text-secondary);
			line-height: 1.4;
		}

		&__guest {
			display: flex;
			align-items: center;
			gap: 12rpx;
			font-size: 24rpx;
			color: var(--ry-text-secondary);
		}

		&__call {
			flex-shrink: 0;
			width: 52rpx;
			height: 52rpx;
			border-radius: 50%;
			background: var(--mc-primary-light, #eaf2ff);
			display: flex;
			align-items: center;
			justify-content: center;

			&:active {
				opacity: 0.75;
			}
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding-top: 10rpx;
			border-top: 1rpx solid #f2f2f2;
		}

		&__amount {
			font-size: 30rpx;
			font-weight: 800;
			color: #ff6600;
		}

		&__action {
			flex-shrink: 0;
			min-width: 160rpx;
			height: 68rpx;
			padding: 0 28rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 26rpx;
				font-weight: 600;
				color: #fff;
			}
		}

		&__tip {
			flex: 1;
			text-align: right;
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}
	}

	.mv-empty {
		padding-top: 80rpx;
	}

	.mv-loading {
		padding: 80rpx 0;
		display: flex;
		justify-content: center;
	}

	.mv-pending {
		margin-top: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 0 4rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			color: var(--ry-text-primary);
		}

		&__count {
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}
	}

	.mv-pending-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
</style>
