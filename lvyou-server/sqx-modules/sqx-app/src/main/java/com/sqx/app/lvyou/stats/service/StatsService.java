package com.sqx.app.lvyou.stats.service;

import com.sqx.app.lvyou.stats.domain.bo.StatsTrendBo;
import com.sqx.app.lvyou.stats.domain.vo.MenuBadgeVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsOverviewVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsTrendVo;

/**
 * 管理端业务统计
 */
public interface StatsService {

    /**
     * 业务概览：今日/本月 GMV、订单总量、待审核商户、新增用户等
     */
    StatsOverviewVo overview();

    /**
     * 近 N 日订单与 GMV 趋势（默认 30 天）
     */
    StatsTrendVo trend(StatsTrendBo trendBo);

    /**
     * 侧边栏菜单角标：待审核/待处理等待办数量
     */
    MenuBadgeVo menuBadges();
}
