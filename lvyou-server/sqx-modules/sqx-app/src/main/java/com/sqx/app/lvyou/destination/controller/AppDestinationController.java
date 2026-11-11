package com.sqx.app.lvyou.destination.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.domain.bo.DestinationSwitchBo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationAppVo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationMerchantVo;
import com.sqx.app.lvyou.destination.service.DestinationService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端目的地
 */
@Tag(name = "用户端-目的地", description = "用户端-目的地")
@RestController
@RequestMapping("/app/destination")
public class AppDestinationController {

    @Resource
    private DestinationService destinationService;

    @Operation(summary = "目的地列表（仅已上线，按排序升序）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = DestinationAppVo.class)))
    @GetMapping("list")
    public Result<List<DestinationAppVo>> list() {
        return Result.success(destinationService.listOnline());
    }

    @Operation(summary = "当前目的地（按经纬度匹配）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DestinationAppVo.class)))
    @GetMapping("current")
    public Result<DestinationAppVo> current(@RequestParam("lat") String lat,
                                          @RequestParam("lng") String lng) {
        return Result.success(destinationService.getCurrent(lat, lng));
    }

    @Operation(summary = "当前位置目的地下已审核商户列表")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = DestinationMerchantVo.class)))
    @GetMapping("current/merchants")
    public Result<List<DestinationMerchantVo>> currentMerchants(@RequestParam("lat") String lat,
                                                                @RequestParam("lng") String lng) {
        return Result.success(destinationService.listCurrentMerchants(lat, lng));
    }

    @Operation(summary = "切换目的地")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = DestinationAppVo.class)))
    @PostMapping("switch")
    public Result<DestinationAppVo> switchDestination(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                      @RequestBody @Validated DestinationSwitchBo switchBo) {
        return Result.success(destinationService.switchDestination(userId, switchBo));
    }
}
