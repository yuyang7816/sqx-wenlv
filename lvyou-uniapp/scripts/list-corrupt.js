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

const scriptCorrupt = [];
for (const file of walk(root)) {
	const rel = file.replace(/\\/g, '/').replace(/.*\/pages\//, 'pages/');
	const s = fs.readFileSync(file, 'utf8');
	const script = (s.split('<script')[1] || '').split('</script>')[0] || '';
	if (/\?\?\?\?/.test(script) || (/[\uFFFD]/.test(script) && /['"`][^'"`]*[\uFFFD]/.test(script))) {
		scriptCorrupt.push(rel);
	}
}
console.log(scriptCorrupt.join('\n'));
