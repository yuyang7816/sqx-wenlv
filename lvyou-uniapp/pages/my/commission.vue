<template>
	<view class="commission-page">
		<view class="commission-hero">
			<view class="commission-hero__badge">{{ ui.commissionOverviewBadge }}</view>
			<text class="commission-hero__desc">{{ ui.commissionOverviewDesc }}</text>

			<view class="commission-hero__stats ry-card">
				<view class="commission-hero__stat">
					<text class="commission-hero__stat-num">{{ summary.total || '0.00' }}</text>
					<text class="commission-hero__stat-label">{{ ui.totalEarnings }}</text>
				</view>
				<view class="commission-hero__stat-divider" />
				<view class="commission-hero__stat">
					<text class="commission-hero__stat-num">{{ summary.level1 || '0.00' }}</text>
					<text class="commission-hero__stat-label">{{ ui.level1Earnings }}</text>
				</view>
				<view class="commission-hero__stat-divider" />
				<view class="commission-hero__stat">
					<text class="commission-hero__stat-num">{{ summary.level2 || '0.00' }}</text>
					<text class="commission-hero__stat-label">{{ ui.level2Earnings }}</text>
				</view>
			</view>
		</view>

		<view class="commission-tabs">
			<view
				class="commission-tabs__item"
				:class="{ 'commission-tabs__item--active': activeLevel === 1 }"
				@click="switchLevel(1)"
			>
				<text>{{ ui.level1Tab }}（{{ summary.level1Count ?? 0 }}）</text>
			</view>
			<view
				class="commission-tabs__item"
				:class="{ 'commission-tabs__item--active': activeLevel === 2 }"
				@click="switchLevel(2)"
			>
				<text>{{ ui.level2Tab }}（{{ summary.level2Count ?? 0 }}）</text>
			</view>
		</view>

		<view class="commission-list ry-card">
			<view class="commission-list__head">
				<text class="commission-list__title">{{ ui.commissionDetailTitle }}</text>
				<text class="commission-list__desc">{{ ui.commissionDetailDesc }}</text>
			</view>

			<view class="commission-list__table-head">
				<text class="commission-list__col commission-list__col--no">{{ ui.colNo }}</text>
				<text class="commission-list__col commission-list__col--avatar">{{ ui.colAvatar }}</text>
				<text class="commission-list__col commission-list__col--name">{{ ui.colNickname }}</text>
				<text class="commission-list__col commission-list__col--reward">{{ ui.colReward }}</text>
			</view>

			<view v-if="records.length" class="commission-list__body">
				<view v-for="item in records" :key="item.id" class="commission-list__row">
					<text class="commission-list__col commission-list__col--no">{{ item.id }}</text>
					<view class="commission-list__col commission-list__col--avatar">
						<image class="commission-list__avatar" :src="resolveAvatar(item.avatar)" mode="aspectFill" />
					</view>
					<text class="commission-list__col commission-list__col--name">{{ item.nickname }}</text>
					<text class="commission-list__col commission-list__col--reward">+{{ item.reward }}</text>
				</view>
			</view>

			<view v-else-if="!loading" class="commission-list__empty">
				<u-icon name="file-text" color="#CCCCCC" :size="48" />
				<text class="commission-list__empty-text">{{ ui.emptyData }}</text>
			</view>

			<view v-if="loading && records.length" class="commission-list__loadmore">
				<text>加载中...</text>
			</view>
			<view v-else-if="!hasMore && records.length" class="commission-list__loadmore">
				<text>没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { getDefaultAvatar, resolveAvatar } from '@/common/avatar.js';
import { INVITE_UI } from '@/common/invite-ui.js';
import { hasToken } from '@/common/api/auth.js';
import {
	fetchInviteCommissionSummary,
	fetchInviteCommissionDetails,
	INVITE_PAGE_SIZE
} from '@/common/api/invite.js';

const ui = INVITE_UI;
const defaultAvatar = getDefaultAvatar();
const summary = ref({});
const activeLevel = ref(1);
const records = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadSummary() {
	try {
		const res = await fetchInviteCommissionSummary();
		if (res.code === 0 && res.data) {
			summary.value = res.data;
		} else if (res.code !== 0) {
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	}
}

async function loadRecords(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchInviteCommissionDetails({
			level: activeLevel.value,
			page: page.value,
			limit: INVITE_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			records.value = reset ? list : [...records.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= INVITE_PAGE_SIZE && records.value.length < total;
		} else {
			if (reset) records.value = [];
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		if (reset) records.value = [];
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function reloadData() {
	await Promise.all([loadSummary(), loadRecords(true)]);
}

function switchLevel(level) {
	if (activeLevel.value === level) return;
	activeLevel.value = level;
	loadRecords(true);
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.commissionTitle });
});

onShow(() => {
	if (!requireLogin()) return;
	reloadData();
});

onPullDownRefresh(async () => {
	if (!hasToken()) {
		uni.stopPullDownRefresh();
		return;
	}
	await reloadData();
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasToken() || loading.value || !hasMore.value) return;
	page.value += 1;
	loadRecords(false);
});
</script>

<style lang="scss" scoped>
.commission-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.commission-hero {
	padding: 28rpx 24rpx;
	border-radius: var(--ry-radius-lg);
	background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%);
	display: flex;
	flex-direction: column;
	gap: 16rpx;

	&__badge {
		align-self: flex-start;
		padding: 6rpx 20rpx;
		border-radius: var(--ry-radius-pill);
		background: rgba(255, 255, 255, 0.95);
		font-size: 22rpx;
		font-weight: 600;
		color: #FF6600;
	}

	&__desc {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.92);
		line-height: 1.5;
	}

	&__stats {
		display: flex;
		align-items: center;
		padding: 28rpx 8rpx;
		margin-top: 4rpx;
	}

	&__stat {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
	}

	&__stat-num {
		font-size: 36rpx;
		font-weight: 700;
		color: #FF6600;
		line-height: 1.1;
	}

	&__stat-label {
		font-size: 22rpx;
		color: var(--ry-text-secondary);
		text-align: center;
	}

	&__stat-divider {
		width: 1rpx;
		height: 56rpx;
		background: #F0F0F0;
	}
}

.commission-tabs {
	display: flex;
	padding: 8rpx;
	border-radius: var(--ry-radius-pill);
	background: #FFFFFF;
	box-shadow: var(--ry-card-shadow);

	&__item {
		flex: 1;
		height: 72rpx;
		border-radius: var(--ry-radius-pill);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		color: var(--ry-text-secondary);

		&--active {
			background: #FF6600;
			color: #FFFFFF;
			font-weight: 600;
		}
	}
}

.commission-list {
	padding: 24rpx 0 16rpx;
	flex: 1;

	&__head {
		padding: 0 24rpx 20rpx;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__desc {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	&__table-head,
	&__row {
		display: flex;
		align-items: center;
		padding: 0 24rpx;
	}

	&__table-head {
		height: 64rpx;
		background: #FFF8F3;
		margin: 0 16rpx;
		border-radius: var(--ry-radius-sm);
	}

	&__col {
		font-size: 24rpx;
		color: var(--ry-text-secondary);

		&--no {
			width: 72rpx;
			flex-shrink: 0;
		}

		&--avatar {
			width: 88rpx;
			flex-shrink: 0;
			display: flex;
			justify-content: center;
		}

		&--name {
			flex: 1;
			min-width: 0;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&--reward {
			width: 120rpx;
			flex-shrink: 0;
			text-align: right;
			font-weight: 600;
			color: #FF6600;
		}
	}

	&__table-head .commission-list__col {
		font-weight: 600;
		color: #FF6600;
	}

	&__row {
		min-height: 96rpx;
		border-bottom: 1rpx solid #F5F5F5;

		.commission-list__col--name {
			color: var(--ry-text-primary);
		}
	}

	&__avatar {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: #F5F5F5;
	}

	&__empty {
		padding: 80rpx 0 60rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
	}

	&__empty-text {
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}

	&__loadmore {
		padding: 24rpx 0;
		text-align: center;
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}
</style>
