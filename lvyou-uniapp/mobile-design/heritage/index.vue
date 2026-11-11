<template>
	<view class="heritage-index-page">
		<view class="heritage-index-body">
			<view class="heritage-index-head">
				<swiper
					v-if="craftList.length"
					class="heritage-index-head__swiper"
					:indicator-dots="craftList.length > 1"
					indicator-color="rgba(255,255,255,0.4)"
					indicator-active-color="#FFFFFF"
					:circular="craftList.length > 1"
					@change="onHeadBannerChange"
				>
					<swiper-item
						v-for="craft in craftList"
						:key="craft.id"
						class="heritage-index-head__item"
						@click="onCraftClick(craft)"
					>
						<image
							class="heritage-index-head__img ry-placeholder-img"
							:src="craft.cover || PLACEHOLDER_LOGO"
							mode="aspectFill"
						/>
					</swiper-item>
				</swiper>
				<view v-else class="heritage-index-head__fallback">
					<view class="heritage-index-head__seal"><text class="heritage-index-head__seal-text">遗</text></view>
				</view>
				<view class="heritage-index-head__mask" />
				<view class="heritage-index-head__content">
					<text class="heritage-index-head__title">非遗文化</text>
					<text class="heritage-index-head__subtitle">活态传承 · 匠心技艺</text>
					<view v-if="craftList.length > 1" class="heritage-index-head__count">
						<text class="heritage-index-head__count-text">{{ headBannerCurrent + 1 }}/{{ craftList.length }}</text>
					</view>
				</view>
			</view>

			<view class="heritage-block">
				<view class="heritage-block__head">
					<view class="heritage-block__title-wrap"><view class="heritage-block__title-deco" /><text class="heritage-block__title">精选非遗</text></view>
					<view class="heritage-block__more" @click="onCatalogClick"><text class="heritage-block__more-text">查看全部</text><u-icon name="arrow-right" :color="heritageColor" :size="12" /></view>
				</view>
				<view v-if="!loading && craftList.length === 0" class="heritage-craft-empty">
					<view class="heritage-craft-empty__icon"><u-icon name="star" color="#D7CCC8" :size="64" /></view>
					<text class="heritage-craft-empty__text">暂无非遗项目</text>
				</view>
				<scroll-view v-else class="heritage-craft-scroll" scroll-x :show-scrollbar="false">
					<view class="heritage-craft-row">
						<view v-for="craft in craftList" :key="craft.id" class="heritage-craft-card" @click="onCraftClick(craft)">
							<view class="heritage-craft-card__cover">
								<image class="heritage-craft-card__img ry-placeholder-img" :src="craft.cover || PLACEHOLDER_LOGO" mode="aspectFill" />
								<view class="heritage-craft-card__level"><text class="heritage-craft-card__level-text">{{ craft.level }}</text></view>
							</view>
							<view class="heritage-craft-card__body">
								<text class="heritage-craft-card__name">{{ craft.name }}</text>
								<text class="heritage-craft-card__tag">{{ craft.tag }}</text>
								<text class="heritage-craft-card__desc">{{ craft.desc }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>
			<view class="heritage-block">
				<view class="heritage-block__head"><view class="heritage-block__title-wrap"><view class="heritage-block__title-deco" /><text class="heritage-block__title">探索非遗</text></view></view>
				<view class="heritage-entry-grid">
					<view v-for="entry in entryList" :key="entry.key" class="heritage-entry" :class="`heritage-entry--${entry.key}`" @click="onEntryClick(entry.key)">
						<view class="heritage-entry__icon-wrap"><u-icon :name="entry.icon" :color="entry.iconColor" :size="24" /></view>
						<text class="heritage-entry__title">{{ entry.title }}</text>
						<text class="heritage-entry__desc">{{ entry.desc }}</text>
					</view>
				</view>
			</view>
			<text class="heritage-footer">非遗 · 活态传承</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, MODULE_COLORS } from '@/common/theme.js';
import { buildLocationListQuery } from '@/common/location-params.js';
import { fetchCraftList, mapCraftItem } from '@/common/api/heritage.js';

const heritageColor = MODULE_COLORS.heritage;
const CATALOG_PATH = '/mobile-design/heritage/catalog';
const CRAFT_INTRO_PATH = '/mobile-design/heritage/craft-intro';
const SHOP_LIST_PATH = '/mobile-design/heritage/shop-list';
const craftList = ref([]);
const loading = ref(false);
const headBannerCurrent = ref(0);

function onHeadBannerChange(event) {
	headBannerCurrent.value = Number(event?.detail?.current ?? 0);
}
const entryList = [
	{ key: 'catalog', title: '非遗名录', desc: '全部项目', icon: 'list', iconColor: '#5D4037', path: CATALOG_PATH },
	{ key: 'shop', title: '非遗好店', desc: '体验购买', icon: 'bag-fill', iconColor: '#8D6E63', path: SHOP_LIST_PATH },
	{ key: 'goods', title: '非遗好物', desc: '夏布安陶', icon: 'gift-fill', iconColor: '#9C27B0', path: '/mobile-design/heritage/goods-list' }
];
const navigateSafe = (url) => uni.navigateTo({ url });
const onCatalogClick = () => navigateSafe(CATALOG_PATH);
const onCraftClick = (craft) => navigateSafe(`${CRAFT_INTRO_PATH}?id=${craft.id}`);
const onEntryClick = (key) => {
	const entry = entryList.find(item => item.key === key);
	if (entry?.path) navigateSafe(entry.path);
};

/** 首页精选非遗（首屏 6 条） */
async function loadFeaturedCrafts() {
	if (loading.value) {
		return;
	}
	loading.value = true;
	const params = await buildLocationListQuery({ page: 1, limit: 6 });
	try {
		const res = await fetchCraftList(params);
		if (res.code === 0 && res.data) {
			craftList.value = (res.data.records || []).map(mapCraftItem);
			headBannerCurrent.value = 0;
		} else {
			craftList.value = [];
		}
	} catch (e) {
		craftList.value = [];
	} finally {
		loading.value = false;
	}
}

onShow(async () => {
	await loadFeaturedCrafts();
});
</script>

<style lang="scss" scoped>
	.heritage-index-page {
		min-height: 100vh;
		background-color: #FAF6F3;
	}

	.heritage-index-body {
		padding: 20rpx var(--ry-page-padding-x) calc(48rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 36rpx;
	}

	.heritage-index-head {
		position: relative;
		height: 320rpx;
		border-radius: var(--ry-radius-lg);
		overflow: hidden;
		box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.18);

		&__swiper,
		&__item {
			width: 100%;
			height: 100%;
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__fallback {
			width: 100%;
			height: 100%;
			background: linear-gradient(135deg, #4A3728 0%, #5D4037 50%, #8D6E63 100%);
		}

		&__seal {
			position: absolute;
			top: 32rpx;
			right: 32rpx;
			width: 72rpx;
			height: 72rpx;
			border: 3rpx solid rgba(255, 255, 255, 0.4);
			border-radius: 8rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			transform: rotate(8deg);
		}

		&__seal-text {
			font-size: 24rpx;
			font-weight: 800;
			color: rgba(255, 255, 255, 0.85);
			letter-spacing: 4rpx;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 180rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.55));
			pointer-events: none;
			z-index: 1;
		}

		&__content {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			z-index: 2;
			padding: 28rpx 32rpx 32rpx;
		}

		&__title {
			display: block;
			font-size: 44rpx;
			font-weight: 800;
			color: #FFFFFF;
			letter-spacing: 6rpx;
		}

		&__subtitle {
			display: block;
			margin-top: 8rpx;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.78);
			letter-spacing: 2rpx;
		}

		&__count {
			position: absolute;
			right: 32rpx;
			bottom: 32rpx;
			padding: 4rpx 14rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(0, 0, 0, 0.35);
		}

		&__count-text {
			font-size: 22rpx;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	/* �?:W� �?*/
	.heritage-block {
		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 20rpx;
			padding: 0 4rpx;
		}

		&__title-wrap {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__title-deco {
			width: 6rpx;
			height: 28rpx;
			border-radius: 3rpx;
			background: linear-gradient(180deg, #B45D2D, #5D4037);
		}

		&__title {
			font-size: 32rpx;
			font-weight: 700;
			color: #4A3728;
			letter-spacing: 2rpx;
		}

		&__more {
			display: flex;
			align-items: center;
			gap: 4rpx;
		}

		&__more-text {
			font-size: 24rpx;
			color: #8D6E63;
		}
	}

	.heritage-craft-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 64rpx 48rpx 72rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 16rpx rgba(74, 55, 40, 0.06);

		&__icon {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			background-color: #F5F0EB;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 24rpx;
		}

		&__text {
			font-size: 28rpx;
			font-weight: 600;
			color: #8D6E63;
		}
	}

	.heritage-craft-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.heritage-craft-row {
		display: inline-flex;
		gap: 20rpx;
		padding: 4rpx 0 8rpx;
	}

	.heritage-craft-card {
		display: inline-flex;
		flex-direction: column;
		flex-shrink: 0;
		width: 341rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 8rpx 28rpx rgba(74, 55, 40, 0.1);
		overflow: hidden;

		&:active {
			opacity: 0.92;
			transform: scale(0.98);
		}

		&__cover {
			position: relative;
			width: 100%;
			height: 240rpx;
			overflow: hidden;
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__level {
			position: absolute;
			top: 16rpx;
			left: 16rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 36rpx;
			padding: 0 14rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(255, 255, 255, 0.92);
		}

		&__level-text {
			font-size: 20rpx;
			font-weight: 600;
			line-height: 1;
			color: #5D4037;
		}

		&__body {
			padding: 20rpx 20rpx 24rpx;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__name {
			font-size: 30rpx;
			font-weight: 700;
			color: #4A3728;
			letter-spacing: 2rpx;
		}

		&__tag {
			font-size: 20rpx;
			color: #B45D2D;
		}

		&__desc {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}

	/* �?�"e�Q< �?*/
	.heritage-entry-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 16rpx;
	}

	.heritage-entry {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 28rpx 12rpx 24rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		border: 1rpx solid #F0EBE6;
		box-shadow: 0 4rpx 16rpx rgba(74, 55, 40, 0.06);

		&:active {
			opacity: 0.88;
			background-color: #F5F0EB;
		}

		&__icon-wrap {
			width: 72rpx;
			height: 72rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 12rpx;
		}

		&--catalog &__icon-wrap {
			background-color: #EFEBE9;
		}

		&--shop &__icon-wrap {
			background-color: #FBE9E7;
		}

		&--goods &__icon-wrap {
			background-color: #F3E5F5;
		}

		&__title {
			font-size: 26rpx;
			font-weight: 600;
			color: #4A3728;
		}

		&__desc {
			margin-top: 4rpx;
			font-size: 20rpx;
			color: #BBBBBB;
		}
	}

	.heritage-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 8rpx;
	}
</style>
