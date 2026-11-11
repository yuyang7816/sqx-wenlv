package com.sqx.app.lvyou.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminPageBo;
import com.sqx.app.lvyou.order.domain.bo.OrderAppActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderGoodsCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderListBo;
import com.sqx.app.lvyou.order.domain.vo.OrderAdminVo;
import com.sqx.app.lvyou.order.domain.vo.OrderAppVo;
import com.sqx.app.lvyou.order.domain.vo.OrderCreateResultVo;
import com.sqx.app.lvyou.order.domain.vo.OrderRefundPreviewVo;

/**
 * 统一订单业务服务
 */
public interface OrderService {

    OrderCreateResultVo create(Long userId, OrderCreateBo createBo);

    OrderCreateResultVo createGoods(Long userId, OrderGoodsCreateBo createBo);

    IPage<OrderAppVo> appPage(Long userId, OrderListBo listBo);

    OrderAppVo appDetail(Long userId, String orderKey);

    OrderAppVo cancel(Long userId, String orderKey);

    OrderAppVo appAction(Long userId, String orderKey, OrderAppActionBo actionBo);

    OrderRefundPreviewVo refundPreview(Long userId, String orderKey);

    IPage<OrderAdminVo> adminPage(OrderAdminPageBo pageBo);

    OrderAdminVo adminDetail(Long orderId);

    OrderAdminVo adminAction(OrderAdminActionBo actionBo);

    OrderRefundPreviewVo adminRouteRefundPreview(Long orderId, Integer refundRatio);
}
