package com.sqx.app.lvyou.merchant.controller;

import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreTypeSaveBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreTypeVo;
import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理端商户入驻类型配置
 */
@Tag(name = "管理端-商户入驻类型", description = "管理端-商户入驻类型与保证金")
@RestController
@RequestMapping("/admin/merchant/store-type")
public class AdminMerchantStoreTypeController {

    @Resource
    private MerchantStoreTypeConfigService merchantStoreTypeConfigService;

    @Operation(summary = "入驻类型列表")
    @RequiresPermissions("lvyou:merchant:page")
    @GetMapping("list")
    public Result<List<MerchantStoreTypeVo>> list() {
        return Result.success(merchantStoreTypeConfigService.listAll());
    }

    @Operation(summary = "新增入驻类型")
    @RequiresPermissions(value = {"lvyou:merchant:save", "lvyou:merchant:update", "lvyou:merchant:adjust", "lvyou:merchant:page"}, logical = Logical.OR)
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MerchantStoreTypeSaveBo saveBo) {
        merchantStoreTypeConfigService.save(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新入驻类型")
    @RequiresPermissions(value = {"lvyou:merchant:update", "lvyou:merchant:adjust", "lvyou:merchant:page"}, logical = Logical.OR)
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MerchantStoreTypeSaveBo saveBo) {
        merchantStoreTypeConfigService.update(saveBo);
        return Result.success();
    }

    @Operation(summary = "删除入驻类型")
    @RequiresPermissions(value = {"lvyou:merchant:delete", "lvyou:merchant:update", "lvyou:merchant:adjust", "lvyou:merchant:page"}, logical = Logical.OR)
    @PostMapping("delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        merchantStoreTypeConfigService.delete(id);
        return Result.success();
    }
}
