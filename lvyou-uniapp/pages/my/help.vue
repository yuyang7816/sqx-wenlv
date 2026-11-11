<template>
	<view class="help-page ry-page">
		<view class="help-body">
			<view class="help-hero ry-card">
				<view class="help-hero__icon">
					<u-icon name="question-circle-fill" color="#FF6600" :size="44" />
				</view>
				<text class="help-hero__title">帮助中心</text>
				<text class="help-hero__desc">常见问题解答，快速了解{{ productName }}使用方法</text>
			</view>

			<view v-if="pageLoading" class="help-loading ry-card">
				<u-loading-icon color="#FF6600" />
				<text class="help-loading__text">加载中...</text>
			</view>

			<view v-else-if="faqList.length" class="help-list ry-card">
				<view
					v-for="(item, index) in faqList"
					:key="item.id"
					class="help-item"
					:class="{ 'help-item--border': index < faqList.length - 1, 'help-item--open': openId === item.id }"
				>
					<view class="help-item__head" @click="toggleItem(item.id)">
						<text class="help-item__question">{{ item.question }}</text>
						<u-icon :name="openId === item.id ? 'arrow-up' : 'arrow-down'" color="#CCCCCC" :size="14" />
					</view>
					<view v-if="openId === item.id" class="help-item__answer">
						<text>{{ item.answer }}</text>
					</view>
				</view>
			</view>

			<view v-else class="help-empty ry-card">
				<text class="help-empty__text">暂无常见问题，请稍后再试</text>
			</view>

			<view class="help-contact ry-card" @click="goService">
				<view class="help-contact__icon">
					<u-icon name="chat-fill" color="#2EAF6A" :size="22" />
				</view>
				<view class="help-contact__info">
					<text class="help-contact__title">没找到答案？</text>
					<text class="help-contact__desc">联系客服获取帮助</text>
				</view>
				<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { fetchHelpFaq } from '@/common/api/help.js';
import { getProductName, loadProductName } from '@/common/brand.js';
import { openKefuCenter } from '@/common/kefu-action.js';

const productName = ref(getProductName());
const openId = ref('');
const pageLoading = ref(false);
const faqList = ref([]);

/** 将分类问答扁平化为手风琴列表 */
function flattenFaqCategories(categories) {
	const list = [];
	(categories || []).forEach((category) => {
		(category.items || []).forEach((item) => {
			list.push({
				id: String(item.id),
				question: item.question || '',
				answer: item.answer || ''
			});
		});
	});
	return list;
}

/** onShow 拉取 FAQ（公开接口，按需加载） */
async function loadHelpFaq() {
	if (pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchHelpFaq();
		if (res.code === 0 && res.data?.categories) {
			faqList.value = flattenFaqCategories(res.data.categories);
		} else {
			faqList.value = [];
			uni.showToast({ title: res.msg || 'FAQ 加载失败', icon: 'none' });
		}
	} catch (e) {
		faqList.value = [];
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

function toggleItem(id) {
	openId.value = openId.value === id ? '' : id;
}

function goService() {
	openKefuCenter();
}

onShow(async () => {
	productName.value = await loadProductName();
	loadHelpFaq();
});
</script>

<style lang="scss" scoped>
.help-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
}

.help-body {
	padding: 24rpx var(--ry-page-padding-x) 48rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.help-hero {
	padding: 40rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
	background: linear-gradient(135deg, #fff6ee, #fff);
}

.help-hero__icon {
	width: 96rpx;
	height: 96rpx;
	border-radius: 28rpx;
	background-color: #fff3eb;
	display: flex;
	align-items: center;
	justify-content: center;
}

.help-hero__title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.help-hero__desc {
	font-size: 24rpx;
	color: #999;
	text-align: center;
}

.help-loading {
	padding: 48rpx 28rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
}

.help-loading__text {
	font-size: 24rpx;
	color: #999;
}

.help-empty {
	padding: 48rpx 28rpx;
	text-align: center;
}

.help-empty__text {
	font-size: 26rpx;
	color: #999;
}

.help-list {
	padding: 0 28rpx;
	overflow: hidden;
}

.help-item {
	padding: 28rpx 0;

	&--border {
		border-bottom: 1rpx solid #f4f4f4;
	}

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;

		&:active {
			opacity: 0.85;
		}
	}

	&__question {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 500;
		color: #333;
		line-height: 1.5;
	}

	&__answer {
		margin-top: 16rpx;
		padding: 20rpx;
		border-radius: 12rpx;
		background: #fafafa;

		text {
			font-size: 26rpx;
			color: #666;
			line-height: 1.6;
		}
	}
}

.help-contact {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 28rpx;

	&:active {
		opacity: 0.88;
	}

	&__icon {
		flex-shrink: 0;
		width: 52rpx;
		height: 52rpx;
		border-radius: 14rpx;
		background: #e8f8ef;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__info {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__title {
		font-size: 28rpx;
		color: #333;
	}

	&__desc {
		font-size: 24rpx;
		color: #999;
	}
}
</style>
