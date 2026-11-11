/**
 * 站内消息类型图标（CDN 图片，列表/详情共用）
 * 资源来源：iconfont Ant Design Icons，经 file/upload 上传
 */
export const MSG_ICON_BY_CATEGORY = {
	system: '',
	activity: ''
};

/** 详情页头部大图（同类型不同尺寸资源） */
export const MSG_ICON_DETAIL_BY_CATEGORY = {
	system: '',
	activity: ''
};

export function getMessageListIcon(category) {
	return MSG_ICON_BY_CATEGORY[category] || MSG_ICON_BY_CATEGORY.system;
}

export function getMessageDetailIcon(category) {
	return MSG_ICON_DETAIL_BY_CATEGORY[category] || MSG_ICON_DETAIL_BY_CATEGORY.system;
}
