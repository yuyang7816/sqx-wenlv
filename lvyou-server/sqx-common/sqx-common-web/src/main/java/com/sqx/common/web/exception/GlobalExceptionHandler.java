package com.sqx.common.web.exception;

import com.sqx.common.core.domain.Result;
import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.exception.PayException;
import com.sqx.common.core.exception.SqxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author chengming
 * @since 2024/7/11
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = BizException.class)
    public Result<String> handleBizException(BizException e) {
        if (e.getErrorCode() != null) {
            return Result.failed(e.getErrorCode());
        }

        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(value = PayException.class)
    public Result<String> handlePayException(PayException e) {
        if (e.getErrorCode() != null) {
            return Result.failed(e.getErrorCode());
        }

        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(value = SqxException.class)
    public Result<String> handleSqxException(SqxException e) {
        return Result.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<String> handlerNoFoundException(Exception e) {
        logger.error(e.getMessage(), e);
        return Result.failed(SystemErrorCode.NO_HANDLER_FOUND);
    }

    /**
     * 参数校验异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }

        return Result.failed(message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        logger.error(e.getMessage(), e);
        String duplicateKeyMessage = resolveDuplicateKeyMessage(e);
        if (duplicateKeyMessage != null) {
            return Result.failed(duplicateKeyMessage);
        }
        String migrationHint = resolveMigrationHint(e);
        if (migrationHint != null) {
            return Result.failed(migrationHint);
        }
        return Result.failed();
    }

    private String resolveDuplicateKeyMessage(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("duplicate entry") || lower.contains("duplicate key")) {
                    if (lower.contains("mobile") || lower.contains("uk_sys_user_mobile")) {
                        return "已有使用该手机号的用户";
                    }
                    return "数据库中已存在该记录";
                }
            }
            current = current.getCause();
        }
        return null;
    }

    private String resolveMigrationHint(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("Unknown column")) {
                String column = extractUnknownColumn(message);
                String script = mapMigrationScript(column);
                if (script != null) {
                    return "数据库 wenlv 缺少字段 " + column + "，请在当前连接库执行：lvyou-server/sql/migration/" + script;
                }
                return "数据库结构未更新（缺少字段: " + (column == null ? "未知" : column)
                        + "），请确认已在 wenlv 库执行 lvyou-server/sql/migration 下对应迁移脚本";
            }
            if (message != null && message.contains("doesn't exist")) {
                String table = extractMissingTable(message);
                String script = mapTableMigrationScript(table);
                if (script != null) {
                    return "数据库 wenlv 缺少表 " + table + "，请在当前连接库执行：lvyou-server/sql/migration/" + script;
                }
                return "数据库结构未更新（缺少表: " + (table == null ? "未知" : table)
                        + "），请确认已在 wenlv 库执行 lvyou-server/sql/migration 下对应迁移脚本";
            }
            current = current.getCause();
        }
        return null;
    }

    private String extractUnknownColumn(String message) {
        int start = message.indexOf("Unknown column '");
        if (start < 0) {
            return null;
        }
        start += "Unknown column '".length();
        int end = message.indexOf('\'', start);
        if (end <= start) {
            return null;
        }
        return message.substring(start, end);
    }

    private String extractMissingTable(String message) {
        int start = message.indexOf("Table '");
        if (start < 0) {
            return null;
        }
        start += "Table '".length();
        int end = message.indexOf('\'', start);
        if (end <= start) {
            return null;
        }
        String qualified = message.substring(start, end);
        int dot = qualified.lastIndexOf('.');
        return dot >= 0 ? qualified.substring(dot + 1) : qualified;
    }

    private String mapMigrationScript(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "check_out_time":
                return "V20261044_hotel_check_out_time.sql";
            case "policy_json":
                return "V20261045_hotel_policy_json.sql";
            case "star_label":
                return "V20261042_merchant_content_publish_link.sql";
            case "merchant_service_id":
                return "V20261042_merchant_content_publish_link.sql";
            case "spec":
                return "V20261042_merchant_content_publish_link.sql";
            case "meet_point":
            case "spots":
            case "goods_specs":
            case "detail":
                return "V20261043_merchant_service_publish_fields.sql";
            case "latitude":
            case "longitude":
                return "V20261049_publish_location_latlng.sql";
            default:
                return null;
        }
    }

    private String mapTableMigrationScript(String table) {
        if (table == null) {
            return null;
        }
        switch (table) {
            case "tb_heritage_shop_category":
                return "V20260925_lvyou_heritage_shop_category.sql";
            default:
                return null;
        }
    }
}
