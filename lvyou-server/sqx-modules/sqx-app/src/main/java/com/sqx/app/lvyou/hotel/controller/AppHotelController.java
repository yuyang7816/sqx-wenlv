package com.sqx.app.lvyou.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.lvyou.hotel.domain.bo.HotelAppListBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelReviewAppListBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelReviewSubmitBo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelAppDetailVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelAppVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomAppVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomDetailAppVo;
import com.sqx.app.lvyou.hotel.service.HotelService;
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
import java.util.List;

/**
 * 用户端酒店
 */
@Tag(name = "用户端-酒店", description = "用户端-酒店")
@RestController
@RequestMapping("/app/hotel")
public class AppHotelController {

    @Resource
    private HotelService hotelService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "酒店列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelAppVo.class)))
    @GetMapping("list")
    public Result<IPage<HotelAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                          @RequestParam(value = "lng", required = false) String lng,
                                          @RequestParam(value = "category", required = false) String category,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "priceOrder", required = false) String priceOrder,
                                          @RequestParam(value = "distanceOrder", required = false) String distanceOrder,
                                          @RequestParam(value = "page", required = false) Long page,
                                          @RequestParam(value = "limit", required = false) Long limit,
                                          @RequestParam(value = "size", required = false) Long size) {
        HotelAppListBo listBo = new HotelAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setCategory(category);
        listBo.setSort(sort);
        listBo.setPriceOrder(priceOrder);
        listBo.setDistanceOrder(distanceOrder);
        listBo.setLat(lat);
        listBo.setLng(lng);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(hotelService.appPage(listBo));
    }

    @Operation(summary = "酒店详情（含房型、图片、设施）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelAppDetailVo.class)))
    @GetMapping("{id}")
    public Result<HotelAppDetailVo> detail(@PathVariable("id") Long id,
                                           @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(hotelService.appDetail(id, userId));
    }

    @Operation(summary = "房型列表（仅在售）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomAppVo.class)))
    @GetMapping("{id}/rooms")
    public Result<List<HotelRoomAppVo>> rooms(@PathVariable("id") Long id,
                                             @RequestParam(value = "checkInDate", required = false) String checkInDate,
                                             @RequestParam(value = "checkOutDate", required = false) String checkOutDate) {
        return Result.success(hotelService.appRooms(id, checkInDate, checkOutDate));
    }

    @Operation(summary = "房型详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomDetailAppVo.class)))
    @GetMapping("{hotelId}/rooms/{roomId}")
    public Result<HotelRoomDetailAppVo> roomDetail(@PathVariable("hotelId") Long hotelId,
                                                     @PathVariable("roomId") Long roomId,
                                                     @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(hotelService.appRoomDetail(hotelId, roomId, userId));
    }

    @Operation(summary = "酒店评价列表")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @GetMapping("{id}/reviews")
    public Result<IPage<ReviewAppVo>> reviews(@PathVariable("id") Long id,
                                              @RequestParam(value = "page", required = false) Long page,
                                              @RequestParam(value = "limit", required = false) Long limit,
                                              @RequestParam(value = "size", required = false) Long size) {
        HotelReviewAppListBo listBo = new HotelReviewAppListBo();
        listBo.setHotelId(id);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(hotelService.appReviews(listBo));
    }

    @Operation(summary = "提交酒店评价")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @PostMapping("{id}/reviews")
    public Result<ReviewAppVo> submitReview(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                            @PathVariable("id") Long id,
                                            @RequestBody @Validated HotelReviewSubmitBo submitBo) {
        return Result.success(hotelService.submitReview(userId, id, submitBo));
    }
}
