/**
 * H5 第三方支付拉起（支付宝表单 / 微信 H5 链接）
 */

function submitAlipayForm(html) {
	// #ifdef H5
	const payWindow = window.open('', '_self');
	if (payWindow) {
		payWindow.document.write(html);
		payWindow.document.close();
		return true;
	}
	const div = document.createElement('div');
	div.innerHTML = html;
	div.style.display = 'none';
	document.body.appendChild(div);
	const form = div.querySelector('form');
	if (form) {
		form.submit();
		return true;
	}
	return false;
	// #endif
	return false;
}

/**
 * 根据后端 unifiedPay 返回值拉起 H5 支付
 * @returns {'redirected'|'invalid'|'none'}
 */
export function launchH5PaymentFromResponse(payData, channel = '') {
	// #ifndef H5
	return 'none';
	// #endif
	// #ifdef H5
	if (payData == null || payData === '') {
		return 'invalid';
	}
	const payload = typeof payData === 'string' ? payData.trim() : String(payData).trim();
	if (!payload) {
		return 'invalid';
	}
	if (/^https?:\/\//i.test(payload)) {
		window.location.href = payload;
		return 'redirected';
	}
	if (channel === 'ali' || channel === 'alipay' || payload.includes('<form')) {
		if (payload.includes('<form') && submitAlipayForm(payload)) {
			return 'redirected';
		}
		return 'invalid';
	}
	return 'none';
	// #endif
}

export function isH5PaymentRedirectResult(result) {
	return result === 'redirected';
}
