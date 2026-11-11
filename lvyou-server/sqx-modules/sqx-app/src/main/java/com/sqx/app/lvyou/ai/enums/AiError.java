package com.sqx.app.lvyou.ai.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * AI 问答错误码
 */
public enum AiError implements IErrorCode {

    AI_PLATFORM_NOT_FOUND(350200, "AI 供应商配置无效"),
    CHAT_FAILED(350201, "AI 回复失败，请稍后重试"),
    ;

    private final Integer code;
    private final String message;

    AiError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
