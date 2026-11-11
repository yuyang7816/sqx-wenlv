package com.sqx.app.lvyou.ai.support;

import com.sqx.app.lvyou.ai.domain.vo.AiChatLinkVo;
import com.sqx.app.lvyou.ai.domain.vo.AiChatVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AI 问答规则 Mock：按关键词匹配 FAQ，未命中返回默认引导语。
 */
public final class AiChatRuleSupport {

    /** 未命中 FAQ 时的默认引导语 */
    public static final String DEFAULT_REPLY =
            "感谢您的提问。您可以在首页浏览「景区旅游」「旅游线路」「特色美食」等模块，或前往「地图导航」查看周边信息。"
                    + "如需人工帮助，请进入「我的 → 客服中心」。";

    private AiChatRuleSupport() {
    }

    public static AiChatVo match(String message) {
        String input = StringUtils.trimToEmpty(message);
        AiChatVo vo = new AiChatVo();
        vo.setLinks(new ArrayList<AiChatLinkVo>());

        if (StringUtils.isBlank(input)) {
            vo.setContent(DEFAULT_REPLY);
            vo.setMatched(Boolean.FALSE);
            return vo;
        }

        for (FaqRule rule : FAQ_RULES) {
            if (rule.matches(input)) {
                vo.setContent(rule.getReply());
                vo.setMatched(Boolean.TRUE);
                vo.setLinks(copyLinks(rule.getLinks()));
                return vo;
            }
        }

        vo.setContent(DEFAULT_REPLY);
        vo.setMatched(Boolean.FALSE);
        return vo;
    }

    private static List<AiChatLinkVo> copyLinks(List<AiChatLinkVo> links) {
        if (links == null || links.isEmpty()) {
            return new ArrayList<AiChatLinkVo>();
        }
        List<AiChatLinkVo> copied = new ArrayList<AiChatLinkVo>(links.size());
        for (AiChatLinkVo link : links) {
            AiChatLinkVo item = new AiChatLinkVo();
            item.setLabel(link.getLabel());
            item.setPath(link.getPath());
            copied.add(item);
        }
        return copied;
    }

    private static AiChatLinkVo link(String label, String path) {
        AiChatLinkVo vo = new AiChatLinkVo();
        vo.setLabel(label);
        vo.setPath(path);
        return vo;
    }

    private static final List<FaqRule> FAQ_RULES = Collections.unmodifiableList(Arrays.asList(
            new FaqRule(
                    Arrays.asList("古镇", "古街", "老城"),
                    "可在「景区旅游」浏览周边古镇与人文景点，结合地图导航规划半日或一日游行程。",
                    Arrays.asList(
                            link("查看景区列表", "/mobile-design/scenic/list"),
                            link("地图导航", "/mobile-design/map/index?type=navigation")
                    )
            ),
            new FaqRule(
                    Arrays.asList("美食", "特色", "吃", "餐厅"),
                    "可在「美食地图」查看周边餐饮与特色美食，也可浏览平台推荐的热门风味门店。",
                    Arrays.asList(
                            link("特色美食", "/mobile-design/goose/list"),
                            link("美食地图", "/mobile-design/map/index?type=food")
                    )
            ),
            new FaqRule(
                    Arrays.asList("线路", "一日游", "行程"),
                    "平台提供多种主题旅游线路，支持在线查看详情并预约，可按天数与兴趣筛选。",
                    Collections.singletonList(link("旅游线路", "/mobile-design/route/list"))
            ),
            new FaqRule(
                    Arrays.asList("酒店", "住宿", "入住"),
                    "可在酒店模块按位置、价格与房型筛选周边住宿，支持在线预约。",
                    Collections.singletonList(link("酒店住宿", "/mobile-design/hotel/list"))
            ),
            new FaqRule(
                    Arrays.asList("活动", "演出", "非遗"),
                    "「精彩活动」模块汇总近期非遗体验、户外与美食主题活动，部分活动支持查看详情。",
                    Collections.singletonList(link("精彩活动", "/mobile-design/activity/list"))
            ),
            new FaqRule(
                    Arrays.asList("交通", "公交", "坐车", "怎么去"),
                    "可在「公共交通」查看线路、站点与时刻表，支持实时位置查询。",
                    Collections.singletonList(link("公共交通", "/mobile-design/transport/list"))
            ),
            new FaqRule(
                    Arrays.asList("地图", "导航", "停车"),
                    "地图导航支持美食、停车与线路指引三类切换，方便您规划出行。",
                    Collections.singletonList(link("打开地图", "/mobile-design/map/index?type=navigation"))
            ),
            new FaqRule(
                    Arrays.asList("陶艺", "手作", "体验"),
                    "可在「精彩活动」与「非遗」频道查看周边文化体验与研学项目，支持在线预约。",
                    Collections.singletonList(link("精彩活动", "/mobile-design/activity/list"))
            ),
            new FaqRule(
                    Arrays.asList("优惠", "券", "领券"),
                    "可在「我的 → 领券中心」领取平台优惠券，酒店、线路、好物下单时均可选择抵扣。",
                    Arrays.asList(
                            link("领券中心", "/pages/my/coupon-center"),
                            link("我的优惠券", "/pages/my/coupons")
                    )
            ),
            new FaqRule(
                    Arrays.asList("公告", "通知"),
                    "最新文旅公告与交通调整可在「平台公告」查看，首页公告栏也会滚动展示重点信息。",
                    Collections.singletonList(link("平台公告", "/mobile-design/notice/list"))
            )
    ));

    private static final class FaqRule {

        private final List<String> keywords;
        private final String reply;
        private final List<AiChatLinkVo> links;

        private FaqRule(List<String> keywords, String reply, List<AiChatLinkVo> links) {
            this.keywords = keywords;
            this.reply = reply;
            this.links = links;
        }

        private boolean matches(String input) {
            for (String keyword : keywords) {
                if (input.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

        private String getReply() {
            return reply;
        }

        private List<AiChatLinkVo> getLinks() {
            return links;
        }
    }
}
