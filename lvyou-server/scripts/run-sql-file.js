/**
 * 执行单个 SQL 文件（支持多语句）
 * 用法: node run-sql-file.js <sql-file> [host] [user] [pass] [db]
 */
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
	const sqlFile = process.argv[2];
	if (!sqlFile) {
		console.error('Usage: node run-sql-file.js <sql-file>');
		process.exit(1);
	}
	const host = process.argv[3] || '127.0.0.1';
	const user = process.argv[4] || 'root';
	const pass = process.argv[5] || 'hanstg';
	const database = process.argv[6] || 'rongeyou';
	const absPath = path.resolve(sqlFile);
	const sql = fs.readFileSync(absPath, 'utf8');

	const conn = await mysql.createConnection({
		host,
		user,
		password: pass,
		database,
		multipleStatements: true,
		charset: 'utf8mb4'
	});

	try {
		console.log(`Executing: ${absPath}`);
		console.log(`Database: ${user}@${host}/${database}`);
		await conn.query(sql);
		console.log('OK: SQL executed successfully.');
	} finally {
		await conn.end();
	}
}

main().catch((err) => {
	console.error('FAIL:', err.message);
	process.exit(1);
});
