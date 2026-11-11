package com.sqx.app.lvyou.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.hotel.domain.bo.HotelInfoBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelPageBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomInfoBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomPageBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomSaveBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomUpdateBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelSaveBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelUpdateBo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelVo;
import com.sqx.app.lvyou.hotel.service.HotelService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端酒店
 */
@Tag(name = "管理端-酒店", description = "管理端-酒店")
@RestController
@RequestMapping("/admin/hotel")
public class AdminHotelController {

    @Resource
    private HotelService hotelService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:hotel:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelVo.class)))
    @PostMapping("page")
    public Result<IPage<HotelVo>> page(@RequestBody @Validated HotelPageBo pageBo) {
        return Result.success(hotelService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:hotel:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HotelSaveBo saveBo) {
        hotelService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:hotel:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HotelUpdateBo updateBo) {
        hotelService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:hotel:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HotelInfoBo infoBo) {
        hotelService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "房型分页列表")
    @RequiresPermissions("lvyou:hotel:room:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomVo.class)))
    @PostMapping("room/page")
    public Result<IPage<HotelRoomVo>> roomPage(@RequestBody @Validated HotelRoomPageBo pageBo) {
        return Result.success(hotelService.roomPageInfo(pageBo));
    }

    @Operation(summary = "新增房型")
    @RequiresPermissions("lvyou:hotel:room:save")
    @PostMapping("room/save")
    public Result<Void> roomSave(@RequestBody @Validated HotelRoomSaveBo saveBo) {
        hotelService.saveRoom(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新房型")
    @RequiresPermissions("lvyou:hotel:room:update")
    @PostMapping("room/update")
    public Result<Void> roomUpdate(@RequestBody @Validated HotelRoomUpdateBo updateBo) {
        hotelService.updateRoom(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除房型")
    @RequiresPermissions("lvyou:hotel:room:delete")
    @PostMapping("room/delete")
    public Result<Void> roomDelete(@RequestBody @Validated HotelRoomInfoBo infoBo) {
        hotelService.deleteRoom(infoBo);
        return Result.success();
    }
}
