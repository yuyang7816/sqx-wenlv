<template>
	<view class="store-routes-page">
		<view class="store-routes-fixed">
			<view class="store-routes-search">
				<u-search
					v-model="keyword"
					:placeholder="ui.storeRoutesSearchPlaceholder"
					action-text="搜索"
					bg-color="#FFFFFF"
					color="#1A1A1A"
					placeholder-color="#999999"
					:search-icon-color="routeColor"
					:action-style="{ color: routeColor, fontWeight: 600 }"
					:input-style="{ color: '#1A1A1A' }"
					height="36"
					@search="onSearch"
					@custom="onSearch"
					@clear="onSearch"
				/>
			</view>
		</view>
		<!-- 与固定顶栏等高，避免内容被遮挡 -->
		<view class="store-routes-fixed-spacer" />

		<view class="store-routes-body">
			<view v-if="!loading && routeList.length" class="store-routes-cards">
				<view
					v-for="item in routeList"
					:key="item.id"
					class="store-routes-card ry-card"
					@click="openDetail(item)"
				>
					<view class="store-routes-card__cover">
						<image class="store-routes-card__img" :src="item.cover" mode="aspectFill" />
						<view v-if="item.duration" class="store-routes-card__duration">
							<text class="store-routes-card__duration-text">{{ item.duration }}</text>
						</view>
					</view>
					<view class="store-routes-card__content">
						<text class="store-routes-card__title">{{ item.name }}</text>
						<view v-if="item.tags.length" class="store-routes-card__tags">
							<view v-for="tag in item.tags" :key="tag.text" class="store-routes-card__tag">
								<text class="store-routes-card__tag-text">{{ tag.text }}</text>
							</view>
						</view>
						<text v-if="item.desc" class="store-routes-card__desc">{{ item.desc }}</text>
						<view class="store-routes-card__foot">
							<view class="store-routes-card__price">
								<text class="store-routes-card__symbol">¥</text>
								<text class="store-routes-card__amount">{{ item.priceFrom }}</text>
								<text class="store-routes-card__suffix">起</text>
							</view>
							<view class="store-routes-card__detail">
								<text class="store-routes-card__detail-text">查看详情</text>
								<u-icon name="arrow-right" :color="routeColor" :size="14" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else-if="!loading && !routeList.length" class="store-routes-empty">
				<u-empty mode="list" :text="emptyText" />
			</view>

			<view v-if="loading && routeList.length" class="store-routes-loadmore">
				<text class="store-routes-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && routeList.length" class="store-routes-loadmore">
				<text class="store-routes-loadmore__text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { normalizeMediaUrl } from '@/common/media-url.js';
import { ROUTE_UI } from '@/common/route-ui.js';
import { fetchRouteList } from '@/common/api/route.js';

const ui = ROUTE_UI;
const routeColor = getModuleColor('route');
const PAGE_SIZE = 10;

const merchantId = ref('');
const storeName = ref('');
const keyword = ref('');
const appliedKeyword = ref('');
const routeList = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

const emptyText = computed(() => {
	if (appliedKeyword.value) {
		return `未找到「${appliedKeyword.value}」相关线路`;
	}
	return ui.emptyStoreRoutes;
});

function mapRouteItem(record) {
	const tags = Array.isArray(record.tags)
		? record.tags.map((tag) => ({
			text: tag.text || tag,
			type: tag.type || ''
		}))
		: [];
	return {
		id: record.id,
		name: record.name || '',
		desc: record.desc || '',
		priceFrom: record.priceFrom != null ? Number(record.priceFrom) : 0,
		duration: record.duration || record.days || '',
		tags,
		cover: normalizeMediaUrl(record.cover) || PLACEHOLDER_LOGO
	};
}

function buildFilterParams() {
	const params = {
		merchantId: merchantId.value,
		page: page.value,
		limit: PAGE_SIZE
	};
	if (appliedKeyword.value) {
		params.name = appliedKeyword.value;
	}
	return params;
}

async function loadRouteList(reset = false) {
	if (!merchantId.value || loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchRouteList(buildFilterParams());
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapRouteItem);
			routeList.value = reset ? records : [...routeList.value, ...records];
			const total = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && routeList.value.length < total;
		} else {
			if (reset) routeList.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) routeList.value = [];
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function onSearch(value) {
	const text = String(value == null ? keyword.value : value).trim();
	keyword.value = text;
	appliedKeyword.value = text;
	loadRouteList(true);
}

function openDetail(item) {
	if (!item?.id) return;
	uni.navigateTo({ url: `/mobile-design/route/detail?id=${item.id}` });
}

onLoad((options) => {
	merchantId.value = options?.merchantId ? String(options.merchantId) : '';
	storeName.value = options?.storeName ? decodeURIComponent(String(options.storeName)) : '';
	uni.setNavigationBarTitle({
		title: storeName.value ? `${storeName.value}线路` : ui.storeRoutesTitle
	});
	loadRouteList(true);
});

onPullDownRefresh(async () => {
	await loadRouteList(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) return;
	page.value += 1;
	loadRouteList(false);
});
</script>

<style lang="scss" scoped>
.store-routes-page {
	min-height: 100vh;
	background: linear-gradient(180deg, var(--ry-brand-light, #fff3eb) 0%, #fff9f0 220rpx, #fff9f0 100%);
}

.store-routes-fixed {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 20;
	padding: 16rpx var(--ry-page-padding-x) 12rpx;
	background: linear-gradient(180deg, #fff3eb 0%, #fff9f0 100%);
	box-shadow: 0 6rpx 16rpx rgba(255, 102, 0, 0.05);
	box-sizing: border-box;
}

.store-routes-fixed-spacer {
	height: 108rpx;
}

.store-routes-search {
	width: 100%;
}

.store-routes-body {
	padding: 8rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
}

.store-routes-cards {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.store-routes-card {
	overflow: hidden;

	&__cover {
		position: relative;
		height: 280rpx;
		background: #f0f0f0;
	}

	&__img {
		width: 100%;
		height: 100%;
	}

	&__duration {
		position: absolute;
		top: 16rpx;
		left: 16rpx;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		min-height: 32rpx;
		padding: 4rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(255, 102, 0, 0.92);
	}

	&__duration-text {
		font-size: 22rpx;
		color: #ffffff;
		line-height: 1;
	}

	&__content {
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	&__title {
		display: -webkit-box;
		overflow: hidden;
		font-size: 32rpx;
		font-weight: 700;
		color: #1a1a1a;
		line-height: 1.4;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		text-overflow: ellipsis;
	}

	&__tags {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	&__tag {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 40rpx;
		padding: 0 20rpx;
		border-radius: 999rpx;
		background: var(--ry-brand-light, #fff3eb);
		box-sizing: border-box;
	}

	&__tag-text {
		font-size: 22rpx;
		color: var(--ry-module-route, #ff6600);
		line-height: 40rpx;
		height: 40rpx;
	}

	&__desc {
		overflow: hidden;
		font-size: 24rpx;
		color: #999999;
		line-height: 1.5;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	&__price {
		display: flex;
		align-items: baseline;
	}

	&__symbol {
		font-size: 24rpx;
		font-weight: 600;
		color: #ff6600;
	}

	&__amount {
		font-size: 36rpx;
		font-weight: 800;
		color: #ff6600;
		line-height: 1;
	}

	&__suffix {
		margin-left: 4rpx;
		font-size: 20rpx;
		color: #999999;
	}

	&__detail {
		display: flex;
		align-items: center;
		gap: 6rpx;
		flex-shrink: 0;
	}

	&__detail-text {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--ry-module-route, #ff6600);
	}
}

.store-routes-empty {
	padding: 120rpx 0;
}

.store-routes-loadmore {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx 0 8rpx;

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint, #999999);
	}
}
</style>
