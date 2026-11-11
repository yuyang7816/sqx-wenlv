/**
 * 客服配置解析与直连动作（在线 IM / 企业微信 / 电话 / 微信展示分离）
 */
import { fetchConfigInfo } from './api/config.js';
import { hasToken } from './api/auth.js';
import { goLogin, openPage, replacePage, PAGES } from './navigation.js';
import { KEFU_IM_PATH } from './kefu-ui.js';
import { resolvePublicUrl } from './media-url.js';

export function resolveKefuType(kefu) {
	const configured = String(kefu?.type || '').trim().toLowerCase();
	if (configured) return configured;
	if (kefu?.phone) return 'phone';
	if (kefu?.wechat || kefu?.wechatQrcode) return 'wechat';
	if (kefu?.weworkUrl) return 'wework';
	return '';
}

export function normalizeKefuConfig(kefu = {}) {
	const rawQrcode = kefu.wechatQrcode || kefu.wechatQrCode || kefu.wechat_qrcode || '';
	return {
		type: kefu.type || '',
		phone: kefu.phone || '',
		wechat: kefu.wechat || '',
		wechatQrcode: resolvePublicUrl(rawQrcode),
		weworkUrl: kefu.weworkUrl || '',
		weworkCorpId: kefu.weworkCorpId || ''
	};
}

export function callKefuPhone(phone) {
	if (!phone) {
		uni.showToast({ title: '暂无客服电话', icon: 'none' });
		return;
	}
	uni.makePhoneCall({
		phoneNumber: phone,
		fail: () => uni.showToast({ title: '当前环境不支持拨号', icon: 'none' })
	});
}

export function copyKefuWechat(wechat) {
	if (!wechat) {
		return;
	}
	uni.setClipboardData({
		data: wechat,
		success: () => uni.showToast({ title: '微信号已复制', icon: 'success' })
	});
}

const WEWORK_MODAL = {
	title: '企业微信客服',
	content: '请在微信小程序中使用企业微信客服，或复制微信号添加客服。'
};

export function openKefuWework(kefu) {
	const { weworkUrl, weworkCorpId, wechat } = kefu;
	// #ifdef MP-WEIXIN
	if (weworkUrl && weworkCorpId && typeof wx !== 'undefined' && wx.openCustomerServiceChat) {
		wx.openCustomerServiceChat({
			extInfo: { url: weworkUrl },
			corpId: weworkCorpId,
			fail: () => showWeworkFallback(wechat)
		});
		return;
	}
	// #endif
	showWeworkFallback(wechat);
}

function showWeworkFallback(wechat) {
	const content = wechat
		? `${WEWORK_MODAL.content}\n\n微信号：${wechat}`
		: WEWORK_MODAL.content;
	uni.showModal({
		title: WEWORK_MODAL.title,
		content,
		confirmText: wechat ? '复制微信号' : '知道了',
		confirmColor: '#FF6600',
		success: (res) => {
			if (res.confirm && wechat) {
				copyKefuWechat(wechat);
			}
		}
	});
}

export function openKefuIm({ replace = false } = {}) {
	if (!hasToken()) {
		uni.showToast({ title: '请先登录后再使用在线客服', icon: 'none' });
		goLogin();
		return false;
	}
	if (replace) {
		replacePage(KEFU_IM_PATH);
	} else {
		openPage(KEFU_IM_PATH);
	}
	return true;
}

/**
 * 根据客服类型直接执行动作
 * @returns {string} 解析出的客服类型
 */
export function handleKefuAction(kefu, options = {}) {
	const type = resolveKefuType(kefu);
	const fromServicePage = !!options.fromServicePage;

	switch (type) {
		case 'phone':
			callKefuPhone(kefu.phone);
			return type;
		case 'im':
			openKefuIm({ replace: fromServicePage });
			return type;
		case 'wework':
			openKefuWework(kefu);
			return type;
		case 'wechat':
			if (!fromServicePage) {
				openPage(PAGES.SERVICE);
			}
			return type;
		default:
			if (!fromServicePage) {
				openPage(PAGES.SERVICE);
			}
			return '';
	}
}

/** 从任意入口打开客服中心：按配置直连，不再展示渠道列表 */
export async function openKefuCenter() {
	try {
		uni.showLoading({ title: '加载中...', mask: true });
		const res = await fetchConfigInfo();
		uni.hideLoading();
		if (res.code !== 0 || !res.data) {
			uni.showToast({ title: res.msg || '客服配置加载失败', icon: 'none' });
			return null;
		}
		const kefu = normalizeKefuConfig(res.data.kefu);
		const type = handleKefuAction(kefu);
		return { kefu, type, productName: res.data.productName };
	} catch (e) {
		uni.hideLoading();
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
		return null;
	}
}
