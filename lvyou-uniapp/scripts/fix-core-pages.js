const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function writeUtf8(rel, content) {
	fs.writeFileSync(path.join(root, rel), content, { encoding: 'utf8' });
}

function readStyles(rel) {
	const s = fs.readFileSync(path.join(root, rel), 'utf8');
	return s.split('<style lang="scss" scoped>')[1] || '';
}

const gooseStyles = readStyles('mobile-design/goose/list.vue');
const myStyles = readStyles('pages/my/my.vue');
const msgStyles = readStyles('pages/msg/msg.vue');

// --- index.vue: fix style comments only ---
(function fixIndexStyleComments() {
	let s = fs.readFileSync(path.join(root, 'pages/index/index.vue'), 'utf8');
	const replacements = [
		[/\/\* ============ v[^*]* Hero ============ \*\//, '/* ============ 顶部 Hero ============ */'],
		[/\/\* ============ n[^*]* Banner ============ \*\//, '/* ============ 轮播 Banner ============ */'],
		[/\/\* ============ [^*]*\uFFFD[^*]* ============ \*\//g, (match) => {
			if (match.includes('Tab')) return '/* ============ 分类 Tab ============ */';
			if (match.includes('Hero') || match.includes('v')) return '/* ============ 顶部 Hero ============ */';
			if (match.includes('Banner') || match.includes('n')) return '/* ============ 轮播 Banner ============ */';
			if (match.includes('notice') || match.includes('lJ')) return '/* ============ 公告栏 ============ */';
			if (match.includes('fab') || match.includes('ne')) return '/* ============ 悬浮按钮 ============ */';
			return '/* ============ 区块 ============ */';
		}]
	];
	for (const [re, rep] of replacements) {
		s = s.replace(re, rep);
	}
	fs.writeFileSync(path.join(root, 'pages/index/index.vue'), s, 'utf8');
})();

// --- goose/list.vue ---
writeUtf8('mobile-design/goose/list.vue', `<template>
	<view class="goose-list-page">
		<view class="goose-list-body">
			<view class="goose-hero">
				<view class="goose-hero__glow" />
				<view class="goose-hero__badge">
					<text class="goose-hero__badge-text">非遗美食</text>
				</view>
				<view class="goose-hero__content">
					<text class="goose-hero__title">荣昌卤鹅</text>
					<text class="goose-hero__subtitle">荣昌游 · 地道卤味</text>
					<view class="goose-hero__stats">
						<view class="goose-hero__stat">
							<text class="goose-hero__stat-num">{{ shopList.length }}+</text>
							<text class="goose-hero__stat-label">精选门店</text>
						</view>
						<view class="goose-hero__stat-divider" />
						<view class="goose-hero__stat">
							<text class="goose-hero__stat-num">4.8</text>
							<text class="goose-hero__stat-label">平均评分</text>
						</view>
						<view class="goose-hero__stat-divider" />
						<view class="goose-hero__stat">
							<text class="goose-hero__stat-num">1200+</text>
							<text class="goose-hero__stat-label">月售</text>
						</view>
					</view>
				</view>
			</view>

			<scroll-view class="goose-tabs" scroll-x :show-scrollbar="false">
				<view class="goose-tabs__row">
					<view v-for="(tab, index) in filterTabs" :key="tab.key" class="goose-tab"
						:class="{ 'goose-tab--active': currentFilter === index }" @click="onFilterTap(index)">
						<text class="goose-tab__text">{{ tab.name }}</text>
						<u-icon v-if="tab.key === 'price'" name="arrow-down-fill"
							:color="currentFilter === index ? '#FFFFFF' : '#999999'" :size="8" />
					</view>
				</view>
			</scroll-view>

			<view class="goose-list">
				<view v-for="shop in displayList" :key="shop.id" class="goose-card">
					<view class="goose-card__cover" @click="onViewDetail(shop)">
						<image class="goose-card__img ry-placeholder-img" :src="shop.cover || PLACEHOLDER_LOGO"
							mode="aspectFill" />
						<view class="goose-card__cover-mask" />
						<view class="goose-card__distance">
							<u-icon name="map-fill" color="#FFFFFF" :size="11" />
							<text class="goose-card__distance-text">{{ shop.distance }}km</text>
						</view>
						<view class="goose-card__price-tag">
							<text class="goose-card__price-text">¥{{ shop.priceFrom }}起</text>
						</view>
					</view>

					<view class="goose-card__body">
						<view class="goose-card__head" @click="onViewDetail(shop)">
							<text class="goose-card__name">{{ shop.name }}</text>
							<view class="goose-card__meta">
								<view class="goose-card__rating">
									<u-icon name="star-fill" color="#FF7A00" :size="12" />
									<text class="goose-card__rating-text">{{ shop.rating }}</text>
								</view>
								<text class="goose-card__sales">月售 {{ shop.monthlySales }}+</text>
							</view>
						</view>

						<view class="goose-card__addr-row" @click="onViewDetail(shop)">
							<u-icon name="map" color="#CCCCCC" :size="13" />
							<text class="goose-card__addr">{{ shop.address }}</text>
						</view>

						<view v-if="shop.tags && shop.tags.length" class="goose-card__tags">
							<view v-for="tag in shop.tags" :key="tag" class="goose-card__tag">
								<text class="goose-card__tag-text">{{ tag }}</text>
							</view>
						</view>

						<view class="goose-card__actions">
							<view class="goose-card__btn goose-card__btn--ghost" @click="onCanteenReserve(shop)">
								<text class="goose-card__btn-text goose-card__btn-text--ghost">餐饮预约</text>
							</view>
							<view class="goose-card__btn goose-card__btn--primary" @click="onViewDetail(shop)">
								<text class="goose-card__btn-text">查看详情</text>
								<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<text class="goose-footer">荣昌非遗美食 · 地道卤味</text>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { PLACEHOLDER_LOGO } from '@/common/theme.js';
	import { MOCK_SHOP_MAP } from '@/common/goose-mock.js';

	const DETAIL_PATH = '/mobile-design/goose/detail';
	const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';

	const filterTabs = [
		{ key: 'recommend', name: '综合推荐' },
		{ key: 'sales', name: '销量优先' },
		{ key: 'rating', name: '评分最高' },
		{ key: 'distance', name: '距离最近' },
		{ key: 'price', name: '价格排序' }
	];

	const LIST_META = {
		'1': { distance: 1.2, priceFrom: 68, recommendScore: 100 },
		'2': { distance: 5.8, priceFrom: 58, recommendScore: 90 },
		'3': { distance: 5.6, priceFrom: 48, recommendScore: 85 },
		'5': { distance: 3.1, priceFrom: 52, recommendScore: 88 },
		'6': { distance: 2.5, priceFrom: 52, recommendScore: 86 }
	};

	const MOCK_SHOP_LIST = Object.keys(MOCK_SHOP_MAP).map((id) => {
		const shop = MOCK_SHOP_MAP[id];
		const meta = LIST_META[id] || { distance: 3, priceFrom: 58, recommendScore: 80 };
		return {
			id: shop.id,
			name: shop.name,
			address: shop.address,
			cover: PLACEHOLDER_LOGO,
			rating: Number(shop.rating),
			monthlySales: String(shop.monthlySales),
			distance: meta.distance,
			priceFrom: meta.priceFrom,
			recommendScore: meta.recommendScore,
			tags: shop.tags || []
		};
	});

	const currentFilter = ref(0);
	const priceSortAsc = ref(true);
	const shopList = ref(MOCK_SHOP_LIST.map(item => ({ ...item })));

	const displayList = computed(() => {
		const list = shopList.value.map(item => ({ ...item }));
		const sortKey = filterTabs[currentFilter.value].key;
		switch (sortKey) {
			case 'sales':
				return list.sort((a, b) => Number(b.monthlySales) - Number(a.monthlySales));
			case 'rating':
				return list.sort((a, b) => b.rating - a.rating);
			case 'distance':
				return list.sort((a, b) => a.distance - b.distance);
			case 'price':
				return list.sort((a, b) => (priceSortAsc.value ? a.priceFrom - b.priceFrom : b.priceFrom - a.priceFrom));
			default:
				return list.sort((a, b) => b.recommendScore - a.recommendScore);
		}
	});

	const onFilterTap = (index) => {
		currentFilter.value = index;
		if (filterTabs[index].key !== 'price') return;
		uni.showActionSheet({
			itemList: ['价格从低到高', '价格从高到低'],
			success: (res) => {
				priceSortAsc.value = res.tapIndex === 0;
				uni.showToast({
					title: priceSortAsc.value ? '已按价格从低到高' : '已按价格从高到低',
					icon: 'none'
				});
			}
		});
	};

	const navigateSafe = (url) => {
		uni.navigateTo({ url });
	};

	const onViewDetail = (shop) => {
		navigateSafe(\`\${DETAIL_PATH}?id=\${shop.id}\`);
	};

	const onCanteenReserve = (shop) => {
		const query = \`shopId=\${shop.id}&shopName=\${encodeURIComponent(shop.name)}\`;
		navigateSafe(\`\${CANTEEN_RESERVE_PATH}?\${query}\`);
	};
</script>

<style lang="scss" scoped>${gooseStyles}`);

// --- my/my.vue ---
writeUtf8('pages/my/my.vue', `<template>
	<view class="my-page">
		<view class="my-header">
			<view class="my-header__bg">
				<view class="my-header__circle my-header__circle--1"></view>
				<view class="my-header__circle my-header__circle--2"></view>
				<view class="my-header__circle my-header__circle--3"></view>
			</view>
		</view>

		<view class="my-body">
			<view class="my-profile" @click="onProfileTap">
				<view class="my-profile__avatar-wrap">
					<image :src="userInfo.avatar" mode="aspectFill" class="my-profile__avatar"></image>
				</view>
				<view class="my-profile__info">
					<text class="my-profile__name">{{ userInfo.nickname }}</text>
					<text class="my-profile__phone">{{ userInfo.phone }}</text>
				</view>
				<view class="my-profile__arrow">
					<u-icon name="arrow-right" :size="16" color="#999999" />
				</view>
			</view>

			<view class="my-stats ry-card">
				<view v-for="stat in statsList" :key="stat.id" class="my-stats__item" @click="onStatClick(stat)">
					<text class="my-stats__value">{{ stat.value }}</text>
					<text class="my-stats__label">{{ stat.label }}</text>
				</view>
			</view>

			<view class="my-section">
				<view class="my-section__title">
					<text class="my-section__text">快捷入口</text>
				</view>
				<view class="my-grid">
					<view v-for="item in quickList" :key="item.id" class="my-grid__item" @click="onQuickClick(item)">
						<view class="my-grid__icon-wrap" :style="{ backgroundColor: item.bgColor }">
							<u-icon :name="item.icon" :color="item.iconColor" :size="24" />
						</view>
						<text class="my-grid__label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<view class="my-section">
				<view class="my-section__title">
					<text class="my-section__text">更多服务</text>
				</view>
				<view class="my-menu ry-card">
					<view v-for="(item, index) in menuList" :key="item.id" class="my-menu__item"
						:class="{ 'my-menu__item--border': index < menuList.length - 1 }" @click="onMenuClick(item)">
						<view class="my-menu__left">
							<view class="my-menu__icon-wrap" :style="{ backgroundColor: item.bgColor }">
								<u-icon :name="item.icon" :color="item.iconColor" :size="20" />
							</view>
							<view class="my-menu__text">
								<text class="my-menu__title">{{ item.title }}</text>
								<text v-if="item.label" class="my-menu__label">{{ item.label }}</text>
							</view>
						</view>
						<view class="my-menu__right">
							<text v-if="item.value" class="my-menu__value"
								:class="{ 'my-menu__value--accent': item.valueAccent }">{{ item.value }}</text>
							<u-icon name="arrow-right" :size="14" color="#CCCCCC" />
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO, BRAND_COLOR } from '@/common/theme.js';
	import { MOCK_USER_PROFILE } from '@/common/feedback-mock.js';
	import { CANTEEN_RECORDS_STORAGE_KEY, DEFAULT_CANTEEN_RECORDS } from '@/common/canteen-mock.js';
	import { isLoggedIn, getFavorites, getFootprints } from '@/common/destination-mock.js';

	const loggedIn = ref(isLoggedIn());

	const userInfo = ref({
		avatar: PLACEHOLDER_LOGO,
		nickname: '游客',
		phone: ''
	});

	const statsList = ref([
		{ id: 'collect', label: '收藏', value: '0', path: '/pages/my/asset?tab=favorite' },
		{ id: 'footprint', label: '足迹', value: '0', path: '/pages/my/asset?tab=footprint' },
		{ id: 'coupon', label: '优惠券', value: '0', path: '/pages/my/asset?tab=coupon' }
	]);

	const quickList = ref([
		{ id: 'reservation', label: '我的预约', icon: 'calendar-fill', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/mobile-design/canteen/records' },
		{ id: 'order', label: '我的订单', icon: 'file-text-fill', bgColor: '#FFF8E6', iconColor: '#FFB800', path: '/pages/order/list' },
		{ id: 'service', label: '客服中心', icon: 'chat-fill', bgColor: '#E8F8EF', iconColor: '#2EAF6A', path: '/pages/my/service' },
		{ id: 'setting', label: '设置', icon: 'setting-fill', bgColor: '#EAF2FF', iconColor: '#2979FF', path: '/pages/my/settings' }
	]);

	const menuList = ref([
		{ id: 'feedback', title: '意见反馈', label: '帮助我们改进产品体验', icon: 'edit-pen', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/mobile-design/feedback/index', value: '' },
		{ id: 'about', title: '关于荣e游', label: '版本 1.0.0', icon: 'info-circle', bgColor: '#F3E8F8', iconColor: '#9C27B0', action: 'about', value: '' }
	]);

	const aboutContent = '荣e游是面向多地区的文旅信息与服务平台，当前以荣昌区为首个运营地区。提供景区、酒店、卤鹅美食、活动预约与出行服务。\\n\\n版本：1.0.0';

	onShow(() => {
		loadMockSummary();
	});

	function maskPhone(phone) {
		const text = String(phone || '');
		if (text.length < 7) return text;
		return \`\${text.slice(0, 3)}****\${text.slice(-4)}\`;
	}

	function getCanteenRecordsFromStorage() {
		try {
			const cached = uni.getStorageSync(CANTEEN_RECORDS_STORAGE_KEY);
			if (Array.isArray(cached) && cached.length > 0) return cached;
		} catch (e) { /* mock mode */ }
		return DEFAULT_CANTEEN_RECORDS;
	}

	function loadMockSummary() {
		loggedIn.value = isLoggedIn();
		if (loggedIn.value) {
			userInfo.value = {
				avatar: PLACEHOLDER_LOGO,
				nickname: MOCK_USER_PROFILE.contactName,
				phone: maskPhone(MOCK_USER_PROFILE.phone)
			};
		} else {
			userInfo.value = {
				avatar: PLACEHOLDER_LOGO,
				nickname: '游客',
				phone: '登录后享受更多服务'
			};
		}

		const collectStat = statsList.value.find((item) => item.id === 'collect');
		if (collectStat) collectStat.value = String(getFavorites().length);
		const footprintStat = statsList.value.find((item) => item.id === 'footprint');
		if (footprintStat) footprintStat.value = String(getFootprints().length);

		const records = getCanteenRecordsFromStorage();
		const pendingCount = records.filter((item) => item.status === 'pending').length;
		const reservationItem = quickList.value.find((item) => item.id === 'reservation');
		if (reservationItem && pendingCount > 0) reservationItem.badge = pendingCount;
	}

	function onProfileTap() {
		if (!loggedIn.value) {
			uni.navigateTo({ url: '/pages/my/login' });
			return;
		}
		uni.navigateTo({ url: '/pages/my/profile' });
	}

	const PRIVATE_PREFIXES = ['/pages/my/asset', '/pages/order/'];

	function requireLogin() {
		if (loggedIn.value) return true;
		uni.navigateTo({ url: '/pages/my/login' });
		return false;
	}

	function onStatClick(stat) {
		if (!stat.path) {
			uni.showToast({ title: '敬请期待', icon: 'none' });
			return;
		}
		if (PRIVATE_PREFIXES.some(prefix => stat.path.startsWith(prefix)) && !requireLogin()) return;
		uni.navigateTo({ url: stat.path });
	}

	function onQuickClick(item) {
		if (!item.path) {
			uni.showToast({ title: '敬请期待', icon: 'none' });
			return;
		}
		if (PRIVATE_PREFIXES.some(prefix => item.path.startsWith(prefix)) && !requireLogin()) return;
		uni.navigateTo({ url: item.path });
	}

	function onMenuClick(item) {
		if (item.path) {
			uni.navigateTo({ url: item.path });
			return;
		}
		if (item.action === 'about') {
			uni.showModal({
				title: '关于荣e游',
				content: aboutContent,
				showCancel: false,
				confirmText: '知道了',
				confirmColor: BRAND_COLOR
			});
		}
	}
</script>

<style lang="scss" scoped>${myStyles}`);

// --- msg/msg.vue ---
writeUtf8('pages/msg/msg.vue', `<template>
	<view class="msg-page ry-page">
		<view class="msg-body">
			<view class="msg-tabs">
				<view v-for="(tab, index) in tabList" :key="tab.name" class="msg-tab"
					:class="{ 'msg-tab--active': currentTab === index }" @click="onTabClick(index)">
					<text class="msg-tab__text">{{ tab.name }}</text>
					<view v-if="tab.badge && tab.badge > 0" class="msg-tab__badge">
						<text class="msg-tab__badge-text">{{ tab.badge > 99 ? '99+' : tab.badge }}</text>
					</view>
				</view>
			</view>

			<view class="msg-list">
				<view v-if="filteredList.length === 0" class="msg-empty">
					<view class="msg-empty__icon">
						<u-icon name="email" color="#CCCCCC" :size="80" />
					</view>
					<text class="msg-empty__text">暂无{{ emptyTabName }}消息</text>
					<text class="msg-empty__hint">新消息将第一时间通知您</text>
				</view>
				<view v-for="item in filteredList" :key="item.id" class="msg-card ry-card" @click="onMsgClick(item)">
					<view class="msg-card__left">
						<view class="msg-card__icon-wrap" :class="\`msg-card__icon-wrap--\${item.category}\`">
							<u-icon :name="item.category === 'system' ? 'bell-fill' : 'calendar-fill'" color="#FFFFFF"
								:size="26" />
						</view>
					</view>
					<view class="msg-card__content">
						<view class="msg-card__head">
							<text class="msg-card__title"
								:class="{ 'msg-card__title--unread': item.unread }">{{ item.title }}</text>
							<view v-if="item.unread" class="msg-card__dot"></view>
						</view>
						<text class="msg-card__summary">{{ item.summary }}</text>
						<view class="msg-card__foot">
							<view class="msg-card__tag" :class="\`msg-card__tag--\${item.category}\`">
								<text class="msg-card__tag-text">{{ item.categoryLabel }}</text>
							</view>
							<text class="msg-card__time">{{ item.time }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';

	const tabList = ref([
		{ name: '全部', badge: 0 },
		{ name: '系统通知', badge: 0 },
		{ name: '活动提醒', badge: 0 }
	]);

	const currentTab = ref(0);
	const DETAIL_PATH = '/pages/msg/detail';

	const messageList = ref([
		{
			id: 1,
			category: 'system',
			categoryLabel: '系统通知',
			title: '餐饮预约提醒',
			summary: '您预约的老字号·荣昌卤鹅旗舰店即将到店，请准时前往。',
			content: '您已成功预约老字号·荣昌卤鹅旗舰店堂食，预约时间：2026年8月10日 12:00，人数 2 人。如需改期请提前联系门店。',
			time: '今天 10:30',
			unread: true,
			linkUrl: '/mobile-design/canteen/records',
			linkText: '查看预约'
		},
		{
			id: 2,
			category: 'activity',
			categoryLabel: '活动提醒',
			title: '精彩活动上线',
			summary: '8月15日至8月20日，万灵古镇非遗体验周免费开放，欢迎参与。',
			content: '荣昌非遗体验周将于 8 月 15 日至 8 月 20 日在万灵古镇举行，包含陶艺体验、卤鹅品鉴等活动，名额有限，请提前预约。',
			time: '昨天 18:00',
			unread: true,
			linkUrl: '/mobile-design/heritage/catalog',
			linkText: '查看活动'
		},
		{
			id: 3,
			category: 'system',
			categoryLabel: '系统通知',
			title: '景区预约成功',
			summary: '您预约的安陶小镇已确认，请按预约时间前往入园。',
			content: '订单号 RY202608081422001 已确认，景区：安陶小镇，预约日期：2026年8月10日，人数 2 人。请携带有效证件按时入园。',
			time: '08-08 14:22',
			unread: false,
			linkUrl: '/mobile-design/scenic/detail?id=1',
			linkText: '查看景区'
		},
		{
			id: 4,
			category: 'activity',
			categoryLabel: '活动提醒',
			title: '景区暑期优惠',
			summary: '濑溪河湿地公园暑期门票优惠，即日起至8月31日。',
			content: '濑溪河湿地公园暑期门票 8 折优惠，活动至 2026 年 8 月 31 日。适合亲子休闲与摄影打卡，欢迎预约入园。',
			time: '08-05 09:15',
			unread: false,
			linkUrl: '/mobile-design/scenic/detail?id=1',
			linkText: '查看景区'
		},
		{
			id: 5,
			category: 'system',
			categoryLabel: '系统通知',
			title: '反馈已回复',
			summary: '您提交的意见反馈已处理，感谢您对荣e游的支持。',
			content: '您于 2026 年 7 月 30 日提交的反馈（编号 FB202607301640）已处理完毕，预计 1-3 个工作日内完成优化。如有疑问请联系 023-46789012。',
			time: '08-01 16:40',
			unread: false,
			linkUrl: '/mobile-design/feedback/index',
			linkText: '查看反馈'
		}
	]);

	const tabCategoryMap = ['all', 'system', 'activity'];

	const filteredList = computed(() => {
		const category = tabCategoryMap[currentTab.value];
		if (category === 'all') return messageList.value;
		return messageList.value.filter((item) => item.category === category);
	});

	const emptyTabName = computed(() => tabList.value[currentTab.value]?.name || '');

	onLoad((options) => {
		const category = options?.category;
		if (category === 'system') currentTab.value = 1;
		else if (category === 'activity') currentTab.value = 2;
	});

	onShow(() => {
		updateTabBadges();
	});

	function updateTabBadges() {
		tabList.value[0].badge = messageList.value.filter(item => item.unread).length;
		tabList.value[1].badge = messageList.value.filter(item => item.unread && item.category === 'system').length;
		tabList.value[2].badge = messageList.value.filter(item => item.unread && item.category === 'activity').length;
	}

	onPullDownRefresh(() => {
		setTimeout(() => {
			uni.stopPullDownRefresh();
			uni.showToast({ title: '已刷新', icon: 'none' });
		}, 600);
	});

	function onTabClick(index) {
		currentTab.value = index;
	}

	const navigateSafe = (url) => {
		uni.navigateTo({ url });
	};

	function onMsgClick(item) {
		if (item.unread) {
			item.unread = false;
			updateTabBadges();
		}
		const query = \`id=\${item.id}&title=\${encodeURIComponent(item.title)}&time=\${encodeURIComponent(item.time)}\`;
		navigateSafe(\`\${DETAIL_PATH}?\${query}\`);
	}
</script>

<style lang="scss" scoped>${msgStyles}`);

console.log('core pages fixed: index styles, goose/list, my/my, msg/msg');
