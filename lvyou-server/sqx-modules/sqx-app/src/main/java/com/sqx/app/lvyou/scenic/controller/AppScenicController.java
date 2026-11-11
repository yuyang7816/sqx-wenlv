package com.sqx.app.lvyou.scenic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicAppListBo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppDetailVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryOptionVo;
import com.sqx.app.lvyou.scenic.service.ScenicCategoryService;
import com.sqx.app.lvyou.scenic.service.ScenicService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端旅游景区
 */
@Tag(name = "用户端-旅游景区", description = "用户端-旅游景区")
@RestController
@RequestMapping("/app/scenic")
public class AppScenicController {

    @Resource
    private ScenicService scenicService;

    @Resource
    private DestinationSupport destinationSupport;

    @Resource
    private ScenicCategoryService scenicCategoryService;

    @Operation(summary = "启用中的景区分类（筛选 Tab）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicCategoryOptionVo.class)))
    @GetMapping("categories")
    public Result<List<ScenicCategoryOptionVo>> categories() {
        return Result.success(scenicCategoryService.listEnabledOptions());
    }

    @Operation(summary = "景区列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicAppVo.class)))
    @GetMapping("list")
    public Result<IPage<ScenicAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                           @RequestParam(value = "lng", required = false) String lng,
                                           @RequestParam(value = "name", required = false) String name,
                                           @RequestParam(value = "category", required = false) String category,
                                           @RequestParam(value = "page", required = false) Long page,
                                           @RequestParam(value = "limit", required = false) Long limit,
                                           @RequestParam(value = "size", required = false) Long size) {
        ScenicAppListBo listBo = new ScenicAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setName(name);
        listBo.setCategory(category);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(scenicService.appPage(listBo));
    }

    @Operation(summary = "景区详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicAppDetailVo.class)))
    @GetMapping("{id}")
    public Result<ScenicAppDetailVo> detail(@PathVariable("id") Long id,
                                            @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(scenicService.appDetail(id, userId));
    }
}
