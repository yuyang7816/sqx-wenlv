<template>
	<view class="search-page">
		<!-- 搜索栏 -->
		<view class="search-bar-row">
			<view class="search-input-wrap">
				<u-icon name="search" color="#B7B7C4" :size="18" />
				<input v-model="keyword" class="search-input" type="text" confirm-type="search" placeholder="搜索景点、酒店、美食..."
					placeholder-class="search-input__placeholder" :focus="autoFocus" @confirm="onSearch" />
				<view v-if="keyword" class="search-input__clear" @click="clearKeyword">
					<u-icon name="close-circle-fill" color="#C8C8D0" :size="16" />
				</view>
			</view>
			<text class="search-bar-row__action" @click="onSearch">搜索</text>
		</view>

		<!-- 分类筛选 -->
		<scroll-view class="search-cats" scroll-x :show-scrollbar="false">
			<view class="search-cats__track">
				<view v-for="cat in categories" :key="cat.key" class="search-cat"
					:class="{ 'search-cat--active': activeCategory === cat.key }" @click="onCategoryTap(cat.key)">
					<u-icon :name="cat.icon" :color="activeCategory === cat.key ? '#FFFFFF' : cat.color" :size="14" />
					<text class="search-cat__text">{{ cat.name }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 列表 -->
		<view class="search-results-area">
			<view class="search-results__head">
				<text class="search-results__title">{{ resultsTitle }}</text>
				<text class="search-results__count">共 {{ listItems.length }} 条</text>
			</view>

			<view v-if="loading && !listItems.length" class="search-loading">
				<text class="search-loading__text">加载中...</text>
			</view>

			<template v-else-if="listItems.length">
				<view class="search-results">
					<view v-for="item in listItems" :key="`${item.type}-${item.id}`" class="search-result-card ry-card"
						@click="openResult(item)">
						<image class="search-result-card__thumb ry-placeholder-img" :src="item.cover || PLACEHOLDER_LOGO"
							mode="aspectFill" />
						<view class="search-result-card__info">
							<view class="search-result-card__head">
								<text class="search-result-card__name">{{ item.name }}</text>
								<view class="search-result-card__type-badge" :style="typeBadgeStyle(item.type)">
									<text class="search-result-card__type-text">{{ item.typeLabel }}</text>
								</view>
							</view>
							<view v-if="item.address" class="search-result-card__meta">
								<u-icon name="map" color="#B7B7C4" :size="12" />
								<text class="search-result-card__addr">{{ item.address }}</text>
							</view>
							<view v-if="item.tag || item.price != null || item.score" class="search-result-card__footer">
								<view v-if="item.tag" class="search-result-card__tag">
									<text class="search-result-card__tag-text">{{ item.tag }}</text>
								</view>
								<text v-if="item.price != null" class="search-result-card__price">¥{{ item.price }}起</text>
								<text v-if="item.score" class="search-result-card__score">{{ item.score }}分</text>
							</view>
						</view>
						<view class="search-result-card__arrow">
							<u-icon name="arrow-right" color="#D0D0D8" :size="14" />
						</view>
					</view>
				</view>
				<view v-if="loading" class="search-loadmore">
					<text class="search-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore" class="search-loadmore">
					<text class="search-loadmore__text">没有更多了</text>
				</view>
			</template>

			<view v-else class="search-empty">
				<u-empty mode="search" :text="emptyText">
					<text v-if="appliedKeyword" class="search-empty__hint">换个关键词或切换分类试试</text>
				</u-empty>
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
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		buildLocationListQuery
	} from '@/common/location-params.js';
	import {
		fetchScenicList
	} from '@/common/api/scenic.js';
	import {
		fetchHotelList
	} from '@/common/api/hotel.js';
	import {
		fetchRouteList
	} from '@/common/api/route.js';
	import {
		fetchActivityList
	} from '@/common/api/activity.js';
	import {
		fetchShopList,
		mapShopItem
	} from '@/common/api/heritage.js';

	const PAGE_SIZE = 10;

	const keyword = ref('');
	const appliedKeyword = ref('');
	const autoFocus = ref(false);
	const activeCategory = ref('scenic');
	const listItems = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	const categories = [{
			key: 'scenic',
			name: '景区',
			icon: 'photo-fill',
			color: '#2EAF6A'
		},
		{
			key: 'hotel',
			name: '酒店',
			icon: 'home-fill',
			color: '#2979FF'
		},
		{
			key: 'route',
			name: '线路',
			icon: 'map-fill',
			color: '#FF6600'
		},
		{
			key: 'activity',
			name: '活动',
			icon: 'star-fill',
			color: '#9C27B0'
		},
		{
			key: 'heritage',
			name: '非遗',
			icon: 'gift-fill',
			color: '#5D4037'
		}
	];

	/** 详情页路径（按服务端 SearchContentType） */
	const DETAIL_PATH_MAP = {
		hotel: (id) => `/mobile-design/hotel/detail?id=${id}`,
		route: (id) => `/mobile-design/route/detail?id=${id}`,
		scenic: (id) => `/mobile-design/scenic/detail?id=${id}`,
		activity: (id) => `/mobile-design/activity/detail?id=${id}`,
		shop: (id) => `/mobile-design/heritage/shop-detail?id=${id}`
	};

	/** 模块色键 */
	const TYPE_MODULE_MAP = {
		hotel: 'hotel',
		route: 'route',
		scenic: 'scenic',
		activity: 'activity',
		shop: 'heritageShop'
	};

	const CATEGORY_LOADERS = {
		scenic: {
			type: 'scenic',
			label: '景区',
			supportsName: true,
			fetch: fetchScenicList,
			mapItem(record) {
				return {
					id: record.id,
					type: 'scenic',
					typeLabel: '景区',
					name: record.name || '',
					cover: record.coverImage || record.cover || PLACEHOLDER_LOGO,
					address: record.address || '',
					tag: (record.tags && record.tags[0]) || record.levelLabel || '',
					price: null,
					score: null
				};
			}
		},
		hotel: {
			type: 'hotel',
			label: '酒店',
			clientFilter: true,
			fetch: fetchHotelList,
			mapItem(record) {
				return {
					id: record.id,
					type: 'hotel',
					typeLabel: '酒店',
					name: record.name || '',
					cover: record.coverImage || PLACEHOLDER_LOGO,
					address: record.address || '',
					tag: record.tag || record.categoryLabel || '',
					price: record.priceFrom != null ? Number(record.priceFrom) : null,
					score: record.score != null ? Number(record.score).toFixed(1) : ''
				};
			}
		},
		route: {
			type: 'route',
			label: '线路',
			supportsName: true,
			fetch: fetchRouteList,
			mapItem(record) {
				return {
					id: record.id,
					type: 'route',
					typeLabel: '线路',
					name: record.name || '',
					cover: record.cover || PLACEHOLDER_LOGO,
					address: '',
					tag: record.duration || record.days || '',
					price: record.priceFrom != null ? Number(record.priceFrom) : null,
					score: null
				};
			}
		},
		activity: {
			type: 'activity',
			label: '活动',
			supportsName: true,
			fetch: fetchActivityList,
			mapItem(record) {
				return {
					id: record.id,
					type: 'activity',
					typeLabel: '活动',
					name: record.name || '',
					cover: record.cover || PLACEHOLDER_LOGO,
					address: record.address || '',
					tag: record.statusLabel || '',
					price: null,
					score: null
				};
			}
		},
		heritage: {
			type: 'shop',
			label: '非遗',
			supportsName: true,
			fetch: fetchShopList,
			mapItem(record) {
				const shop = mapShopItem(record);
				return {
					id: shop.id,
					type: 'shop',
					typeLabel: '非遗',
					name: shop.name || '',
					cover: shop.cover || PLACEHOLDER_LOGO,
					address: shop.address || '',
					tag: shop.tag || (shop.tags && shop.tags[0]) || '',
					price: null,
					score: shop.rating || ''
				};
			}
		}
	};

	const currentCategory = computed(() =>
		categories.find(cat => cat.key === activeCategory.value) || categories[0]
	);

	const resultsTitle = computed(() => {
		if (appliedKeyword.value) {
			return `「${appliedKeyword.value}」搜索结果`;
		}
		return `全部${currentCategory.value.name}`;
	});

	const emptyText = computed(() => {
		if (appliedKeyword.value) {
			return `未找到「${appliedKeyword.value}」相关${currentCategory.value.name}`;
		}
		return `暂无${currentCategory.value.name}内容`;
	});

	function filterByKeyword(items, text) {
		if (!text) {
			return items;
		}
		const lower = text.toLowerCase();
		return items.filter(item =>
			(item.name || '').toLowerCase().includes(lower) ||
			(item.address || '').toLowerCase().includes(lower)
		);
	}

	function typeBadgeStyle(type) {
		const moduleKey = TYPE_MODULE_MAP[type] || type;
		const color = getModuleColor(moduleKey);
		return {
			backgroundColor: `${color}18`,
			color
		};
	}

	/** 按当前分类分页拉取列表 */
	async function loadList(reset = false) {
		if (loading.value) {
			return;
		}
		if (!reset && !hasMore.value) {
			return;
		}

		const loader = CATEGORY_LOADERS[activeCategory.value];
		if (!loader) {
			return;
		}

		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}

		loading.value = true;
		const base = {
			page: page.value,
			limit: PAGE_SIZE
		};
		const name = appliedKeyword.value;
		if (name && loader.supportsName) {
			base.name = name;
		}
		const params = await buildLocationListQuery(base);

		try {
			const res = await loader.fetch(params);
			if (res.code === 0 && res.data) {
				let records = (res.data.records || []).map(loader.mapItem);
				if (name && loader.clientFilter) {
					records = filterByKeyword(records, name);
				}
				listItems.value = reset ? records : [...listItems.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = (res.data.records || []).length >= PAGE_SIZE && listItems.value.length < total;
			} else {
				if (reset) {
					listItems.value = [];
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				listItems.value = [];
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function onSearch() {
		appliedKeyword.value = keyword.value.trim();
		loadList(true);
	}

	function onCategoryTap(key) {
		if (activeCategory.value === key) {
			return;
		}
		activeCategory.value = key;
		loadList(true);
	}

	function clearKeyword() {
		keyword.value = '';
		appliedKeyword.value = '';
		loadList(true);
	}

	function openResult(item) {
		const buildPath = DETAIL_PATH_MAP[item.type];
		if (buildPath && item?.id) {
			uni.navigateTo({
				url: buildPath(item.id)
			});
		}
	}

	onLoad(async (options) => {
		if (options?.keyword) {
			keyword.value = decodeURIComponent(options.keyword);
			appliedKeyword.value = keyword.value.trim();
		}
		await loadList(true);
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) {
			return;
		}
		page.value += 1;
		loadList(false);
	});
</script>

<style lang="scss" scoped>
	.search-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg, #F5F7FF);
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	}

	.search-bar-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 16rpx var(--ry-page-padding-x) 20rpx;
		background-color: #FFFFFF;
		border-bottom: 1rpx solid #F0F0F0;
	}

	.search-input-wrap {
		flex: 1;
		display: flex;
		align-items: center;
		gap: 12rpx;
		min-width: 0;
		height: 72rpx;
		padding: 0 24rpx;
		border-radius: var(--ry-radius-pill);
		background: #F5F6FA;
		border: 2rpx solid transparent;
		transition: border-color 0.2s;

		&:focus-within {
			background: #FFFFFF;
			border-color: rgba(255, 102, 0, 0.35);
			box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.08);
		}
	}

	.search-input {
		flex: 1;
		min-width: 0;
		height: 72rpx;
		font-size: 28rpx;
		color: var(--ry-text-primary);
		line-height: 72rpx;
	}

	.search-input__placeholder {
		color: #B7B7C4;
		font-size: 28rpx;
	}

	.search-input__clear {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 8rpx;
		flex-shrink: 0;
	}

	.search-bar-row__action {
		flex-shrink: 0;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-brand);
		line-height: 1;

		&:active {
			opacity: 0.7;
		}
	}

	.search-cats {
		background: #FFFFFF;
		white-space: nowrap;
	}

	.search-cats__track {
		display: inline-flex;
		gap: 12rpx;
		padding: 16rpx var(--ry-page-padding-x) 20rpx;
	}

	.search-cat {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 12rpx 24rpx;
		border-radius: var(--ry-radius-pill);
		background: #F5F6FA;
		transition: all 0.2s;

		&--active {
			background: linear-gradient(135deg, #FF6600 0%, #FF8533 100%);
			box-shadow: 0 4rpx 12rpx rgba(255, 102, 0, 0.25);
		}

		&__text {
			font-size: 24rpx;
			color: var(--ry-text-secondary);
			line-height: 1;
			white-space: nowrap;
		}

		&--active &__text {
			color: #FFFFFF;
			font-weight: 500;
		}
	}

	.search-results-area {
		padding: 20rpx var(--ry-page-padding-x) 0;
	}

	.search-loading {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 80rpx 0;
	}

	.search-loading__text {
		font-size: 28rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	.search-loadmore {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24rpx 0 8rpx;
	}

	.search-loadmore__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.search-results__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 8rpx 4rpx 20rpx;
	}

	.search-results__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
	}

	.search-results__count {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.search-results {
		display: flex;
		flex-direction: column;
		gap: var(--ry-card-gap, 16rpx);
	}

	.search-result-card {
		display: flex;
		align-items: center;
		padding: 20rpx;
		gap: 20rpx;

		&:active {
			opacity: 0.92;
		}

		&__thumb {
			flex-shrink: 0;
			width: 128rpx;
			height: 128rpx;
			border-radius: var(--ry-radius-md);
			background-color: #F0F0F0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: center;
			gap: 10rpx;
			min-height: 128rpx;
		}

		&__head {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 30rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__type-badge {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			min-height: 36rpx;
			padding: 0 12rpx;
			border-radius: var(--ry-radius-xs);
		}

		&__type-text {
			font-size: 22rpx;
			font-weight: 500;
			line-height: 1.2;
			white-space: nowrap;
		}

		&__meta {
			display: flex;
			align-items: center;
			gap: 6rpx;
			min-width: 0;
		}

		&__addr {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: var(--ry-text-hint);
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__footer {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 12rpx;
			min-width: 0;
		}

		&__tag {
			flex-shrink: 1;
			max-width: 100%;
			display: inline-flex;
			align-items: center;
			height: 36rpx;
			padding: 0 14rpx;
			border-radius: var(--ry-radius-pill);
			background: #F5F6FA;
			min-width: 0;
		}

		&__tag-text {
			font-size: 20rpx;
			color: var(--ry-text-hint);
			line-height: 1.2;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__price {
			flex-shrink: 0;
			margin-left: auto;
			font-size: 24rpx;
			font-weight: 600;
			color: var(--ry-brand);
			line-height: 1;
		}

		&__score {
			flex-shrink: 0;
			margin-left: auto;
			font-size: 24rpx;
			font-weight: 600;
			color: #FFB800;
			line-height: 1;
		}

		&__arrow {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 32rpx;
			align-self: center;
		}
	}

	.search-empty {
		padding: 80rpx 0 60rpx;
	}

	.search-empty__hint {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
		text-align: center;
	}
</style>
