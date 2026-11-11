package com.sqx.app.lvyou.order.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.order.domain.bo.ServiceRecordSubmitBo;
import com.sqx.app.lvyou.order.domain.vo.ServiceRecordSubmitVo;
import com.sqx.app.lvyou.order.service.ServiceRecordService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端服务预约
 */
@Tag(name = "用户端-服务预约", description = "用户端-服务预约")
@RestController
@RequestMapping("/app/service-record")
public class AppServiceRecordController {

    @Resource
    private ServiceRecordService serviceRecordService;

    @Operation(summary = "提交服务预约")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = ServiceRecordSubmitVo.class)))
    @PostMapping("submit")
    public Result<ServiceRecordSubmitVo> submit(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                              @RequestBody @Validated ServiceRecordSubmitBo submitBo) {
        return Result.success(serviceRecordService.submit(userId, submitBo));
    }
}
