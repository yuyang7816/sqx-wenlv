/**
 * 从 iconfont（Ant Design Icons 图标库）渲染 PNG，经 file/upload 上传后回写 messages-icons.js
 *
 * 用法：node upload-msg-icons.js
 * 依赖：sharp、form-data（npm install sharp form-data --no-save）
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const sharp = require('sharp');

const API = process.env.UPLOAD_API || 'http://localhost:9165/sqx_fast/file/upload';
const ROOT = path.resolve(__dirname, '../..');
const TMP_DIR = path.join(ROOT, 'lvyou-uniapp/static/msg/_tmp');
const ICONS_JS = path.join(ROOT, 'lvyou-uniapp/common/messages-icons.js');
const DETAIL_VUE = path.join(ROOT, 'lvyou-uniapp/pages/msg/detail.vue');

/** iconfont Ant Design Icons 常用路径 */
const ICONS = [
	{
		key: 'system',
		filename: 'icon-system.png',
		gradient: ['#FF8A3D', '#FF6600'],
		path:
			'M512 792c32.8 0 59.2-26.4 59.2-59.2H452.8c0 32.8 26.4 59.2 59.2 59.2zm256-148.8c0-89.6-53.6-167.2-132-200.8V340c0-32.8-26.4-59.2-59.2-59.2h-19.2c-21.6 0-40.8 12-50.4 29.6L396 372c-78.4 33.6-132 111.2-132 200.8v19.2h704v-19.2z',
		badge: true
	},
	{
		key: 'activity',
		filename: 'icon-activity.png',
		gradient: ['#52C41A', '#2E9F0D'],
		path:
			'M880 184H752v-56c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56H344v-56c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56H144c-17.7 0-32 14.3-32 32v664c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V216c0-17.7-14.3-32-32-32zm-40 656H184V460h656v380zM184 392V256h128v48c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-48h256v48c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-48h128v136H184z',
		check: true
	}
];

function buildSvg(icon) {
	const [c1, c2] = icon.gradient;
	const badgeSvg = icon.badge
		? '<circle cx="720" cy="320" r="64" fill="#FF4D4F"/><path d="M720 288v64M688 320h64" stroke="#fff" stroke-width="16" stroke-linecap="round"/>'
		: '';
	const checkSvg = icon.check
		? '<circle cx="720" cy="560" r="56" fill="#FF6600"/><path d="m698 560 18 18 36-36" stroke="#fff" stroke-width="16" stroke-linecap="round" stroke-linejoin="round" fill="none"/>'
		: '';
	return `<svg xmlns="http://www.w3.org/2000/svg" width="192" height="192" viewBox="0 0 1024 1024">
  <defs>
    <linearGradient id="bg" x1="80" y1="80" x2="944" y2="944" gradientUnits="userSpaceOnUse">
      <stop stop-color="${c1}"/>
      <stop offset="1" stop-color="${c2}"/>
    </linearGradient>
  </defs>
  <circle cx="512" cy="512" r="512" fill="url(#bg)"/>
  <g transform="translate(512 512) scale(0.58) translate(-512 -512)">
    <path d="${icon.path}" fill="#ffffff"/>
  </g>
  ${badgeSvg}
  ${checkSvg}
</svg>`;
}

async function renderPng(icon, outPath) {
	await sharp(Buffer.from(buildSvg(icon))).png().toFile(outPath);
}

function uploadFile(filePath) {
	const output = execFileSync(
		'curl.exe',
		['-s', '-X', 'POST', API, '-F', `file=@${filePath}`],
		{ encoding: 'utf8' }
	);
	const body = JSON.parse(output);
	if (!body || body.code !== 0 || !body.data) {
		throw new Error(`上传失败: ${output}`);
	}
	return body.data.path || body.data.relativePath;
}

function writeMessagesIcons(urls) {
	const content = `/**
 * 站内消息类型图标（CDN 图片，列表/详情共用）
 * 资源来源：iconfont Ant Design Icons，经 file/upload 上传
 */
export const MSG_ICON_BY_CATEGORY = {
\tsystem: '${urls.system}',
\tactivity: '${urls.activity}'
};

/** 详情页头部大图（同类型不同尺寸资源） */
export const MSG_ICON_DETAIL_BY_CATEGORY = {
\tsystem: '${urls.system}',
\tactivity: '${urls.activity}'
};

export function getMessageListIcon(category) {
\treturn MSG_ICON_BY_CATEGORY[category] || MSG_ICON_BY_CATEGORY.system;
}

export function getMessageDetailIcon(category) {
\treturn MSG_ICON_DETAIL_BY_CATEGORY[category] || MSG_ICON_DETAIL_BY_CATEGORY.system;
}
`;
	fs.writeFileSync(ICONS_JS, content, 'utf8');
}

function patchDetailVue(systemUrl) {
	let text = fs.readFileSync(DETAIL_VUE, 'utf8');
	if (!text.includes("from '@/common/messages-icons.js'")) {
		text = text.replace(
			"import { fetchMessageInfo, markMessageRead, mapMessageVo } from '@/common/api/messages.js';",
			"import { fetchMessageInfo, markMessageRead, mapMessageVo } from '@/common/api/messages.js';\nimport { getMessageDetailIcon, getMessageListIcon } from '@/common/messages-icons.js';"
		);
	}
	text = text.replace(
		/\ticon: '[^']*',\n\ticonDetail: '[^']*',/,
		`\ticon: getMessageListIcon('system'),\n\ticonDetail: getMessageDetailIcon('system'),`
	);
	fs.writeFileSync(DETAIL_VUE, text, 'utf8');
}

async function main() {
	fs.mkdirSync(TMP_DIR, { recursive: true });
	const urls = {};

	for (const icon of ICONS) {
		const filePath = path.join(TMP_DIR, icon.filename);
		console.log(`[render] ${icon.key} -> ${filePath}`);
		await renderPng(icon, filePath);
		console.log(`[upload] ${icon.key}`);
		urls[icon.key] = uploadFile(filePath);
		console.log(`[done] ${icon.key}: ${urls[icon.key]}`);
	}

	writeMessagesIcons(urls);
	patchDetailVue(urls.system);

	const legacySvgs = [
		path.join(ROOT, 'lvyou-uniapp/static/msg/icon-system.svg'),
		path.join(ROOT, 'lvyou-uniapp/static/msg/icon-activity.svg')
	];
	for (const svg of legacySvgs) {
		if (fs.existsSync(svg)) {
			fs.unlinkSync(svg);
			console.log(`[remove] ${svg}`);
		}
	}

	console.log('\n上传完成，已更新 messages-icons.js 与 detail.vue');
	console.log(JSON.stringify(urls, null, 2));
}

main().catch((err) => {
	console.error(err);
	process.exit(1);
});
