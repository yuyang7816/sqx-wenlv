package com.sqx.app.lvyou.heritage.goods.util;

import com.alibaba.fastjson.JSON;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsSaveBo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsAppVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsSpecSkuVo;
import com.sqx.app.lvyou.heritage.util.HeritageJsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 非遗商品 JSON 字段工具
 */
public final class GoodsJsonUtil {

    private GoodsJsonUtil() {
    }

    public static String specsToJson(List<String> specs) {
        return HeritageJsonUtil.toJsonArray(specs);
    }

    public static List<String> parseSpecs(String raw) {
        return GoodsSpecsUtil.toNameList(GoodsSpecsUtil.parseGoodsSpecs(raw, null, null));
    }

    public static List<GoodsSpecSkuVo> parseSpecSkus(String raw, java.math.BigDecimal fallbackPrice, Integer fallbackStock) {
        return GoodsSpecsUtil.parseGoodsSpecs(raw, fallbackPrice, fallbackStock);
    }

    public static String bannersToJson(List<GoodsSaveBo.BannerItem> banners) {
        if (CollectionUtils.isEmpty(banners)) {
            return "[]";
        }
        List<GoodsAppVo.BannerVo> items = new ArrayList<>();
        for (GoodsSaveBo.BannerItem banner : banners) {
            if (banner == null || StringUtils.isBlank(banner.getImage())) {
                continue;
            }
            GoodsAppVo.BannerVo vo = new GoodsAppVo.BannerVo();
            vo.setImage(banner.getImage().trim());
            items.add(vo);
        }
        return JSON.toJSONString(items);
    }

    public static List<GoodsAppVo.BannerVo> parseBanners(String raw) {
        List<GoodsAppVo.BannerVo> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<GoodsAppVo.BannerVo> items = JSON.parseArray(raw.trim(), GoodsAppVo.BannerVo.class);
            if (CollectionUtils.isNotEmpty(items)) {
                for (GoodsAppVo.BannerVo item : items) {
                    if (item != null && StringUtils.isNotBlank(item.getImage())) {
                        result.add(item);
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }

    public static String detailImagesToJson(List<String> detailImages) {
        if (CollectionUtils.isEmpty(detailImages)) {
            return "[]";
        }
        List<String> items = new ArrayList<>();
        for (String image : detailImages) {
            if (StringUtils.isNotBlank(image)) {
                items.add(image.trim());
            }
        }
        return JSON.toJSONString(items);
    }

    public static List<String> parseDetailImages(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<String> items = JSON.parseArray(raw.trim(), String.class);
            if (CollectionUtils.isNotEmpty(items)) {
                for (String item : items) {
                    if (StringUtils.isNotBlank(item)) {
                        result.add(item.trim());
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }
}
