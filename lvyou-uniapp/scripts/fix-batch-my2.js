const { patchPage } = require('./fix-batch-shared');

const inputStyle = `{ padding: '0', height: '100rpx', fontSize: '30rpx', color: '#2B2B33' }`;

patchPage('pages/my/forget.vue', `<template>
	<view class="forget-page">
		<view class="forget-back" @click="onBack"><u-icon name="arrow-left" color="#5A4A3A" :size="22" /></view>
		<view class="forget-orb forget-orb--one"></view>
		<view class="forget-orb forget-orb--two"></view>
		<view class="forget-head">
			<view class="forget-head__icon"><u-icon name="lock-open" color="#FF6600" :size="40" /></view>
			<text class="forget-head__title">找回密码</text>
			<text class="forget-head__sub">验证手机号后设置新密码</text>
		</view>
		<view class="forget-panel">
			<view class="forget-step">
				<view class="forget-step__item forget-step__item--active"><text class="forget-step__num">1</text><text class="forget-step__label">验证手机</text></view>
				<view class="forget-step__line"></view>
				<view class="forget-step__item forget-step__item--active"><text class="forget-step__num">2</text><text class="forget-step__label">重置密码</text></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.phone" type="number" maxlength="11" placeholder="请输入注册手机号" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.code" type="number" maxlength="6" placeholder="请输入验证码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
				<view class="forget-code" :class="{ 'forget-code--disabled': countDown > 0 }" @click="sendCode"><text class="forget-code__text">{{ codeText }}</text></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.password" :password="true" maxlength="20" placeholder="新密码（6-20位）" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.confirm" :password="true" maxlength="20" placeholder="请再次输入密码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-submit" @click="onSubmit">
				<text class="forget-submit__text">确认重置</text>
				<view class="forget-submit__arrow"><u-icon name="arrow-right" color="#FFFFFF" :size="18" /></view>
			</view>
			<view class="forget-back2login" @click="onBack">
				<text class="forget-back2login__text">想起密码了？</text>
				<text class="forget-back2login__link">返回登录</text>
			</view>
		</view>
		<view class="forget-foot"><text class="forget-foot__tip">演示 Mock 找回，验证码固定 123456</text></view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { findAccount, updateAccountPassword } from '@/common/destination-mock.js';

const form = ref({ phone: '', code: '', password: '', confirm: '' });
const countDown = ref(0);
const inputStyle = ${inputStyle};
const codeText = computed(() => countDown.value > 0 ? \`\${countDown.value}s 后重发\` : '获取验证码');

onLoad((options) => { if (options?.phone) form.value.phone = String(options.phone); });
function onBack() { uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/my/my' }) }); }
function sendCode() {
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (!findAccount(form.value.phone)) { uni.showToast({ title: '该手机号未注册', icon: 'none' }); return; }
	if (countDown.value > 0) return;
	form.value.code = '123456';
	countDown.value = 60;
	const timer = setInterval(() => { countDown.value -= 1; if (countDown.value <= 0) { clearInterval(timer); countDown.value = 0; } }, 1000);
	uni.showToast({ title: '验证码已发送：123456', icon: 'none' });
}
function onSubmit() {
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (!findAccount(form.value.phone)) { uni.showToast({ title: '该手机号未注册', icon: 'none' }); return; }
	if (!/^\\d{6}$/.test(form.value.code)) { uni.showToast({ title: '请输入6位验证码', icon: 'none' }); return; }
	if (String(form.value.password).length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return; }
	if (String(form.value.password) !== String(form.value.confirm)) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return; }
	updateAccountPassword(form.value.phone, form.value.password);
	uni.showToast({ title: '密码重置成功', icon: 'success' });
	setTimeout(() => uni.navigateBack(), 400);
}
</script>`);

patchPage('pages/my/settings.vue', `<template>
	<view class="settings-page ry-page">
		<view class="settings-body">
			<view class="settings-group ry-card">
				<view class="settings-row" @click="onDestination">
					<text class="settings-row__label">当前地区</text>
					<view class="settings-row__value"><text class="settings-row__text">{{ currentDestination.name }}</text><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
				<view class="settings-row" @click="onMessageSwitch">
					<text class="settings-row__label">消息推送</text>
					<view class="settings-row__value"><u-switch v-model="messagePush" active-color="#FF6600" size="18" /></view>
				</view>
				<view class="settings-row" @click="onNoticeSwitch">
					<text class="settings-row__label">活动提醒</text>
					<view class="settings-row__value"><u-switch v-model="activityNotice" active-color="#FF6600" size="18" /></view>
				</view>
				<view class="settings-row" @click="onAbout">
					<text class="settings-row__label">关于荣e游</text>
					<view class="settings-row__value"><text class="settings-row__text">版本 1.0.0</text><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
			</view>
			<u-button v-if="loggedIn" text="退出登录" block plain color="#FF6600" :custom-style="logoutStyle" @click="onLogout" />
			<u-button v-else text="去登录" block color="#FF6600" :custom-style="logoutStyle" @click="onLogin" />
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { BRAND_COLOR } from '@/common/theme.js';
import { getCurrentDestination, isLoggedIn, setLoggedIn } from '@/common/destination-mock.js';

const currentDestination = ref(getCurrentDestination());
const loggedIn = ref(isLoggedIn());
const messagePush = ref(true);
const activityNotice = ref(true);
const logoutStyle = { height: '44px', borderRadius: '22px', marginTop: '40rpx' };

onShow(() => { currentDestination.value = getCurrentDestination(); loggedIn.value = isLoggedIn(); });
function onDestination() { uni.navigateTo({ url: '/pages/destination/select' }); }
function onMessageSwitch() { uni.showToast({ title: messagePush.value ? '已开启消息推送' : '已关闭消息推送', icon: 'none' }); }
function onNoticeSwitch() { uni.showToast({ title: activityNotice.value ? '已开启活动提醒' : '已关闭活动提醒', icon: 'none' }); }
function onAbout() {
	uni.showModal({ title: '关于荣e游', content: '荣e游是面向多地区的文旅信息与服务平台，提供景区、酒店、卤鹅美食、活动预约与出行服务。\\n\\n版本：1.0.0', showCancel: false, confirmText: '知道了', confirmColor: BRAND_COLOR });
}
function onLogout() {
	uni.showModal({ title: '退出登录', content: '确定要退出当前账号吗？', confirmColor: '#FF6600', success: (res) => {
		if (!res.confirm) return;
		setLoggedIn(false);
		loggedIn.value = false;
		uni.showToast({ title: '已退出登录', icon: 'none' });
	}});
}
function onLogin() { uni.navigateTo({ url: '/pages/my/login' }); }
</script>`);

patchPage('pages/my/asset.vue', `<template>
	<view class="asset-page ry-page">
		<view class="asset-tabs">
			<view v-for="(tab, i) in tabs" :key="tab.key" class="asset-tab" :class="{ 'asset-tab--active': currentTab === i }" @click="onTabChange(i)"><text>{{ tab.name }}</text></view>
		</view>
		<view v-if="currentTab === 0" class="asset-list">
			<view v-if="favorites.length === 0" class="asset-empty">
				<u-empty mode="list" text="暂无收藏" />
				<u-button text="去逛逛" color="#FF6600" plain @click="goIndex" />
			</view>
			<view v-for="item in favorites" :key="item.moduleType + '-' + item.id" class="asset-card ry-card" @click="openItem(item)">
				<text class="asset-card__name">{{ item.name }}</text>
				<text class="asset-card__address">{{ item.address }}</text>
				<view class="asset-card__foot">
					<text class="asset-card__type">{{ moduleLabel(item.moduleType) }}</text>
					<u-button text="查看" size="small" plain color="#FF6600" :custom-style="btnStyle" @click.stop="openItem(item)" />
				</view>
			</view>
		</view>
		<view v-else-if="currentTab === 1" class="asset-list">
			<view v-if="footprints.length === 0" class="asset-empty">
				<u-empty mode="list" text="暂无足迹" />
				<u-button text="去探索" color="#FF6600" plain @click="goIndex" />
			</view>
			<view v-for="item in footprints" :key="item.moduleType + '-' + item.id" class="asset-card ry-card" @click="openItem(item)">
				<text class="asset-card__name">{{ item.name }}</text>
				<text class="asset-card__address">{{ item.address }}</text>
				<view class="asset-card__foot">
					<text class="asset-card__type">{{ moduleLabel(item.moduleType) }}</text>
					<u-button text="再看" size="small" plain color="#FF6600" :custom-style="btnStyle" @click.stop="openItem(item)" />
				</view>
			</view>
		</view>
		<view v-else class="asset-list">
			<view v-if="coupons.length === 0" class="asset-empty">
				<u-empty mode="list" text="暂无可用优惠券" />
				<text class="asset-empty__tip">参与活动或关注公告，领取更多优惠</text>
			</view>
			<view v-for="c in coupons" :key="c.id" class="asset-coupon ry-card">
				<view class="asset-coupon__left">
					<text class="asset-coupon__amount">¥{{ c.amount }}</text>
					<text class="asset-coupon__limit">{{ c.limit }}</text>
				</view>
				<view class="asset-coupon__right">
					<text class="asset-coupon__name">{{ c.name }}</text>
					<text class="asset-coupon__time">{{ c.time }}</text>
					<text class="asset-coupon__status">{{ c.status }}</text>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { getFavorites, getFootprints } from '@/common/destination-mock.js';

const tabs = [{ name: '收藏', key: 'favorite' }, { name: '足迹', key: 'footprint' }, { name: '优惠券', key: 'coupon' }];
const currentTab = ref(0);
const favorites = ref([]);
const footprints = ref([]);
const coupons = ref([]);
const btnStyle = { height: '28px', fontSize: '22rpx', padding: '0 20rpx', margin: '0' };
const MODULE_LABEL = { scenic: '景区', hotel: '酒店', goose: '餐饮', activity: '活动', route: '线路', heritage: '非遗' };

function load() {
	favorites.value = getFavorites();
	footprints.value = getFootprints();
	coupons.value = [
		{ id: 1, amount: 20, limit: '满100可用', name: '新人专享券', time: '有效期至 2026-08-31', status: '未使用' },
		{ id: 2, amount: 10, limit: '满50可用', name: '卤鹅美食券', time: '有效期至 2026-09-30', status: '未使用' }
	];
}
function onTabChange(i) { currentTab.value = i; }
function moduleLabel(key) { return MODULE_LABEL[key] || '其他'; }
function openItem(item) {
	if (item.path) { uni.navigateTo({ url: item.path }); return; }
	uni.showToast({ title: '暂无法打开', icon: 'none' });
}
function goIndex() { uni.switchTab({ url: '/pages/index/index' }); }
onLoad((options) => {
	const map = { favorite: 0, footprint: 1, coupon: 2 };
	if (options?.tab && map[options.tab] !== undefined) currentTab.value = map[options.tab];
});
onShow(load);
</script>`);

patchPage('pages/my/service.vue', `<template>
	<view class="service-page ry-page">
		<view class="service-body">
			<view class="service-hero ry-card">
				<u-icon name="headphones-fill" color="#FF6600" :size="40" />
				<text class="service-hero__title">荣e游客服</text>
				<text class="service-hero__desc">服务时间 09:00 - 21:00，节假日照常服务</text>
			</view>
			<view class="service-card ry-card" @click="onCall">
				<u-icon name="phone-fill" color="#2EAF6A" :size="22" />
				<view class="service-card__info">
					<text class="service-card__title">电话咨询</text>
					<text class="service-card__desc">023-46789012</text>
				</view>
				<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
			</view>
			<view class="service-card ry-card" @click="onFeedback">
				<u-icon name="edit-pen" color="#2979FF" :size="22" />
				<view class="service-card__info">
					<text class="service-card__title">意见反馈</text>
					<text class="service-card__desc">反馈问题，帮助我们改进</text>
				</view>
				<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
			</view>
			<view class="service-card ry-card" @click="onRecords">
				<u-icon name="file-text-fill" color="#9C27B0" :size="22" />
				<view class="service-card__info">
					<text class="service-card__title">我的预约</text>
					<text class="service-card__desc">查看预约记录与进度</text>
				</view>
				<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
			</view>
			<view class="service-faq ry-card">
				<text class="service-faq__title">常见问题</text>
				<view v-for="faq in faqList" :key="faq.q" class="service-faq__item">
					<text class="service-faq__q">Q：{{ faq.q }}</text>
					<text class="service-faq__a">A：{{ faq.a }}</text>
				</view>
			</view>
		</view>
	</view>
</template>`, `<script setup>
import { ref } from 'vue';

const faqList = ref([
	{ q: '订单如何取消？', a: '商户服务请联系对应商户处理；平台服务可在订单详情中申请取消或联系客服。' },
	{ q: '景区预约如何核销？', a: '按预约时间前往景区，出示预约码或手机号，工作人员核验后入园。' },
	{ q: '优惠券如何使用？', a: '在支持优惠券的商户下单时自动抵扣，具体规则以券面说明为准。' }
]);

function onCall() {
	uni.makePhoneCall({ phoneNumber: '023-46789012', fail: () => uni.showToast({ title: '当前环境不支持拨号', icon: 'none' }) });
}
function onFeedback() { uni.navigateTo({ url: '/mobile-design/feedback/index' }); }
function onRecords() { uni.navigateTo({ url: '/mobile-design/canteen/records' }); }
</script>`);

console.log('fix-batch-my2 done');
