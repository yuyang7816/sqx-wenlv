<template>
	<view class="route-list-page">
		<view class="route-list-search">
			<u-search
				v-model="keyword"
				placeholder="搜索线路名称"
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

		<view class="route-list-body">
			<view v-if="!loading && routeList.length" class="route-list-cards">
				<view
					v-for="item in routeList"
					:key="item.id"
					class="route-list-card ry-card"
					@click="openDetail(item)"
				>
					<view class="route-list-card__cover">
						<image class="route-list-card__img ry-placeholder-img" :src="item.cover" mode="aspectFill" />
						<view class="route-list-card__duration">
							<text class="route-list-card__duration-text">{{ item.duration }}</text>
						</view>
					</view>

					<view class="route-list-card__content">
						<text class="route-list-card__title">{{ item.name }}</text>

						<view v-if="item.tags.length" class="route-list-card__tags">
							<view v-for="tag in item.tags" :key="tag.text" class="route-list-card__tag">
								<text class="route-list-card__tag-text">{{ tag.text }}</text>
							</view>
						</view>

						<text v-if="item.desc" class="route-list-card__desc">{{ item.desc }}</text>

						<view class="route-list-card__foot">
							<view class="route-list-card__price">
								<text class="route-list-card__symbol">¥</text>
								<text class="route-list-card__amount">{{ item.priceFrom }}</text>
								<text class="route-list-card__suffix">起</text>
							</view>
							<view class="route-list-card__detail">
								<text class="route-list-card__foot-text">查看详情</text>
								<u-icon name="arrow-right" :color="routeColor" :size="14" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else-if="!loading && !routeList.length" class="route-list-empty">
				<u-empty mode="list" :text="ui.emptyText" />
			</view>

			<view v-if="loading && routeList.length" class="route-list-loadmore">
				<text class="route-list-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && routeList.length" class="route-list-loadmore">
				<text class="route-list-loadmore__text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { ROUTE_UI } from '@/common/route-ui.js';
import { buildLocationListQuery } from '@/common/location-params.js';
import { fetchRouteList } from '@/common/api/route.js';

const ui = ROUTE_UI;
const routeColor = getModuleColor('route');
const PAGE_SIZE = 10;

const keyword = ref('');
const appliedKeyword = ref('');
const routeList = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
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
		spots: Array.isArray(record.spots) ? record.spots : [],
		cover: record.cover || PLACEHOLDER_LOGO
	};
}

/** 分页拉取线路列表（公开接口） */
async function loadRouteList(reset = false) {
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
	const base = {
		page: page.value,
		limit: PAGE_SIZE
	};
	if (appliedKeyword.value) {
		base.name = appliedKeyword.value;
	}
	const params = await buildLocationListQuery(base);

	try {
		const res = await fetchRouteList(params);
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapRouteItem);
			routeList.value = reset ? records : [...routeList.value, ...records];
			const total = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && routeList.value.length < total;
		} else {
			if (reset) {
				routeList.value = [];
			}
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) {
			routeList.value = [];
		}
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function openDetail(item) {
	uni.navigateTo({ url: `/mobile-design/route/detail?id=${item.id}` });
}

function onSearch(value) {
	const text = String(value == null ? keyword.value : value).trim();
	keyword.value = text;
	appliedKeyword.value = text;
	loadRouteList(true);
}

onLoad(async () => {
	uni.setNavigationBarTitle({ title: ui.listTitle });
	await loadRouteList(true);
});

onPullDownRefresh(async () => {
	await loadRouteList(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) {
		return;
	}
	page.value += 1;
	loadRouteList(false);
});
</script>

<style lang="scss" scoped>
.route-list-page {
	min-height: 100vh;
	background: linear-gradient(180deg, var(--ry-brand-light, #fff3eb) 0%, #fff9f0 220rpx, #fff9f0 100%);
}

.route-list-search {
	padding: 20rpx var(--ry-page-padding-x) 8rpx;
}

.route-list-body {
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.route-list-cards {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding: 16rpx var(--ry-page-padding-x) 0;
}

.route-list-card {
	overflow: hidden;
}

.route-list-card__cover {
	position: relative;
	height: 280rpx;
	background: #f0f0f0;
}

.route-list-card__img {
	width: 100%;
	height: 100%;
}

.route-list-card__duration {
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

.route-list-card__duration-text {
	font-size: 22rpx;
	font-weight: 400;
	color: #ffffff;
	line-height: 1;
}

.route-list-card__content {
	padding: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.route-list-card__title {
	display: -webkit-box;
	overflow: hidden;
	font-size: 32rpx;
	font-weight: 700;
	color: #1a1a1a;
	line-height: 1.4;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	text-overflow: ellipsis;
}

.route-list-card__price {
	display: flex;
	align-items: baseline;
	flex-shrink: 0;
}

.route-list-card__symbol {
	font-size: 24rpx;
	font-weight: 600;
	color: #ff6600;
}

.route-list-card__amount {
	font-size: 36rpx;
	font-weight: 800;
	color: #ff6600;
	line-height: 1;
}

.route-list-card__suffix {
	margin-left: 4rpx;
	font-size: 20rpx;
	color: #999999;
}

.route-list-card__tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.route-list-card__tag {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 40rpx;
	padding: 8rpx 24rpx;
	border-radius: 999rpx;
	background: var(--ry-brand-light, #fff3eb);
}

.route-list-card__tag-text {
	font-size: 22rpx;
	font-weight: 400;
	color: var(--ry-module-route, #ff6600);
	line-height: 1;
}

.route-list-card__desc {
	overflow: hidden;
	font-size: 24rpx;
	color: #999999;
	line-height: 1.5;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.route-list-card__foot {
	margin-top: 4rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.route-list-card__detail {
	display: flex;
	align-items: center;
	flex-shrink: 0;
	gap: 6rpx;
}

.route-list-card__foot-text {
	font-size: 24rpx;
	font-weight: 600;
	color: var(--ry-module-route, #ff6600);
}

.route-list-empty {
	padding: 120rpx 0;
}

.route-list-loadmore {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx 0 8rpx;
}

.route-list-loadmore__text {
	font-size: 24rpx;
	color: var(--ry-text-hint, #999999);
}
</style>
