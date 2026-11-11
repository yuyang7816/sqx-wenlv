<template>
	<view class="goose-menu-page">
		<view class="goose-menu-body">
			<text class="goose-menu__title">{{ shopName }}</text>
			<text class="goose-menu__subtitle">全部菜品</text>
			<view v-if="loading" class="goose-menu-status">
				<text class="goose-menu-status__text">加载中...</text>
			</view>
			<view v-else-if="!menuList.length" class="goose-menu-status">
				<text class="goose-menu-status__text">暂无在售菜品</text>
			</view>
			<view v-else class="goose-menu-grid">
				<view
					v-for="item in menuList"
					:key="item.id"
					class="goose-menu-card"
					@click="onDishClick(item)"
				>
					<view class="goose-menu-card__img-wrap">
						<image
							class="goose-menu-card__img ry-placeholder-img"
							:src="item.cover || PLACEHOLDER_LOGO"
							mode="aspectFill"
						/>
					</view>
					<text class="goose-menu-card__name">{{ item.name }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { fetchGooseMenu } from '@/common/api/goose.js';

const shopId = ref('');
const shopName = ref('卤鹅');
const menuList = ref([]);
const loading = ref(false);
const GOOSE_DISH_DETAIL_PATH = '/mobile-design/goose/dish-detail';

function mapMenuItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		cover: record.cover || ''
	};
}

/** onLoad 拉取全部在售菜单（公开接口） */
async function loadMenuList() {
	if (!shopId.value || loading.value) {
		return;
	}
	loading.value = true;
	try {
		const res = await fetchGooseMenu(shopId.value);
		if (res.code === 0 && Array.isArray(res.data)) {
			menuList.value = res.data.map(mapMenuItem);
		} else {
			menuList.value = [];
			uni.showToast({ title: res.msg || '菜单加载失败', icon: 'none' });
		}
	} catch (e) {
		menuList.value = [];
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad((options) => {
	shopId.value = options?.id ? String(options.id) : '';
	if (options?.shopName) {
		shopName.value = decodeURIComponent(options.shopName);
	}
	uni.setNavigationBarTitle({ title: `${shopName.value}·菜单` });
	if (shopId.value) {
		loadMenuList();
	}
});

function onDishClick(item) {
	if (!item?.id) return;
	uni.navigateTo({
		url: `${GOOSE_DISH_DETAIL_PATH}?id=${item.id}&name=${encodeURIComponent(item.name || '')}`
	});
}
</script>

<style lang="scss" scoped>
	.goose-menu-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.goose-menu-body {
		padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.goose-menu__title {
		font-size: 32rpx;
		font-weight: 800;
		color: #333333;
		line-height: 1.4;
	}

	.goose-menu__subtitle {
		font-size: 24rpx;
		color: #999999;
	}

	.goose-menu-status {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 120rpx 0 80rpx;
	}

	.goose-menu-status__text {
		font-size: 28rpx;
		color: var(--ry-text-hint);
	}

	.goose-menu-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx 16rpx;
	}

	.goose-menu-card {
		width: calc((100% - 16rpx) / 2);
		display: flex;
		flex-direction: column;

		&:active {
			opacity: 0.88;
		}

		&__img-wrap {
			width: 100%;
			aspect-ratio: 1;
			border-radius: 16rpx;
			overflow: hidden;
			background-color: #FFF3EB;
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__name {
			margin-top: 12rpx;
			font-size: 26rpx;
			font-weight: 500;
			color: #333333;
			line-height: 1.4;
			text-align: center;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}
	}
</style>
