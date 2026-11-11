/**
 * 从 luyingzulin 库同步微信/支付宝支付配置到 rongeyou 库
 * 用法: node scripts/sync-pay-config-from-luyingzulin.js
 */
const mysql = require('D:/ideaproject/luyingshebeizulin_work/luyingzulin_admin/node_modules/mysql2/promise');

const SOURCE_DB = process.env.SOURCE_DB || 'luyingzulin';
const TARGET_DB = process.env.TARGET_DB || 'rongeyou';
const DB_HOST = process.env.DB_HOST || '127.0.0.1';
const DB_USER = process.env.DB_USER || 'root';
const DB_PASSWORD = process.env.DB_PASSWORD || 'hanstg';
const USER_H5_DOMAIN = process.env.USER_H5_DOMAIN || 'http://127.0.0.1:9186';

const PAY_KEYS = [
  'pay_ali_appid',
  'pay_ali_public_key',
  'pay_ali_private_key',
  'pay_ali_mode',
  'pay_ali_sandbox',
  'pay_ali_app_cert_path',
  'pay_ali_alipay_public_cert_path',
  'pay_ali_root_cert_path',
  'pay_wx_mch_id',
  'pay_wx_mch_key',
  'pay_wechat_mch_id',
  'pay_wechat_mch_key',
  'pay_wx_key_path',
  'sys_wx_key_path',
  'pay_wx_app_id',
  'pay_wx_h5_app_id',
  'pay_wx_jsapi_app_id',
  'pay_wechat_app_appid',
  'pay_wechat_h5_appid',
  'pay_wechat_jsapi_appid',
  'sys_wechat_app_appid',
  'sys_wechat_h5_appid',
  'sys_wechat_jsapi_appid',
  'sys_user_ma_appid',
  'sys_user_mp_appid',
  'sys_user_app_wx_appid',
];

const DOMAIN_KEYS = [
  { key: 'sys_h5_domain', value: USER_H5_DOMAIN, remark: '用户端 H5 域名', type: 'sys', desc: '域名' },
  { key: 'sys_domain', value: USER_H5_DOMAIN, remark: '用户端业务域名', type: 'sys', desc: '域名' },
  {
    key: 'file_public_base_url',
    value: `${USER_H5_DOMAIN.replace(/\/$/, '')}/sqx_fast`,
    remark: '公网 API 前缀（支付回调）',
    type: 'sys',
    desc: '域名',
  },
];

async function upsertConfig(conn, row) {
  const [existing] = await conn.query(
    `SELECT id FROM \`${TARGET_DB}\`.sys_config WHERE param_key = ? LIMIT 1`,
    [row.param_key]
  );

  if (existing.length) {
    await conn.query(
      `UPDATE \`${TARGET_DB}\`.sys_config
       SET param_value = ?, status = ?, remark = ?, param_type = ?, param_type_desc = ?
       WHERE param_key = ?`,
      [row.param_value, row.status, row.remark, row.param_type, row.param_type_desc, row.param_key]
    );
    return 'updated';
  }

  await conn.query(
    `INSERT INTO \`${TARGET_DB}\`.sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [row.param_key, row.param_value, row.status, row.remark, row.param_type, row.param_type_desc]
  );
  return 'inserted';
}

async function main() {
  const conn = await mysql.createConnection({
    host: DB_HOST,
    user: DB_USER,
    password: DB_PASSWORD,
    charset: 'utf8mb4',
  });

  const [sourceRows] = await conn.query(
    `SELECT param_key, param_value, status, remark, param_type, param_type_desc
     FROM \`${SOURCE_DB}\`.sys_config
     WHERE param_key IN (?)
     ORDER BY param_key`,
    [PAY_KEYS]
  );

  if (!sourceRows.length) {
    throw new Error(`源库 ${SOURCE_DB} 未找到支付配置`);
  }

  let updated = 0;
  let inserted = 0;

  for (const row of sourceRows) {
    const action = await upsertConfig(conn, row);
    if (action === 'updated') updated += 1;
    else inserted += 1;
    const preview = String(row.param_value || '');
    console.log(
      `${row.param_key}: ${preview.length > 60 ? `${preview.slice(0, 60)}... (${preview.length})` : preview}`
    );
  }

  console.log('\n--- 域名配置 ---');
  for (const item of DOMAIN_KEYS) {
    const row = {
      param_key: item.key,
      param_value: item.value,
      status: 1,
      remark: item.remark,
      param_type: item.type,
      param_type_desc: item.desc,
    };
    const action = await upsertConfig(conn, row);
    if (action === 'updated') updated += 1;
    else inserted += 1;
    console.log(`${item.key}: ${item.value}`);
  }

  await conn.end();
  console.log(`\n同步完成：更新 ${updated} 条，新增 ${inserted} 条（支付 ${sourceRows.length} 条 + 域名 ${DOMAIN_KEYS.length} 条）`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
