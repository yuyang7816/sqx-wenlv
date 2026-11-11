package com.sqx.app.lvyou.heritage.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsInfoBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsPageBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsSaveBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsUpdateBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsUpdateStateBo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsVo;
import com.sqx.app.lvyou.heritage.goods.service.GoodsService;
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
 * 管理端非遗商品
 */
@Tag(name = "管理端-非遗商品", description = "管理端-非遗商品")
@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController {

    @Resource
    private GoodsService goodsService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:goods:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = GoodsVo.class)))
    @PostMapping("page")
    public Result<IPage<GoodsVo>> page(@RequestBody @Validated GoodsPageBo pageBo) {
        return Result.success(goodsService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:goods:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated GoodsSaveBo saveBo) {
        goodsService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:goods:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated GoodsUpdateBo updateBo) {
        goodsService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:goods:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated GoodsInfoBo infoBo) {
        goodsService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新上下架状态（同步商户服务）")
    @RequiresPermissions("lvyou:goods:update")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated GoodsUpdateStateBo updateStateBo) {
        goodsService.updateState(updateStateBo);
        return Result.success();
    }
}
