package com.sqx.app.lvyou.order.schedule;

import com.sqx.app.lvyou.order.service.OrderLifecycleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 订单生命周期定时任务：支付超时、No-show、离店自动完成
 */
@Slf4j
@Component
public class OrderLifecycleScheduler {

    @Resource
    private OrderLifecycleService orderLifecycleService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void runLifecycleTasks() {
        try {
            int cancelled = orderLifecycleService.cancelExpiredUnpaidOrders();
            int noShow = orderLifecycleService.processHotelNoShowOrders();
            int completed = orderLifecycleService.autoCompleteHotelOrders();
            if (cancelled + noShow + completed > 0) {
                log.info("OrderLifecycleScheduler cancelled={}, noShow={}, completed={}",
                        cancelled, noShow, completed);
            }
        } catch (Exception ex) {
            log.error("OrderLifecycleScheduler failed", ex);
        }
    }
}
