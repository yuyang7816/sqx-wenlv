const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function writeUtf8(rel, content) {
	fs.writeFileSync(path.join(root, rel), content, { encoding: 'utf8' });
}

writeUtf8('mobile-design/scenic/list.vue', `<template>
	<view class="scenic-list-page">
		<view class="scenic-list-body">
			<view class="scenic-list-header">
				<view class="scenic-list-header__map" @click="onMapClick">
					<u-icon name="map-fill" :color="accentColor" :size="16" />
					<text class="scenic-list-header__map-text">地图</text>
				</view>
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
				<view v-for="item in displayList" :key="item.id" class="scenic-card">
					<view class="scenic-card__cover" @click="onViewDetail(item)">
						<image class="scenic-card__img ry-placeholder-img" :src="item.cover || PLACEHOLDER_LOGO"
							mode="aspectFill" />
						<view class="scenic-card__cover-mask" />
						<view class="scenic-card__distance">
							<u-icon name="map-fill" color="#FFFFFF" :size="11" />
							<text class="scenic-card__distance-text">{{ item.distance }}km</text>
						</view>
						<view class="scenic-card__level">
							<text class="scenic-card__level-text">{{ item.levelLabel }}</text>
						</view>
					</view>

					<view class="scenic-card__body">
						<view class="scenic-card__head" @click="onViewDetail(item)">
							<text class="scenic-card__name">{{ item.name }}</text>
						</view>
						<view class="scenic-card__addr-row" @click="onViewDetail(item)">
							<u-icon name="map" :color="accentColor" :size="13" />
							<text class="scenic-card__addr">{{ item.address }}</text>
						</view>
						<view class="scenic-card__tags-row">
							<view v-if="item.tags && item.tags.length" class="scenic-card__tags">
								<view v-for="tag in item.tags" :key="tag" class="scenic-card__tag">
									<text class="scenic-card__tag-text">{{ tag }}</text>
								</view>
							</view>
							<view class="scenic-card__btn scenic-card__btn--primary" @click.stop="onViewDetail(item)">
								<text class="scenic-card__btn-text">查看详情</text>
								<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<text class="scenic-footer">荣昌山水人文 · 诗意之旅</text>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';

	const accentColor = getModuleColor('scenic');
	const DETAIL_PATH = '/mobile-design/scenic/detail';
	const MAP_PATH = '/mobile-design/map/index';

	const filterTabs = [
		{ key: 'all', name: '全部景区' },
		{ key: 'culture', name: '人文古迹' },
		{ key: 'nature', name: '自然风光' },
		{ key: 'red', name: '红色旅游' },
		{ key: 'folk', name: '民俗风情' }
	];

	const MOCK_SCENIC_LIST = [
		{ id: '1', name: '万灵古镇', levelLabel: '4A级', address: '荣昌区万灵镇', category: 'culture', distance: 3.2, tags: ['千年古邑', '水乡古镇'] },
		{ id: '2', name: '荣昌夏布小镇', levelLabel: '4A级', address: '荣昌区昌州街道', category: 'folk', distance: 1.8, tags: ['非遗体验', '夏布织造'] },
		{ id: '3', name: '安陶博物馆', levelLabel: '3A级', address: '荣昌区安富街道', category: 'culture', distance: 5.6, tags: ['陶艺文化', '亲子研学'] },
		{ id: '4', name: '荣昌博物馆', levelLabel: '3A级', address: '荣昌区昌州大道', category: 'culture', distance: 2.1, tags: ['历史文化', '免费开放'] }
	];

	const currentTab = ref(0);
	const scenicList = ref(MOCK_SCENIC_LIST.map(item => ({ ...item })));

	const displayList = computed(() => {
		const tabKey = filterTabs[currentTab.value].key;
		if (tabKey === 'all') return scenicList.value;
		return scenicList.value.filter(item => item.category === tabKey);
	});

	const onTabTap = (index) => { currentTab.value = index; };
	const onViewDetail = (item) => { uni.navigateTo({ url: \`\${DETAIL_PATH}?id=\${item.id}\` }); };
	const onMapClick = () => { uni.navigateTo({ url: MAP_PATH }); };
</script>

<style lang="scss" scoped>
	.scenic-list-page { min-height: 100vh; background-color: #F0FAF4; }
	.scenic-list-body { padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom)); display: flex; flex-direction: column; gap: 24rpx; }
	.scenic-list-header { display: flex; justify-content: flex-end; }
	.scenic-list-header__map { display: flex; align-items: center; gap: 6rpx; height: 56rpx; padding: 0 20rpx; border-radius: var(--ry-radius-pill); background-color: #FFFFFF; box-shadow: 0 4rpx 16rpx rgba(46, 175, 106, 0.12); &:active { opacity: 0.88; } }
	.scenic-list-header__map-text { font-size: 24rpx; font-weight: 600; color: #2EAF6A; }
	.scenic-tabs { width: 100%; white-space: nowrap; }
	.scenic-tabs__row { display: inline-flex; gap: 12rpx; padding: 4rpx; }
	.scenic-tab { flex-shrink: 0; display: flex; align-items: center; justify-content: center; height: 64rpx; padding: 0 28rpx; border-radius: var(--ry-radius-pill); background-color: #FFFFFF;
		&__text { font-size: 26rpx; color: #999999; }
		&--active { background: linear-gradient(135deg, #5FD68A, #2EAF6A); box-shadow: 0 6rpx 20rpx rgba(46, 175, 106, 0.3); .scenic-tab__text { color: #FFFFFF; font-weight: 700; } }
	}
	.scenic-list { display: flex; flex-direction: column; gap: 20rpx; }
	.scenic-card { border-radius: var(--ry-radius-lg); background-color: #FFFFFF; box-shadow: 0 8rpx 28rpx rgba(46, 175, 106, 0.08); overflow: hidden;
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
`);

console.log('utf8 fixes done');
