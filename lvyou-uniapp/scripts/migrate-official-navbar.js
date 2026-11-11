const fs = require('fs');
const path = require('path');

const pagesDir = path.join(__dirname, '..', 'pages');

function fixMojibake(text) {
	try {
		return Buffer.from(text, 'latin1').toString('utf8');
	} catch (e) {
		return text;
	}
}

function stripNavbar(text) {
	return text
		.replace(/\t?<ry-navbar[\s\S]*?<\/ry-navbar>\s*\n?/g, '')
		.replace(/\t?<ry-navbar[^>]*?\/>\s*\n?/g, '');
}

function walk(dir, files = []) {
	for (const name of fs.readdirSync(dir)) {
		const full = path.join(dir, name);
		const stat = fs.statSync(full);
		if (stat.isDirectory()) walk(full, files);
		else if (name.endsWith('.vue')) files.push(full);
	}
	return files;
}

let count = 0;
for (const file of walk(pagesDir)) {
	const raw = fs.readFileSync(file, 'utf8');
	const fixed = stripNavbar(fixMojibake(raw));
	if (fixed !== raw) {
		fs.writeFileSync(file, fixed, 'utf8');
		count += 1;
		console.log('updated:', path.relative(pagesDir, file));
	}
}
console.log(`done, ${count} files updated`);
