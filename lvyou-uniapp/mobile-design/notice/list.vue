<template>
	<view class="notice-list-page">
		<scroll-view class="notice-list-tabs" scroll-x :show-scrollbar="false">
			<view class="notice-list-tabs__track">
				<view v-for="(tab, index) in tabs" :key="tab.key" class="notice-list-tabs__item"
					:class="{ 'notice-list-tabs__item--active': currentTab === index }" @click="switchTab(index)">
					<text class="notice-list-tabs__text">{{ tab.label }}</text>
					<view v-if="currentTab === index" class="notice-list-tabs__line" />
				</view>
			</view>
		</scroll-view>

		<view class="notice-list-body">

			<view v-if="!loading && noticeList.length === 0" class="notice-list-empty">
				<u-empty mode="list" :text="ui.emptyText" />
			</view>
			<view v-else class="notice-list-cards">
				<view v-for="item in noticeList" :key="item.id" class="notice-list-card ry-card" @click="openDetail(item)">
					<view class="notice-list-card__head">
						<view v-if="item.pinned" class="notice-list-card__tag">
							<text class="notice-list-card__tag-text">{{ ui.tagPinned }}</text>
						</view>
						<text class="notice-list-card__title">{{ item.title }}</text>
					</view>
					<text class="notice-list-card__summary">{{ item.summary }}</text>
					<view class="notice-list-card__foot">
						<view class="notice-list-card__meta">
							<text class="notice-list-card__category">{{ item.categoryLabel }}</text>
							<text v-if="item.isNew" class="notice-list-card__new">{{ ui.tagNew }}</text>
						</view>
						<text class="notice-list-card__time">{{ item.time }}</text>
					</view>
				</view>
				<view v-if="loading && noticeList.length" class="notice-list-loadmore">
					<text class="notice-list-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && noticeList.length" class="notice-list-loadmore">
					<text class="notice-list-loadmore__text">没有更多了</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		onLoad,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		buildLocationListQuery
	} from '@/common/location-params.js';
	import {
		NOTICE_UI,
		NOTICE_CATEGORIES,
		fetchNoticeList
	} from '@/common/api/notice.js';

	const ui = NOTICE_UI;
	const tabs = NOTICE_CATEGORIES;
	const PAGE_SIZE = 10;

	const currentTab = ref(0);
	const noticeList = ref([]);
	const totalCount = ref(0);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	function mapNoticeItem(record) {
		return {
			id: record.id,
			title: record.title || '',
			summary: record.summary || '',
			time: record.time || '',
			category: record.category || '',
			categoryLabel: record.categoryLabel || '',
			source: record.source || '',
			pinned: !!record.pinned,
			isNew: !!record.isNew
		};
	}

	/** 分页拉取公告列表（服务端 category 筛选） */
	async function loadNoticeList(reset = false) {
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
		const base = {
			page: page.value,
			limit: PAGE_SIZE
		};
		if (tabKey !== 'all') {
			base.category = tabKey;
		}
		const params = await buildLocationListQuery(base);

		try {
			const res = await fetchNoticeList(params);
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapNoticeItem);
				noticeList.value = reset ? records : [...noticeList.value, ...records];
				totalCount.value = Number(res.data.total ?? noticeList.value.length);
				hasMore.value = records.length >= PAGE_SIZE && noticeList.value.length < totalCount.value;
			} else {
				if (reset) {
					noticeList.value = [];
					totalCount.value = 0;
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				noticeList.value = [];
				totalCount.value = 0;
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function switchTab(index) {
		if (currentTab.value === index) {
			return;
		}
		currentTab.value = index;
		loadNoticeList(true);
	}

	function openDetail(item) {
		uni.navigateTo({
			url: `/mobile-design/notice/detail?id=${item.id}`
		});
	}

	onLoad(async () => {
		uni.setNavigationBarTitle({
			title: ui.listTitle
		});
		await loadNoticeList(true);
	});

	onPullDownRefresh(async () => {
		await loadNoticeList(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) {
			return;
		}
		page.value += 1;
		loadNoticeList(false);
	});
</script>

<style lang="scss" scoped>
	.notice-list-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.notice-list-tabs {
		background-color: #ffffff;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.notice-list-tabs__track {
		display: flex;
		flex-direction: row;
		padding: 0 var(--ry-page-padding-x);
	}

	.notice-list-tabs__item {
		position: relative;
		flex-shrink: 0;
		height: 80rpx;
		padding: 0 20rpx;
		display: flex;
		align-items: center;
	}

	.notice-list-tabs__text {
		font-size: 28rpx;
		color: #666666;
		line-height: 1.4;
	}

	.notice-list-tabs__item--active .notice-list-tabs__text {
		font-weight: 600;
		color: var(--ry-brand);
	}

	.notice-list-tabs__line {
		position: absolute;
		left: 20rpx;
		right: 20rpx;
		bottom: 0;
		height: 4rpx;
		border-radius: 2rpx;
		background-color: var(--ry-brand);
	}

	.notice-list-body {
		padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	}

	.notice-list-hero {
		margin-bottom: 24rpx;
		padding: 28rpx 24rpx;
		background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
		border-radius: var(--ry-radius-md);
		border: 1rpx solid #ffe4cc;
	}

	.notice-list-hero__title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
	}

	.notice-list-hero__desc {
		display: block;
		margin-top: 8rpx;
		font-size: 26rpx;
		color: var(--ry-text-secondary);
		line-height: 1.5;
	}

	.notice-list-toolbar {
		margin-bottom: 20rpx;
	}

	.notice-list-toolbar__hint {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.notice-list-cards {
		display: flex;
		flex-direction: column;
		gap: var(--ry-card-gap);
	}

	.notice-list-empty {
		padding: 120rpx 48rpx;
		background-color: var(--ry-card-bg);
		border-radius: var(--ry-radius-md);
		box-shadow: var(--ry-card-shadow);
	}

	.notice-list-loadmore {
		padding: 24rpx 0;
		text-align: center;
	}

	.notice-list-loadmore__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.notice-list-card {
		padding: 28rpx 24rpx 24rpx;
		overflow: hidden;
	}

	.notice-list-card__head {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 16rpx;
	}

	.notice-list-card__tag {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 6rpx 16rpx;
		background-color: var(--ry-brand);
		border-radius: var(--ry-radius-xs);
	}

	.notice-list-card__tag-text {
		font-size: 22rpx;
		font-weight: 500;
		color: #ffffff;
		line-height: 1.4;
	}

	.notice-list-card__title {
		flex: 1;
		min-width: 0;
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.notice-list-card__summary {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
		line-height: 1.6;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		text-overflow: ellipsis;
		min-height: 83rpx;
	}

	.notice-list-card__foot {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.notice-list-card__meta {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.notice-list-card__category {
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		line-height: 1.4;
	}

	.notice-list-card__new {
		font-size: 22rpx;
		color: var(--ry-brand);
		line-height: 1.4;
	}

	.notice-list-card__time {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}
</style>
