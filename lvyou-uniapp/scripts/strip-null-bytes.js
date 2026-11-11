const fs = require('fs');
const path = require('path');

const pagesDir = path.join(__dirname, '..', 'pages');

function walk(dir, files = []) {
	for (const name of fs.readdirSync(dir)) {
		const full = path.join(dir, name);
		if (fs.statSync(full).isDirectory()) walk(full, files);
		else if (name.endsWith('.vue')) files.push(full);
	}
	return files;
}

let count = 0;
for (const file of walk(pagesDir)) {
	const raw = fs.readFileSync(file);
	if (!raw.includes(0)) continue;
	const fixed = Buffer.from(raw.filter((b) => b !== 0));
	fs.writeFileSync(file, fixed);
	count += 1;
	console.log('stripped null bytes:', path.relative(pagesDir, file));
}
console.log(`done, ${count} files fixed`);
