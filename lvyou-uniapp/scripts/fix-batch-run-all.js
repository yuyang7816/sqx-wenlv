#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const scripts = [
	'fix-batch-part1.js',
	'fix-batch-my.js',
	'fix-batch-my2.js',
	'fix-batch-profile-goose.js',
	'fix-batch-remaining.js',
	'fix-batch-remaining-b.js',
	'fix-batch-remaining-c.js',
	'fix-batch-remaining-d.js',
	'fix-batch-auto-strings.js'
];

for (const s of scripts) {
	const p = path.join(__dirname, s);
	if (!fs.existsSync(p)) {
		console.log('skip (missing):', s);
		continue;
	}
	console.log('\n>>> running', s);
	require(p);
}

console.log('\n>>> all batch fixes executed');
