package com.sqx.app.lvyou.order.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.order.domain.bo.AddressDeleteBo;
import com.sqx.app.lvyou.order.domain.bo.AddressSaveBo;
import com.sqx.app.lvyou.order.domain.vo.AddressVo;
import com.sqx.app.lvyou.order.service.UserAddressService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端收货地址
 */
@Tag(name = "用户端-收货地址", description = "用户端-收货地址")
@RestController
@RequestMapping("/app/address")
public class AppAddressController {

    @Resource
    private UserAddressService userAddressService;

    @Operation(summary = "地址列表")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = AddressVo.class)))
    @GetMapping("list")
    public Result<List<AddressVo>> list(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(userAddressService.listByUser(userId));
    }

    @Operation(summary = "保存地址（新增或编辑）")
    @Login
    @PostMapping("save")
    public Result<Long> save(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                             @RequestBody @Validated AddressSaveBo saveBo) {
        return Result.success(userAddressService.save(userId, saveBo));
    }

    @Operation(summary = "删除地址")
    @Login
    @PostMapping("delete")
    public Result<Void> delete(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                               @RequestBody @Validated AddressDeleteBo deleteBo) {
        userAddressService.delete(userId, deleteBo);
        return Result.success();
    }
}
