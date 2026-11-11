package com.sqx.app.lvyou.canteen.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminActionBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminPageBo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationAdminVo;
import com.sqx.app.lvyou.canteen.service.CanteenReservationService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端餐饮预约监管
 */
@Tag(name = "管理端-餐饮预约", description = "管理端-餐饮预约")
@RestController
@RequestMapping("/admin/canteen")
public class AdminCanteenController {

    @Resource
    private CanteenReservationService canteenReservationService;

    @Operation(summary = "餐饮预约分页列表")
    @RequiresPermissions("lvyou:canteen:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationAdminVo.class)))
    @PostMapping("page")
    public Result<IPage<CanteenReservationAdminVo>> page(@RequestBody @Validated CanteenReservationAdminPageBo pageBo) {
        return Result.success(canteenReservationService.adminPage(pageBo));
    }

    @Operation(summary = "餐饮预约详情")
    @RequiresPermissions("lvyou:canteen:info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationAdminVo.class)))
    @GetMapping("{id}")
    public Result<CanteenReservationAdminVo> detail(@PathVariable("id") Long id) {
        return Result.success(canteenReservationService.adminDetail(id));
    }

    @Operation(summary = "餐饮预约操作（确认/取消/完成）")
    @RequiresPermissions("lvyou:canteen:action")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationAdminVo.class)))
    @PostMapping("action")
    public Result<CanteenReservationAdminVo> action(@RequestBody @Validated CanteenReservationAdminActionBo actionBo) {
        return Result.success(canteenReservationService.adminAction(actionBo));
    }
}
