<template>
	<view class="profile-page">
		<view v-if="pageLoading" class="profile-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
		</view>
		<template v-else>
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
						<view class="profile-item profile-item--input">
							<view class="profile-item__left">
								<view class="profile-item__icon-wrap" style="background-color: #FFF3EB;"><u-icon name="account-fill"
										color="#FF6600" :size="20" /></view>
								<text class="profile-item__label">昵称</text>
							</view>
							<view class="profile-item__right profile-item__right--input">
								<u-input
									v-model="userInfo.nickname"
									placeholder="请输入昵称"
									:maxlength="20"
									border="none"
									input-align="right"
									:custom-style="nicknameInputStyle"
								/>
							</view>
						</view>
						<view class="profile-item profile-item--disabled">
							<view class="profile-item__left">
								<view class="profile-item__icon-wrap" style="background-color: #E8F8EF;"><u-icon name="phone-fill"
										color="#2EAF6A" :size="20" /></view>
								<text class="profile-item__label">手机号</text>
							</view>
							<view class="profile-item__right"><text
									class="profile-item__value profile-item__value--disabled">{{ userInfo.phone }}</text></view>
						</view>
					</view>
				</view>
				<view class="profile-section">
					<view class="profile-section__title"><text class="profile-section__text">更多信息</text></view>
					<view class="profile-group">
						<view class="profile-item" @click="onGenderClick">
							<view class="profile-item__left">
								<view class="profile-item__icon-wrap" style="background-color: #EAF2FF;"><u-icon name="man"
										color="#2979FF" :size="20" /></view>
								<text class="profile-item__label">性别</text>
							</view>
							<view class="profile-item__right"><text class="profile-item__value">{{ genderText }}</text><u-icon
									name="arrow-right" :size="14" color="#CCCCCC" /></view>
						</view>
					</view>
				</view>
			</view>
			<view class="profile-footer">
				<view class="ry-btn ry-btn--primary ry-btn--large " @click="onSave"><text>保存</text></view>
			</view>
		</template>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		getDefaultAvatar,
		isDefaultAvatar,
		resolveAvatar
	} from '@/common/avatar.js';
	import {
		hasToken
	} from '@/common/api/auth.js';
	import {
		fetchUserProfile,
		updateUserProfile,
		uploadUserFile,
		resolveUploadUrl,
		isLocalTempPath
	} from '@/common/api/user.js';

	const userInfo = ref({
		userId: null,
		avatar: getDefaultAvatar(),
		nickname: '',
		phone: '',
		gender: 0
	});
	const pageLoading = ref(false);
	const saving = ref(false);
	const uploading = ref(false);
	const nicknameInputStyle = {
		fontSize: '28rpx',
		color: '#1A1A1A'
	};
	const genderText = computed(() => ({
		0: '未设置',
		1: '男',
		2: '女'
	} [userInfo.value.gender] || '未设置'));

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		});
		setTimeout(() => uni.navigateTo({
			url: '/pages/my/login'
		}), 400);
		return false;
	}

	function maskPhone(phone) {
		const text = String(phone || '');
		if (text.length < 7) return text;
		return `${text.slice(0, 3)}****${text.slice(-4)}`;
	}

	function mapProfileFromServer(user) {
		if (!user) return;
		userInfo.value = {
			userId: user.userId || user.id || null,
			avatar: resolveAvatar(user.avatar),
			nickname: user.userName || user.nickname || user.name || '用户',
			phone: maskPhone(user.phone || user.phoneNo || ''),
			gender: user.sex != null ? user.sex : 0
		};
	}

	async function loadProfile() {
		if (!hasToken()) return;
		pageLoading.value = true;
		try {
			const res = await fetchUserProfile();
			if (res.code === 0 && res.data) {
				mapProfileFromServer(res.data);
			} else {
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			pageLoading.value = false;
		}
	}

	onLoad(() => {
		if (!requireLogin()) return;
	});

	onShow(() => {
		if (!requireLogin()) return;
		loadProfile();
	});

	async function uploadAvatarIfNeeded(path) {
		if (!path || !isLocalTempPath(path)) return path;
		const uploadRes = await uploadUserFile(path);
		const url = resolveUploadUrl(uploadRes);
		if (!url) throw new Error('头像上传失败');
		return url;
	}

	function onAvatarClick() {
		if (uploading.value || saving.value) return;
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const tempPath = res.tempFilePaths[0];
				if (!tempPath) return;
				uploading.value = true;
				uni.showLoading({
					title: '上传中...'
				});
				try {
					const url = await uploadAvatarIfNeeded(tempPath);
					userInfo.value.avatar = url;
					if (userInfo.value.userId) {
						const updateRes = await updateUserProfile({
							userId: userInfo.value.userId,
							avatar: isDefaultAvatar(url) ? '' : url
						});
						if (updateRes.code !== 0) {
							uni.showToast({
								title: updateRes.msg || '头像保存失败',
								icon: 'none'
							});
							return;
						}
					}
					uni.showToast({
						title: '头像已更新',
						icon: 'success'
					});
				} catch (e) {
					uni.showToast({
						title: (e && e.message) || '头像上传失败',
						icon: 'none'
					});
				} finally {
					uploading.value = false;
					uni.hideLoading();
				}
			}
		});
	}

	function onGenderClick() {
		uni.showActionSheet({
			itemList: ['男', '女'],
			success: (res) => {
				userInfo.value.gender = res.tapIndex + 1;
			}
		});
	}

	async function onSave() {
		if (saving.value || uploading.value) return;
		if (!requireLogin()) return;
		if (!userInfo.value.userId) {
			uni.showToast({
				title: '用户信息加载中，请稍后',
				icon: 'none'
			});
			return;
		}
		if (!userInfo.value.nickname.trim()) {
			uni.showToast({
				title: '昵称不能为空',
				icon: 'none'
			});
			return;
		}

		saving.value = true;
		uni.showLoading({
			title: '保存中...'
		});
		try {
			let avatar = userInfo.value.avatar;
			if (isLocalTempPath(avatar)) {
				avatar = await uploadAvatarIfNeeded(avatar);
				userInfo.value.avatar = avatar;
			}
			const payload = {
				userId: userInfo.value.userId,
				userName: userInfo.value.nickname.trim(),
				avatar: isDefaultAvatar(avatar) ? '' : avatar
			};
			if (userInfo.value.gender === 1 || userInfo.value.gender === 2) {
				payload.sex = userInfo.value.gender;
			}
			const res = await updateUserProfile(payload);
			if (res.code === 0) {
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				});
				setTimeout(() => uni.navigateBack(), 800);
			} else {
				uni.showToast({
					title: res.msg || '保存失败',
					icon: 'none'
				});
			}
		} catch (e) {
			uni.showToast({
				title: (e && e.message) || '保存失败',
				icon: 'none'
			});
		} finally {
			saving.value = false;
			uni.hideLoading();
		}
	}
</script>

<style lang="scss" scoped>
	.profile-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.profile-loading {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 60vh;
	}

	.profile-header {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx 0 32rpx;
		background-color: #FFFFFF;
		margin-bottom: 24rpx;
	}

	.profile-header__avatar {
		position: relative;
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
		overflow: hidden;
		background-color: #F5F5F5;
		margin-bottom: 16rpx;
	}

	.profile-header__avatar-img {
		width: 100%;
		height: 100%;
		display: block;
	}

	.profile-header__camera {
		position: absolute;
		right: 0;
		bottom: 0;
		width: 48rpx;
		height: 48rpx;
		background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 100%);
		border-radius: 50%;
		border: 4rpx solid #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba(255, 102, 0, 0.3);
	}

	.profile-header__tip {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.profile-body {
		padding: 0 var(--ry-page-padding-x) 40rpx;
	}

	.profile-section {
		margin-bottom: 24rpx;
	}

	.profile-section__title {
		padding: 16rpx 8rpx 12rpx;
	}

	.profile-section__text {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-secondary);
		line-height: 1.4;
	}

	.profile-group {
		background-color: #FFFFFF;
		border-radius: var(--ry-radius-lg);
		box-shadow: var(--ry-card-shadow);
		overflow: hidden;
	}

	.profile-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 28rpx 24rpx;
		min-height: 104rpx;
		border-bottom: 1rpx solid #F5F5F5;
	}

	.profile-item:last-child {
		border-bottom: none;
	}

	.profile-item--disabled {
		opacity: 0.6;
	}

	.profile-item__left {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex: 1;
		min-width: 0;
	}

	.profile-item__icon-wrap {
		width: 64rpx;
		height: 64rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.profile-item__label {
		font-size: 28rpx;
		color: var(--ry-text-primary);
		font-weight: 500;
		line-height: 1.4;
	}

	.profile-item__right {
		display: flex;
		align-items: center;
		gap: 8rpx;
		flex-shrink: 0;
	}

	.profile-item__value {
		font-size: 28rpx;
		color: var(--ry-text-secondary);
		line-height: 1.4;
		max-width: 400rpx;
		text-align: right;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.profile-item__value--disabled {
		color: var(--ry-text-hint);
	}

	.profile-item--input {
		cursor: text;
	}

	.profile-item__right--input {
		flex: 1;
		justify-content: flex-end;
		min-width: 0;
		max-width: 420rpx;

		:deep(.u-input__content__field-wrapper__field) {
			color: #1A1A1A !important;
		}

		:deep(.input-placeholder) {
			color: #BBBBBB !important;
		}
	}

	.profile-footer {
		padding: 32rpx var(--ry-page-padding-x);
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	}

</style>
