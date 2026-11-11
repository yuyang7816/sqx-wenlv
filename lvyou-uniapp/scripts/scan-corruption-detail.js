const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..', 'pages');

function walk(dir, out = []) {
	for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
		const p = path.join(dir, entry.name);
		if (entry.isDirectory()) walk(p, out);
		else if (entry.name.endsWith('.vue')) out.push(p);
	}
	return out;
}

const brokenTags = [];
const scriptCorrupt = [];
const templateCorrupt = [];

for (const file of walk(root)) {
	const rel = file.replace(/\\/g, '/').replace(/.*\/pages\//, 'pages/');
	const s = fs.readFileSync(file, 'utf8');
	const template = (s.split('<template')[1] || '').split('</template>')[0] || '';
	const script = (s.split('<script')[1] || '').split('</script>')[0] || '';

	if (/<[^>]*\?\/text>/.test(template) || /chip\">[^<]*\?\/view>/.test(template) || /\?\?\?\?/.test(template)) {
		brokenTags.push(rel);
	}
	if (/\?\?\?\?/.test(script) || (/[\uFFFD]/.test(script) && /['"`][^'"`]*[\uFFFD]/.test(script))) {
		scriptCorrupt.push(rel);
	}
	if (/[\uFFFD]/.test(template) && !brokenTags.includes(rel)) {
		templateCorrupt.push(rel);
	}
}

console.log('broken HTML tags:', brokenTags.length, brokenTags.slice(0, 20));
console.log('script string corruption:', scriptCorrupt.length, scriptCorrupt.slice(0, 20));
console.log('template other FFFD:', templateCorrupt.length, templateCorrupt.slice(0, 20));
