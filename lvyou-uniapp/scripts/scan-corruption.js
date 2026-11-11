/**
 * 扫描源码中的 UTF-8 损坏特征（连续 ??、替换字符、乱码）。
 * 用法：node scripts/scan-corruption.js
 * 退出码：0=通过，1=发现问题
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const SCAN_DIRS = ['pages', 'common', 'mobile-design', 'merchant-portal'];
const EXT = new Set(['.vue', '.js', '.ts', '.json']);

function walk(dir, out = []) {
	if (!fs.existsSync(dir)) return out;
	for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
		if (entry.name === 'node_modules' || entry.name === 'unpackage') continue;
		const p = path.join(dir, entry.name);
		if (entry.isDirectory()) walk(p, out);
		else if (EXT.has(path.extname(entry.name))) out.push(p);
	}
	return out;
}

function inspectFile(file) {
	const s = fs.readFileSync(file, 'utf8');
	const issues = [];
	if (/(?<![?])\?\?\?\?(?!\?)/.test(s)) issues.push('连续 ??（疑似中文损坏）');
	if (/\uFFFD/.test(s)) issues.push('Unicode 替换字符');
	if (/ï¿½/.test(s)) issues.push('UTF-8 乱码 mojibake');
	if (/<[^>]*\?\/text>/.test(s)) issues.push('损坏的 text 标签');
	return issues;
}

const bad = [];
for (const dir of SCAN_DIRS) {
	for (const file of walk(path.join(ROOT, dir))) {
		const issues = inspectFile(file);
		if (issues.length) {
			bad.push({
				file: path.relative(ROOT, file).replace(/\\/g, '/'),
				issues
			});
		}
	}
}

if (bad.length) {
	console.error('[scan-corruption] 发现编码损坏：');
	for (const item of bad) {
		console.error(`  - ${item.file}: ${item.issues.join('、')}`);
	}
	process.exit(1);
}

console.log('[scan-corruption] 通过，未发现 ?? 乱码');
process.exit(0);
