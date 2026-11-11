<template>
	<view class="scenic-list-page">
		<view class="scenic-list-body">
			<view class="scenic-list-header">
				<u-search
					v-model="keyword"
					placeholder="搜索景区名称"
					action-text="搜索"
					bg-color="#FFFFFF"
					color="#1A1A1A"
					placeholder-color="#999999"
					:search-icon-color="accentColor"
					:action-style="{ color: accentColor, fontWeight: 600 }"
					:input-style="{ color: '#1A1A1A' }"
					height="36"
					@search="onSearch"
					@custom="onSearch"
					@clear="onSearch"
				/>
			</view>

			<scroll-view class="scenic-tabs" scroll-x :show-scrollbar="false">
				<view class="scenic-tabs__row">
					<view v-for="(tab, index) in filterTabs" :key="tab.key" class="scenic-tab"
						:class="{ 'scenic-tab--active': currentTab === index }" @click="onTabTap(index)">
						<text class="scenic-tab__text">{{ tab.name }}</text>
					</view>
				</view>
			</scroll-view>

			<view class="scenic-list">
				<view v-if="!loading && scenicList.length === 0" class="scenic-list-empty">
					<u-empty mode="search" :text="emptyText" />
				</view>
				<template v-else>
					<view v-for="item in scenicList" :key="item.id" class="scenic-card" @click="onViewDetail(item)">
						<view class="scenic-card__cover">
							<image class="scenic-card__img ry-placeholder-img" :src="item.cover || PLACEHOLDER_LOGO"
								mode="aspectFill" />
							<view class="scenic-card__cover-mask" />
							<view v-if="item.distance != null" class="scenic-card__distance">
								<u-icon name="map-fill" color="#FFFFFF" :size="11" />
								<text class="scenic-card__distance-text">{{ item.distance }}km</text>
							</view>
							<view v-if="item.levelLabel" class="scenic-card__level">
								<text class="scenic-card__level-text">{{ item.levelLabel }}</text>
							</view>
						</view>

						<view class="scenic-card__body">
							<view class="scenic-card__head">
								<text class="scenic-card__name">{{ item.name }}</text>
							</view>
							<view class="scenic-card__addr-row">
								<u-icon name="map" :color="accentColor" :size="13" />
								<text class="scenic-card__addr">{{ item.address }}</text>
							</view>
							<view class="scenic-card__tags-row">
								<view v-if="item.tags && item.tags.length" class="scenic-card__tags">
									<view v-for="tag in item.tags" :key="tag" class="scenic-card__tag">
										<text class="scenic-card__tag-text">{{ tag }}</text>
									</view>
								</view>
								<view class="scenic-card__btn scenic-card__btn--primary">
									<text class="scenic-card__btn-text">查看详情</text>
									<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
								</view>
							</view>
						</view>
					</view>
				</template>
				<view v-if="loading && scenicList.length" class="scenic-list-loadmore">
					<text class="scenic-list-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && scenicList.length" class="scenic-list-loadmore">
					<text class="scenic-list-loadmore__text">没有更多了</text>
				</view>
			</view>

			<text class="scenic-footer">山水人文 · 诗意之旅</text>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
	import { buildLocationListQuery } from '@/common/location-params.js';
	import { fetchScenicList, fetchScenicCategories } from '@/common/api/scenic.js';

	const accentColor = getModuleColor('scenic');
	const DETAIL_PATH = '/mobile-design/scenic/detail';
	const PAGE_SIZE = 10;

	const DEFAULT_TABS = [{ key: 'all', name: '全部景区' }];

	const filterTabs = ref([...DEFAULT_TABS]);
	const currentTab = ref(0);
	const keyword = ref('');
	const appliedKeyword = ref('');
	const scenicList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);
	let pendingReset = false;
	const emptyCategoryName = computed(() => filterTabs.value[currentTab.value]?.name || '景区');
	const emptyText = computed(() => {
		if (appliedKeyword.value) {
			return `未找到「${appliedKeyword.value}」相关景区`;
		}
		return `暂无${emptyCategoryName.value}相关景区`;
	});

	function mapScenicItem(record) {
		return {
			id: record.id,
			name: record.name || '',
			levelLabel: record.levelLabel || '',
			address: record.address || '',
			category: record.category,
			tags: record.tags || [],
			cover: record.coverImage || record.cover || PLACEHOLDER_LOGO,
			distance: record.distance != null ? Number(record.distance) : null
		};
	}

	async function loadCategories() {
		try {
			const res = await fetchScenicCategories();
			if (res.code === 0 && Array.isArray(res.data)) {
				filterTabs.value = [
					...DEFAULT_TABS,
					...res.data.map((item) => ({
						key: item.code,
						name: item.name || item.code
					}))
				];
				return;
			}
		} catch (e) {
			// 回退默认文案，避免筛选区空白
		}
		filterTabs.value = [
			...DEFAULT_TABS,
			{ key: 'culture', name: '人文古迹' },
			{ key: 'nature', name: '自然风光' },
			{ key: 'red', name: '红色旅游' },
			{ key: 'folk', name: '民俗风情' }
		];
	}

	/** 分页拉取景区列表（服务端 category 筛选） */
	async function loadScenicList(reset = false) {
		if (loading.value) {
			if (reset) pendingReset = true;
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
		const tabKey = filterTabs.value[currentTab.value]?.key || 'all';
		const base = {
			page: page.value,
			limit: PAGE_SIZE
		};
		if (tabKey !== 'all') {
			base.category = tabKey;
		}
		const name = appliedKeyword.value;
		if (name) {
			base.name = name;
		}
		const params = await buildLocationListQuery(base);

		try {
			const res = await fetchScenicList(params);
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapScenicItem);
				scenicList.value = reset ? records : [...scenicList.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && scenicList.value.length < total;
			} else {
				if (reset) {
					scenicList.value = [];
				}
				uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
			}
		} catch (e) {
			if (reset) {
				scenicList.value = [];
			}
			uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
		} finally {
			loading.value = false;
			if (pendingReset) {
				pendingReset = false;
				loadScenicList(true);
			}
		}
	}

	const onTabTap = (index) => {
		if (currentTab.value === index) {
			return;
		}
		currentTab.value = index;
		loadScenicList(true);
	};

	const onViewDetail = (item) => {
		const query = `id=${item.id}&name=${encodeURIComponent(item.name)}`;
		uni.navigateTo({ url: `${DETAIL_PATH}?${query}` });
	};

	function onSearch(value) {
		const text = String(value == null ? keyword.value : value).trim();
		keyword.value = text;
		appliedKeyword.value = text;
		loadScenicList(true);
	}

	onLoad(async () => {
		await loadCategories();
		if (currentTab.value >= filterTabs.value.length) {
			currentTab.value = 0;
		}
		await loadScenicList(true);
	});

	onPullDownRefresh(async () => {
		await loadScenicList(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) {
			return;
		}
		page.value += 1;
		loadScenicList(false);
	});
</script>

<style lang="scss" scoped>
	.scenic-list-page { min-height: 100vh; background-color: #F0FAF4; }
	.scenic-list-body { padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom)); display: flex; flex-direction: column; gap: 24rpx; }
	.scenic-list-header { width: 100%; }
	.scenic-tabs { width: 100%; white-space: nowrap; }
	.scenic-tabs__row { display: inline-flex; gap: 12rpx; padding: 4rpx; }
	.scenic-tab { flex-shrink: 0; display: flex; align-items: center; justify-content: center; height: 64rpx; padding: 0 28rpx; border-radius: var(--ry-radius-pill); background-color: #FFFFFF;
		&__text { font-size: 26rpx; color: #999999; }
		&--active { background: linear-gradient(135deg, #5FD68A, #2EAF6A); box-shadow: 0 6rpx 20rpx rgba(46, 175, 106, 0.3); .scenic-tab__text { color: #FFFFFF; font-weight: 700; } }
	}
	.scenic-list { display: flex; flex-direction: column; gap: 20rpx; }
	.scenic-list-empty { padding: 120rpx 0 80rpx; }
	.scenic-list-loadmore { display: flex; align-items: center; justify-content: center; padding: 24rpx 0 8rpx; }
	.scenic-list-loadmore__text { font-size: 24rpx; color: var(--ry-text-hint); }
	.scenic-card { border-radius: var(--ry-radius-lg); background-color: #FFFFFF; box-shadow: 0 8rpx 28rpx rgba(46, 175, 106, 0.08); overflow: hidden;
		&:active { opacity: 0.96; }
		&__cover { position: relative; width: 100%; height: 320rpx; overflow: hidden; background-color: #E8F8EF; }
		&__img { width: 100%; height: 100%; display: block; transform: scale(2.2); transform-origin: center center; }
		&__cover-mask { position: absolute; left: 0; right: 0; bottom: 0; height: 80rpx; background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.22)); pointer-events: none; }
		&__distance { position: absolute; top: 16rpx; left: 16rpx; display: flex; align-items: center; gap: 4rpx; height: 40rpx; padding: 0 14rpx; border-radius: var(--ry-radius-pill); background-color: rgba(0, 0, 0, 0.35); }
		&__distance-text { font-size: 20rpx; color: #FFFFFF; }
		&__level { position: absolute; top: 16rpx; right: 16rpx; display: flex; align-items: center; height: 44rpx; padding: 0 22rpx; border-radius: var(--ry-radius-pill); background-color: rgba(46, 175, 106, 0.92); }
		&__level-text { font-size: 22rpx; font-weight: 700; color: #FFFFFF; }
		&__body { padding: 24rpx 28rpx 28rpx; }
		&__name { font-size: 32rpx; font-weight: 700; color: #333333; }
		&__addr-row { display: flex; align-items: center; gap: 8rpx; margin-top: 12rpx; }
		&__addr { flex: 1; font-size: 24rpx; color: #999999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
		&__tags-row { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; margin-top: 16rpx; }
		&__tags { flex: 1; display: flex; flex-wrap: wrap; gap: 10rpx; }
		&__tag { height: 40rpx; padding: 0 18rpx; border-radius: var(--ry-radius-pill); background-color: #E8F8EF; display: flex; align-items: center; }
		&__tag-text { font-size: 20rpx; color: #2EAF6A; }
		&__btn { flex-shrink: 0; display: flex; align-items: center; gap: 6rpx; height: 56rpx; padding: 0 24rpx; border-radius: 999rpx; &--primary { background: linear-gradient(135deg, #5FD68A, #2EAF6A); box-shadow: 0 6rpx 20rpx rgba(46, 175, 106, 0.28); } &:active { opacity: 0.88; } }
		&__btn-text { font-size: 26rpx; font-weight: 600; color: #FFFFFF; }
	}
	.scenic-footer { display: block; text-align: center; font-size: 22rpx; color: #CCCCCC; padding-top: 8rpx; }
</style>
