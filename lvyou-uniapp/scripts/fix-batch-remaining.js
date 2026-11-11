const fs = require('fs');
const path = require('path');
const { patchPage } = require('./fix-batch-shared');

// Auto-fix: replace corrupted string literals in script/template while keeping structure
function fixStringsInFile(rel, replacements) {
	const full = path.join(__dirname, '..', rel);
	let s = fs.readFileSync(full, 'utf8');
	for (const [from, to] of replacements) {
		if (typeof from === 'string') s = s.split(from).join(to);
		else s = s.replace(from, to);
	}
	fs.writeFileSync(full, s, { encoding: 'utf8' });
}

// canteen records
patchPage('mobile-design/canteen/records.vue', `<template>
	<view class="canteen-records-page">
		<view class="canteen-records-body">
			<scroll-view class="records-tabs" scroll-x :show-scrollbar="false">
				<view class="records-tabs__row">
					<view v-for="(tab, index) in filterTabs" :key="tab.key" class="records-tab" :class="{ 'records-tab--active': currentFilter === index }" @click="onFilterTap(index)">
						<text class="records-tab__text">{{ tab.name }}</text>
						<view v-if="tab.count > 0" class="records-tab__count"><text class="records-tab__count-text">{{ tab.count }}</text></view>
					</view>
				</view>
			</scroll-view>
			<view class="records-list">
				<view v-if="filteredRecords.length === 0" class="records-empty">
					<view class="records-empty__icon"><u-icon name="calendar" :color="accentColor" :size="48" /></view>
					<text class="records-empty__title">暂无预约记录</text>
					<text class="records-empty__desc">您还没有餐饮预约，快去预约吧</text>
					<view class="records-empty__btn" @click="goReserve"><text class="records-empty__btn-text">立即预约</text></view>
				</view>
				<view v-for="record in filteredRecords" :key="record.id" class="records-card ry-card" @click="onCardTap(record)">
					<view class="records-card__head">
						<view class="records-card__head-left">
							<view class="records-card__avatar"><text class="records-card__avatar-text">{{ record.contactName.slice(0, 1) }}</text></view>
							<view class="records-card__head-info">
								<text class="records-card__name">{{ record.contactName }}</text>
								<text class="records-card__guest">{{ record.guestCount }} 人用餐</text>
							</view>
						</view>
						<view class="records-card__badge" :class="\`records-card__badge--\${record.status}\`"><text class="records-card__badge-text">{{ record.statusLabel }}</text></view>
					</view>
					<view class="records-card__fields">
						<view class="records-card__row"><u-icon name="clock-fill" :color="accentColor" :size="14" /><text class="records-card__row-text">{{ record.reserveTime }}</text></view>
						<view class="records-card__row"><u-icon name="phone-fill" :color="accentColor" :size="14" /><text class="records-card__row-text">{{ record.phone }}</text></view>
						<view class="records-card__row records-card__row--addr"><u-icon name="map-fill" :color="accentColor" :size="14" /><text class="records-card__row-text">{{ record.address }}</text></view>
					</view>
					<view v-if="record.status === 'pending'" class="records-card__actions" @click.stop>
						<view class="records-card__btn records-card__btn--ghost" @click="onCancel(record)"><text class="records-card__btn-text records-card__btn-text--ghost">取消</text></view>
						<view class="records-card__btn records-card__btn--primary" @click="onModify(record)"><text class="records-card__btn-text">修改</text></view>
					</view>
				</view>
			</view>
			<text class="records-footer">荣昌非遗美食 · 地道卤味</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { CANTEEN_RECORDS_STORAGE_KEY, DEFAULT_CANTEEN_RECORDS } from '@/common/canteen-mock.js';

const accentColor = getModuleColor('goose');
const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
const CANTEEN_DETAIL_PATH = '/mobile-design/canteen/detail';
const FILTER_STATUS_MAP = ['all', 'pending', 'completed', 'cancelled'];
const currentFilter = ref(0);
const recordList = ref([]);
const pendingCount = computed(() => recordList.value.filter(item => item.status === 'pending').length);
const completedCount = computed(() => recordList.value.filter(item => item.status === 'completed').length);
const filterTabs = computed(() => [
	{ key: 'all', name: '全部', count: recordList.value.length },
	{ key: 'pending', name: '待到店', count: pendingCount.value },
	{ key: 'completed', name: '已完成', count: completedCount.value },
	{ key: 'cancelled', name: '已取消', count: recordList.value.filter(item => item.status === 'cancelled').length }
]);
const filteredRecords = computed(() => {
	const statusKey = FILTER_STATUS_MAP[currentFilter.value];
	if (statusKey === 'all') return recordList.value;
	return recordList.value.filter(item => item.status === statusKey);
});
const loadRecordsFromStorage = () => {
	try {
		const cached = uni.getStorageSync(CANTEEN_RECORDS_STORAGE_KEY);
		if (Array.isArray(cached) && cached.length >= 3) { recordList.value = cached; return; }
	} catch (e) { /* ignore */ }
	recordList.value = DEFAULT_CANTEEN_RECORDS.map(item => ({ ...item }));
	saveRecordsToStorage();
};
const saveRecordsToStorage = () => { try { uni.setStorageSync(CANTEEN_RECORDS_STORAGE_KEY, recordList.value); } catch (e) { /* ignore */ } };
const onFilterTap = (index) => { currentFilter.value = index; };
const navigateSafe = (url) => uni.navigateTo({ url });
const goReserve = () => navigateSafe(\`\${CANTEEN_RESERVE_PATH}?mode=new\`);
const onCardTap = (record) => navigateSafe(\`\${CANTEEN_DETAIL_PATH}?id=\${encodeURIComponent(record.id)}\`);
const onCancel = (record) => {
	uni.showModal({ title: '取消预约', content: \`确定取消 \${record.contactName} 的餐饮预约吗？\`, confirmColor: accentColor, success: (res) => {
		if (!res.confirm) return;
		const target = recordList.value.find(item => item.id === record.id);
		if (target) { target.status = 'cancelled'; target.statusLabel = '已取消'; saveRecordsToStorage(); }
		uni.showToast({ title: '已取消', icon: 'success' });
	}});
};
const onModify = (record) => navigateSafe(\`\${CANTEEN_RESERVE_PATH}?mode=edit&id=\${encodeURIComponent(record.id)}\`);
onShow(() => loadRecordsFromStorage());
</script>`);

// heritage index
patchPage('mobile-design/heritage/index.vue', `<template>
	<view class="heritage-index-page">
		<view class="heritage-index-body">
			<view class="heritage-hero">
				<view class="heritage-hero__pattern" />
				<view class="heritage-hero__seal"><text class="heritage-hero__seal-text">遗</text></view>
				<view class="heritage-hero__content">
					<text class="heritage-hero__title">荣昌非遗</text>
					<text class="heritage-hero__subtitle">传承 · 体验 · 文创</text>
					<view class="heritage-hero__stats">
						<view class="heritage-hero__stat"><text class="heritage-hero__stat-num">12+</text><text class="heritage-hero__stat-label">非遗项目</text></view>
						<view class="heritage-hero__stat-divider" />
						<view class="heritage-hero__stat"><text class="heritage-hero__stat-num">6</text><text class="heritage-hero__stat-label">体验点</text></view>
						<view class="heritage-hero__stat-divider" />
						<view class="heritage-hero__stat"><text class="heritage-hero__stat-num">50+</text><text class="heritage-hero__stat-label">文创好物</text></view>
					</view>
				</view>
			</view>
			<view class="heritage-block">
				<view class="heritage-block__head">
					<view class="heritage-block__title-wrap"><view class="heritage-block__title-deco" /><text class="heritage-block__title">精选非遗</text></view>
					<view class="heritage-block__more" @click="onCatalogClick"><text class="heritage-block__more-text">查看全部</text><u-icon name="arrow-right" :color="heritageColor" :size="12" /></view>
				</view>
				<scroll-view class="heritage-craft-scroll" scroll-x :show-scrollbar="false">
					<view class="heritage-craft-row">
						<view v-for="craft in craftList" :key="craft.id" class="heritage-craft-card" @click="onCraftClick(craft)">
							<view class="heritage-craft-card__cover" :style="{ background: \`linear-gradient(145deg, \${craft.coverFrom}, \${craft.coverTo})\` }">
								<image class="heritage-craft-card__img ry-placeholder-img" :src="PLACEHOLDER_LOGO" mode="aspectFit" />
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
				<view class="heritage-block__head"><view class="heritage-block__title-wrap"><view class="heritage-block__title-deco" /><text class="heritage-block__title">探索荣昌</text></view></view>
				<view class="heritage-entry-grid">
					<view v-for="entry in entryList" :key="entry.key" class="heritage-entry" :class="\`heritage-entry--\${entry.key}\`" @click="onEntryClick(entry.key)">
						<view class="heritage-entry__icon-wrap"><u-icon :name="entry.icon" :color="entry.iconColor" :size="24" /></view>
						<text class="heritage-entry__title">{{ entry.title }}</text>
						<text class="heritage-entry__desc">{{ entry.desc }}</text>
					</view>
				</view>
			</view>
			<text class="heritage-footer">荣昌非遗 · 活态传承</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { PLACEHOLDER_LOGO, MODULE_COLORS } from '@/common/theme.js';
import { getCraftList } from '@/common/heritage-craft-mock.js';

const heritageColor = MODULE_COLORS.heritage;
const CATALOG_PATH = '/mobile-design/heritage/catalog';
const CRAFT_INTRO_PATH = '/mobile-design/heritage/craft-intro';
const SHOP_LIST_PATH = '/mobile-design/heritage/shop-list';
const STORE_RECOMMEND_PATH = '/mobile-design/store/recommend';
const craftList = ref(getCraftList());
const entryList = [
	{ key: 'catalog', title: '非遗名录', desc: '全部项目', icon: 'list', iconColor: '#5D4037', path: CATALOG_PATH },
	{ key: 'shop', title: '非遗好店', desc: '体验购买', icon: 'bag-fill', iconColor: '#8D6E63', path: SHOP_LIST_PATH },
	{ key: 'goods', title: '文创好物', desc: '夏布安陶', icon: 'gift-fill', iconColor: '#9C27B0', path: '/mobile-design/heritage/goods-list' },
	{ key: 'store', title: '推荐门店', desc: '精选商户', icon: 'star-fill', iconColor: '#FF6600', path: STORE_RECOMMEND_PATH }
];
const navigateSafe = (url) => uni.navigateTo({ url });
const onCatalogClick = () => navigateSafe(CATALOG_PATH);
const onCraftClick = (craft) => navigateSafe(\`\${CRAFT_INTRO_PATH}?id=\${craft.id}\`);
const onEntryClick = (key) => {
	const entry = entryList.find(item => item.key === key);
	if (entry?.path) navigateSafe(entry.path);
};
</script>`);

// goods-list using MOCK_GOODS_MAP
patchPage('mobile-design/heritage/goods-list.vue', `<template>
	<view class="goods-list-page">
		<view class="goods-list-body">
			<view class="goods-banner">
				<view class="goods-banner__pattern" />
				<view class="goods-banner__content">
					<text class="goods-banner__title">文创好物</text>
					<text class="goods-banner__subtitle">{{ shopName }}</text>
					<view class="goods-banner__count">
						<text class="goods-banner__count-num">{{ displayList.length }}</text>
						<text class="goods-banner__count-label">件 · {{ categoryTabs[currentTab].name }}</text>
					</view>
				</view>
			</view>
			<scroll-view class="goods-tabs" scroll-x :show-scrollbar="false">
				<view class="goods-tabs__row">
					<view v-for="(tab, index) in categoryTabs" :key="tab.key" class="goods-tab" :class="{ 'goods-tab--active': currentTab === index }" @click="onTabClick(index)">
						<text class="goods-tab__text">{{ tab.name }}</text>
					</view>
				</view>
			</scroll-view>
			<view class="goods-list">
				<view v-if="displayList.length === 0" class="goods-empty">
					<view class="goods-empty__icon"><u-icon name="bag" color="#D7CCC8" :size="64" /></view>
					<text class="goods-empty__text">暂无商品</text>
				</view>
				<view v-for="item in displayList" :key="item.id" class="goods-card" @click="onGoodsClick(item)">
					<view class="goods-card__cover"><image class="goods-card__img" :src="item.cover || PLACEHOLDER_LOGO" mode="aspectFill" /></view>
					<view class="goods-card__body">
						<text class="goods-card__name">{{ item.name }}</text>
						<text class="goods-card__desc">{{ item.desc }}</text>
						<view class="goods-card__foot">
							<view class="goods-card__price"><text class="goods-card__price-symbol">¥</text><text class="goods-card__price-value">{{ item.price }}</text></view>
							<text class="goods-card__sales">已售 {{ item.sales }}</text>
						</view>
					</view>
				</view>
			</view>
			<text class="goods-footer">荣昌非遗 · 匠心文创</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { MOCK_GOODS_MAP } from '@/common/heritage-goods-mock.js';

const shopName = ref('荣昌夏布文创店');
const shopId = ref('1');
const categoryTabs = [
	{ key: 'all', name: '全部' },
	{ key: 'featured', name: '精选' },
	{ key: 'new', name: '新品' },
	{ key: 'hot', name: '热销' }
];
const currentTab = ref(0);
const goodsList = ref(Object.values(MOCK_GOODS_MAP).map((item, index) => ({
	id: item.id,
	name: item.name,
	desc: item.desc,
	price: item.price,
	sales: item.sales,
	category: index % 4 === 0 ? 'featured' : index % 4 === 1 ? 'hot' : index % 4 === 2 ? 'new' : 'featured',
	cover: item.cover
})));

const displayList = computed(() => {
	const key = categoryTabs[currentTab.value].key;
	if (key === 'all') return goodsList.value;
	return goodsList.value.filter(item => item.category === key);
});

const onTabClick = (index) => { currentTab.value = index; };
const onGoodsClick = (item) => { uni.navigateTo({ url: \`/mobile-design/heritage/goods-detail?id=\${item.id}\` }); };
onLoad((options) => {
	if (options?.shopName) shopName.value = decodeURIComponent(options.shopName);
	if (options?.shopId) shopId.value = options.shopId;
});
</script>`);

// transport list
patchPage('mobile-design/transport/list.vue', `<template>
	<view class="transport-list-page">
		<view class="transport-list-body">
			<view v-for="route in routeList" :key="route.id" class="transport-card ry-card" @click="openRoute(route)">
				<view class="transport-card__head">
					<text class="transport-card__name">{{ route.name }}</text>
					<text class="transport-card__status" :style="{ color: route.accentColor }">{{ route.statusLabel }}</text>
				</view>
				<text class="transport-card__line">{{ route.startStation }} → {{ route.endStation }}</text>
				<view class="transport-card__meta">
					<text>首班 {{ route.firstBus }}</text>
					<text>末班 {{ route.lastBus }}</text>
					<text>{{ route.stationCount }} 站</text>
				</view>
			</view>
			<text class="transport-footer">荣昌公共交通 · 便捷出行</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { MOCK_ROUTE_LIST } from '@/common/transport-mock.js';

const routeList = ref(MOCK_ROUTE_LIST.map(item => ({ ...item })));
const openRoute = (route) => {
	uni.navigateTo({ url: \`/mobile-design/transport/stations?routeId=\${route.id}\` });
};
</script>`);

console.log('fix-batch-remaining part A done');
