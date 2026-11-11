/**
 * 同步协议正文到 sys_config（管理端平台配置 → 协议配置）
 *
 * 用法：
 *   node lvyou-server/scripts/sync-agreement-content.js
 *
 * 环境变量：
 *   ADMIN_API_BASE  默认依次尝试 192.168.0.119:9186 / 127.0.0.1:9186
 *   ADMIN_USER      默认 admin
 *   ADMIN_PASS      默认 123456
 *   DB_HOST / DB_USER / DB_PASS / DB_NAME  用于读取验证码（可选）
 */

const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

const ADMIN_USER = process.env.ADMIN_USER || 'admin';
const ADMIN_PASS = process.env.ADMIN_PASS || '123456';
const LOCAL_DB = {
	host: '127.0.0.1',
	user: 'root',
	password: 'hanstg',
	database: 'rongeyou',
};

function resolveDbConfig() {
	if (process.env.DB_HOST) {
		return {
			host: process.env.DB_HOST,
			user: process.env.DB_USER || 'root',
			password: process.env.DB_PASS || '',
			database: process.env.DB_NAME || 'rongeyou',
		};
	}
	return LOCAL_DB;
}

const API_CANDIDATES = (
	process.env.ADMIN_API_BASE
		? [process.env.ADMIN_API_BASE.replace(/\/$/, '')]
		: [
				'http://192.168.0.119:9186/sqx_fast',
				'http://127.0.0.1:9186/sqx_fast',
			]
).map((u) => u.replace(/\/$/, ''));

const AGREEMENT_DIR = path.resolve(__dirname, '../sql/agreement');
const AGREEMENT_KEYS = [
	{ paramKey: 'agreement_user', file: 'agreement_user.html' },
	{ paramKey: 'agreement_privacy', file: 'agreement_privacy.html' },
	{ paramKey: 'agreement_about', file: 'agreement_about.html' },
];

function randomUuid() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
		const r = (Math.random() * 16) | 0;
		const v = c === 'x' ? r : (r & 0x3) | 0x8;
		return v.toString(16);
	});
}

function request(method, url, { headers = {}, body = null, timeout = 30000 } = {}) {
	const lib = url.startsWith('https') ? https : http;
	return new Promise((resolve, reject) => {
		const urlObj = new URL(url);
		const payload = body == null ? null : JSON.stringify(body);
		const req = lib.request(
			{
				hostname: urlObj.hostname,
				port: urlObj.port || (urlObj.protocol === 'https:' ? 443 : 80),
				path: urlObj.pathname + urlObj.search,
				method,
				headers: {
					...(payload ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) } : {}),
					...headers,
				},
				timeout,
				rejectUnauthorized: false,
			},
			(res) => {
				const chunks = [];
				res.on('data', (c) => chunks.push(c));
				res.on('end', () => {
					const raw = Buffer.concat(chunks).toString('utf8');
					let json = null;
					try {
						json = JSON.parse(raw);
					} catch (e) {
						/* ignore */
					}
					resolve({ status: res.statusCode, json, raw, headers: res.headers });
				});
			}
		);
		req.on('error', reject);
		req.on('timeout', () => req.destroy(new Error('request timeout')));
		if (payload) req.write(payload);
		req.end();
	});
}

async function readCaptchaFromDb(uuid, apiBase) {
	try {
		const mysql = require('mysql2/promise');
		const db = resolveDbConfig(apiBase);
		const conn = await mysql.createConnection(db);
		const [rows] = await conn.query('SELECT code FROM sys_captcha WHERE uuid = ? LIMIT 1', [uuid]);
		await conn.end();
		return rows[0]?.code || null;
	} catch (e) {
		return null;
	}
}

async function ensureParamValueCapacity(conn) {
	const [cols] = await conn.query("SHOW COLUMNS FROM sys_config LIKE 'param_value'");
	const type = String(cols[0]?.Type || '').toLowerCase();
	if (type.startsWith('varchar') || type === 'text') {
		await conn.query('ALTER TABLE sys_config MODIFY COLUMN param_value MEDIUMTEXT NULL COMMENT \'参数值\'');
		console.log('OK expanded sys_config.param_value -> MEDIUMTEXT');
	}
}

async function updateAgreementsViaDb(contents, apiBase) {
	const mysql = require('mysql2/promise');
	const db = resolveDbConfig(apiBase);
	const conn = await mysql.createConnection(db);
	await ensureParamValueCapacity(conn);
	for (const { paramKey, html } of contents) {
		const [result] = await conn.query('UPDATE sys_config SET param_value = ? WHERE param_key = ?', [html, paramKey]);
		if (result.affectedRows === 0) {
			await conn.query(
				`INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
         VALUES (?, ?, '1', ?, 'agreement', '协议配置')`,
				[paramKey, html, paramKey]
			);
		}
	}
	await conn.end();
	return true;
}

async function tryLogin(base) {
	const uuid = randomUuid();
	await request('GET', `${base}/captcha.jpg?uuid=${encodeURIComponent(uuid)}&t=${Date.now()}`);
	let captcha = await readCaptchaFromDb(uuid, base);
	if (!captcha) {
		return { ok: false, reason: '无法从数据库读取验证码（请确认 MySQL 与 API 为同一套环境）' };
	}
	const loginResp = await request('POST', `${base}/sys/login`, {
		body: { username: ADMIN_USER, password: ADMIN_PASS, uuid, captcha },
	});
	if (loginResp.status !== 200 || !loginResp.json || loginResp.json.code !== 0) {
		return {
			ok: false,
			reason: loginResp.json?.msg || loginResp.raw?.slice(0, 200) || `HTTP ${loginResp.status}`,
		};
	}
	const token = loginResp.json.token || loginResp.json.data?.token;
	if (!token) {
		return { ok: false, reason: '登录成功但未返回 token' };
	}
	return { ok: true, token, base };
}

async function syncViaApi(base, token, contents) {
	const listResp = await request('GET', `${base}/sys/config/list`, {
		headers: { token },
	});
	if (listResp.status !== 200 || !listResp.json || listResp.json.code !== 0) {
		throw new Error(listResp.json?.msg || '读取配置列表失败');
	}
	const groups = listResp.json.data || listResp.json.list || [];
	const agreementGroup = groups.find((g) => g.paramType === 'agreement');
	const items = agreementGroup?.list || agreementGroup?.sysConfigEntityList || [];
	const indexed = {};
	items.forEach((item) => {
		if (item?.paramKey) indexed[item.paramKey] = item;
	});

	for (const { paramKey, html } of contents) {
		const entity = indexed[paramKey];
		if (entity?.id) {
			const updateResp = await request('POST', `${base}/sys/config/update`, {
				headers: { token },
				body: { ...entity, paramValue: html },
			});
			if (updateResp.status !== 200 || updateResp.json?.code !== 0) {
				throw new Error(`${paramKey} 更新失败: ${updateResp.json?.msg || updateResp.raw}`);
			}
			console.log(`OK update ${paramKey} id=${entity.id}`);
		} else {
			const saveResp = await request('POST', `${base}/sys/config/save`, {
				headers: { token },
				body: {
					paramKey,
					paramValue: html,
					status: '1',
					remark: paramKey,
					paramType: 'agreement',
					paramTypeDesc: '协议配置',
				},
			});
			if (saveResp.status !== 200 || saveResp.json?.code !== 0) {
				throw new Error(`${paramKey} 新建失败: ${saveResp.json?.msg || saveResp.raw}`);
			}
			console.log(`OK create ${paramKey}`);
		}
	}
	const refreshResp = await request('GET', `${base}/sys/config/refresh`, { headers: { token } });
	if (refreshResp.json?.code !== 0) {
		console.warn('WARN config refresh:', refreshResp.json?.msg || refreshResp.raw);
	} else {
		console.log('OK config cache refreshed');
	}
}

async function main() {
	const contents = AGREEMENT_KEYS.map(({ paramKey, file }) => ({
		paramKey,
		html: fs.readFileSync(path.join(AGREEMENT_DIR, file), 'utf8').trim(),
	}));

	console.log('协议正文已加载，共', contents.length, '项');

	for (const base of API_CANDIDATES) {
		console.log('\n尝试 API:', base);
		try {
			const ping = await request('GET', `${base}/captcha.jpg?uuid=ping-test`);
			if (ping.status >= 500) {
				console.log('跳过（服务不可用）:', ping.status);
				continue;
			}
		} catch (e) {
			console.log('跳过（连接失败）:', e.message);
			continue;
		}

		const login = await tryLogin(base);
		if (!login.ok) {
			console.log('登录失败:', login.reason);
			continue;
		}
		console.log('登录成功');
		await syncViaApi(login.base, login.token, contents);
		console.log('\n完成：已通过管理端 API 写入协议配置');
		return;
	}

	console.log('\nAPI 方式均失败，尝试直连 MySQL 更新 sys_config ...');
	const dbBase = API_CANDIDATES.find((u) => /lywladmin|9186|127\.0\.0\.1/.test(u)) || API_CANDIDATES[0];
	await updateAgreementsViaDb(contents, dbBase);
	console.log('完成：已通过 MySQL 写入协议配置（请在管理端刷新或重启服务后查看）');
}

main().catch((err) => {
	console.error('FAIL:', err.message || err);
	process.exit(1);
});
