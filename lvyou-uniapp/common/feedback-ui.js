/** 意见反馈页 UI 文案与类型选项 */

export const FEEDBACK_UI = {
	heroTitle: '您的反馈很重要',
	heroDesc: '我们会认真阅读每条反馈，并在 1-3 个工作日内处理',
	sectionType: '反馈类型',
	sectionContent: '问题描述',
	contentPlaceholder: '请详细描述您遇到的问题或改进建议，便于我们快速定位',
	sectionImages: '上传截图',
	imagesTip: '最多 3 张，选填',
	imagesAdd: '添加图片',
	sectionContact: '联系方式',
	typePlaceholder: '请选择反馈类型',
	typePopupTitle: '选择反馈类型',
	phonePlaceholder: '请输入11位手机号',
	submitBtn: '提交反馈',
	footerTip: '提交即表示您同意我们使用反馈信息改进产品与服务',
	toastContent: '请填写反馈内容',
	toastContentMin: '反馈内容至少 10 个字',
	toastType: '请选择反馈类型',
	toastPhone: '请输入正确手机号',
	toastSuccess: '提交成功',
	toastImageLimit: '最多上传 3 张图片',
	toastUploadFail: '图片上传失败',
	toastSubmitFail: '提交失败，请稍后重试',
	toastNetworkError: '网络异常，请稍后重试',
	loginTip: '请先登录'
};

/** 反馈类型选项（与服务端 FeedbackType 中文标签一致） */
export const FEEDBACK_TYPE_OPTIONS = [
	{ name: '功能异常', icon: 'warning-fill', bgColor: '#FFF3EB', iconColor: '#FF6600' },
	{ name: '界面体验', icon: 'eye-fill', bgColor: '#EAF2FF', iconColor: '#2979FF' },
	{ name: '订单问题', icon: 'file-text-fill', bgColor: '#FFF8E6', iconColor: '#FFB800' },
	{ name: '建议反馈', icon: 'edit-pen-fill', bgColor: '#E8F8EF', iconColor: '#2EAF6A' },
	{ name: '其他', icon: 'more-circle-fill', bgColor: '#F3E8F8', iconColor: '#9C27B0' }
];
