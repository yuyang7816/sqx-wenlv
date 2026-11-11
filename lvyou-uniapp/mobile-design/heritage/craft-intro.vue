<template>
	<view class="craft-intro-page">
		<!-- 顶部轮播 Hero -->
		<view class="craft-hero">
			<swiper class="craft-hero__swiper" :indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF" :circular="bannerList.length > 1"
				@change="onBannerChange">
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="craft-hero__item">
					<image class="craft-hero__img ry-placeholder-img" :src="item || PLACEHOLDER_LOGO" mode="aspectFill"
						@click="previewBannerImages(index)" />
				</swiper-item>
			</swiper>
			<view class="craft-hero__mask" />
			<view v-if="craft.level" class="craft-hero__level">
				<text class="craft-hero__level-text">{{ craft.level }}</text>
			</view>
			<!-- <view v-if="craft.tag" class="craft-hero__tag">
				<text class="craft-hero__tag-text">{{ craft.tag }}</text>
			</view> -->
			<view v-if="bannerList.length > 1" class="craft-hero__count">
				<text class="craft-hero__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="craft-intro-body">
			<!-- 标题信息卡 -->
			<view class="craft-info">
				<text class="craft-info__name">{{ craft.name }}</text>
				<view class="craft-info__badge">
					<text class="craft-info__badge-text">{{ craft.levelLabel }}</text>
				</view>
				<text class="craft-info__desc">{{ craft.desc }}</text>
			</view>

			<!-- 内容区块 -->
			<view v-for="section in sections" :key="section.key" class="craft-section">
				<view class="craft-section__head">
					<view class="craft-section__deco" />
					<text class="craft-section__title-text">{{ section.title }}</text>
					<text class="craft-section__quote">"</text>
				</view>
				<view class="craft-section__body">
					<text class="craft-section__content">{{ section.content }}</text>
				</view>
			</view>

		</view>

		<!-- 底部操作栏 -->
		<view class="craft-intro-bar">
			<view class="craft-intro-bar__btn craft-intro-bar__btn--ghost" @click="goGoods">
				<u-icon name="gift-fill" color="#8D6E63" :size="18" />
				<text class="craft-intro-bar__btn-text craft-intro-bar__btn-text--ghost">{{ ui.btnGoods }}</text>
			</view>
			<view class="craft-intro-bar__btn craft-intro-bar__btn--primary" @click="goShop">
				<u-icon name="bag-fill" color="#FFFFFF" :size="18" />
				<text class="craft-intro-bar__btn-text">{{ ui.btnShop }}</text>
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
		onShow
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO
	} from '@/common/theme.js';
	import {
		fetchCraftDetail,
		mapCraftItem
	} from '@/common/api/heritage.js';
	import {
		HERITAGE_UI
	} from '@/common/heritage-ui.js';

	const ui = HERITAGE_UI;
	const craftId = ref('');
	const bannerCurrent = ref(0);
	const craft = ref({
		name: '',
		desc: '',
		level: '',
		levelLabel: '',
		tag: '',
		cover: PLACEHOLDER_LOGO,
		bannerImages: [],
		overview: '',
		feature: '',
		culture: ''
	});
	const loading = ref(false);

	const bannerList = computed(() => {
		const images = (craft.value.bannerImages || []).filter(Boolean);
		if (images.length) {
			return images;
		}
		return craft.value.cover ? [craft.value.cover] : [PLACEHOLDER_LOGO];
	});

	const sections = computed(() => [{
			key: 'overview',
			title: ui.sectionOverview,
			content: craft.value.overview
		},
		{
			key: 'feature',
			title: ui.sectionFeature,
			content: craft.value.feature
		},
		{
			key: 'culture',
			title: ui.sectionCulture,
			content: craft.value.culture
		}
	]);

	function onBannerChange(event) {
		bannerCurrent.value = Number(event?.detail?.current ?? 0);
	}

	function previewBannerImages(index) {
		const urls = bannerList.value.filter(Boolean);
		if (!urls.length) {
			return;
		}
		uni.previewImage({
			urls,
			current: urls[index] || urls[0]
		});
	}

	function goGoods() {
		uni.navigateTo({
			url: '/mobile-design/heritage/goods-list'
		});
	}

	function goShop() {
		uni.navigateTo({
			url: '/mobile-design/heritage/shop-list'
		});
	}

	async function loadCraftDetail() {
		if (!craftId.value || loading.value) {
			return;
		}
		loading.value = true;
		try {
			const res = await fetchCraftDetail(craftId.value);
			if (res.code === 0 && res.data) {
				craft.value = mapCraftItem(res.data);
				bannerCurrent.value = 0;
				uni.setNavigationBarTitle({
					title: craft.value.name || '非遗介绍'
				});
			} else {
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	onLoad((options) => {
		craftId.value = options?.id || '';
	});

	onShow(async () => {
		if (!craftId.value) {
			return;
		}
		await loadCraftDetail();
	});
</script>

<style lang="scss" scoped>
	.craft-intro-page {
		min-height: 100vh;
		background-color: #faf6f3;
	}

	.craft-hero {
		position: relative;
		height: 520rpx;
		overflow: hidden;

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

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 120rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.18));
			pointer-events: none;
			z-index: 1;
		}

		&__level {
			position: absolute;
			top: 32rpx;
			left: 32rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 44rpx;
			padding: 0 18rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(255, 255, 255, 0.92);
			z-index: 2;
		}

		&__level-text {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1;
			color: #5d4037;
		}

		&__tag {
			position: absolute;
			bottom: 72rpx;
			left: 50%;
			transform: translateX(-50%);
			display: flex;
			align-items: center;
			justify-content: center;
			height: 44rpx;
			padding: 0 24rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(255, 255, 255, 0.18);
			border: 1rpx solid rgba(255, 255, 255, 0.35);
			z-index: 2;
		}

		&__tag-text {
			font-size: 22rpx;
			line-height: 1;
			color: rgba(255, 255, 255, 0.9);
			letter-spacing: 2rpx;
		}

		&__count {
			position: absolute;
			right: 32rpx;
			bottom: 32rpx;
			padding: 6rpx 16rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(0, 0, 0, 0.35);
			z-index: 2;
		}

		&__count-text {
			font-size: 22rpx;
			line-height: 1;
			color: #ffffff;
		}
	}

	.craft-intro-body {
		position: relative;
		margin-top: -48rpx;
		padding: 0 var(--ry-page-padding-x) calc(160rpx + env(safe-area-inset-bottom));
		z-index: 3;
	}

	.craft-info {
		padding: 36rpx 32rpx 32rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #ffffff;
		box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.12);
		text-align: center;

		&__name {
			display: block;
			font-size: 44rpx;
			font-weight: 800;
			color: #4a3728;
			letter-spacing: 6rpx;
		}

		&__badge {
			display: flex;
			justify-content: center;
			margin-top: 16rpx;
		}

		&__badge-text {
			padding: 8rpx 24rpx;
			border-radius: var(--ry-radius-pill);
			border: 1rpx solid #e8ddd4;
			background-color: #faf6f3;
			font-size: 22rpx;
			color: #8d6e63;
		}

		&__desc {
			display: block;
			margin-top: 16rpx;
			font-size: 26rpx;
			color: #b45d2d;
			letter-spacing: 4rpx;
		}
	}

	.craft-section {
		margin-top: 24rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #ffffff;
		box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);
		overflow: hidden;

		&__head {
			display: flex;
			align-items: center;
			gap: 12rpx;
			padding: 24rpx 28rpx 0;
		}

		&__deco {
			width: 6rpx;
			height: 28rpx;
			border-radius: 3rpx;
			background: linear-gradient(180deg, #b45d2d, #5d4037);
		}

		&__title-text {
			font-size: 30rpx;
			font-weight: 700;
			color: #4a3728;
			letter-spacing: 2rpx;
		}

		&__quote {
			font-size: 30rpx;
			font-weight: 700;
			color: #b45d2d;
		}

		&__body {
			padding: 20rpx 28rpx 28rpx;
		}

		&__content {
			display: block;
			font-size: 26rpx;
			color: #666666;
			line-height: 1.9;
			text-align: justify;
		}
	}

	.craft-intro-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background-color: #ffffff;
		border-top: 1rpx solid #f0ebe6;
		box-shadow: 0 -4rpx 16rpx rgba(74, 55, 40, 0.06);
		z-index: 99;

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 80rpx;
			border-radius: 999rpx;

			&--ghost {
				border: 1rpx solid #e8ddd4;
				background-color: #faf6f3;
			}

			&--primary {
				background: linear-gradient(135deg, #8d6e63, #5d4037);
				box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.3);
			}

			&:active {
				opacity: 0.9;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 600;
			color: #ffffff;

			&--ghost {
				color: #8d6e63;
			}
		}
	}
</style>
