package com.sqx.app.lvyou.heritage.craft.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftAppListBo;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftAppVo;
import com.sqx.app.lvyou.heritage.craft.service.HeritageCraftService;
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
 * 用户端非遗工艺
 */
@Tag(name = "用户端-非遗工艺", description = "用户端-非遗工艺")
@RestController
@RequestMapping("/app/heritage/craft")
public class AppHeritageCraftController {

    @Resource
    private HeritageCraftService heritageCraftService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "工艺列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageCraftAppVo.class)))
    @GetMapping("list")
    public Result<IPage<HeritageCraftAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                                  @RequestParam(value = "lng", required = false) String lng,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "tag", required = false) String tag,
                                                  @RequestParam(value = "page", required = false) Long page,
                                                  @RequestParam(value = "limit", required = false) Long limit,
                                                  @RequestParam(value = "size", required = false) Long size) {
        HeritageCraftAppListBo listBo = new HeritageCraftAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setName(name);
        listBo.setTag(tag);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(heritageCraftService.appPage(listBo));
    }

    @Operation(summary = "工艺详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageCraftAppVo.class)))
    @GetMapping("{id}")
    public Result<HeritageCraftAppVo> detail(@PathVariable("id") Long id) {
        return Result.success(heritageCraftService.appDetail(id));
    }
}
