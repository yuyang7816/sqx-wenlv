const { patchPage } = require('./fix-batch-shared');

// Simplified booking confirm (hotel/scenic from query)
patchPage('mobile-design/booking/confirm.vue', `<template>
	<view class="booking-confirm-page">
		<view class="booking-confirm-body">
			<view class="booking-confirm-summary ry-card">
				<view class="booking-confirm-summary__main">
					<image class="booking-confirm-summary__cover ry-placeholder-img" :src="PLACEHOLDER_LOGO" mode="aspectFill" />
					<view class="booking-confirm-summary__info">
						<view class="booking-confirm-summary__tag" :style="{ backgroundColor: tagBgColor }">
							<text class="booking-confirm-summary__tag-text" :style="{ color: accentColor }">{{ booking.typeLabel }}</text>
						</view>
						<text class="booking-confirm-summary__title">{{ booking.title }}</text>
						<text class="booking-confirm-summary__sub">{{ booking.subtitle }}</text>
						<view class="booking-confirm-summary__price-row">
							<template v-if="booking.unitPrice === 0">
								<text class="booking-confirm-summary__price booking-confirm-summary__price--free">免费</text>
							</template>
							<template v-else>
								<text class="booking-confirm-summary__symbol">¥</text>
								<text class="booking-confirm-summary__price">{{ booking.unitPrice }}</text>
								<text class="booking-confirm-summary__unit">{{ booking.priceUnit }}</text>
							</template>
						</view>
					</view>
				</view>
			</view>
			<view class="booking-confirm-form ry-card">
				<text class="booking-confirm-form__title">预约信息</text>
				<view class="booking-confirm-form__fields">
					<view class="booking-confirm-field">
						<text class="booking-confirm-field__label">联系人</text>
						<view class="booking-confirm-field__input-wrap">
							<u-input v-model="form.contactName" placeholder="请输入联系人" border="none" :custom-style="inputStyle" />
						</view>
					</view>
					<view class="booking-confirm-field">
						<text class="booking-confirm-field__label">手机号</text>
						<view class="booking-confirm-field__input-wrap">
							<u-input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" border="none" :custom-style="inputStyle" />
						</view>
					</view>
					<view class="booking-confirm-field" @click="openDatePicker">
						<text class="booking-confirm-field__label">{{ dateFieldLabel }}</text>
						<view class="booking-confirm-field__picker">
							<text class="booking-confirm-field__picker-text" :class="{ 'booking-confirm-field__picker-text--placeholder': !form.date }">{{ form.date || datePlaceholder }}</text>
							<u-icon name="arrow-right" :size="14" color="#999999" />
						</view>
					</view>
					<view class="booking-confirm-field booking-confirm-field--count">
						<text class="booking-confirm-field__label">{{ guestCountLabel }}</text>
						<u-number-box v-model="form.guestCount" :min="1" :max="10" :color="accentColor" />
					</view>
					<view class="booking-confirm-field booking-confirm-field--textarea">
						<text class="booking-confirm-field__label">备注</text>
						<view class="booking-confirm-field__textarea-wrap">
							<u-textarea v-model="form.remark" placeholder="选填，如有特殊需求请说明" :maxlength="200" count border="none" />
						</view>
					</view>
				</view>
			</view>
		</view>
		<view class="booking-confirm-bar">
			<view class="booking-confirm-bar__price">
				<text class="booking-confirm-bar__label">合计</text>
				<text class="booking-confirm-bar__symbol">¥</text>
				<text class="booking-confirm-bar__amount">{{ totalAmount }}</text>
			</view>
			<u-button :text="submitBtnText" :color="accentColor" :custom-style="submitBtnStyle" @click="onSubmit" />
		</view>
		<u-datetime-picker v-model:show="showDatePicker" v-model="datePickerValue" mode="date" :min-date="minDate" :confirm-color="accentColor" @confirm="onDateConfirm" />
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { getCurrentDestination, MOCK_SERVICE_RECORDS, SERVICE_RECORDS_KEY } from '@/common/destination-mock.js';
import { MOCK_USER_PROFILE } from '@/common/feedback-mock.js';

const BOOKING_SUCCESS_PATH = '/mobile-design/booking/success';
const inputStyle = { padding: '0', backgroundColor: 'transparent' };
const submitBtnStyle = { width: '110px', height: '44px', borderRadius: '22px', fontSize: '15px', fontWeight: '600', margin: '0' };

const booking = ref({
	type: 'hotel', typeLabel: '酒店住宿', moduleKey: 'hotel', title: '荣昌华茂国际大酒店', subtitle: '豪华大床房 · 含早餐', unitPrice: 398, priceUnit: '/晚', dateLabel: '入住日期', datePlaceholder: '请选择入住日期'
});
const form = ref({ contactName: MOCK_USER_PROFILE.contactName, phone: MOCK_USER_PROFILE.phone, date: '', guestCount: 1, remark: '' });
const showDatePicker = ref(false);
const datePickerValue = ref(Date.now());
const minDate = Date.now();

const accentColor = computed(() => getModuleColor(booking.value.moduleKey || 'hotel'));
const tagBgColor = computed(() => \`\${accentColor.value}1A\`);
const dateFieldLabel = computed(() => booking.value.dateLabel || '预约日期');
const datePlaceholder = computed(() => booking.value.datePlaceholder || '请选择日期');
const guestCountLabel = computed(() => booking.value.type === 'hotel' ? '入住人数' : booking.value.type === 'activity' ? '参与人数' : '出行人数');
const submitBtnText = computed(() => booking.value.unitPrice === 0 ? '确认预约' : '确认支付');
const totalAmount = computed(() => Math.max(0, (Number(booking.value.unitPrice) || 0) * (booking.value.type === 'hotel' ? 1 : Number(form.value.guestCount) || 1)));

const formatDate = (timestamp) => {
	const d = new Date(timestamp);
	return \`\${d.getFullYear()}-\${String(d.getMonth()+1).padStart(2,'0')}-\${String(d.getDate()).padStart(2,'0')}\`;
};
const openDatePicker = () => { showDatePicker.value = true; };
const onDateConfirm = (e) => { form.value.date = formatDate(e.value); };

const loadBookingByParams = (options) => {
	const type = String(options?.type || 'hotel');
	const title = options?.title ? decodeURIComponent(options.title) : booking.value.title;
	const room = options?.room ? decodeURIComponent(options.room) : '';
	const subtitle = options?.subtitle ? decodeURIComponent(options.subtitle) : (room || booking.value.subtitle);
	const price = Number(options?.price || booking.value.unitPrice);
	const typeLabelMap = { hotel: '酒店住宿', scenic: '景区门票', route: '旅游线路', activity: '精彩活动' };
	booking.value = {
		type,
		typeLabel: typeLabelMap[type] || '预约服务',
		moduleKey: type === 'scenic' ? 'scenic' : type,
		title,
		subtitle,
		unitPrice: price,
		priceUnit: type === 'hotel' ? '/晚' : type === 'scenic' && price === 0 ? '' : '/人',
		dateLabel: type === 'hotel' ? '入住日期' : type === 'scenic' ? '游玩日期' : '预约日期',
		datePlaceholder: type === 'hotel' ? '请选择入住日期' : '请选择日期'
	};
	form.value.guestCount = type === 'hotel' ? 1 : 2;
};

const validateForm = () => {
	if (!form.value.contactName.trim()) { uni.showToast({ title: '请输入联系人', icon: 'none' }); return false; }
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return false; }
	if (!form.value.date) { uni.showToast({ title: datePlaceholder.value, icon: 'none' }); return false; }
	return true;
};

const onSubmit = () => {
	if (!validateForm()) return;
	const { type, id = '1', title } = booking.value;
	const query = [\`type=\${type}\`, \`id=\${id}\`, \`title=\${encodeURIComponent(title)}\`, \`date=\${encodeURIComponent(form.value.date)}\`, \`guests=\${form.value.guestCount}\`, \`amount=\${totalAmount.value}\`].join('&');
	try {
		const cached = uni.getStorageSync(SERVICE_RECORDS_KEY);
		const records = (Array.isArray(cached) && cached.length ? cached : MOCK_SERVICE_RECORDS).map(item => ({ ...item }));
		const typeMap = { hotel: '酒店服务', route: '线路预约', activity: '活动报名', scenic: '景区预约' };
		records.unshift({ id: \`RYMOCK\${Date.now()}\`, destinationId: getCurrentDestination().id, merchantId: \`\${type}-\${id}\`, merchantName: title, type: typeMap[type] || '平台服务', date: form.value.date, status: '待使用', statusColor: '#2EAF6A', source: '平台服务' });
		uni.setStorageSync(SERVICE_RECORDS_KEY, records);
	} catch (e) { /* mock */ }
	uni.navigateTo({ url: \`\${BOOKING_SUCCESS_PATH}?\${query}\` });
};

onLoad((options) => { loadBookingByParams(options); });
</script>`);

// canteen reserve
patchPage('mobile-design/canteen/reserve.vue', `<template>
	<view class="canteen-reserve-page">
		<view class="canteen-reserve-body">
			<view class="canteen-reserve-shop ry-card">
				<text class="canteen-reserve-shop__label">预约门店</text>
				<text class="canteen-reserve-shop__name">{{ shopName }}</text>
				<text v-if="dishName" class="canteen-reserve-shop__dish">预约菜品：{{ dishName }}</text>
			</view>
			<view class="canteen-reserve-form ry-card">
				<u-input v-model="form.contactName" placeholder="联系人" border="bottom" />
				<u-input v-model="form.phone" placeholder="手机号" type="number" maxlength="11" border="bottom" />
				<u-input v-model="form.guestCount" placeholder="用餐人数" type="number" border="bottom" />
				<u-input v-model="form.reserveTime" placeholder="预约时间，如 2026-08-10 12:00" border="bottom" />
				<u-input v-model="form.address" placeholder="用餐地址" border="bottom" />
			</view>
			<u-button text="提交预约" color="#FF6600" @click="onSubmit" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { MOCK_USER_PROFILE } from '@/common/feedback-mock.js';
import { CANTEEN_RECORDS_STORAGE_KEY, DEFAULT_CANTEEN_RECORDS } from '@/common/canteen-mock.js';

const shopName = ref('老字号·荣昌卤鹅旗舰店');
const dishName = ref('');
const form = ref({ contactName: MOCK_USER_PROFILE.contactName, phone: MOCK_USER_PROFILE.phone, guestCount: '2', reserveTime: '2026-08-10 12:00', address: '荣昌区昌州大道中段128号' });

onLoad((options) => {
	if (options?.shopName) shopName.value = decodeURIComponent(options.shopName);
	if (options?.dishName) dishName.value = decodeURIComponent(options.dishName);
	if (options?.shopAddress) form.value.address = decodeURIComponent(options.shopAddress);
});

function onSubmit() {
	if (!form.value.contactName.trim() || !/^1\\d{10}$/.test(form.value.phone)) {
		uni.showToast({ title: '请填写联系人和手机号', icon: 'none' }); return;
	}
	let list = [];
	try { list = uni.getStorageSync(CANTEEN_RECORDS_STORAGE_KEY) || DEFAULT_CANTEEN_RECORDS.map(i => ({ ...i })); } catch (e) { list = DEFAULT_CANTEEN_RECORDS.map(i => ({ ...i })); }
	list.unshift({ id: String(Date.now()), contactName: form.value.contactName, phone: form.value.phone, guestCount: Number(form.value.guestCount) || 2, reserveTime: form.value.reserveTime, address: form.value.address, status: 'pending', statusLabel: '待到店' });
	uni.setStorageSync(CANTEEN_RECORDS_STORAGE_KEY, list);
	uni.showToast({ title: '预约成功', icon: 'success' });
	setTimeout(() => uni.navigateTo({ url: '/mobile-design/canteen/records' }), 500);
}
</script>`);

// notice list
patchPage('mobile-design/notice/list.vue', `<template>
	<view class="notice-list-page ry-page">
		<view class="notice-list-body">
			<view v-for="item in noticeList" :key="item.id" class="notice-card ry-card" @click="openDetail(item)">
				<text class="notice-card__title">{{ item.title }}</text>
				<text class="notice-card__summary">{{ item.summary }}</text>
				<text class="notice-card__time">{{ item.time }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';

const noticeList = ref([
	{ id: 1, title: '荣昌区暑期文旅活动公告', summary: '万灵古镇夜场开放，欢迎前来体验。', time: '2026-08-01' },
	{ id: 2, title: '景区预约须知', summary: '请提前预约，携带有效证件入园。', time: '2026-07-28' },
	{ id: 3, title: '公共交通调整通知', summary: '101路末班时间延长至21:00。', time: '2026-07-20' }
]);

function openDetail(item) { uni.navigateTo({ url: \`/mobile-design/notice/detail?id=\${item.id}&title=\${encodeURIComponent(item.title)}\` }); }
</script>`);

patchPage('mobile-design/notice/detail.vue', `<template>
	<view class="notice-detail-page ry-page">
		<view class="notice-detail-body ry-card">
			<text class="notice-detail__title">{{ title }}</text>
			<text class="notice-detail__time">{{ time }}</text>
			<text class="notice-detail__content">{{ content }}</text>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const title = ref('公告详情');
const time = ref('2026-08-01');
const content = ref('荣昌区暑期文旅活动火热进行中，万灵古镇夜场开放，安陶小镇体验周同步上线。请广大游客提前预约，文明游览。');

onLoad((options) => {
	if (options?.title) title.value = decodeURIComponent(options.title);
	uni.setNavigationBarTitle({ title: title.value });
});
</script>`);

// activity list
patchPage('mobile-design/activity/list.vue', `<template>
	<view class="activity-list-page">
		<view class="activity-list-body">
			<view v-for="item in activityList" :key="item.id" class="activity-card ry-card" @click="openDetail(item)">
				<text class="activity-card__name">{{ item.name }}</text>
				<text class="activity-card__addr">{{ item.address }}</text>
				<text class="activity-card__time">{{ item.time }}</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';

const activityList = ref([
	{ id: 1, name: '荣昌非遗体验周', address: '万灵古镇', time: '8月15日-8月20日' },
	{ id: 2, name: '濑溪河亲子露营', address: '濑溪河湿地公园', time: '8月每周末' },
	{ id: 3, name: '荣昌卤鹅美食节', address: '昌州大道', time: '9月1日-9月7日' }
]);
function openDetail(item) { uni.navigateTo({ url: \`/mobile-design/activity/detail?id=\${item.id}\` }); }
</script>`);

patchPage('mobile-design/activity/detail.vue', `<template>
	<view class="activity-detail-page">
		<view class="activity-detail-body">
			<view class="activity-info ry-card">
				<text class="activity-info__name">{{ activity.name }}</text>
				<text class="activity-info__addr">{{ activity.address }}</text>
				<text class="activity-info__time">{{ activity.time }}</text>
				<text class="activity-info__desc">{{ activity.desc }}</text>
			</view>
			<u-button text="立即报名" color="#FF6600" @click="onBook" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const MAP = {
	'1': { name: '荣昌非遗体验周', address: '万灵古镇', time: '8月15日-8月20日', desc: '陶艺体验、卤鹅品鉴、夏布织造等非遗活动集中呈现。' },
	'2': { name: '濑溪河亲子露营', address: '濑溪河湿地公园', time: '8月每周末', desc: '亲子露营、户外拓展，适合家庭周末休闲。' },
	'3': { name: '荣昌卤鹅美食节', address: '昌州大道', time: '9月1日-9月7日', desc: '品鉴地道荣昌卤鹅，感受非遗美食文化。' }
};
const activity = ref({ ...MAP['1'] });
onLoad((options) => { activity.value = { ...(MAP[String(options?.id || '1')] || MAP['1']) }; uni.setNavigationBarTitle({ title: activity.value.name }); });
function onBook() { uni.navigateTo({ url: \`/mobile-design/booking/confirm?type=activity&id=\${activity.value.id}&title=\${encodeURIComponent(activity.value.name)}&price=0\` }); }
</script>`);

// route list
patchPage('mobile-design/route/list.vue', `<template>
	<view class="route-list-page">
		<view class="route-list-body">
			<view v-for="item in routeList" :key="item.id" class="route-card ry-card" @click="openDetail(item)">
				<text class="route-card__name">{{ item.name }}</text>
				<text class="route-card__desc">{{ item.desc }}</text>
				<text class="route-card__price">¥{{ item.priceFrom }}起</text>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
const routeList = ref([
	{ id: 1, name: '荣昌一日游·古镇非遗', desc: '万灵古镇 + 安陶小镇 + 卤鹅品鉴', priceFrom: 99 },
	{ id: 2, name: '美食文化半日游', desc: '荣昌卤鹅 + 夏布小镇', priceFrom: 68 },
	{ id: 3, name: '濑溪河生态游', desc: '湿地公园 + 骑行 + 摄影', priceFrom: 128 }
]);
function openDetail(item) { uni.navigateTo({ url: \`/mobile-design/route/detail?id=\${item.id}\` }); }
</script>`);

patchPage('mobile-design/route/detail.vue', `<template>
	<view class="route-detail-page">
		<view class="route-detail-body">
			<view class="route-info ry-card">
				<text class="route-info__name">{{ route.name }}</text>
				<text class="route-info__desc">{{ route.desc }}</text>
				<text class="route-info__duration">行程时长：{{ route.duration }}</text>
			</view>
			<u-button text="预约线路" color="#FF6600" @click="onBook" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
const MAP = {
	'1': { name: '荣昌一日游·古镇非遗', desc: '万灵古镇 + 安陶小镇 + 卤鹅品鉴', duration: '1天', price: 99 },
	'2': { name: '美食文化半日游', desc: '荣昌卤鹅 + 夏布小镇', duration: '半天', price: 68 },
	'3': { name: '濑溪河生态游', desc: '湿地公园 + 骑行 + 摄影', duration: '1天', price: 128 }
};
const route = ref({ ...MAP['1'] });
onLoad((options) => { route.value = { ...(MAP[String(options?.id || '1')] || MAP['1']) }; uni.setNavigationBarTitle({ title: route.value.name }); });
function onBook() { uni.navigateTo({ url: \`/mobile-design/booking/confirm?type=route&id=\${route.value.id || 1}&title=\${encodeURIComponent(route.value.name)}&price=\${route.value.price}\` }); }
</script>`);

// simple utility pages
const simplePages = [
	['mobile-design/map/index.vue', '地图导航', '荣昌文旅地图', '查看景区、酒店、餐饮等 POI 位置（Mock）'],
	['mobile-design/food-map/index.vue', '美食地图', '荣昌美食地图', '探索荣昌卤鹅与本地特色餐饮（Mock）'],
	['mobile-design/parking/index.vue', '停车通行', '景区停车信息', '万灵古镇、安陶小镇等停车场余位（Mock）'],
	['mobile-design/store/recommend.vue', '推荐门店', '精选推荐', '平台精选文旅商户与体验门店（Mock）']
];
for (const [rel, navTitle, title, desc] of simplePages) {
	patchPage(rel, `<template><view class="utility-page ry-page"><view class="utility-body ry-card"><text class="utility-title">${title}</text><text class="utility-desc">${desc}</text></view></view></template>`, `<script setup>import { onLoad } from '@dcloudio/uni-app'; onLoad(() => { uni.setNavigationBarTitle({ title: '${navTitle}' }); });</script>`);
}

console.log('fix-batch-remaining-c done');
