<template>
	<view class="activity-list-page">
		<scroll-view class="activity-list-tabs" scroll-x :show-scrollbar="false">
			<view class="activity-list-tabs__track">
				<view
					v-for="(tab, index) in tabs"
					:key="tab.key"
					class="activity-list-tabs__item"
					:class="{ 'activity-list-tabs__item--active': currentTab === index }"
					@click="onTabTap(index)"
				>
					<text class="activity-list-tabs__text">{{ tab.label }}</text>
					<view v-if="currentTab === index" class="activity-list-tabs__line" />
				</view>
			</view>
		</scroll-view>

		<view class="activity-list-body">
			<view v-if="activityList.length" class="activity-list-cards">
				<view
					v-for="item in activityList"
					:key="item.id"
					class="activity-list-card ry-card"
					@click="openDetail(item)"
				>
					<view class="activity-list-card__cover">
						<image class="activity-list-card__img ry-placeholder-img" :src="item.cover" mode="aspectFill" />
						<view
							class="activity-list-card__status"
							:class="`activity-list-card__status--${item.status}`"
						>
							<text class="activity-list-card__status-text">{{ item.statusLabel }}</text>
						</view>
					</view>
					<view class="activity-list-card__info">
						<text class="activity-list-card__title">{{ item.name }}</text>
						<view v-if="item.tags?.length" class="activity-list-card__tags">
							<view v-for="tag in item.tags" :key="tag" class="activity-list-card__tag">
								<text class="activity-list-card__tag-text">{{ tag }}</text>
							</view>
						</view>
						<view class="activity-list-card__meta">
							<u-icon name="map" color="#999999" :size="13" />
							<text class="activity-list-card__meta-text">{{ item.address }}</text>
						</view>
						<view class="activity-list-card__meta">
							<u-icon name="calendar" color="#999999" :size="13" />
							<text class="activity-list-card__meta-text">{{ item.time }}</text>
						</view>
						<view class="activity-list-card__footer">
							<text class="activity-list-card__count">{{ item.participants }}{{ ui.labelParticipants }}</text>
							<u-icon name="arrow-right" color="#9C27B0" :size="14" />
						</view>
					</view>
				</view>
			</view>
			<view v-else-if="!loading" class="activity-list-empty">
				<u-empty mode="list" :text="ui.emptyText" />
			</view>
			<view v-if="loading && activityList.length" class="activity-list-loadmore">
				<text class="activity-list-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && activityList.length" class="activity-list-loadmore">
				<text class="activity-list-loadmore__text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { ACTIVITY_UI, ACTIVITY_TABS } from '@/common/activity-ui.js';
import { buildLocationListQuery } from '@/common/location-params.js';
import { fetchActivityList } from '@/common/api/activity.js';

const ui = ACTIVITY_UI;
const tabs = ACTIVITY_TABS;
const PAGE_SIZE = 10;

const currentTab = ref(0);
const activityList = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
function mapActivityItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		address: record.address || '',
		time: record.time || '',
		status: record.status || '',
		statusLabel: record.statusLabel || '',
		participants: record.participants != null ? Number(record.participants) : 0,
		cover: record.cover || PLACEHOLDER_LOGO,
		tags: Array.isArray(record.tags) ? record.tags : []
	};
}

/** 分页拉取活动列表（服务端 status 阶段筛选） */
async function loadActivityList(reset = false) {
	if (loading.value) {
		return;
	}
	if (!reset && !hasMore.value) {
		return;
	}

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	const tabKey = tabs[currentTab.value].key;
	const params = await buildLocationListQuery({
		page: page.value,
		limit: PAGE_SIZE,
		status: tabKey
	});

	try {
		const res = await fetchActivityList(params);
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapActivityItem);
			activityList.value = reset ? records : [...activityList.value, ...records];
			const total = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && activityList.value.length < total;
		} else {
			if (reset) {
				activityList.value = [];
			}
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) {
			activityList.value = [];
		}
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function onTabTap(index) {
	if (currentTab.value === index) {
		return;
	}
	currentTab.value = index;
	loadActivityList(true);
}

function openDetail(item) {
	uni.navigateTo({ url: `/mobile-design/activity/detail?id=${item.id}` });
}

onLoad(async () => {
	uni.setNavigationBarTitle({ title: ui.listTitle });
	await loadActivityList(true);
});

/** 从详情页报名返回后刷新参与人数等列表字段 */
onShow(async () => {
	await loadActivityList(true);
});

onPullDownRefresh(async () => {
	await loadActivityList(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) {
		return;
	}
	page.value += 1;
	loadActivityList(false);
});
</script>

<style lang="scss" scoped>
.activity-list-page {
	min-height: 100vh;
	background-color: #f8f0fa;
}

.activity-list-body {
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.activity-list-tabs {
	background-color: #ffffff;
	border-bottom: 1rpx solid #f0f0f0;
}

.activity-list-tabs__track {
	display: flex;
	flex-direction: row;
	overflow-x: auto;
	padding: 0 var(--ry-page-padding-x);
}

.activity-list-tabs__item {
	position: relative;
	flex-shrink: 0;
	height: 80rpx;
	padding: 0 20rpx;
	display: flex;
	align-items: center;
}

.activity-list-tabs__text {
	font-size: 28rpx;
	color: #666666;
	white-space: nowrap;
}

.activity-list-tabs__item--active .activity-list-tabs__text {
	font-weight: 600;
	color: var(--ry-module-activity, #9c27b0);
}

.activity-list-tabs__line {
	position: absolute;
	left: 50%;
	bottom: 8rpx;
	width: 48rpx;
	height: 6rpx;
	border-radius: 3rpx;
	background-color: var(--ry-module-activity, #9c27b0);
	transform: translateX(-50%);
}

.activity-list-cards {
	display: flex;
	flex-direction: column;
	gap: var(--ry-card-gap, 20rpx);
	padding: 24rpx var(--ry-page-padding-x) 0;
}

.activity-list-card {
	overflow: hidden;
}

.activity-list-card__cover {
	position: relative;
	height: 280rpx;
}

.activity-list-card__img {
	width: 100%;
	height: 100%;
}

.activity-list-card__status {
	position: absolute;
	top: 16rpx;
	left: 16rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 40rpx;
	padding: 8rpx 24rpx;
	border-radius: var(--ry-radius-pill, 999rpx);
}

.activity-list-card__status--ongoing {
	background-color: rgba(156, 39, 176, 0.92);
}

.activity-list-card__status--upcoming {
	background-color: rgba(255, 102, 0, 0.92);
}

.activity-list-card__status--ended {
	background-color: rgba(153, 153, 153, 0.92);
}

.activity-list-card__status-text {
	font-size: 22rpx;
	font-weight: 400;
	color: #ffffff;
	line-height: 1;
}

.activity-list-card__info {
	padding: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.activity-list-card__title {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--ry-text-primary, #1a1a1a);
}

.activity-list-card__tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.activity-list-card__tag {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 40rpx;
	padding: 8rpx 24rpx;
	border-radius: 999rpx;
	background-color: #f3e8f8;
}

.activity-list-card__tag-text {
	font-size: 22rpx;
	font-weight: 400;
	color: #9c27b0;
	line-height: 1;
}

.activity-list-card__meta {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 8rpx;
}

.activity-list-card__meta-text {
	font-size: 24rpx;
	color: var(--ry-text-secondary, #666666);
}

.activity-list-card__footer {
	margin-top: 8rpx;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.activity-list-card__count {
	font-size: 22rpx;
	color: var(--ry-text-hint, #999999);
}

.activity-list-empty {
	padding: 120rpx 0;
}

.activity-list-loadmore {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx 0 8rpx;
}

.activity-list-loadmore__text {
	font-size: 24rpx;
	color: var(--ry-text-hint, #999999);
}
</style>
