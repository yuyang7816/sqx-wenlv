<template>
	<view class="team-page">
		<view class="team-hero">
			<view class="team-hero__badge">{{ ui.teamDataBadge }}</view>
			<text class="team-hero__desc">{{ ui.teamDataDesc }}</text>

			<view class="team-hero__stats ry-card">
				<view class="team-hero__stat">
					<text class="team-hero__stat-num">{{ summary.inviteCount ?? 0 }}</text>
					<text class="team-hero__stat-label">{{ ui.inviteCountLabel }}</text>
				</view>
				<view class="team-hero__stat-divider" />
				<view class="team-hero__stat team-hero__stat--link" @click="goCommission">
					<text class="team-hero__stat-num">{{ summary.totalEarnings || '0.00' }}</text>
					<text class="team-hero__stat-label">{{ ui.earningsLabel }}</text>
				</view>
			</view>

			<view class="team-hero__rebate">
				<text class="team-hero__rebate-title">{{ ui.rebateTitle }}</text>
				<text class="team-hero__rebate-line">{{ config.rebateLevel1 || '—' }}</text>
				<text class="team-hero__rebate-line">{{ config.rebateLevel2 || '—' }}</text>
			</view>

			<view class="team-hero__invite-btn" @click="goInvite">
				<text>{{ ui.inviteFriendsBtn }}</text>
			</view>
		</view>

		<view class="team-tabs">
			<view
				class="team-tabs__item"
				:class="{ 'team-tabs__item--active': activeLevel === 1 }"
				@click="switchLevel(1)"
			>
				<text>{{ ui.level1Tab }}（{{ summary.level1Count ?? 0 }}）</text>
			</view>
			<view
				class="team-tabs__item"
				:class="{ 'team-tabs__item--active': activeLevel === 2 }"
				@click="switchLevel(2)"
			>
				<text>{{ ui.level2Tab }}（{{ summary.level2Count ?? 0 }}）</text>
			</view>
		</view>

		<view class="team-list ry-card">
			<view class="team-list__head">
				<text class="team-list__title">{{ ui.teamMembersTitle }}</text>
				<text class="team-list__desc">{{ ui.teamMembersDesc }}</text>
			</view>

			<view class="team-list__table-head">
				<text class="team-list__col team-list__col--no">{{ ui.colNo }}</text>
				<text class="team-list__col team-list__col--avatar">{{ ui.colAvatar }}</text>
				<text class="team-list__col team-list__col--name">{{ ui.colNickname }}</text>
				<text class="team-list__col team-list__col--time">{{ ui.colTime }}</text>
			</view>

			<view v-if="members.length" class="team-list__body">
				<view v-for="item in members" :key="item.id" class="team-list__row">
					<text class="team-list__col team-list__col--no">{{ item.id }}</text>
					<view class="team-list__col team-list__col--avatar">
						<image class="team-list__avatar" :src="resolveAvatar(item.avatar)" mode="aspectFill" />
					</view>
					<text class="team-list__col team-list__col--name">{{ item.nickname }}</text>
					<text class="team-list__col team-list__col--time">{{ item.joinTime }}</text>
				</view>
			</view>

			<view v-else-if="!loading" class="team-list__empty">
				<u-icon name="file-text" color="#CCCCCC" :size="48" />
				<text class="team-list__empty-text">{{ ui.emptyData }}</text>
			</view>

			<view v-if="loading && members.length" class="team-list__loadmore">
				<text>加载中...</text>
			</view>
			<view v-else-if="!hasMore && members.length" class="team-list__loadmore">
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
	fetchInviteConfig,
	fetchInviteTeamSummary,
	fetchInviteTeamMembers,
	INVITE_PAGE_SIZE
} from '@/common/api/invite.js';

const ui = INVITE_UI;
const defaultAvatar = getDefaultAvatar();
const config = ref({});
const summary = ref({});
const activeLevel = ref(1);
const members = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadSummaryAndConfig() {
	try {
		const [configRes, summaryRes] = await Promise.all([
			fetchInviteConfig(),
			fetchInviteTeamSummary()
		]);
		if (configRes.code === 0 && configRes.data) {
			config.value = configRes.data;
		}
		if (summaryRes.code === 0 && summaryRes.data) {
			summary.value = summaryRes.data;
		} else if (summaryRes.code !== 0) {
			uni.showToast({ title: summaryRes.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	}
}

async function loadMembers(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchInviteTeamMembers({
			level: activeLevel.value,
			page: page.value,
			limit: INVITE_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			members.value = reset ? list : [...members.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= INVITE_PAGE_SIZE && members.value.length < total;
		} else {
			if (reset) members.value = [];
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		if (reset) members.value = [];
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function reloadData() {
	await Promise.all([loadSummaryAndConfig(), loadMembers(true)]);
}

function switchLevel(level) {
	if (activeLevel.value === level) return;
	activeLevel.value = level;
	loadMembers(true);
}

function goInvite() {
	uni.navigateTo({ url: '/pages/my/invite' });
}

function goCommission() {
	uni.navigateTo({ url: '/pages/my/commission' });
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.teamTitle });
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
	loadMembers(false);
});
</script>

<style lang="scss" scoped>
.team-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.team-hero {
	padding: 28rpx 24rpx 24rpx;
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
		padding: 28rpx 16rpx;
		margin-top: 4rpx;
	}

	&__stat {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;

		&--link:active {
			opacity: 0.85;
		}
	}

	&__stat-num {
		font-size: 44rpx;
		font-weight: 700;
		color: #FF6600;
		line-height: 1.1;
	}

	&__stat-label {
		font-size: 24rpx;
		color: var(--ry-text-secondary);
	}

	&__stat-divider {
		width: 1rpx;
		height: 64rpx;
		background: #F0F0F0;
	}

	&__rebate {
		padding: 20rpx 24rpx;
		border-radius: var(--ry-radius-md);
		background: rgba(255, 255, 255, 0.18);
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__rebate-title {
		font-size: 26rpx;
		font-weight: 600;
		color: #FFFFFF;
	}

	&__rebate-line {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.88);
		line-height: 1.5;
	}

	&__invite-btn {
		margin-top: 4rpx;
		height: 88rpx;
		border-radius: var(--ry-radius-pill);
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 30rpx;
		font-weight: 600;
		color: #FF6600;

		&:active {
			opacity: 0.9;
		}
	}
}

.team-tabs {
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
		transition: all 0.2s;

		&--active {
			background: #FF6600;
			color: #FFFFFF;
			font-weight: 600;
		}
	}
}

.team-list {
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
		margin: 0;
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

		&--time {
			width: 200rpx;
			flex-shrink: 0;
			text-align: right;
			font-size: 22rpx;
			color: var(--ry-text-hint);
		}
	}

	&__table-head .team-list__col {
		font-weight: 600;
		color: #FF6600;
	}

	&__row {
		min-height: 96rpx;
		border-bottom: 1rpx solid #F5F5F5;

		.team-list__col--name {
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
