#!/usr/bin/env node
/**
 * Move pages/mobile-design -> mobile-design (subpackage root)
 * and update pages.json + all route references.
 */
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const srcDir = path.join(root, 'pages', 'mobile-design');
const destDir = path.join(root, 'mobile-design');

function walk(dir, out = []) {
	if (!fs.existsSync(dir)) return out;
	for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
		const p = path.join(dir, entry.name);
		if (entry.name === 'node_modules' || entry.name === 'uni_modules' || entry.name === '.git') continue;
		if (entry.isDirectory()) walk(p, out);
		else if (/\.(vue|js|json|ts|scss|md)$/.test(entry.name)) out.push(p);
	}
	return out;
}

function fixPagesJson() {
	const pagesJsonPath = path.join(root, 'pages.json');
	const raw = fs.readFileSync(pagesJsonPath, 'utf8');
	const stripped = raw.replace(/\/\/.*$/gm, '').replace(/,\s*([\]}])/g, '$1');
	const config = JSON.parse(stripped);

	const PREFIX = 'pages/mobile-design/';
	const MD_PREFIX = 'mobile-design/';
	const mainPages = [];
	const subPages = [];

	for (const page of config.pages) {
		if (page.path.startsWith(PREFIX)) {
			subPages.push({ path: page.path.slice(PREFIX.length), style: page.style || {} });
		} else if (page.path.startsWith(MD_PREFIX)) {
			subPages.push({ path: page.path.slice(MD_PREFIX.length), style: page.style || {} });
		} else {
			mainPages.push(page);
		}
	}

	if (subPages.length) {
		config.pages = mainPages;
		config.subPackages = [{ root: 'mobile-design', pages: subPages }];
		fs.writeFileSync(pagesJsonPath, JSON.stringify(config, null, '\t') + '\n', 'utf8');
	}
	console.log('pages.json: main pages =', mainPages.length, ', subpackage pages =', subPages.length);
}

// 1. Move directory
if (!fs.existsSync(destDir)) {
	if (!fs.existsSync(srcDir)) {
		console.error('Source not found:', srcDir);
		process.exit(1);
	}
	fs.renameSync(srcDir, destDir);
	console.log('Moved:', srcDir, '->', destDir);
} else if (fs.existsSync(srcDir)) {
	console.error('Both src and dest exist; manual merge required');
	process.exit(1);
} else {
	console.log('Already moved:', destDir);
}

// 2. Split pages.json into main package + subPackage (before path replace)
fixPagesJson();

// 3. Replace navigation paths in source files (skip pages.json)
const skipDirs = new Set(['node_modules', 'uni_modules', '.git', 'dist', 'unpackage']);
const files = walk(root).filter((f) => !f.split(path.sep).some((p) => skipDirs.has(p)));

let replacedFiles = 0;
for (const file of files) {
	if (file.endsWith('migrate-mobile-design-subpackage.js')) continue;
	if (file.endsWith('pages.json')) continue;
	let content = fs.readFileSync(file, 'utf8');
	const next = content
		.replace(/\/pages\/mobile-design\//g, '/mobile-design/')
		.replace(/pages\/mobile-design\//g, 'mobile-design/');
	if (next !== content) {
		fs.writeFileSync(file, next, 'utf8');
		replacedFiles++;
	}
}
console.log('Updated references in', replacedFiles, 'files');
