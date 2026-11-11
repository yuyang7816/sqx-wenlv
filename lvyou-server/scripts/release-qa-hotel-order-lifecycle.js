/**
 * 酒店订单全状态 E2E + 多端联调测试
 * 前置：Redis(6379) + MySQL(rongeyou) + server:9165 已启动；已执行 migration
 *
 * 用法：
 *   node lvyou-server/scripts/release-qa-hotel-order-lifecycle.js
 *
 * 环境变量：
 *   QA_BASE_URL  默认 http://127.0.0.1:9165/sqx_fast
 *   QA_PHONE     默认 18892020826
 *   QA_PASSWORD  默认 111111（失败时自动尝试 123456）
 *   QA_LAT / QA_LNG  酒店列表定位坐标（默认西安）
 *   QA_ADMIN_USER / QA_ADMIN_PASS  管理端（可选）
 */

const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const BASE = process.env.QA_BASE_URL || 'http://127.0.0.1:9165/sqx_fast';
const PHONE = process.env.QA_PHONE || '18892020826';
const PASSWORDS = [process.env.QA_PASSWORD || '111111', '123456'].filter((v, i, a) => a.indexOf(v) === i);
/** 西安市坐标（当前种子酒店「石榴大酒店」所在目的地） */
const QA_LAT = process.env.QA_LAT || '34.341568';
const QA_LNG = process.env.QA_LNG || '108.940174';

const results = [];
const failures = [];
const stateCovers = {};

function log(msg) {
	console.log(`\n==> ${msg}`);
}

function record(name, ok, detail) {
	results.push({ name, ok, detail });
	stateCovers[name] = ok;
	if (!ok) failures.push(`${name}: ${detail}`);
	console.log(ok ? `OK: ${name}` : `FAIL: ${name} — ${detail}`);
}

async function request(method, reqPath, { headers = {}, body = null } = {}) {
	const url = new URL(BASE + reqPath);
	const lib = url.protocol === 'https:' ? https : http;
	const payload = body == null ? null : JSON.stringify(body);
	return new Promise((resolve) => {
		const req = lib.request(
			{
				hostname: url.hostname,
				port: url.port || (url.protocol === 'https:' ? 443 : 80),
				path: url.pathname + url.search,
				method,
				headers: {
					'Content-Type': 'application/json',
					...(payload ? { 'Content-Length': Buffer.byteLength(payload) } : {}),
					...headers
				},
				timeout: 30000
			},
			(res) => {
				let raw = '';
				res.on('data', (chunk) => (raw += chunk));
				res.on('end', () => {
					let json = null;
					try {
						json = JSON.parse(raw);
					} catch (e) {
						/* ignore */
					}
					resolve({ status: res.statusCode, json, raw });
				});
			}
		);
		req.on('error', (err) => resolve({ status: 0, json: null, raw: err.message }));
		if (payload) req.write(payload);
		req.end();
	});
}

async function api(method, reqPath, opts = {}) {
	const resp = await request(method, reqPath, opts);
	const ok = resp.status === 200 && resp.json && resp.json.code === 0;
	return { ...resp, ok };
}

async function loginApp() {
	for (const password of PASSWORDS) {
		const resp = await api('POST', '/app/base/auth/phonePasswordLogin', {
			body: { phoneNo: PHONE, password }
		});
		if (resp.ok && resp.json.data && resp.json.data.token) {
			return resp.json.data.token;
		}
	}
	return null;
}

function authHeaders(token) {
	return { token };
}

function assertStatus(order, expectedCode, label) {
	const code = order.statusCode || order.status;
	if (code !== expectedCode) {
		throw new Error(`${label} 期望 ${expectedCode} 实际 ${code}`);
	}
}

async function createHotelOrder(token, hotelId, roomId, checkIn, checkOut, roomCount = 1) {
	const resp = await api('POST', '/app/order/create', {
		headers: authHeaders(token),
		body: {
			category: 'hotel',
			hotelId,
			roomId,
			checkInDate: checkIn,
			checkOutDate: checkOut,
			roomCount,
			guestCount: 1,
			guestNames: ['QA测试'],
			contactName: 'QA测试',
			contactPhone: PHONE,
			expectedArrivalTime: '18:00 前'
		}
	});
	if (!resp.ok) throw new Error(resp.raw);
	return resp.json.data;
}

async function payOrder(token, orderNo) {
	const resp = await api('POST', '/app/wallet/pay', {
		headers: authHeaders(token),
		body: { orderNo }
	});
	if (!resp.ok) throw new Error(resp.raw);
}

async function getOrder(token, orderId) {
	const resp = await api('GET', `/app/order/${orderId}`, { headers: authHeaders(token) });
	if (!resp.ok) throw new Error(resp.raw);
	return resp.json.data;
}

async function main() {
	log('0. health');
	const ping = await api('GET', '/app/config/info');
	if (!ping.ok) {
		record('server reachable', false, ping.raw);
		writeReport();
		process.exit(1);
	}
	record('server reachable', true, BASE);

	log('1. login');
	const token = await loginApp();
	if (!token) {
		record('app login', false, `phone ${PHONE} password tried: ${PASSWORDS.join(',')}`);
		writeReport();
		process.exit(1);
	}
	record('app login', true, PHONE);

	log('2. discover hotel');
	const hotelList = await api('GET', `/app/hotel/list?page=1&limit=10&lat=${QA_LAT}&lng=${QA_LNG}`);
	const hotel = hotelList.json?.data?.records?.[0];
	if (!hotel) {
		record('hotel seed', false, 'empty list');
		writeReport();
		process.exit(1);
	}
	const checkIn = fmtDate(addDays(new Date(), 7));
	const checkOut = fmtDate(addDays(new Date(), 8));
	const roomsResp = await api('GET', `/app/hotel/${hotel.id}/rooms?checkInDate=${checkIn}&checkOutDate=${checkOut}`);
	const room = roomsResp.json?.data?.[0];
	if (!room) {
		record('room seed', false, 'no rooms');
		writeReport();
		process.exit(1);
	}
	record('hotel quote', roomsResp.ok, `hotel=${hotel.id} room=${room.id}`);

	log('3. status UNPAID');
	const unpaid = await createHotelOrder(token, hotel.id, room.id, checkIn, checkOut, 1);
	const unpaidDetail = await getOrder(token, unpaid.orderId);
	assertStatus(unpaidDetail, 'pending_pay', 'UNPAID');
	record('status UNPAID', true, unpaidDetail.status);
	if (!unpaidDetail.payExpireTime) {
		record('payExpireTime snapshot', false, 'missing');
	} else {
		record('payExpireTime snapshot', true, unpaidDetail.payExpireTime);
	}

	log('4. status CANCELLED (unpaid cancel)');
	await api('POST', `/app/order/${unpaid.orderId}/cancel`, { headers: authHeaders(token) });
	const cancelled = await getOrder(token, unpaid.orderId);
	assertStatus(cancelled, 'cancelled', 'CANCELLED');
	record('status CANCELLED unpaid', true, cancelled.status);

	log('5. status PENDING_USE (pay)');
	const paidOrder = await createHotelOrder(token, hotel.id, room.id, checkIn, checkOut, 1);
	await payOrder(token, paidOrder.orderNo);
	const pendingUse = await getOrder(token, paidOrder.orderId);
	assertStatus(pendingUse, 'pending_use', 'PENDING_USE');
	record('status PENDING_USE', true, pendingUse.status);
	if (!pendingUse.verifyCode) {
		record('verifyCode issued', false, 'missing');
	} else {
		record('verifyCode issued', true, pendingUse.verifyCode);
	}
	if (!pendingUse.cancelPolicySummary) {
		record('cancelPolicySummary', false, 'missing');
	} else {
		record('cancelPolicySummary', true, pendingUse.cancelPolicySummary);
	}

	log('6. status REFUNDING (user apply refund)');
	const refundApply = await api('POST', `/app/order/${paidOrder.orderId}/action`, {
		headers: authHeaders(token),
		body: { action: 'refund', reason: 'QA测试退款' }
	});
	if (!refundApply.ok) {
		record('status REFUNDING', false, refundApply.raw);
	} else {
		const refunding = await getOrder(token, paidOrder.orderId);
		assertStatus(refunding, 'refunding', 'REFUNDING');
		record('status REFUNDING', true, refunding.status);

		log('7. merchant refund_agree -> CANCELLED');
		const agree = await api('POST', `/app/merchant/orders/${paidOrder.orderId}/action`, {
			headers: authHeaders(token),
			body: { action: 'refund_agree' }
		});
		if (!agree.ok) {
			record('merchant refund_agree', false, agree.raw);
		} else {
			const refunded = await getOrder(token, paidOrder.orderId);
			assertStatus(refunded, 'cancelled', 'CANCELLED after refund');
			record('status CANCELLED refund', true, refunded.status);
		}
	}

	log('8. full flow: pay -> verify -> complete');
	const flow = await createHotelOrder(token, hotel.id, room.id, checkIn, checkOut, 2);
	await payOrder(token, flow.orderNo);
	const verifyTarget = await getOrder(token, flow.orderId);
	if (verifyTarget.roomCount !== 2) {
		record('roomCount persisted', false, `expected 2 got ${verifyTarget.roomCount}`);
	} else {
		record('roomCount persisted', true, '2');
	}
	const verifyResp = await api('POST', `/app/merchant/orders/${flow.orderId}/action`, {
		headers: authHeaders(token),
		body: { action: 'verify' }
	});
	if (!verifyResp.ok) {
		record('merchant verify', false, verifyResp.raw);
	} else {
		const verified = await getOrder(token, flow.orderId);
		if (!verified.verified) {
			record('status verified check-in', false, 'verified=false');
		} else {
			record('status verified check-in', true, verified.verifiedAt);
			assertStatus(verified, 'pending_use', 'still PENDING_USE after verify');
		}
		const completeResp = await api('POST', `/app/merchant/orders/${flow.orderId}/action`, {
			headers: authHeaders(token),
			body: { action: 'complete' }
		});
		if (!completeResp.ok) {
			record('status COMPLETED', false, completeResp.raw);
		} else {
			const completed = await getOrder(token, flow.orderId);
			assertStatus(completed, 'completed', 'COMPLETED');
			record('status COMPLETED', true, completed.status);
		}
	}

	log('9. free cancel paid order');
	const freeCancelCheckIn = fmtDate(addDays(new Date(), 10));
	const freeCancelCheckOut = fmtDate(addDays(new Date(), 11));
	const freeOrder = await createHotelOrder(token, hotel.id, room.id, freeCancelCheckIn, freeCancelCheckOut, 1);
	await payOrder(token, freeOrder.orderNo);
	const cancelPaid = await api('POST', `/app/order/${freeOrder.orderId}/cancel`, { headers: authHeaders(token) });
	if (!cancelPaid.ok) {
		record('paid free cancel', false, cancelPaid.raw);
	} else {
		const afterCancel = await getOrder(token, freeOrder.orderId);
		assertStatus(afterCancel, 'cancelled', 'free cancel');
		record('paid free cancel', true, afterCancel.status);
	}

	log('10. merchant calendar API');
	const calSave = await api('POST', `/app/merchant/hotel/room/${room.id}/calendar`, {
		headers: authHeaders(token),
		body: {
			startDate: checkIn,
			endDate: checkIn,
			price: room.price || 268,
			stock: 5,
			status: 1
		}
	});
	if (!calSave.ok) {
		record('merchant calendar save', false, calSave.raw);
	} else {
		const calGet = await api(
			'GET',
			`/app/merchant/hotel/room/${room.id}/calendar?startDate=${checkIn}&endDate=${checkIn}`,
			{ headers: authHeaders(token) }
		);
		record('merchant calendar get', calGet.ok, calGet.raw.slice(0, 120));
	}

	log('11. list filters multi-end');
	const listPending = await api('GET', '/app/order/list?category=hotel&status=待入住&page=1&limit=5', {
		headers: authHeaders(token)
	});
	record('user list 待入住', listPending.ok, `count=${listPending.json?.data?.records?.length || 0}`);
	const mOrders = await api('GET', '/app/merchant/orders?tab=processing&page=1&limit=5', {
		headers: authHeaders(token)
	});
	record('merchant list processing', mOrders.ok, `count=${mOrders.json?.data?.records?.length || 0}`);

	writeReport();
	process.exit(failures.length ? 1 : 0);
}

function addDays(date, days) {
	const d = new Date(date);
	d.setDate(d.getDate() + days);
	return d;
}

function fmtDate(date) {
	const y = date.getFullYear();
	const m = String(date.getMonth() + 1).padStart(2, '0');
	const d = String(date.getDate()).padStart(2, '0');
	return `${y}-${m}-${d}`;
}

function writeReport() {
	const passCount = results.filter((r) => r.ok).length;
	const failCount = results.filter((r) => !r.ok).length;
	console.log(`\nSummary: ${passCount} pass, ${failCount} fail`);
	results.forEach((r) => console.log(`  [${r.ok ? 'OK' : 'FAIL'}] ${r.name}${r.detail ? ' — ' + r.detail : ''}`));

	const repoRoot = path.resolve(__dirname, '../..');
	const outFile = path.join(repoRoot, 'docs/project-extension/release-qa-hotel-order-lifecycle-verify.json');
	const report = {
		taskId: 'release-qa-hotel-order-lifecycle',
		verifiedAt: new Date().toISOString(),
		baseUrl: BASE,
		phone: PHONE,
		passCount,
		failCount,
		allPassed: failCount === 0 && failures.length === 0,
		stateCoverage: {
			UNPAID: !!stateCovers['status UNPAID'],
			CANCELLED: !!stateCovers['status CANCELLED unpaid'] || !!stateCovers['status CANCELLED refund'],
			PENDING_USE: !!stateCovers['status PENDING_USE'],
			REFUNDING: !!stateCovers['status REFUNDING'],
			COMPLETED: !!stateCovers['status COMPLETED'],
			VERIFIED: !!stateCovers['status verified check-in']
		},
		results,
		failures
	};
	fs.mkdirSync(path.dirname(outFile), { recursive: true });
	fs.writeFileSync(outFile, JSON.stringify(report, null, 2), 'utf8');
	console.log(`Report: ${outFile}`);
}

main().catch((err) => {
	console.error('FATAL', err);
	process.exit(1);
});
