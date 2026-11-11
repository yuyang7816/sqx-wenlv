const { patchPage, MSG_MOCK_MAP } = require('./fix-batch-shared');

// --- destination ---
patchPage('pages/destination/select.vue', `<template>
	<view class="destination-page ry-page">
		<view class="destination-body">
			<view class="destination-current ry-card">
				<text class="destination-eyebrow">当前地区</text>
				<text class="destination-current__name">{{ current.name }}</text>
				<text class="destination-current__intro">{{ current.intro }}</text>
			</view>
			<text class="destination-title">切换地区</text>
			<view class="destination-list">
				<view v-for="item in destinations" :key="item.id" class="destination-item ry-card" @click="selectDestination(item)">
					<view class="destination-item__main">
						<text class="destination-item__name">{{ item.name }}</text>
						<text class="destination-item__intro">{{ item.intro }}</text>
						<text class="destination-item__meta">{{ item.resourceCount }} 项文旅资源 · {{ item.merchantCount }} 家商户</text>
					</view>
					<view class="destination-item__right">
						<text class="destination-item__status" :class="{ online: item.status === '已上线' }">{{ item.status }}</text>
						<u-icon :name="current.id === item.id ? 'checkmark' : 'arrow-right'" :color="current.id === item.id ? '#FF6600' : '#CCCCCC'" :size="18" />
					</view>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { DESTINATIONS, getCurrentDestination, setCurrentDestination } from '@/common/destination-mock.js';

const destinations = DESTINATIONS;
const current = ref(getCurrentDestination());

onShow(() => { current.value = getCurrentDestination(); });

function selectDestination(item) {
	if (item.status !== '已上线') {
		uni.showToast({ title: \`\${item.name}即将上线\`, icon: 'none' });
		return;
	}
	current.value = setCurrentDestination(item.id);
	uni.showToast({ title: \`已切换至\${item.name}\`, icon: 'success' });
	setTimeout(() => uni.navigateBack(), 250);
}
</script>`);

// --- order ---
patchPage('pages/order/list.vue', `<template>
	<view class="order-page ry-page">
		<view class="order-body">
			<view class="order-tip">
				<u-icon name="info-circle" color="#FF6600" :size="16" />
				<text>平台仅展示服务记录，实际履约由对应商户负责，如有疑问请联系商户或平台客服。</text>
			</view>
			<view class="order-tabs">
				<view v-for="(tab, i) in tabs" :key="tab" class="order-tab" :class="{ active: currentTab === i }" @click="currentTab = i">{{ tab }}</view>
			</view>
			<view v-if="filteredRecords.length" class="order-list">
				<view v-for="record in filteredRecords" :key="record.id" class="order-card ry-card" @click="openDetail(record)">
					<view class="order-card__head">
						<text class="order-card__type">{{ record.type }}</text>
						<text :style="{ color: record.statusColor }">{{ record.status }}</text>
					</view>
					<text class="order-card__merchant">{{ record.merchantName }}</text>
					<view class="order-card__row"><text>地区</text><text>{{ destinationName(record.destinationId) }}</text></view>
					<view class="order-card__row"><text>服务时间</text><text>{{ record.date }}</text></view>
					<view class="order-card__foot"><text>{{ record.source }}</text><u-icon name="arrow-right" color="#CCC" :size="14" /></view>
				</view>
			</view>
			<view v-else class="order-empty">
				<u-empty mode="list" text="暂无服务记录" />
				<u-button text="去首页" color="#FF6600" @click="goHome" />
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { computed, ref } from 'vue';
import { MOCK_SERVICE_RECORDS, DESTINATIONS, SERVICE_RECORDS_KEY } from '@/common/destination-mock.js';

const tabs = ['全部', '待处理', '待使用', '已完成'];
const currentTab = ref(0);
const records = ref([]);

function loadRecords() {
	const cached = uni.getStorageSync(SERVICE_RECORDS_KEY);
	records.value = (Array.isArray(cached) && cached.length ? cached : MOCK_SERVICE_RECORDS).map(item => ({ ...item }));
	if (!cached) uni.setStorageSync(SERVICE_RECORDS_KEY, records.value);
}
loadRecords();

const filteredRecords = computed(() => {
	const tab = tabs[currentTab.value];
	if (tab === '全部') return records.value;
	if (tab === '待处理') return records.value.filter(item => item.status === '商户处理中' || item.status === '待到店');
	if (tab === '待使用') return records.value.filter(item => item.status === '待使用');
	return records.value.filter(item => item.status === '已完成' || item.status === '已取消');
});

function destinationName(id) { return DESTINATIONS.find(item => item.id === id)?.name || '未知地区'; }
function openDetail(record) { uni.navigateTo({ url: \`/pages/order/detail?id=\${record.id}\` }); }
function goHome() { uni.switchTab({ url: '/pages/index/index' }); }
</script>`);

patchPage('pages/order/detail.vue', `<template>
	<view class="order-detail-page ry-page">
		<view class="order-detail-body">
			<view class="status-panel ry-card">
				<text class="status-panel__label">{{ record.status }}</text>
				<text>服务单号：{{ record.id }}</text>
			</view>
			<view class="detail-card ry-card">
				<text class="detail-title">服务信息</text>
				<view class="detail-row"><text>地区</text><text>{{ destinationName }}</text></view>
				<view class="detail-row"><text>服务类型</text><text>{{ record.type }}</text></view>
				<view class="detail-row"><text>商户/资源</text><text>{{ record.merchantName }}</text></view>
				<view class="detail-row"><text>服务时间</text><text>{{ record.date }}</text></view>
				<view class="detail-row"><text>来源说明</text><text>{{ record.source === '平台服务' ? '平台官方服务记录' : '商户自营服务' }}</text></view>
			</view>
			<view class="detail-card ry-card">
				<text class="detail-title">下一步操作</text>
				<text class="detail-desc">{{ actionText }}</text>
				<u-button v-if="record.source === '商户服务'" text="联系商户" plain color="#FF6600" @click="contactMerchant" />
				<u-button text="查看地图" color="#FF6600" @click="openMap" />
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { MOCK_SERVICE_RECORDS, DESTINATIONS, SERVICE_RECORDS_KEY } from '@/common/destination-mock.js';

const record = ref({ ...MOCK_SERVICE_RECORDS[0] });
const destinationName = computed(() => DESTINATIONS.find(item => item.id === record.value.destinationId)?.name || '未知地区');
const actionText = computed(() => record.value.source === '商户服务'
	? '请按预约时间前往商户，如需改期请提前联系商户处理。平台协助跟进服务进度。'
	: '请按预约时间前往景区/场馆，携带有效证件按时入园或使用服务。');

onLoad(options => {
	const cached = uni.getStorageSync(SERVICE_RECORDS_KEY) || MOCK_SERVICE_RECORDS;
	const found = cached.find(item => item.id === String(options?.id));
	if (found) record.value = { ...found };
});

function contactMerchant() { uni.showToast({ title: '请拨打商户电话（Mock）', icon: 'none' }); }
function openMap() { uni.navigateTo({ url: \`/mobile-design/map/index?destinationId=\${record.value.destinationId}\` }); }
</script>`);

// --- msg detail ---
const msgMapStr = JSON.stringify(MSG_MOCK_MAP, null, '\t').replace(/"/g, "'").replace(/'/g, "'");

patchPage('pages/msg/detail.vue', `<template>
	<view class="msg-detail-page ry-page">
		<view class="msg-detail-body">
			<view class="msg-header">
				<view class="msg-header__icon-wrap" :class="\`msg-header__icon-wrap--\${message.category}\`">
					<u-icon :name="message.category === 'system' ? 'bell-fill' : 'calendar-fill'" color="#FFFFFF" :size="36" />
				</view>
				<view class="msg-header__tag" :class="\`msg-header__tag--\${message.category}\`">
					<text class="msg-header__tag-text">{{ message.categoryLabel }}</text>
				</view>
			</view>
			<view class="msg-content ry-card">
				<view class="msg-content__head">
					<text class="msg-content__title">{{ message.title }}</text>
					<view class="msg-content__meta">
						<u-icon name="clock" color="#CCCCCC" :size="14" />
						<text class="msg-content__time">{{ message.time }}</text>
					</view>
				</view>
				<view class="msg-content__divider"></view>
				<text class="msg-content__text">{{ message.content }}</text>
				<view v-if="message.linkUrl" class="msg-content__action">
					<view class="msg-action-btn" @click="onActionClick">
						<text class="msg-action-btn__text">{{ message.linkText }}</text>
						<u-icon name="arrow-right" color="#FF6600" :size="16" />
					</view>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const MOCK_MSG_MAP = ${JSON.stringify(MSG_MOCK_MAP, null, '\t')};

const message = ref({ ...MOCK_MSG_MAP['1'] });

onLoad((options) => {
	const key = String(options?.id || '1');
	message.value = { ...(MOCK_MSG_MAP[key] || MOCK_MSG_MAP['1']) };
	if (options?.title) message.value.title = decodeURIComponent(options.title);
	if (options?.time) message.value.time = decodeURIComponent(options.time);
});

function onActionClick() {
	if (!message.value.linkUrl) return;
	uni.navigateTo({ url: message.value.linkUrl });
}
</script>`);

// --- merchant service ---
patchPage('pages/merchant/service.vue', `<template>
	<view class="merchant-service-page ry-page">
		<view class="service-body">
			<view class="service-head ry-card">
				<text class="service-label">当前商户</text>
				<text class="service-name">{{ merchant.name }}</text>
				<text class="service-address">{{ merchant.address }}</text>
			</view>
			<view class="service-card ry-card">
				<text class="service-title">服务说明</text>
				<text class="service-desc">商户自行管理预约、库存与履约。提交后平台将记录服务意向并同步至商户，具体规则以商户说明为准。</text>
				<view class="service-row"><text>所属地区</text><text>{{ destination.name }}</text></view>
				<view class="service-row"><text>商户类型</text><text>{{ merchant.type }}</text></view>
			</view>
			<view class="service-card ry-card">
				<text class="service-title">联系信息</text>
				<u-input v-model="name" placeholder="请输入联系人" border="bottom" />
				<u-input v-model="phone" placeholder="请输入手机号" type="number" maxlength="11" border="bottom" />
				<u-button text="提交商户服务（Mock）" color="#FF6600" @click="submit" />
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { DESTINATION_MERCHANTS, getCurrentDestination, MOCK_SERVICE_RECORDS, SERVICE_RECORDS_KEY } from '@/common/destination-mock.js';

const destination = ref(getCurrentDestination());
const merchant = ref(DESTINATION_MERCHANTS.rongchang[0]);
const name = ref('');
const phone = ref('');

onLoad(options => {
	const list = DESTINATION_MERCHANTS[destination.value.id] || [];
	merchant.value = list.find(item => item.id === options?.merchantId) || merchant.value;
});

function submit() {
	if (!name.value.trim() || !/^1\\d{10}$/.test(phone.value)) {
		uni.showToast({ title: '请填写联系人和手机号', icon: 'none' });
		return;
	}
	const cached = uni.getStorageSync(SERVICE_RECORDS_KEY);
	const records = (Array.isArray(cached) && cached.length ? cached : MOCK_SERVICE_RECORDS).map(item => ({ ...item }));
	records.unshift({
		id: \`RYMOCK\${Date.now()}\`,
		destinationId: destination.value.id,
		merchantId: merchant.value.id,
		merchantName: merchant.value.name,
		type: \`\${merchant.value.type}服务\`,
		date: '商户确认中',
		status: '商户处理中',
		statusColor: '#2979FF',
		source: '商户服务',
		contactName: name.value,
		phone: phone.value
	});
	uni.setStorageSync(SERVICE_RECORDS_KEY, records);
	uni.showToast({ title: '已提交，等待商户确认', icon: 'success' });
	setTimeout(() => uni.navigateTo({ url: '/pages/order/list' }), 400);
}
</script>`);

console.log('fix-batch-part1: destination, order, msg/detail, merchant done');
