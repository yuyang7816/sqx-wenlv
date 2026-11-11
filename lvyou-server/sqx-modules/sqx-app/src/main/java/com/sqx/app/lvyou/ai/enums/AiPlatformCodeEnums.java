package com.sqx.app.lvyou.ai.enums;

import lombok.Getter;

/**
 * AI 问答供应商（对应 sys_config.ai_platform）
 */
@Getter
public enum AiPlatformCodeEnums {

    LOCAL_TEMPLATE("local_template", "本地规则回复（开发/兜底）"),
    DOUBAO("doubao", "豆包大模型"),
    ;

    private final String code;
    private final String desc;

    AiPlatformCodeEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
