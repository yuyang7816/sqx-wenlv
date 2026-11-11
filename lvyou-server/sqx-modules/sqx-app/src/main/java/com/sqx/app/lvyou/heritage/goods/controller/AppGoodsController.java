package com.sqx.app.lvyou.heritage.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsAppListBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsReviewAppListBo;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsReviewSubmitBo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsAppVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.heritage.goods.service.GoodsService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端非遗商品
 */
@Tag(name = "用户端-非遗商品", description = "用户端-非遗商品")
@RestController
@RequestMapping("/app/goods")
public class AppGoodsController {

    @Resource
    private GoodsService goodsService;

    @Operation(summary = "商品列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GoodsAppVo.class)))
    @GetMapping("list")
    public Result<IPage<GoodsAppVo>> list(@RequestParam(value = "shopId", required = false) Long shopId,
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "category", required = false) String category,
                                          @RequestParam(value = "page", required = false) Long page,
                                          @RequestParam(value = "limit", required = false) Long limit,
                                          @RequestParam(value = "size", required = false) Long size) {
        GoodsAppListBo listBo = new GoodsAppListBo();
        listBo.setShopId(shopId);
        listBo.setName(name);
        listBo.setCategory(category);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(goodsService.appPage(listBo));
    }

    @Operation(summary = "商品详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GoodsAppVo.class)))
    @GetMapping("{id}")
    public Result<GoodsAppVo> detail(@PathVariable("id") Long id,
                                     @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(goodsService.appDetail(id, userId));
    }

    @Operation(summary = "商品评价列表（分页）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @GetMapping("{id}/reviews")
    public Result<IPage<ReviewAppVo>> reviews(@PathVariable("id") Long id,
                                               @RequestParam(value = "page", required = false) Long page,
                                               @RequestParam(value = "limit", required = false) Long limit,
                                               @RequestParam(value = "size", required = false) Long size) {
        GoodsReviewAppListBo listBo = new GoodsReviewAppListBo();
        listBo.setGoodsId(id);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(goodsService.appReviews(listBo));
    }

    @Operation(summary = "提交商品评价（须关联已完成订单）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @PostMapping("{id}/reviews")
    public Result<ReviewAppVo> submitReview(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                              @PathVariable("id") Long id,
                                              @RequestBody @Validated GoodsReviewSubmitBo submitBo) {
        return Result.success(goodsService.submitReview(userId, id, submitBo));
    }
}
