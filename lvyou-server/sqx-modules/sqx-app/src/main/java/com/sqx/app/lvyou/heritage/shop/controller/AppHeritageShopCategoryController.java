package com.sqx.app.lvyou.heritage.shop.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryOptionVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopCategoryService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端非遗好店分类
 */
@Tag(name = "用户端-非遗好店分类", description = "用户端-非遗好店分类")
@RestController
@RequestMapping("/app/heritage/shop/category")
public class AppHeritageShopCategoryController {

    @Resource
    private HeritageShopCategoryService heritageShopCategoryService;

    @Operation(summary = "启用中的分类选项")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopCategoryOptionVo.class)))
    @GetMapping("options")
    public Result<List<HeritageShopCategoryOptionVo>> options() {
        return Result.success(heritageShopCategoryService.listEnabledOptions());
    }
}
