const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function writeUtf8(rel, content) {
	fs.writeFileSync(path.join(root, rel), content, { encoding: 'utf8' });
}

const indexStyles = fs.readFileSync(path.join(root, 'pages/index/index.vue'), 'utf8')
	.split('<style lang="scss" scoped>')[1] || '';

writeUtf8('pages/index/index.vue', `<template>
	<view class="ry-page index-page">
		<!-- 顶部 Hero -->
		<view class="home-hero">
			<view class="home-hero__glow home-hero__glow--one"></view>
			<view class="home-hero__glow home-hero__glow--two"></view>
			<view class="home-hero__glow home-hero__glow--three"></view>

			<view class="home-hero__head">
				<view class="home-hero__hello">
					<text class="home-hero__dot"></text>
					<text class="home-hero__hello-text">HI，您好</text>
				</view>
				<text class="home-hero__slogan">荣昌游 · 发现荣昌美景</text>
			</view>

			<view class="home-search-row">
				<view class="home-dest" @click="openDestinationSelect">
					<u-icon name="map-fill" color="#FF7A00" :size="15" />
					<text class="home-dest__name">{{ currentDestination.shortName }}</text>
					<u-icon name="arrow-down" color="#D98A52" :size="13" />
				</view>
				<view class="home-search" @click="onSearchClick">
					<u-icon name="search" color="#B7B7C4" :size="19" />
					<text class="home-search__placeholder">搜索景点、酒店、美食...</text>
					<view class="home-search__btn">
						<text class="home-search__btn-text">搜索</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 轮播 Banner -->
		<view class="index-banner">
			<u-swiper :list="bannerList" key-name="image" height="160" radius="18" bg-color="transparent" :indicator="true"
				indicator-mode="dot" :indicator-style="{ right: '24rpx', bottom: '22rpx' }"
				indicator-active-color="#FFFFFF" indicator-inactive-color="rgba(255,255,255,0.45)" :circular="true"
				:autoplay="true" :interval="4000" @click="onBannerClick">
				<template #default="{ item }">
					<view class="index-banner__slide">
						<image class="index-banner__img" :src="item.image" mode="aspectFill" />
						<view class="index-banner__veil"></view>
						<view class="index-banner__overlay">
							<view class="index-banner__chip">精选推荐</view>
							<text class="index-banner__title">{{ item.title }}</text>
						</view>
					</view>
				</template>
			</u-swiper>
		</view>

		<!-- 快捷入口 -->
		<view class="home-grid">
			<view class="home-grid__head">
				<text class="home-grid__title">快捷入口</text>
			</view>
			<view class="home-grid__panel ry-card">
				<u-grid :col="5" :border="false">
					<u-grid-item v-for="item in gridList" :key="item.id" @click="onGridClick(item)">
						<view class="home-grid__item">
							<view class="home-grid__icon" :style="{ backgroundColor: item.bgColor }">
								<u-icon :name="item.icon" :color="item.iconColor" :size="21" />
							</view>
							<text class="home-grid__label">{{ item.label }}</text>
						</view>
					</u-grid-item>
				</u-grid>
			</view>
		</view>

		<!-- 公告栏 -->
		<view class="index-notice ry-card" @click="onNoticeMore">
			<view class="index-notice__tag">
				<u-icon name="volume" color="#FFFFFF" :size="13" />
				<text class="index-notice__tag-text">公告</text>
			</view>
			<view class="index-notice__body">
				<u-notice-bar :text="noticeText" direction="row" :icon="''" color="#6B6B78" bg-color="transparent"
					:speed="60" />
			</view>
			<view class="index-notice__more">
				<text class="index-notice__more-text">更多</text>
				<u-icon name="arrow-right" color="#B7B7C4" :size="12" />
			</view>
		</view>

		<!-- 分类 Tab -->
		<view class="home-tabs">
			<view v-for="(tab, index) in tabList" :key="tab.name" class="home-tab"
				:class="{ 'home-tab--active': currentTab === index }" @click="currentTab = index">
				<text class="home-tab__text">{{ tab.name }}</text>
			</view>
		</view>

		<!-- 推荐列表 -->
		<view class="index-list">
			<view v-if="filteredRecommendList.length === 0" class="index-list__empty">
				<u-empty mode="list" text="暂无推荐" />
			</view>
			<view v-for="item in filteredRecommendList" :key="item.id" class="home-card ry-card"
				@click="onCardClick(item)">
				<view class="home-card__cover">
					<view class="home-card__type" :class="\`home-card__type--\${item.moduleType}\`">
						<text class="home-card__type-text">{{ moduleTypeName(item.moduleType) }}</text>
					</view>
					<image class="home-card__img" :src="item.cover" mode="aspectFill" />
				</view>
				<view class="home-card__info">
					<view class="home-card__name-row">
						<text class="home-card__name">{{ item.name }}</text>
					</view>
					<view class="home-card__addr">
						<u-icon name="map" color="#B7B7C4" :size="13" />
						<text class="home-card__addr-text">{{ item.address }}</text>
					</view>
					<view class="home-card__tags">
						<view v-for="tag in item.tags" :key="tag" class="home-card__tag">
							<text class="home-card__tag-text">{{ tag }}</text>
						</view>
					</view>
					<view class="home-card__foot">
						<view v-if="item.price > 0" class="home-card__price">
							<text class="home-card__price-symbol">¥</text>
							<text class="home-card__price-value">{{ item.price }}</text>
							<text class="home-card__price-unit">起</text>
						</view>
						<view v-else class="home-card__price home-card__price--free">
							<text class="home-card__price-value">免费</text>
						</view>
						<view class="home-card__arrow">
							<text class="home-card__arrow-text">查看详情</text>
							<u-icon name="arrow-right" color="#FF6600" :size="13" />
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="index-list__tail">
			<text class="index-list__tail-text">— 已经到底啦 —</text>
		</view>

		<!-- 美食地图悬浮按钮 -->
		<view class="home-fab home-fab--food" @click="onFoodMapFab">
			<view class="home-fab__bg"></view>
			<u-icon name="map-fill" color="#FF6600" :size="24" />
		</view>

		<!-- AI 助手悬浮按钮 -->
		<view class="home-fab home-fab--ai" @click="onAiFab">
			<view class="home-fab__bg"></view>
			<view class="home-fab__ring"></view>
			<u-icon name="chat-fill" color="#FF6600" :size="28" />
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO } from '@/common/theme.js';
	import { getCurrentDestination } from '@/common/destination-mock.js';

	const currentDestination = ref(getCurrentDestination());

	const MODULE_TYPE_NAMES = {
		goose: '卤鹅美食',
		scenic: '景区景点',
		hotel: '酒店住宿'
	};

	const moduleTypeName = (key) => MODULE_TYPE_NAMES[key] || '推荐';

	const DETAIL_PATH = {
		goose: '/mobile-design/goose/detail',
		scenic: '/mobile-design/scenic/detail',
		hotel: '/mobile-design/hotel/detail'
	};

	const createBannerList = () => ([
		{
			image: PLACEHOLDER_LOGO,
			title: '荣昌卤鹅 · 地道招牌美食',
			linkPath: '/mobile-design/goose/list'
		},
		{
			image: PLACEHOLDER_LOGO,
			title: '万灵古镇 · 水乡千年古邑',
			linkPath: '/mobile-design/scenic/list'
		},
		{
			image: PLACEHOLDER_LOGO,
			title: '精彩活动 · 等你来参与',
			linkPath: '/mobile-design/activity/list'
		}
	]);

	const bannerList = ref(createBannerList());

	const gridList = ref([
		{ id: 1, label: '荣昌卤鹅', icon: 'bag-fill', bgColor: '#FFF0E6', iconColor: '#FF7A00', path: '/mobile-design/goose/list' },
		{ id: 2, label: '旅游景区', icon: 'map-fill', bgColor: '#E8F8EF', iconColor: '#2EAF6A', path: '/mobile-design/scenic/list' },
		{ id: 3, label: '酒店住宿', icon: 'home-fill', bgColor: '#EAF2FF', iconColor: '#2979FF', path: '/mobile-design/hotel/list' },
		{ id: 4, label: '美食地图', icon: 'pushpin-fill', bgColor: '#FFE8EE', iconColor: '#FF6B8A', path: '/mobile-design/food-map/index' },
		{ id: 5, label: '停车通行', icon: 'car-fill', bgColor: '#FFF8E6', iconColor: '#FFB800', path: '/mobile-design/parking/index' },
		{ id: 6, label: '精彩活动', icon: 'calendar-fill', bgColor: '#F3E8F8', iconColor: '#9C27B0', path: '/mobile-design/activity/list' },
		{ id: 7, label: '非遗文化', icon: 'star-fill', bgColor: '#EFEBE9', iconColor: '#5D4037', path: '/mobile-design/heritage/index' },
		{ id: 8, label: '地图导航', icon: 'map', bgColor: '#E0F7FA', iconColor: '#00BCD4', path: '/mobile-design/map/index' },
		{ id: 9, label: '公共交通', icon: 'car', bgColor: '#EDE7F6', iconColor: '#9575CD', path: '/mobile-design/transport/list' },
		{ id: 10, label: '旅游线路', icon: 'list-dot', bgColor: '#FCE4EC', iconColor: '#E91E8C', path: '/mobile-design/route/list' }
	]);

	const noticeText = ref('荣昌区暑期文旅活动火热进行中，万灵古镇夜场开放，欢迎前来体验。');

	const tabList = ref([
		{ name: '热门' },
		{ name: '荣昌卤鹅' },
		{ name: '旅游景区' },
		{ name: '酒店住宿' }
	]);

	const currentTab = ref(0);

	const createRecommendList = () => ([
		{
			id: 1,
			category: 1,
			moduleType: 'goose',
			name: '老字号·荣昌卤鹅旗舰店',
			address: '荣昌区昌州大道中段128号',
			price: 68,
			tags: ['百年传承', '地道口味'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.goose
		},
		{
			id: 2,
			category: 1,
			moduleType: 'goose',
			name: '荣昌卤鹅·万灵古镇体验店',
			address: '荣昌区万灵镇滨河路56号',
			price: 58,
			tags: ['非遗技艺', '古镇特色'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.goose
		},
		{
			id: 3,
			category: 2,
			moduleType: 'scenic',
			name: '万灵古镇',
			address: '荣昌区万灵镇',
			price: 0,
			tags: ['千年古邑', '免费开放'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.scenic
		},
		{
			id: 4,
			category: 3,
			moduleType: 'hotel',
			name: '荣昌华茂国际大酒店',
			address: '荣昌区昌州大道中段128号',
			price: 268,
			tags: ['商务出行', '近景区'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.hotel
		},
		{
			id: 5,
			category: 1,
			moduleType: 'goose',
			name: '吴记非遗卤鹅馆',
			address: '荣昌区万灵镇古镇街12号',
			price: 48,
			tags: ['非遗技艺', '文化体验'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.goose
		},
		{
			id: 6,
			category: 1,
			moduleType: 'goose',
			name: '荣州卤鹅·传承老店',
			address: '荣昌区昌元街道荣州大道32号',
			price: 52,
			tags: ['家传老卤', '本地口碑'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.goose
		},
		{
			id: 7,
			category: 2,
			moduleType: 'scenic',
			name: '荣昌安陶小镇',
			address: '荣昌区安富街道安陶路',
			price: 0,
			tags: ['陶艺文化', '亲子研学'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.scenic
		},
		{
			id: 8,
			category: 2,
			moduleType: 'scenic',
			name: '荣昌博物馆',
			address: '荣昌区昌州大道中段18号',
			price: 20,
			tags: ['历史文化', '免费开放'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.scenic
		},
		{
			id: 9,
			category: 3,
			moduleType: 'hotel',
			name: '荣昌万灵古镇精品客栈',
			address: '荣昌区万灵镇滨河路88号',
			price: 328,
			tags: ['古镇风情', '免费停车'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.hotel
		},
		{
			id: 10,
			category: 3,
			moduleType: 'hotel',
			name: '荣昌文旅度假酒店',
			address: '荣昌区昌元街道荣峰大道168号',
			price: 198,
			tags: ['休闲度假', '近景区'],
			cover: PLACEHOLDER_LOGO,
			detailPath: DETAIL_PATH.hotel
		}
	]);

	const MORE_RECOMMEND_POOL = [
		{
			moduleType: 'goose',
			name: '荣昌卤鹅·昌元街道店',
			address: '荣昌区昌元街道荣州大道52号',
			price: 45,
			tags: ['堂食外带', '人气门店'],
			category: 1
		},
		{
			moduleType: 'scenic',
			name: '荣昌夏布小镇',
			address: '荣昌区昌州街道',
			price: 15,
			tags: ['非遗体验', '夏布织造'],
			category: 2
		},
		{
			moduleType: 'hotel',
			name: '荣昌商务快捷酒店',
			address: '荣昌区昌州街道荣州大道69号',
			price: 218,
			tags: ['交通便利', '性价比高'],
			category: 3
		}
	];

	const recommendList = ref(createRecommendList());
	const loadingMore = ref(false);

	const filteredRecommendList = computed(() => {
		if (currentTab.value === 0) {
			return recommendList.value;
		}
		return recommendList.value.filter(item => item.category === currentTab.value);
	});

	const navigateSafe = (url) => {
		uni.navigateTo({ url });
	};

	const TAB_GRID_MAP = {
		荣昌卤鹅: 1,
		旅游景区: 2,
		酒店住宿: 3
	};

	const onGridClick = (item) => {
		const tabIndex = TAB_GRID_MAP[item.label];
		if (tabIndex !== undefined) {
			currentTab.value = tabIndex;
		}
		if (item.path) {
			navigateSafe(item.path);
		}
	};

	const onBannerClick = (index) => {
		const banner = bannerList.value[index];
		if (banner?.linkPath) {
			navigateSafe(banner.linkPath);
		}
	};

	const onNoticeMore = () => {
		navigateSafe('/mobile-design/notice/list');
	};

	const onCardClick = (item) => {
		const path = item.detailPath || DETAIL_PATH[item.moduleType] || DETAIL_PATH.goose;
		navigateSafe(\`\${path}?id=\${item.id}\`);
	};

	const appendMoreRecommend = () => {
		if (loadingMore.value) {
			return;
		}
		const existIds = new Set(recommendList.value.map(i => i.id));
		const pending = MORE_RECOMMEND_POOL
			.map((item, idx) => ({ item, id: 11 + idx }))
			.filter(({ id }) => !existIds.has(id));
		if (pending.length === 0) {
			uni.showToast({ title: '没有更多了', icon: 'none' });
			return;
		}
		loadingMore.value = true;
		setTimeout(() => {
			pending.forEach(({ item, id }) => {
				recommendList.value.push({
					id,
					category: item.category,
					moduleType: item.moduleType,
					name: item.name,
					address: item.address,
					price: item.price,
					tags: item.tags,
					cover: PLACEHOLDER_LOGO,
					detailPath: DETAIL_PATH[item.moduleType]
				});
			});
			loadingMore.value = false;
		}, 400);
	};

	const onFoodMapFab = () => {
		navigateSafe('/mobile-design/food-map/index');
	};

	const onAiFab = () => {
		navigateSafe('/mobile-design/ai/chat');
	};

	const onSearchClick = () => {
		navigateSafe('/mobile-design/search/index');
	};

	const openDestinationSelect = () => {
		navigateSafe('/pages/destination/select');
	};

	onLoad(() => {});

	onShow(() => {
		currentDestination.value = getCurrentDestination();
	});

	onPullDownRefresh(() => {
		bannerList.value = createBannerList();
		recommendList.value = createRecommendList();
		setTimeout(() => {
			uni.stopPullDownRefresh();
			uni.showToast({ title: '已刷新', icon: 'none' });
		}, 600);
	});

	onReachBottom(() => {
		appendMoreRecommend();
	});
</script>

<style lang="scss" scoped>${indexStyles.startsWith('\n') ? indexStyles : '\n' + indexStyles}`);

const hotelStyles = fs.readFileSync(path.join(root, 'mobile-design/hotel/detail.vue'), 'utf8')
	.split('<style lang="scss" scoped>')[1];

writeUtf8('mobile-design/hotel/detail.vue', `<template>
	<view class="hotel-detail-page">
		<view class="hotel-cover">
			<swiper
				class="hotel-cover__swiper"
				:indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)"
				indicator-active-color="#FFFFFF"
				:autoplay="bannerList.length > 1"
				:circular="true"
				:interval="4000"
				duration="300"
				@change="onBannerChange"
			>
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="hotel-cover__item">
					<image class="hotel-cover__img ry-placeholder-img" :src="item" mode="aspectFill" />
				</swiper-item>
			</swiper>
			<view class="hotel-cover__mask" />
			<view v-if="hotel.starLabel" class="hotel-cover__badge">
				<text class="hotel-cover__badge-text">{{ hotel.starLabel }}</text>
			</view>
			<view class="hotel-cover__favorite" @click="onFavorite">
				<u-icon
					:name="favorited ? 'heart-fill' : 'heart'"
					:color="favorited ? '#FF4757' : '#FFFFFF'"
					:size="22"
				/>
			</view>
			<view v-if="bannerList.length > 1" class="hotel-cover__count">
				<text class="hotel-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="hotel-detail-body">
			<view class="hotel-info ry-card">
				<view class="hotel-info__head">
					<text class="hotel-info__name">{{ hotel.name }}</text>
					<view class="hotel-info__price">
						<text class="hotel-info__price-symbol">¥</text>
						<text class="hotel-info__price-value">{{ priceFrom }}</text>
						<text class="hotel-info__price-suffix">起</text>
					</view>
				</view>

				<view class="hotel-info__rating">
					<u-rate
						:model-value="hotel.starLevel"
						:count="5"
						:readonly="true"
						:allow-half="false"
						active-color="#FFB800"
						inactive-color="#E8E8E8"
						:size="14"
						:gutter="3"
					/>
					<text class="hotel-info__rating-label">{{ hotel.starLabel }}</text>
				</view>

				<view class="hotel-info__addr" @click="onAddressClick">
					<view class="hotel-info__addr-icon">
						<u-icon name="map-fill" :color="accentColor" :size="14" />
					</view>
					<text class="hotel-info__addr-text">{{ hotel.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>

				<view class="hotel-info__meta">
					<view class="hotel-info__meta-item">
						<u-icon name="clock-fill" :color="accentColor" :size="15" />
						<text class="hotel-info__meta-label">入住时间</text>
						<text class="hotel-info__meta-value">{{ hotel.checkInTime }}</text>
					</view>
					<view class="hotel-info__meta-divider" />
					<view class="hotel-info__meta-item">
						<u-icon name="phone-fill" :color="accentColor" :size="15" />
						<text class="hotel-info__meta-label">联系电话</text>
						<text class="hotel-info__meta-value">{{ hotel.phone }}</text>
					</view>
				</view>
			</view>

			<view class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="grid-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">酒店设施</text>
				</view>
				<view class="hotel-facilities">
					<view v-for="item in hotel.facilities" :key="item.id" class="hotel-facilities__item">
						<view class="hotel-facilities__icon-wrap">
							<u-icon :name="item.icon" :color="accentColor" :size="22" />
						</view>
						<text class="hotel-facilities__label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<view class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="home-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">房型推荐</text>
				</view>
				<view class="hotel-rooms">
					<view v-for="room in hotel.roomList" :key="room.id" class="hotel-room">
						<view class="hotel-room__thumb-wrap">
							<image class="hotel-room__thumb ry-placeholder-img" :src="PLACEHOLDER_LOGO" mode="aspectFill" />
						</view>
						<view class="hotel-room__info">
							<text class="hotel-room__name">{{ room.name }}</text>
							<text class="hotel-room__spec">{{ room.spec }}</text>
							<view class="hotel-room__foot">
								<view class="hotel-room__price-row">
									<text class="hotel-room__symbol">¥</text>
									<text class="hotel-room__price">{{ room.price }}</text>
									<text class="hotel-room__unit">/晚</text>
								</view>
								<view class="hotel-room__btn" @click.stop="onRoomBook(room)">
									<text class="hotel-room__btn-text">联系商户</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<text class="hotel-footer">荣昌品质住宿 · 舒心之选</text>
		</view>

		<view class="hotel-bar">
			<view class="hotel-bar__btn hotel-bar__btn--outline" @click="onContactFrontDesk">
				<u-icon name="phone-fill" :color="accentColor" :size="18" />
				<text class="hotel-bar__btn-text hotel-bar__btn-text--outline">联系前台</text>
			</view>
			<view class="hotel-bar__btn hotel-bar__btn--primary" @click="onBookNow">
				<u-icon name="calendar-fill" color="#FFFFFF" :size="16" />
				<text class="hotel-bar__btn-text">立即预定</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
	import { isFavorited, toggleFavorite, logFootprint } from '@/common/destination-mock.js';

	const accentColor = getModuleColor('hotel');

	const BOOKING_CONFIRM_PATH = '/mobile-design/booking/confirm';
	const MERCHANT_SERVICE_PATH = '/pages/merchant/service';

	const HOTEL_BASE = {
		starLevel: 5,
		starLabel: '五星级',
		checkInTime: '14:00 后入住',
		bannerImage: '',
		bannerImages: [],
		facilities: [
			{ id: 1, icon: 'wifi', label: '免费WiFi' },
			{ id: 2, icon: 'car-fill', label: '免费停车' },
			{ id: 3, icon: 'grid-fill', label: '健身房' },
			{ id: 4, icon: 'gift-fill', label: '含早餐' }
		]
	};

	const MOCK_HOTEL_MAP = {
		'1': {
			id: '1',
			name: '荣昌华茂国际大酒店',
			address: '荣昌区昌州大道中段128号',
			latitude: 29.4167,
			longitude: 105.5944,
			phone: '023-46781234',
			...HOTEL_BASE,
			roomList: [
				{ id: 1, name: '豪华大床房', spec: '30㎡ | 大床 | 含早餐', price: '398' },
				{ id: 2, name: '行政套房', spec: '60㎡ | 双床 | 含早餐', price: '688' },
				{ id: 3, name: '商务双床房', spec: '35㎡ | 双床 | 含早餐', price: '458' }
			]
		},
		'2': {
			id: '2',
			name: '荣昌万灵古镇精品客栈',
			starLevel: 4,
			starLabel: '四星级',
			address: '荣昌区万灵镇滨河路88号',
			latitude: 29.4021,
			longitude: 105.5789,
			phone: '023-46785678',
			checkInTime: '15:00 后入住',
			bannerImage: '',
			bannerImages: [],
			facilities: HOTEL_BASE.facilities,
			roomList: [
				{ id: 1, name: '古镇观景大床房', spec: '28㎡ | 大床 | 含早餐', price: '268' },
				{ id: 2, name: '水乡特色双床房', spec: '32㎡ | 双床 | 含早餐', price: '328' },
				{ id: 3, name: '家庭亲子套房', spec: '48㎡ | 双床 | 含早餐', price: '528' }
			]
		},
		'3': {
			id: '3',
			name: '荣昌文旅度假酒店',
			address: '荣昌区昌元街道荣峰大道168号',
			latitude: 29.4102,
			longitude: 105.5888,
			phone: '023-46782345',
			...HOTEL_BASE,
			roomList: [
				{ id: 1, name: '湖景大床房', spec: '38㎡ | 大床 | 含早餐', price: '488' },
				{ id: 2, name: '园景双床房', spec: '36㎡ | 双床 | 含早餐', price: '428' },
				{ id: 3, name: '度假别墅', spec: '80㎡ | 大床 | 含早餐', price: '1288' }
			]
		}
	};

	const hotel = ref({ ...MOCK_HOTEL_MAP['1'] });
	const favorited = ref(false);
	const bannerCurrent = ref(0);

	const bannerList = computed(() => {
		const images = (hotel.value.bannerImages || []).filter(Boolean);
		if (images.length) return images;
		if (hotel.value.bannerImage) return [hotel.value.bannerImage];
		return [PLACEHOLDER_LOGO, PLACEHOLDER_LOGO, PLACEHOLDER_LOGO];
	});

	const priceFrom = computed(() => {
		const prices = (hotel.value.roomList || []).map((room) => Number(room.price)).filter(Boolean);
		if (!prices.length) return '--';
		return Math.min(...prices);
	});

	const navigateSafe = (url) => {
		uni.navigateTo({ url });
	};

	const loadHotelById = (id) => {
		const key = String(id || '1');
		const mock = MOCK_HOTEL_MAP[key] || MOCK_HOTEL_MAP['1'];
		hotel.value = { ...mock };
		favorited.value = isFavorited(favoriteItem());
		logFootprint(favoriteItem());
	};

	const favoriteItem = () => ({
		moduleType: 'hotel',
		id: hotel.value.id,
		name: hotel.value.name,
		address: hotel.value.address,
		path: \`/mobile-design/hotel/detail?id=\${hotel.value.id}\`
	});

	onLoad((options) => {
		loadHotelById(options?.id);
	});

	function onBannerChange(e) {
		bannerCurrent.value = e.detail.current;
	}

	function onFavorite() {
		favorited.value = toggleFavorite(favoriteItem());
		uni.showToast({
			title: favorited.value ? '已收藏' : '已取消收藏',
			icon: 'none'
		});
	}

	function onAddressClick() {
		const { latitude, longitude, address, name } = hotel.value;
		if (!latitude || !longitude) {
			uni.showToast({ title: address, icon: 'none' });
			return;
		}
		uni.openLocation({
			latitude,
			longitude,
			name,
			address,
			fail: () => {
				uni.showToast({ title: address, icon: 'none' });
			}
		});
	}

	function onContactFrontDesk() {
		const { phone } = hotel.value;
		if (!phone) {
			uni.showToast({ title: '暂无联系电话', icon: 'none' });
			return;
		}
		/* #ifdef H5 */
		uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' });
		return;
		/* #endif */
		uni.makePhoneCall({
			phoneNumber: phone,
			fail: () => {
				uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' });
			}
		});
	}

	function buildBookingUrl(room) {
		const { id, name } = hotel.value;
		const targetRoom = room || hotel.value.roomList[0];
		const roomName = targetRoom?.name || '';
		const roomSpec = targetRoom?.spec || '';
		const roomPrice = targetRoom?.price || '398';
		const query = [
			'type=hotel',
			\`id=\${id}\`,
			\`title=\${encodeURIComponent(name)}\`,
			\`room=\${encodeURIComponent(roomName)}\`,
			\`subtitle=\${encodeURIComponent(roomSpec)}\`,
			\`price=\${roomPrice}\`
		].join('&');
		return \`\${BOOKING_CONFIRM_PATH}?\${query}\`;
	}

	function onRoomBook(room) {
		navigateSafe(\`\${MERCHANT_SERVICE_PATH}?merchantId=hotel-\${hotel.value.id}\`);
	}

	function onBookNow() {
		navigateSafe(buildBookingUrl());
	}
</script>

<style lang="scss" scoped>${hotelStyles.startsWith('\n') ? hotelStyles : '\n' + hotelStyles}`);

console.log('index.vue and hotel/detail.vue fixed');
