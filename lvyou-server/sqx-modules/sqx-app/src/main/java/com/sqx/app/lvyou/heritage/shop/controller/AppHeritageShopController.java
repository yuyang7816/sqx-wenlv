package com.sqx.app.lvyou.heritage.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopAppListBo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopAppVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端非遗店铺
 */
@Tag(name = "用户端-非遗店铺", description = "用户端-非遗店铺")
@RestController
@RequestMapping("/app/heritage/shop")
public class AppHeritageShopController {

    @Resource
    private HeritageShopService heritageShopService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "店铺列表（分页，仅营业中）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopAppVo.class)))
    @GetMapping("list")
    public Result<IPage<HeritageShopAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                                 @RequestParam(value = "lng", required = false) String lng,
                                                 @RequestParam(value = "name", required = false) String name,
                                                 @RequestParam(value = "category", required = false) String category,
                                                 @RequestParam(value = "page", required = false) Long page,
                                                 @RequestParam(value = "limit", required = false) Long limit,
                                                 @RequestParam(value = "size", required = false) Long size) {
        HeritageShopAppListBo listBo = new HeritageShopAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setName(name);
        listBo.setCategory(category);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(heritageShopService.appPage(listBo));
    }

    @Operation(summary = "店铺详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopAppVo.class)))
    @GetMapping("{id}")
    public Result<HeritageShopAppVo> detail(@PathVariable("id") Long id) {
        return Result.success(heritageShopService.appDetail(id));
    }
}
