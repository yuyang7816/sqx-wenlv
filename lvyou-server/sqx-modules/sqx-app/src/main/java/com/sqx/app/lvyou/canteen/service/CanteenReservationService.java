package com.sqx.app.lvyou.canteen.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminActionBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminPageBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationListBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationSubmitBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationUpdateBo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationAdminVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationPendingCountVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationSubmitVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationVo;

/**
 * 餐饮预约业务服务
 */
public interface CanteenReservationService {

    CanteenReservationSubmitVo submit(Long userId, CanteenReservationSubmitBo submitBo);

    IPage<CanteenReservationVo> page(Long userId, CanteenReservationListBo listBo);

    CanteenReservationVo detail(Long userId, String id);

    CanteenReservationPendingCountVo pendingCount(Long userId);

    CanteenReservationVo cancel(Long userId, String id);

    CanteenReservationVo update(Long userId, String id, CanteenReservationUpdateBo updateBo);

    /**
     * 商户订单操作后同步预约状态
     */
    void syncStatusByOrderId(Long orderId, String action);

    IPage<CanteenReservationAdminVo> adminPage(CanteenReservationAdminPageBo pageBo);

    CanteenReservationAdminVo adminDetail(Long reservationId);

    CanteenReservationAdminVo adminAction(CanteenReservationAdminActionBo actionBo);
}
