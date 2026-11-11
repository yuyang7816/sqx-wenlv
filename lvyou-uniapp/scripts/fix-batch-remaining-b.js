const { patchPage } = require('./fix-batch-shared');

// craft-intro
patchPage('mobile-design/heritage/craft-intro.vue', `<template>
	<view class="craft-intro-page">
		<view class="craft-intro-body">
			<view class="craft-hero" :style="{ background: \`linear-gradient(145deg, \${craft.coverFrom}, \${craft.coverTo})\` }">
				<image class="craft-hero__img ry-placeholder-img" :src="PLACEHOLDER_LOGO" mode="aspectFit" />
				<view class="craft-hero__badge"><text class="craft-hero__badge-text">{{ craft.level }}</text></view>
			</view>
			<view class="craft-info ry-card">
				<text class="craft-info__name">{{ craft.name }}</text>
				<text class="craft-info__level">{{ craft.levelLabel }}</text>
				<text class="craft-info__tag">{{ craft.tag }}</text>
			</view>
			<view class="craft-section ry-card">
				<text class="craft-section__title">项目概述</text>
				<text class="craft-section__text">{{ craft.overview }}</text>
			</view>
			<view class="craft-section ry-card">
				<text class="craft-section__title">技艺特色</text>
				<text class="craft-section__text">{{ craft.feature }}</text>
			</view>
			<view class="craft-section ry-card">
				<text class="craft-section__title">文化传承</text>
				<text class="craft-section__text">{{ craft.culture }}</text>
			</view>
			<text class="craft-footer">荣昌非遗 · 活态传承</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { getCraftById } from '@/common/heritage-craft-mock.js';

const craft = ref({ ...getCraftById('1') });

onLoad((options) => {
	craft.value = { ...getCraftById(options?.id || '1') };
	uni.setNavigationBarTitle({ title: craft.value.name });
});
</script>`);

// goods-detail
patchPage('mobile-design/heritage/goods-detail.vue', `<template>
	<view class="goods-detail-page">
		<view class="goods-detail-body">
			<swiper class="goods-swiper" :indicator-dots="true" indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#FFFFFF">
				<swiper-item v-for="(banner, index) in goods.banners" :key="index">
					<image class="goods-swiper__img ry-placeholder-img" :src="banner.image || PLACEHOLDER_LOGO" mode="aspectFill" />
				</swiper-item>
			</swiper>
			<view class="goods-info ry-card">
				<text class="goods-info__name">{{ goods.name }}</text>
				<text class="goods-info__desc">{{ goods.desc }}</text>
				<view class="goods-info__price-row">
					<text class="goods-info__symbol">¥</text>
					<text class="goods-info__price">{{ goods.price }}</text>
					<text class="goods-info__sales">已售 {{ goods.sales }}</text>
				</view>
			</view>
			<view class="goods-detail-section ry-card">
				<text class="goods-detail-section__title">商品详情</text>
				<text class="goods-detail-section__text">{{ goods.detail }}</text>
			</view>
			<view class="goods-bar">
				<view class="goods-bar__btn goods-bar__btn--outline" @click="onContact"><text>联系商户</text></view>
				<view class="goods-bar__btn goods-bar__btn--primary" @click="onBuy"><text>立即购买</text></view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { getGoodsById } from '@/common/heritage-goods-mock.js';

const goods = ref({ ...getGoodsById('1') });
onLoad((options) => {
	goods.value = { ...getGoodsById(options?.id || '1') };
	uni.setNavigationBarTitle({ title: goods.value.name });
});
function onContact() { uni.showToast({ title: '请联系商户咨询（Mock）', icon: 'none' }); }
function onBuy() { uni.navigateTo({ url: \`/mobile-design/heritage/order-confirm?id=\${goods.value.id}\` }); }
</script>`);

// goose menu
patchPage('mobile-design/goose/menu.vue', `<template>
	<view class="goose-menu-page">
		<view class="goose-menu-body">
			<text class="goose-menu__title">{{ shopName }}</text>
			<text class="goose-menu__subtitle">全部菜品</text>
			<view class="goose-menu-list">
				<view v-for="item in menuList" :key="item.id" class="goose-menu-item ry-card">
					<view class="goose-menu-item__info">
						<text class="goose-menu-item__name">{{ item.name }}</text>
						<text class="goose-menu-item__desc">{{ item.desc }}</text>
						<view class="goose-menu-item__foot">
							<text class="goose-menu-item__price">¥{{ item.price }}</text>
							<text class="goose-menu-item__sold">月售 {{ item.sold }}</text>
						</view>
					</view>
					<view class="goose-menu-item__btn" @click="onReserve(item)"><text>预约</text></view>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getGooseMenuList } from '@/common/goose-mock.js';

const shopId = ref('1');
const shopName = ref('荣昌卤鹅');
const menuList = ref([]);
const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';

onLoad((options) => {
	shopId.value = String(options?.id || '1');
	if (options?.shopName) shopName.value = decodeURIComponent(options.shopName);
	menuList.value = getGooseMenuList(shopId.value);
	uni.setNavigationBarTitle({ title: \`\${shopName.value}·菜单\` });
});

function onReserve(item) {
	const query = \`shopId=\${shopId.value}&shopName=\${encodeURIComponent(shopName.value)}&dishName=\${encodeURIComponent(item.name)}\`;
	uni.navigateTo({ url: \`\${CANTEEN_RESERVE_PATH}?\${query}\` });
}
</script>`);

// booking success
patchPage('mobile-design/booking/success.vue', `<template>
	<view class="booking-success-page">
		<view class="booking-success-body">
			<u-icon name="checkmark-circle-fill" color="#2EAF6A" :size="80" />
			<text class="booking-success__title">预约成功</text>
			<text class="booking-success__desc">{{ summaryText }}</text>
			<view class="booking-success__actions">
				<u-button text="查看订单" plain color="#FF6600" @click="goOrders" />
				<u-button text="返回首页" color="#FF6600" @click="goHome" />
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const info = ref({ title: '', date: '', guests: 1, amount: 0 });
const summaryText = computed(() => \`\${info.value.title}\\n预约日期：\${info.value.date}\\n人数：\${info.value.guests} 人\\n合计：¥\${info.value.amount}\`);

onLoad((options) => {
	info.value = {
		title: options?.title ? decodeURIComponent(options.title) : '预约项目',
		date: options?.date ? decodeURIComponent(options.date) : '',
		guests: Number(options?.guests || 1),
		amount: Number(options?.amount || 0)
	};
});

function goOrders() { uni.navigateTo({ url: '/pages/order/list' }); }
function goHome() { uni.switchTab({ url: '/pages/index/index' }); }
</script>`);

// feedback
patchPage('mobile-design/feedback/index.vue', `<template>
	<view class="feedback-page ry-page">
		<view class="feedback-body">
			<view class="feedback-section ry-card">
				<text class="feedback-label">反馈类型</text>
				<view class="feedback-types">
					<view v-for="item in typeOptions" :key="item.name" class="feedback-type" :class="{ 'feedback-type--active': form.type === item.name }" @click="form.type = item.name">
						<text>{{ item.name }}</text>
					</view>
				</view>
			</view>
			<view class="feedback-section ry-card">
				<text class="feedback-label">反馈内容</text>
				<u-textarea v-model="form.content" placeholder="请描述您遇到的问题或建议" :maxlength="500" count />
			</view>
			<view class="feedback-section ry-card">
				<text class="feedback-label">联系方式</text>
				<u-input v-model="form.contactName" placeholder="联系人" border="bottom" />
				<u-input v-model="form.phone" placeholder="手机号" type="number" maxlength="11" border="bottom" />
			</view>
			<u-button text="提交反馈" color="#FF6600" @click="onSubmit" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { FEEDBACK_TYPE_OPTIONS, MOCK_USER_PROFILE } from '@/common/feedback-mock.js';

const typeOptions = FEEDBACK_TYPE_OPTIONS;
const form = ref({ type: typeOptions[0].name, content: '', contactName: MOCK_USER_PROFILE.contactName, phone: MOCK_USER_PROFILE.phone });

function onSubmit() {
	if (!form.value.content.trim()) { uni.showToast({ title: '请填写反馈内容', icon: 'none' }); return; }
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	uni.showToast({ title: '反馈已提交', icon: 'success' });
	setTimeout(() => uni.navigateBack(), 800);
}
</script>`);

// search
patchPage('mobile-design/search/index.vue', `<template>
	<view class="search-page">
		<view class="search-header">
			<u-search v-model="keyword" placeholder="搜索景点、酒店、美食..." :show-action="true" action-text="搜索" @search="onSearch" @custom="onSearch" />
		</view>
		<view class="search-body">
			<text class="search-section-title">热门搜索</text>
			<view class="search-tags">
				<view v-for="tag in hotTags" :key="tag" class="search-tag" @click="onTagClick(tag)"><text>{{ tag }}</text></view>
			</view>
			<view v-if="results.length" class="search-results">
				<view v-for="item in results" :key="item.id" class="search-result ry-card" @click="openResult(item)">
					<text class="search-result__name">{{ item.name }}</text>
					<text class="search-result__type">{{ item.type }}</text>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';

const keyword = ref('');
const hotTags = ['万灵古镇', '荣昌卤鹅', '酒店住宿', '非遗体验', '公交线路'];
const MOCK_RESULTS = [
	{ id: 1, name: '万灵古镇', type: '景区', path: '/mobile-design/scenic/detail?id=1' },
	{ id: 2, name: '老字号·荣昌卤鹅旗舰店', type: '餐饮', path: '/mobile-design/goose/detail?id=1' },
	{ id: 3, name: '荣昌华茂国际大酒店', type: '酒店', path: '/mobile-design/hotel/detail?id=1' }
];
const results = ref([]);

function onSearch() {
	const key = keyword.value.trim();
	if (!key) { uni.showToast({ title: '请输入搜索关键词', icon: 'none' }); return; }
	results.value = MOCK_RESULTS.filter(item => item.name.includes(key) || item.type.includes(key));
	if (!results.value.length) uni.showToast({ title: '暂无相关结果', icon: 'none' });
}
function onTagClick(tag) { keyword.value = tag; onSearch(); }
function openResult(item) { if (item.path) uni.navigateTo({ url: item.path }); }
</script>`);

// ai chat
patchPage('mobile-design/ai/chat.vue', `<template>
	<view class="ai-chat-page">
		<scroll-view class="ai-chat-list" scroll-y :scroll-into-view="scrollIntoView">
			<view v-for="msg in messages" :key="msg.id" :id="'msg-' + msg.id" class="ai-msg" :class="{ 'ai-msg--user': msg.role === 'user' }">
				<text class="ai-msg__text">{{ msg.content }}</text>
			</view>
		</scroll-view>
		<view class="ai-chat-bar">
			<u-input v-model="inputText" placeholder="问我荣昌旅游相关问题..." border="surround" />
			<u-button text="发送" size="small" color="#FF6600" @click="onSend" />
		</view>
	</view>
</template>`, `<script setup>
import { ref, nextTick } from 'vue';

const inputText = ref('');
const scrollIntoView = ref('');
const messages = ref([
	{ id: 1, role: 'assistant', content: '您好，我是荣e游 AI 助手，可以为您推荐荣昌景点、美食、酒店和出行信息。' }
]);
let msgId = 2;

function onSend() {
	const text = inputText.value.trim();
	if (!text) return;
	messages.value.push({ id: msgId++, role: 'user', content: text });
	inputText.value = '';
	const replies = [
		'推荐您前往万灵古镇感受水乡古邑风情，品尝地道荣昌卤鹅。',
		'荣昌安陶小镇适合亲子研学，可体验制陶全过程。',
		'如需酒店推荐，荣昌华茂国际大酒店和万灵古镇精品客栈都是不错的选择。'
	];
	const reply = replies[(msgId - 2) % replies.length];
	setTimeout(() => {
		messages.value.push({ id: msgId++, role: 'assistant', content: reply });
		nextTick(() => { scrollIntoView.value = 'msg-' + (msgId - 1); });
	}, 400);
	nextTick(() => { scrollIntoView.value = 'msg-' + (msgId - 2); });
}
</script>`);

console.log('fix-batch-remaining-b done');
