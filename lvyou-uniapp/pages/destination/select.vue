<template>
	<view class="city-page">
		<view class="city-search">
			<u-icon name="search" color="#B7B7C4" :size="18" />
			<input
				v-model="keyword"
				class="city-search__input"
				type="text"
				confirm-type="search"
				placeholder="搜索城市"
				placeholder-style="color: #b7b7c4;"
			/>
		</view>
		<view class="city-current">
			<text class="city-current__label">当前地址</text>
			<text class="city-current__name">{{ currentAddress }}</text>
			<text class="city-current__action" @click="useCurrentLocation">使用当前位置</text>
		</view>
		<view class="city-list">
			<uni-indexed-list
				v-if="options.length"
				:options="options"
				:show-select="false"
				@click="onCityClick"
			/>
			<view v-else class="city-empty">没有找到相关城市</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { CITY_INDEX_GROUPS } from '@/common/city-index.js';
import {
	clearBrowseDestination,
	readBrowseDestination,
	saveBrowseDestination
} from '@/common/browse-destination.js';

const keyword = ref('');
const currentAddress = ref('当前位置');
const cities = CITY_INDEX_GROUPS.flatMap((group) => group.cities);
const filteredGroups = computed(() => {
	const query = keyword.value.trim();
	if (!query) return CITY_INDEX_GROUPS;
	return CITY_INDEX_GROUPS.map((group) => ({
		letter: group.letter,
		cities: group.cities.filter((city) =>
			city.shortName.includes(query) || city.name.includes(query) || city.province.includes(query)
		)
	})).filter((group) => group.cities.length);
});
const options = computed(() => filteredGroups.value.map((group) => ({
	letter: group.letter,
	data: group.cities.map((city) => city.shortName)
})));

function refreshCurrentAddress() {
	const saved = readBrowseDestination();
	currentAddress.value = saved?.name || saved?.shortName || '当前位置';
}

onShow(() => {
	refreshCurrentAddress();
});

function onCityClick(event) {
	const name = event?.item?.name;
	const city = cities.find((item) => item.shortName === name);
	if (!city) return;
	saveBrowseDestination({
		id: city.shortName,
		name: `${city.province} ${city.name}`,
		shortName: city.shortName
	});
	uni.navigateBack();
}

function useCurrentLocation() {
	clearBrowseDestination();
	uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.city-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f7f7f7;
}

.city-search {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin: 20rpx 24rpx 0;
	padding: 0 24rpx;
	height: 72rpx;
	border-radius: 999rpx;
	background: #ffffff;
}

.city-search__input {
	flex: 1;
	min-width: 0;
	height: 72rpx;
	font-size: 28rpx;
	color: #191919;
}

.city-current {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin: 16rpx 24rpx 0;
	padding: 24rpx 28rpx;
	border-radius: 20rpx;
	background: #ffffff;
}

.city-current__label {
	flex-shrink: 0;
	font-size: 24rpx;
	color: #ff6600;
}

.city-current__name {
	flex: 1;
	min-width: 0;
	font-size: 30rpx;
	font-weight: 600;
	color: #191919;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.city-current__action {
	flex-shrink: 0;
	font-size: 24rpx;
	color: #999999;
}

.city-empty {
	padding: 80rpx 0;
	text-align: center;
	font-size: 28rpx;
	color: #999999;
}

.city-list {
	position: relative;
	flex: 1;
	min-height: 0;
}
</style>
