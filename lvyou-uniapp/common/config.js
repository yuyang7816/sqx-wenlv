/** 荣e游 API 基址（本地对接 SqxApplication，端口见 application.yml） */
import { USER_H5_ORIGIN } from './domain.js';

/** 本地 server API 根地址（不含 /sqx_fast） */
const LOCAL_API_ORIGIN = 'http://127.0.0.1:9186';
/** true=本地 server；false=线上（与 USER_H5_ORIGIN 同域） */
const USE_LOCAL_SERVER = false;

function toWsOrigin(httpOrigin) {
	if (httpOrigin.startsWith('https://')) {
		return `wss://${httpOrigin.slice('https://'.length)}`;
	}
	if (httpOrigin.startsWith('http://')) {
		return `ws://${httpOrigin.slice('http://'.length)}`;
	}
	return httpOrigin;
}

const apiOrigin = USE_LOCAL_SERVER ? LOCAL_API_ORIGIN : USER_H5_ORIGIN;

const config = {
	APIHOST: `${apiOrigin}/sqx_fast`,
	WSHOST1: `${toWsOrigin(apiOrigin)}/sqx_fast/ws`
};

export default config;
export { USER_H5_ORIGIN };
