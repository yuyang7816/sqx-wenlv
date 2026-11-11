const { patchPage } = require('./fix-batch-shared');
const fs = require('fs');
const path = require('path');

// profile - preserve popup structure
patchPage('pages/my/profile.vue', `<template>
	<view class="profile-page">
		<view class="profile-header" @click="onAvatarClick">
			<view class="profile-header__avatar">
				<image class="profile-header__avatar-img" :src="userInfo.avatar" mode="aspectFill" />
				<view class="profile-header__camera"><u-icon name="camera-fill" color="#FFFFFF" :size="20" /></view>
			</view>
			<text class="profile-header__tip">点击更换头像</text>
		</view>
		<view class="profile-body">
			<view class="profile-section">
				<view class="profile-section__title"><text class="profile-section__text">基本信息</text></view>
				<view class="profile-group">
					<view class="profile-item" @click="onNicknameClick">
						<view class="profile-item__left">
							<view class="profile-item__icon-wrap" style="background-color: #FFF3EB;"><u-icon name="account-fill" color="#FF6600" :size="20" /></view>
							<text class="profile-item__label">昵称</text>
						</view>
						<view class="profile-item__right"><text class="profile-item__value">{{ userInfo.nickname }}</text><u-icon name="arrow-right" :size="14" color="#CCCCCC" /></view>
					</view>
					<view class="profile-item profile-item--disabled">
						<view class="profile-item__left">
							<view class="profile-item__icon-wrap" style="background-color: #E8F8EF;"><u-icon name="phone-fill" color="#2EAF6A" :size="20" /></view>
							<text class="profile-item__label">手机号</text>
						</view>
						<view class="profile-item__right"><text class="profile-item__value profile-item__value--disabled">{{ userInfo.phone }}</text></view>
					</view>
				</view>
			</view>
			<view class="profile-section">
				<view class="profile-section__title"><text class="profile-section__text">更多信息</text></view>
				<view class="profile-group">
					<view class="profile-item" @click="onGenderClick">
						<view class="profile-item__left">
							<view class="profile-item__icon-wrap" style="background-color: #EAF2FF;"><u-icon name="man" color="#2979FF" :size="20" /></view>
							<text class="profile-item__label">性别</text>
						</view>
						<view class="profile-item__right"><text class="profile-item__value">{{ genderText }}</text><u-icon name="arrow-right" :size="14" color="#CCCCCC" /></view>
					</view>
					<view class="profile-item" @click="onBirthdayClick">
						<view class="profile-item__left">
							<view class="profile-item__icon-wrap" style="background-color: #FFF8E6;"><u-icon name="calendar-fill" color="#FFB800" :size="20" /></view>
							<text class="profile-item__label">生日</text>
						</view>
						<view class="profile-item__right"><text class="profile-item__value">{{ userInfo.birthday || '未设置' }}</text><u-icon name="arrow-right" :size="14" color="#CCCCCC" /></view>
					</view>
					<view class="profile-item" @click="onRegionClick">
						<view class="profile-item__left">
							<view class="profile-item__icon-wrap" style="background-color: #F3E8F8;"><u-icon name="map-fill" color="#9C27B0" :size="20" /></view>
							<text class="profile-item__label">地区</text>
						</view>
						<view class="profile-item__right"><text class="profile-item__value">{{ userInfo.region || '未设置' }}</text><u-icon name="arrow-right" :size="14" color="#CCCCCC" /></view>
					</view>
				</view>
			</view>
		</view>
		<view class="profile-footer">
			<view class="ry-btn ry-btn--primary ry-btn--large ry-btn--block" @click="onSave"><text>保存</text></view>
		</view>
		<u-popup :show="showNicknamePopup" mode="center" :round="20" @close="showNicknamePopup = false">
			<view class="edit-popup">
				<view class="edit-popup__header"><text class="edit-popup__title">修改昵称</text></view>
				<view class="edit-popup__body"><u-input v-model="tempNickname" placeholder="请输入昵称" :maxlength="20" :border="true" :custom-style="inputStyle" /></view>
				<view class="edit-popup__footer">
					<view class="ry-btn ry-btn--secondary ry-btn--medium" @click="showNicknamePopup = false"><text>取消</text></view>
					<view class="ry-btn ry-btn--primary ry-btn--medium" @click="onConfirmNickname"><text>确定</text></view>
				</view>
			</view>
		</u-popup>
		<u-datetime-picker :show="showBirthdayPicker" v-model="birthdayValue" mode="date" :max-date="maxDate" :min-date="minDate" @confirm="onConfirmBirthday" @cancel="showBirthdayPicker = false" />
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { MOCK_USER_PROFILE } from '@/common/feedback-mock.js';

const userInfo = ref({ avatar: PLACEHOLDER_LOGO, nickname: MOCK_USER_PROFILE.contactName, phone: MOCK_USER_PROFILE.phone, gender: 1, birthday: '1990-01-01', region: '重庆市荣昌区' });
const showNicknamePopup = ref(false);
const tempNickname = ref('');
const showBirthdayPicker = ref(false);
const birthdayValue = ref(new Date('1990-01-01').getTime());
const maxDate = ref(Date.now());
const minDate = ref(new Date('1900-01-01').getTime());
const inputStyle = { padding: '24rpx', fontSize: '28rpx' };
const genderText = computed(() => ({ 0: '未设置', 1: '男', 2: '女' }[userInfo.value.gender] || '未设置'));

onLoad(() => {});
function onAvatarClick() {
	uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'], success: (res) => {
		userInfo.value.avatar = res.tempFilePaths[0];
		uni.showToast({ title: '头像已更新', icon: 'success' });
	}});
}
function onNicknameClick() { tempNickname.value = userInfo.value.nickname; showNicknamePopup.value = true; }
function onConfirmNickname() {
	if (!tempNickname.value.trim()) { uni.showToast({ title: '昵称不能为空', icon: 'none' }); return; }
	userInfo.value.nickname = tempNickname.value.trim();
	showNicknamePopup.value = false;
	uni.showToast({ title: '昵称已更新', icon: 'success' });
}
function onGenderClick() {
	uni.showActionSheet({ itemList: ['男', '女'], success: (res) => { userInfo.value.gender = res.tapIndex + 1; uni.showToast({ title: '性别已更新', icon: 'success' }); }});
}
function onBirthdayClick() {
	if (userInfo.value.birthday) birthdayValue.value = new Date(userInfo.value.birthday).getTime();
	showBirthdayPicker.value = true;
}
function onConfirmBirthday(e) {
	const date = new Date(e.value);
	userInfo.value.birthday = \`\${date.getFullYear()}-\${String(date.getMonth()+1).padStart(2,'0')}-\${String(date.getDate()).padStart(2,'0')}\`;
	showBirthdayPicker.value = false;
	uni.showToast({ title: '生日已更新', icon: 'success' });
}
function onRegionClick() {
	uni.chooseLocation({ success: (res) => { userInfo.value.region = res.address || res.name; uni.showToast({ title: '地区已更新', icon: 'success' }); }});
}
function onSave() {
	uni.showLoading({ title: '保存中...' });
	setTimeout(() => { uni.hideLoading(); uni.showToast({ title: '保存成功', icon: 'success' }); setTimeout(() => uni.navigateBack(), 1000); }, 500);
}
</script>`);

// goose detail - template + script with getGooseShopById
patchPage('mobile-design/goose/detail.vue', `<template>
	<view class="goose-detail-page">
		<view class="goose-cover">
			<swiper class="goose-cover__swiper" :indicator-dots="bannerList.length > 1" indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF" :autoplay="bannerList.length > 1" :circular="true" :interval="4000" duration="300" @change="onBannerChange">
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="goose-cover__item">
					<image class="goose-cover__img ry-placeholder-img" :src="item" mode="aspectFill" />
				</swiper-item>
			</swiper>
			<view class="goose-cover__mask" />
			<view class="goose-cover__badge"><text class="goose-cover__badge-text">非遗美食</text></view>
			<view v-if="bannerList.length > 1" class="goose-cover__count"><text class="goose-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text></view>
		</view>
		<view class="goose-detail-body">
			<view class="goose-header ry-card">
				<text class="goose-header__name">{{ shop.name }}</text>
				<view class="goose-header__stats">
					<view class="goose-header__stat">
						<view class="goose-header__stat-top"><u-icon name="star-fill" :color="accentColor" :size="14" /><text class="goose-header__stat-num">{{ shop.rating }}</text></view>
						<text class="goose-header__stat-label">评分</text>
					</view>
					<view class="goose-header__stat-divider" />
					<view class="goose-header__stat"><text class="goose-header__stat-num">{{ shop.monthlySales }}</text><text class="goose-header__stat-label">月售</text></view>
					<view class="goose-header__stat-divider" />
					<view class="goose-header__stat"><text class="goose-header__stat-num goose-header__stat-num--price">¥{{ priceFrom }}</text><text class="goose-header__stat-label">起</text></view>
				</view>
				<view v-if="shop.tags && shop.tags.length" class="goose-header__tags">
					<view v-for="tag in shop.tags" :key="tag" class="goose-header__tag"><text class="goose-header__tag-text">{{ tag }}</text></view>
				</view>
				<view class="goose-header__address" @click="onAddressClick">
					<view class="goose-header__address-icon"><u-icon name="map-fill" :color="accentColor" :size="14" /></view>
					<text class="goose-header__address-text">{{ shop.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>
				<view class="goose-header__quick">
					<view class="goose-header__quick-item" @click="onPhoneCall"><u-icon name="phone-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">电话</text></view>
					<view class="goose-header__quick-divider" />
					<view class="goose-header__quick-item" @click="onAddressClick"><u-icon name="map-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">导航</text></view>
					<view class="goose-header__quick-divider" />
					<view class="goose-header__quick-item" @click="onCanteenReserve"><u-icon name="calendar-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">预约</text></view>
				</view>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head"><view class="goose-section__icon"><u-icon name="file-text-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">店铺介绍</text></view>
				<text class="goose-section__para">{{ shop.intro }}</text>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head goose-section__head--split">
					<view class="goose-section__head-left"><view class="goose-section__icon"><u-icon name="gift-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">招牌菜品</text></view>
					<view class="goose-section__more" @click="onViewAllMenu"><text class="goose-section__more-text">查看全部</text><u-icon name="arrow-right" :color="accentColor" :size="12" /></view>
				</view>
				<view class="goose-specialty">
					<view v-for="item in previewSpecialtyList" :key="item.id" class="goose-specialty__item" @click="onSpecialtyClick(item)">
						<view class="goose-specialty__thumb-wrap"><image class="goose-specialty__thumb ry-placeholder-img" :src="PLACEHOLDER_LOGO" mode="aspectFill" /></view>
						<view class="goose-specialty__info">
							<text class="goose-specialty__name">{{ item.name }}</text>
							<text class="goose-specialty__desc">{{ item.desc }}</text>
							<view class="goose-specialty__foot">
								<view class="goose-specialty__price-wrap"><text class="goose-specialty__symbol">¥</text><text class="goose-specialty__price">{{ item.price }}</text></view>
								<text class="goose-specialty__sales">月售 {{ item.sold }} 份</text>
							</view>
						</view>
						<view class="goose-specialty__action"><text class="goose-specialty__action-text">预约</text></view>
					</view>
				</view>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head"><view class="goose-section__icon"><u-icon name="clock-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">营业时间</text></view>
				<view class="goose-hours">
					<view v-for="row in shop.businessHours" :key="row.id" class="goose-hours__row">
						<view class="goose-hours__dot" /><text class="goose-hours__label">{{ row.label }}</text><text class="goose-hours__value">{{ row.time }}</text>
					</view>
				</view>
			</view>
			<text class="goose-footer">荣昌非遗美食 · 地道卤味</text>
		</view>
		<view class="goose-bar">
			<view class="goose-bar__phone" @click="onPhoneCall"><u-icon name="phone-fill" :color="accentColor" :size="22" /><text class="goose-bar__phone-text">电话咨询</text></view>
			<view class="goose-bar__submit" @click="onCanteenReserve"><text class="goose-bar__submit-text">餐饮预约</text><u-icon name="arrow-right" color="#FFFFFF" :size="14" /></view>
		</view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { getGooseShopById } from '@/common/goose-mock.js';

const accentColor = getModuleColor('goose');
const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
const GOOSE_MENU_PATH = '/mobile-design/goose/menu';
const shop = ref({ ...getGooseShopById('1') });
const bannerCurrent = ref(0);
const bannerList = computed(() => shop.value.bannerList?.length ? shop.value.bannerList : [PLACEHOLDER_LOGO, PLACEHOLDER_LOGO, PLACEHOLDER_LOGO]);
const priceFrom = computed(() => {
	const list = shop.value.specialtyList || [];
	if (!list.length) return '--';
	return Math.min(...list.map(item => Number(item.price)));
});
const previewSpecialtyList = computed(() => (shop.value.specialtyList || []).slice(0, 3));
const navigateSafe = (url) => uni.navigateTo({ url });
const loadShopById = (id) => { shop.value = getGooseShopById(id); bannerCurrent.value = 0; };
const onBannerChange = (e) => { bannerCurrent.value = e.detail.current; };
onLoad((options) => { loadShopById(options?.id); });
function onAddressClick() {
	const { latitude, longitude, address, name } = shop.value;
	if (!latitude || !longitude) { uni.showToast({ title: address, icon: 'none' }); return; }
	uni.openLocation({ latitude, longitude, name, address, fail: () => uni.showToast({ title: address, icon: 'none' }) });
}
function onPhoneCall() {
	const { phone } = shop.value;
	if (!phone) { uni.showToast({ title: '暂无联系电话', icon: 'none' }); return; }
	/* #ifdef H5 */
	uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' });
	return;
	/* #endif */
	uni.makePhoneCall({ phoneNumber: phone.replace(/-/g, ''), fail: () => uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' }) });
}
const buildCanteenQuery = (dishName = '') => {
	const { id, name, address } = shop.value;
	const parts = [\`shopId=\${id}\`, \`shopName=\${encodeURIComponent(name)}\`, \`shopAddress=\${encodeURIComponent(address)}\`];
	if (dishName) parts.push(\`dishName=\${encodeURIComponent(dishName)}\`);
	return parts.join('&');
};
function onSpecialtyClick(item) { navigateSafe(\`\${CANTEEN_RESERVE_PATH}?\${buildCanteenQuery(item.name)}\`); }
function onCanteenReserve() { navigateSafe(\`\${CANTEEN_RESERVE_PATH}?\${buildCanteenQuery()}\`); }
function onViewAllMenu() { navigateSafe(\`\${GOOSE_MENU_PATH}?id=\${shop.value.id}&shopName=\${encodeURIComponent(shop.value.name)}\`); }
</script>`);

console.log('fix-batch-profile-goose done');
