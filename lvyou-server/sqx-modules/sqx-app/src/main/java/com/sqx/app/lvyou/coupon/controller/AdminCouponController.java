package com.sqx.app.lvyou.coupon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateInfoBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplatePageBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateSaveBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateUpdateBo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponTemplateVo;
import com.sqx.app.lvyou.coupon.service.CouponService;
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
 * 管理端优惠券模板
 */
@Tag(name = "管理端-优惠券", description = "管理端-优惠券模板")
@RestController
@RequestMapping("/admin/coupon/template")
public class AdminCouponController {

    @Resource
    private CouponService couponService;

    @Operation(summary = "模板分页列表")
    @RequiresPermissions("lvyou:coupon:template:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponTemplateVo.class)))
    @PostMapping("page")
    public Result<IPage<CouponTemplateVo>> page(@RequestBody CouponTemplatePageBo pageBo) {
        return Result.success(couponService.templatePage(pageBo));
    }

    @Operation(summary = "新增模板")
    @RequiresPermissions("lvyou:coupon:template:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated CouponTemplateSaveBo saveBo) {
        couponService.templateSave(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新模板")
    @RequiresPermissions("lvyou:coupon:template:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated CouponTemplateUpdateBo updateBo) {
        couponService.templateUpdate(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @RequiresPermissions("lvyou:coupon:template:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated CouponTemplateInfoBo infoBo) {
        couponService.templateDelete(infoBo);
        return Result.success();
    }
}
