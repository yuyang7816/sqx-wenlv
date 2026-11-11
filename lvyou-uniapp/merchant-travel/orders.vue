<template>
	<view class="rt-page">
		<!-- 顶栏：分段 Tab -->
		<view class="rt-head">
			<view class="rt-seg">
				<view v-for="tab in tabs" :key="tab.key" class="rt-seg__item"
					:class="{ 'rt-seg__item--active': activeTab === tab.key }"
					:style="activeTab === tab.key ? { background: theme.primary, boxShadow: `0 6rpx 20rpx ${theme.primary}40` } : {}"
					@click="switchTab(tab.key)">
					<text class="rt-seg__text">{{ tab.label }}</text>
					<view v-if="tab.key === 'verify' && pendingCount > 0" class="rt-seg__dot" />
				</view>
			</view>

			<view v-if="activeTab === 'verify' && pendingCount > 0" class="rt-tip">
				<view class="rt-tip__icon">
					<u-icon name="info-circle-fill" :color="theme.primary" :size="14" />
				</view>
				<text class="rt-tip__text">{{ pendingCount }} 条订单待核销</text>
			</view>
		</view>

		<!-- 列表 -->
		<view v-if="orders.length" class="rt-list">
			<view v-for="item in orders" :key="item.id" class="rt-item" @click="goDetail(item.id)">
				<view class="rt-item__inner">
					<view class="rt-item__top">
						<view class="rt-item__top-left">
							<text class="rt-item__status" :class="`rt-item__status--${statusBadgeClass(item.status)}`">
								{{ item.statusLabel }}
							</text>
							<text v-if="item.orderNo" class="rt-item__no">{{ item.orderNo }}</text>
						</view>
						<text class="rt-item__time">{{ formatListTime(item.createdAt) }}</text>
					</view>

					<view class="rt-item__core">
						<view v-if="item.tourDate" class="rt-item__date" :style="{ background: theme.primaryLight }">
							<text class="rt-item__date-day" :style="{ color: theme.primary }">{{ tourDay(item.tourDate) }}</text>
							<text class="rt-item__date-sub">{{ tourMonth(item.tourDate) }}</text>
						</view>

						<view class="rt-item__main">
							<text class="rt-item__title">{{ item.mainName }}</text>
							<view v-if="metaRows(item).length" class="rt-item__meta">
								<view v-for="row in metaRows(item)" :key="row.key" class="rt-item__meta-row">
									<view class="rt-item__meta-icon">
										<u-icon :name="row.icon" :color="theme.primary" :size="14" />
									</view>
									<text class="rt-item__meta-text">{{ row.text }}</text>
								</view>
							</view>
						</view>
					</view>

					<view class="rt-item__foot">
						<view class="rt-item__amount-wrap">
							<template v-if="isFreeBooking(item)">
								<text class="rt-item__amount rt-item__amount--free">{{ MERCHANT_ORDER_UI.labelFreeBooking }}</text>
							</template>
							<template v-else>
								<text class="rt-item__amount" :style="{ color: theme.primary }">{{ formatMoney(orderAmount(item)) }}</text>
							</template>
						</view>
						<view v-if="merchantActions(item).length" class="rt-item__actions" @click.stop>
							<view v-for="action in merchantActions(item)" :key="action.key" class="rt-item__btn"
								:style="{ background: theme.primary }" @click="onMerchantAction(action.key, item)">
								<text>{{ action.label }}</text>
							</view>
						</view>
						<view v-else class="rt-item__more">
							<text>查看详情</text>
							<view class="rt-item__more-icon">
								<u-icon name="arrow-right" color="#C0C0C8" :size="14" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-if="loading" class="rt-foot-loading">
				<u-loading-icon mode="circle" :color="theme.primary" :size="20" />
			</view>
			<view v-else-if="!hasMore" class="rt-foot-end">
				<text>— 没有更多了 —</text>
			</view>
		</view>

		<!-- 空态 -->
		<view v-else-if="!loading" class="rt-empty">
			<view class="rt-empty__icon" :style="{ background: theme.primaryLight }">
				<u-icon name="file-text-fill" :color="theme.primary" :size="48" />
			</view>
			<text class="rt-empty__title">{{ emptyTitle }}</text>
			<text class="rt-empty__desc">{{ emptyDesc }}</text>
		</view>

		<!-- 首屏加载 -->
		<view v-if="loading && orders.length === 0" class="rt-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		onLoad,
		onShow,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		formatMoney,
		maskPhone
	} from '@/common/merchant-center.js';
	import {
		mapMerchantOrder,
		statusBadgeClass,
		MERCHANT_ORDER_UI
	} from '@/common/merchant-order-ui.js';
	import {
		getMerchantOrderActions,
		runMerchantOrderAction
	} from '@/common/order-actions.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantDashboard,
		fetchMerchantOrders
	} from '@/common/api/merchant.js';

	const PACKAGE_STORE_TYPE = 'travel';
	const {
		theme,
		businessUi,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const ui = businessUi;
	const PAGE_SIZE = 10;
	const activeTab = ref('all');
	const orders = ref([]);
	const pendingCount = ref(0);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	const tabs = computed(() => [{
			key: 'all',
			label: ui.value.tabAll
		},
		{
			key: 'verify',
			label: '待核销'
		},
		{
			key: 'processing',
			label: ui.value.tabProcessing
		},
		{
			key: 'completed',
			label: ui.value.tabCompleted
		}
	]);

	const emptyTitle = computed(() => {
		if (activeTab.value === 'verify') return '暂无待核销订单';
		if (activeTab.value === 'processing') return '暂无进行中订单';
		if (activeTab.value === 'completed') return '暂无已完成订单';
		return ui.value.emptyOrders;
	});

	const emptyDesc = computed(() => {
		if (activeTab.value === 'verify') return '支付成功的线路订单会出现在这里';
		return '切换其他分类看看';
	});

	async function loadPendingCount() {
		try {
			const res = await fetchMerchantDashboard();
			if (res.code === 0) {
				pendingCount.value = Number(res.data?.order?.pendingVerify ?? 0);
			}
		} catch (e) {
			/* ignore */
		}
	}

	async function loadOrders(reset = false) {
		if (loading.value) return;
		if (!reset && !hasMore.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		loading.value = true;
		try {
			const res = await fetchMerchantOrders({
				tab: activeTab.value,
				page: page.value,
				limit: PAGE_SIZE
			});
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapMerchantOrder).filter(Boolean);
				orders.value = reset ? records : [...orders.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && orders.value.length < total;
			} else {
				if (reset) orders.value = [];
				uni.showToast({
					title: res.msg || ui.value.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) orders.value = [];
			uni.showToast({
				title: ui.value.networkError,
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	async function reload(reset = true) {
		await Promise.all([loadOrders(reset), loadPendingCount()]);
	}

	function switchTab(key) {
		if (activeTab.value === key) return;
		activeTab.value = key;
		reload(true);
	}

	function goDetail(id) {
		uni.navigateTo({
			url: `/merchant-travel/order-detail?id=${id}`
		});
	}

	function merchantActions(order) {
		return getMerchantOrderActions(order);
	}

	function contactName(item) {
		return item.customerName || item.guestNames || '';
	}

	function metaRows(item) {
		if (!item) return [];
		const rows = [];
		const name = contactName(item);
		if (name) {
			rows.push({
				key: 'contact',
				icon: 'account-fill',
				text: `${MERCHANT_ORDER_UI.labelContact}：${name}`
			});
		}
		if (item.customerPhone) {
			rows.push({
				key: 'phone',
				icon: 'phone-fill',
				text: `电话：${maskPhone(item.customerPhone)}`
			});
		}
		return rows;
	}

	function tourDay(dateStr) {
		if (!dateStr) return '--';
		const parts = String(dateStr).split('-');
		return parts.length >= 3 ? Number(parts[2]) : dateStr;
	}

	function tourMonth(dateStr) {
		if (!dateStr) return '';
		const parts = String(dateStr).split('-');
		if (parts.length < 2) return '';
		return `${Number(parts[1])}月出行`;
	}

	function orderAmount(item) {
		return Number(item?.totalAmount) > 0 ? Number(item.totalAmount) : Number(item?.amount) || 0;
	}

	function isFreeBooking(item) {
		return orderAmount(item) <= 0;
	}

	function formatListTime(dateTime) {
		if (!dateTime) return '';
		const text = String(dateTime);
		if (text.includes(' ')) {
			const [date, time] = text.split(' ');
			const today = new Date();
			const y = today.getFullYear();
			const m = String(today.getMonth() + 1).padStart(2, '0');
			const d = String(today.getDate()).padStart(2, '0');
			const todayStr = `${y}-${m}-${d}`;
			if (date === todayStr) return `今天 ${time?.slice(0, 5) || ''}`;
			return `${date.slice(5)} ${time?.slice(0, 5) || ''}`;
		}
		return text;
	}

	async function onMerchantAction(actionKey, order) {
		await runMerchantOrderAction(actionKey, order, {
			onChanged: () => reload(true)
		});
	}

	onLoad(async (options) => {
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		uni.setNavigationBarTitle({
			title: ui.value.ordersTitle
		});
		applyNavBarColor();
		if (options?.tab) activeTab.value = options.tab;
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		applyNavBarColor();
		uni.setNavigationBarTitle({
			title: ui.value.ordersTitle
		});
		await reload(true);
	});

	onPullDownRefresh(async () => {
		await reload(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(async () => {
		if (!hasMore.value || loading.value) return;
		page.value += 1;
		await loadOrders(false);
	});
</script>

<style lang="scss" scoped>
	.rt-page {
		min-height: 100vh;
		background: #F5F6F8;
		padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
	}

	/* ── 顶栏 ── */
	.rt-head {
		position: sticky;
		top: 0;
		z-index: 20;
		padding: 20rpx var(--ry-page-padding-x) 16rpx;
		background: #F5F6F8;
	}

	.rt-seg {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx;
		border-radius: 20rpx;
		background: #EBECF0;

		&__item {
			position: relative;
			flex: 1;
			height: 68rpx;
			border-radius: 14rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			transition: all 0.22s ease;
		}

		&__item--active .rt-seg__text {
			color: #FFFFFF;
			font-weight: 700;
		}

		&__text {
			font-size: 26rpx;
			color: #6B6B78;
			line-height: 1;
		}

		&__dot {
			position: absolute;
			top: 10rpx;
			right: 18rpx;
			width: 14rpx;
			height: 14rpx;
			border-radius: 50%;
			background: #FF4D4F;
			border: 2rpx solid #FFFFFF;
			box-sizing: border-box;
		}
	}

	.rt-tip {
		display: flex;
		align-items: center;
		gap: 10rpx;
		margin-top: 16rpx;
		padding: 16rpx 20rpx;
		border-radius: 14rpx;
		background: #FFFFFF;

		&__icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__text {
			font-size: 24rpx;
			color: #4A4A56;
			line-height: 36rpx;
		}
	}

	/* ── 列表 ── */
	.rt-list {
		padding: 8rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.rt-item {
		border-radius: 20rpx;
		background: #FFFFFF;
		overflow: hidden;
		box-shadow: 0 8rpx 32rpx rgba(15, 23, 42, 0.05);

		&__inner {
			padding: 24rpx;
		}

		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 20rpx;
		}

		&__top-left {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #B0B0B8;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__status {
			padding: 6rpx 16rpx;
			border-radius: 999rpx;
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1.2;

			&--is-pending {
				background: #FFF3EB;
				color: #FF6600;
			}

			&--is-ship {
				background: #EAF2FF;
				color: #2979FF;
			}

			&--is-done {
				background: #E8F8EF;
				color: #2EAF6A;
			}

			&--is-cancel {
				background: #FFF0F0;
				color: #FF4D4F;
			}

			&--is-default {
				background: #F2F3F7;
				color: #8A8A96;
			}
		}

		&__time {
			font-size: 22rpx;
			color: #B0B0B8;
			flex-shrink: 0;
		}

		&__core {
			display: flex;
			align-items: center;
			gap: 20rpx;
		}

		&__date {
			width: 145rpx;
			flex-shrink: 0;
			border-radius: 16rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 4rpx;
			padding: 32rpx 8rpx;
			box-sizing: border-box;
		}

		&__date-day {
			font-size: 44rpx;
			font-weight: 800;
			line-height: 1;
		}

		&__date-sub {
			font-size: 20rpx;
			color: #6B6B78;
			font-weight: 600;
			line-height: 1.2;
			text-align: center;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: center;
			gap: 12rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__meta {
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__meta-row {
			display: flex;
			align-items: center;
			gap: 10rpx;
			min-height: 36rpx;
		}

		&__meta-icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__meta-text {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #6B6B78;
			line-height: 36rpx;
		}

		&__foot {
			margin-top: 20rpx;
			padding-top: 20rpx;
			border-top: 1rpx solid #F0F1F5;
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__amount-wrap {
			flex-shrink: 0;
			display: flex;
			align-items: baseline;
			gap: 6rpx;
		}

		&__amount {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1.2;

			&--free {
				font-size: 24rpx;
				font-weight: 600;
				color: #2EAF6A;
			}
		}

		&__actions {
			flex-shrink: 0;
		}

		&__btn {
			min-width: 168rpx;
			height: 64rpx;
			padding: 0 28rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;

			text {
				font-size: 26rpx;
				font-weight: 600;
				color: #FFFFFF;
				line-height: 1;
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__more {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 4rpx;

			text {
				font-size: 24rpx;
				color: #B0B0B8;
				line-height: 36rpx;
			}
		}

		&__more-icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}

	.rt-foot-loading,
	.rt-foot-end {
		padding: 24rpx 0 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.rt-foot-end text {
		font-size: 22rpx;
		color: #C8C8D0;
	}

	/* ── 空态 / 加载 ── */
	.rt-empty {
		padding: 160rpx 48rpx 80rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;

		&__icon {
			width: 128rpx;
			height: 128rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 8rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 600;
			color: #1A1A22;
		}

		&__desc {
			font-size: 24rpx;
			color: #B0B0B8;
		}
	}

	.rt-loading {
		padding: 200rpx 0;
		display: flex;
		justify-content: center;
	}
</style>
