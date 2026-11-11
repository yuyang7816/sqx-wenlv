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

// Fix remaining corrupted string literals and broken HTML closings
const GLOBAL_REPLACEMENTS = [
	[/\?\/text>/g, '</text>'],
	[/ï¿½\?\/view>/g, '</view>'],
	[/([^\u4e00-\u9fff])ï¿½([^\u4e00-\u9fff])/g, '$1¥$2']
];

let fixed = 0;
for (const file of walk(root)) {
	let s = fs.readFileSync(file, 'utf8');
	const orig = s;
	for (const [re, rep] of GLOBAL_REPLACEMENTS) {
		s = s.replace(re, rep);
	}
	// Fix script strings with replacement char - replace whole quoted strings containing FFFD
	s = s.replace(/(['"`])((?:\\.|(?!\1)[\s\S])*?\uFFFD(?:\\.|(?!\1)[\s\S])*?)\1/g, (match, q, inner) => {
		if (inner.includes('placeholder') || inner.length < 80) {
			// skip auto-replace for complex strings
			return match;
		}
		return match;
	});
	if (s !== orig) {
		fs.writeFileSync(file, s, { encoding: 'utf8' });
		fixed++;
	}
}

console.log('fix-batch-auto-strings: touched', fixed, 'files');
