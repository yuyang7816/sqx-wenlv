package com.sqx.app.base.map.controller;

import com.sqx.app.base.map.domain.vo.MapPickerConfigVo;
import com.sqx.app.base.map.service.MapService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端地图选点
 */
@Tag(name = "管理端-地图", description = "管理端-地图选点配置")
@RestController
@RequestMapping("/admin/map")
public class AdminMapController {

    @Resource
    private MapService mapService;

    @Operation(summary = "地图选点配置")
    @RequiresPermissions(value = {"lvyou:merchant:save", "lvyou:merchant:adjust"}, logical = Logical.OR)
    @GetMapping("picker-config")
    public Result<MapPickerConfigVo> pickerConfig() {
        return Result.success(mapService.pickerConfig());
    }
}
