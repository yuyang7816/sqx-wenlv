package com.sqx.app.lvyou.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.hotel.domain.bo.*;
import com.sqx.app.lvyou.hotel.domain.vo.*;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;

import java.util.List;

/**
 * 酒店业务服务
 */
public interface HotelService {

    /**
     * 查询首页推荐酒店
     */
    List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit);

    /**
     * 用户端酒店分页列表（仅上架）
     */
    IPage<HotelAppVo> appPage(HotelAppListBo listBo);

    /**
     * 用户端酒店详情（含房型）
     */
    HotelAppDetailVo appDetail(Long id, Long userId);

    /**
     * 用户端房型列表（仅在售，可选日期报价）
     */
    List<HotelRoomAppVo> appRooms(Long hotelId, String checkInDate, String checkOutDate);

    /**
     * 商户维护房型日历
     */
    List<HotelRoomCalendarDayVo> merchantRoomCalendar(Long userId, Long roomId, String startDate, String endDate);

    void merchantSaveRoomCalendar(Long userId, Long roomId, HotelRoomCalendarSaveBo saveBo);

    /**
     * 用户端房型列表（仅在售）
     */
    default List<HotelRoomAppVo> appRooms(Long hotelId) {
        return appRooms(hotelId, null, null);
    }

    /**
     * 用户端房型详情
     */
    HotelRoomDetailAppVo appRoomDetail(Long hotelId, Long roomId, Long userId);

    /**
     * 用户端酒店评价列表
     */
    IPage<ReviewAppVo> appReviews(HotelReviewAppListBo listBo);

    /**
     * 用户端提交酒店评价
     */
    ReviewAppVo submitReview(Long userId, Long hotelId, HotelReviewSubmitBo submitBo);

    /**
     * 管理端酒店分页
     */
    IPage<HotelVo> pageInfo(HotelPageBo pageBo);

    /**
     * 管理端新增酒店
     */
    void saveInfo(HotelSaveBo saveBo);

    /**
     * 管理端更新酒店
     */
    void updateInfo(HotelUpdateBo updateBo);

    /**
     * 管理端删除酒店
     */
    void delete(HotelInfoBo infoBo);

    /**
     * 管理端房型分页
     */
    IPage<HotelRoomVo> roomPageInfo(HotelRoomPageBo pageBo);

    /**
     * 管理端新增房型
     */
    void saveRoom(HotelRoomSaveBo saveBo);

    /**
     * 管理端更新房型
     */
    void updateRoom(HotelRoomUpdateBo updateBo);

    /**
     * 管理端删除房型
     */
    void deleteRoom(HotelRoomInfoBo infoBo);
}
