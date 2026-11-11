export const getWxAppid = () => {
	return 'wxbf1429781694727b';
};
export const getInvitation = () => {
	return uni.getStorageSync('publicRelation');
};
export const getAppDownUrl = () => {
	return uni.getStorageSync('appurl');
};
export const publicYuMing = () => {
	return 'http://127.0.0.1:9186';
};