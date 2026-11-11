const { patchPage } = require('./fix-batch-shared');

patchPage('mobile-design/heritage/catalog.vue', `<template>
	<view class="heritage-catalog-page">
		<view class="heritage-catalog-body">
			<view v-for="craft in craftList" :key="craft.id" class="catalog-card ry-card" @click="openCraft(craft)">
				<text class="catalog-card__name">{{ craft.name }}</text>
				<text class="catalog-card__level">{{ craft.levelLabel }}</text>
				<text class="catalog-card__desc">{{ craft.desc }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { getCraftList } from '@/common/heritage-craft-mock.js';
const craftList = ref(getCraftList());
function openCraft(craft) { uni.navigateTo({ url: \`/mobile-design/heritage/craft-intro?id=\${craft.id}\` }); }
</script>`);

patchPage('mobile-design/heritage/shop-list.vue', `<template>
	<view class="shop-list-page">
		<view class="shop-list-body">
			<view v-for="shop in shopList" :key="shop.id" class="shop-card ry-card" @click="openShop(shop)">
				<text class="shop-card__name">{{ shop.name }}</text>
				<text class="shop-card__addr">{{ shop.address }}</text>
				<text class="shop-card__tag">{{ shop.tag }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
const shopList = ref([
	{ id: 1, name: '安陶非遗体验店', address: '安富街道安陶路88号', tag: '陶艺体验' },
	{ id: 2, name: '荣昌夏布文创店', address: '昌州大道128号', tag: '夏布织造' },
	{ id: 3, name: '万灵古镇非遗馆', address: '万灵镇滨河路56号', tag: '古镇文化' }
]);
function openShop(shop) { uni.navigateTo({ url: \`/mobile-design/heritage/shop-detail?id=\${shop.id}\` }); }
</script>`);

patchPage('mobile-design/heritage/shop-detail.vue', `<template>
	<view class="shop-detail-page">
		<view class="shop-detail-body">
			<view class="shop-info ry-card">
				<text class="shop-info__name">{{ shop.name }}</text>
				<text class="shop-info__addr">{{ shop.address }}</text>
				<text class="shop-info__tag">{{ shop.tag }}</text>
				<text class="shop-info__intro">{{ shop.intro }}</text>
			</view>
			<u-button text="查看商品" color="#FF6600" @click="goGoods" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
const MAP = {
	'1': { name: '安陶非遗体验店', address: '安富街道安陶路88号', tag: '陶艺体验', intro: '提供制陶体验、文创销售与非遗展示。' },
	'2': { name: '荣昌夏布文创店', address: '昌州大道128号', tag: '夏布织造', intro: '夏布手提包、茶巾、挂画等文创产品。' },
	'3': { name: '万灵古镇非遗馆', address: '万灵镇滨河路56号', tag: '古镇文化', intro: '古镇非遗文化展示与体验。' }
};
const shop = ref({ ...MAP['1'] });
onLoad((options) => { shop.value = { ...(MAP[String(options?.id || '1')] || MAP['1']) }; uni.setNavigationBarTitle({ title: shop.value.name }); });
function goGoods() { uni.navigateTo({ url: \`/mobile-design/heritage/goods-list?shopId=\${shop.value.id}&shopName=\${encodeURIComponent(shop.value.name)}\` }); }
</script>`);

patchPage('mobile-design/heritage/address-list.vue', `<template>
	<view class="address-list-page">
		<view class="address-list-body">
			<view v-for="item in addressList" :key="item.id" class="address-card ry-card" @click="selectAddress(item)">
				<text class="address-card__name">{{ item.name }} {{ item.phone }}</text>
				<text class="address-card__addr">{{ item.region }}{{ item.detail }}</text>
				<text v-if="item.isDefault" class="address-card__default">默认</text>
			</view>
			<u-button text="新增地址" color="#FF6600" @click="goAdd" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getAddressList, setSelectedAddressId, formatAddress } from '@/common/address-mock.js';

const addressList = ref([]);
onShow(() => { addressList.value = getAddressList(); });
function selectAddress(item) { setSelectedAddressId(item.id); uni.showToast({ title: formatAddress(item), icon: 'none' }); setTimeout(() => uni.navigateBack(), 300); }
function goAdd() { uni.navigateTo({ url: '/mobile-design/heritage/address-form' }); }
</script>`);

patchPage('mobile-design/heritage/address-form.vue', `<template>
	<view class="address-form-page">
		<view class="address-form-body ry-card">
			<u-input v-model="form.name" placeholder="收货人" border="bottom" />
			<u-input v-model="form.phone" placeholder="手机号" type="number" maxlength="11" border="bottom" />
			<u-input v-model="form.region" placeholder="省市区" border="bottom" />
			<u-input v-model="form.detail" placeholder="详细地址" border="bottom" />
			<u-button text="保存地址" color="#FF6600" @click="onSave" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { addAddress } from '@/common/address-mock.js';

const form = ref({ name: '', phone: '', region: '重庆市 荣昌区', detail: '', isDefault: false });
function onSave() {
	if (!form.value.name.trim() || !/^1\\d{10}$/.test(form.value.phone) || !form.value.detail.trim()) {
		uni.showToast({ title: '请填写完整地址信息', icon: 'none' }); return;
	}
	addAddress(form.value);
	uni.showToast({ title: '保存成功', icon: 'success' });
	setTimeout(() => uni.navigateBack(), 500);
}
</script>`);

patchPage('mobile-design/heritage/order-confirm.vue', `<template>
	<view class="order-confirm-page">
		<view class="order-confirm-body">
			<view class="order-goods ry-card">
				<text class="order-goods__name">{{ goods.name }}</text>
				<text class="order-goods__price">¥{{ goods.price }}</text>
			</view>
			<view class="order-addr ry-card" @click="goAddress">
				<text class="order-addr__label">收货地址</text>
				<text class="order-addr__value">{{ addressText || '请选择收货地址' }}</text>
			</view>
			<u-button text="提交订单" color="#FF6600" @click="onSubmit" />
		</view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getGoodsById } from '@/common/heritage-goods-mock.js';
import { getSelectedAddress, formatAddress } from '@/common/address-mock.js';

const goods = ref({ ...getGoodsById('1') });
const address = ref(null);
const addressText = computed(() => address.value ? formatAddress(address.value) : '');

onLoad((options) => { goods.value = { ...getGoodsById(options?.id || '1') }; });
onShow(() => { address.value = getSelectedAddress(); });

function goAddress() { uni.navigateTo({ url: '/mobile-design/heritage/address-list' }); }
function onSubmit() {
	if (!address.value) { uni.showToast({ title: '请选择收货地址', icon: 'none' }); return; }
	uni.showToast({ title: '订单提交成功（Mock）', icon: 'success' });
	setTimeout(() => uni.navigateBack(), 800);
}
</script>`);

patchPage('mobile-design/heritage/review-list.vue', `<template>
	<view class="review-list-page">
		<view class="review-list-body">
			<view v-for="item in reviews" :key="item.id" class="review-card ry-card">
				<text class="review-card__user">{{ item.user }}</text>
				<text class="review-card__content">{{ item.content }}</text>
				<text class="review-card__time">{{ item.time }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const reviews = ref([
	{ id: 1, user: '游客A', content: '夏布手提包质感很好，非遗工艺值得支持。', time: '2026-07-20' },
	{ id: 2, user: '游客B', content: '包装精美，送货及时，推荐购买。', time: '2026-07-15' }
]);
onLoad((options) => {
	if (options?.title) uni.setNavigationBarTitle({ title: decodeURIComponent(options.title) + '·评价' });
});
</script>`);

patchPage('mobile-design/heritage/detail.vue', `<template>
	<view class="heritage-detail-page">
		<view class="heritage-detail-body">
			<view class="heritage-detail-info ry-card">
				<text class="heritage-detail-info__name">{{ detail.name }}</text>
				<text class="heritage-detail-info__desc">{{ detail.desc }}</text>
			</view>
			<u-button text="了解更多" color="#FF6600" @click="goIntro" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getCraftById } from '@/common/heritage-craft-mock.js';

const detail = ref({ name: '荣昌非遗', desc: '千年传承，活态保护' });
onLoad((options) => {
	const craft = getCraftById(options?.id || '1');
	detail.value = { name: craft.name, desc: craft.overview };
	uni.setNavigationBarTitle({ title: craft.name });
});
function goIntro() { uni.navigateTo({ url: '/mobile-design/heritage/craft-intro?id=1' }); }
</script>`);

patchPage('mobile-design/canteen/detail.vue', `<template>
	<view class="canteen-detail-page">
		<view class="canteen-detail-body">
			<view class="canteen-detail-card ry-card">
				<text class="canteen-detail-card__name">{{ record.contactName }}</text>
				<text class="canteen-detail-card__status">{{ record.statusLabel }}</text>
				<view class="canteen-detail-row"><text>预约时间</text><text>{{ record.reserveTime }}</text></view>
				<view class="canteen-detail-row"><text>联系电话</text><text>{{ record.phone }}</text></view>
				<view class="canteen-detail-row"><text>用餐地址</text><text>{{ record.address }}</text></view>
				<view class="canteen-detail-row"><text>用餐人数</text><text>{{ record.guestCount }} 人</text></view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getCanteenRecordById } from '@/common/canteen-mock.js';

const record = ref(getCanteenRecordById('1') || {});
onLoad((options) => {
	const found = getCanteenRecordById(options?.id);
	if (found) record.value = found;
	uni.setNavigationBarTitle({ title: '预约详情' });
});
</script>`);

patchPage('mobile-design/transport/stations.vue', `<template>
	<view class="transport-stations-page">
		<view class="transport-stations-body">
			<view class="route-head ry-card">
				<text class="route-head__name">{{ route.name }}</text>
				<text class="route-head__line">{{ route.startStation }} → {{ route.endStation }}</text>
			</view>
			<view v-for="(station, index) in route.stations" :key="station" class="station-row ry-card">
				<text class="station-row__index">{{ index + 1 }}</text>
				<text class="station-row__name">{{ station }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getRouteById } from '@/common/transport-mock.js';

const route = ref({ ...getRouteById('101') });
onLoad((options) => {
	route.value = { ...getRouteById(options?.routeId || '101') };
	uni.setNavigationBarTitle({ title: route.value.name + '站点' });
});
</script>`);

console.log('fix-batch-remaining-d done');
