package com.sqx.app.lvyou.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.notice.domain.bo.NoticeAppListBo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeAppVo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeMarqueeVo;
import com.sqx.app.lvyou.notice.service.NoticeService;
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
 * 用户端公告
 */
@Tag(name = "用户端-公告", description = "用户端-公告")
@RestController
@RequestMapping("/app/notice")
public class AppNoticeController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "公告列表（分页，仅已发布且未过期）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = NoticeAppVo.class)))
    @GetMapping("list")
    public Result<IPage<NoticeAppVo>> list(@RequestParam(value = "page", required = false) Long page,
                                           @RequestParam(value = "limit", required = false) Long limit,
                                           @RequestParam(value = "lat", required = false) String lat,
                                           @RequestParam(value = "lng", required = false) String lng,
                                           @RequestParam(value = "category", required = false) String category) {
        NoticeAppListBo listBo = new NoticeAppListBo();
        listBo.setPage(page);
        listBo.setLimit(limit);
        listBo.setDestinationId(destinationSupport.tryResolveAppDestinationId(lat, lng));
        listBo.setCategory(category);
        return Result.success(noticeService.appPage(listBo));
    }

    @Operation(summary = "首页公告栏（当前置顶且已发布的1条）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = NoticeMarqueeVo.class)))
    @GetMapping("marquee")
    public Result<NoticeMarqueeVo> marquee(@RequestParam(value = "lat", required = false) String lat,
                                           @RequestParam(value = "lng", required = false) String lng,
                                           @RequestParam(value = "destinationId", required = false) String destinationId) {
        Long resolvedDestinationId = destinationSupport.resolveBrowseDestinationId(destinationId, lat, lng);
        return Result.success(noticeService.marquee(resolvedDestinationId));
    }

    @Operation(summary = "公告详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = NoticeAppVo.class)))
    @GetMapping("{id}")
    public Result<NoticeAppVo> detail(@PathVariable("id") Long id) {
        return Result.success(noticeService.appDetail(id));
    }
}
