const mysql = require('mysql2/promise');

(async () => {
	const conn = await mysql.createConnection({
		host: '127.0.0.1',
		user: 'root',
		password: 'hanstg',
		database: 'rongeyou'
	});
	const [tables] = await conn.query("SHOW TABLES LIKE 'tb_hotel_room_standard'");
	console.log('tb_hotel_room_standard exists:', tables.length > 0);
	const [cols1] = await conn.query("SHOW COLUMNS FROM tb_merchant_service LIKE 'room_standard_id'");
	console.log('tb_merchant_service.room_standard_id:', cols1.length > 0 ? 'OK' : 'MISSING');
	const [cols2] = await conn.query("SHOW COLUMNS FROM tb_hotel_room LIKE 'room_standard_id'");
	console.log('tb_hotel_room.room_standard_id:', cols2.length > 0 ? 'OK' : 'MISSING');
	const [standards] = await conn.query('SELECT COUNT(*) AS cnt FROM tb_hotel_room_standard');
	console.log('standards total:', standards[0].cnt);
	const [linkedMs] = await conn.query('SELECT COUNT(*) AS cnt FROM tb_merchant_service WHERE room_standard_id IS NOT NULL');
	console.log('linked merchant_service:', linkedMs[0].cnt);
	const [linkedRoom] = await conn.query('SELECT COUNT(*) AS cnt FROM tb_hotel_room WHERE room_standard_id IS NOT NULL');
	console.log('linked hotel_room:', linkedRoom[0].cnt);
	const [sample] = await conn.query('SELECT id, hotel_id, merchant_id, name FROM tb_hotel_room_standard LIMIT 5');
	console.log('sample:', sample);
	await conn.end();
})().catch((err) => {
	console.error('FAIL:', err.message);
	process.exit(1);
});
