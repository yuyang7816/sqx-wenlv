package com.sqx.app.lvyou.help.util;

import com.sqx.app.lvyou.help.domain.entity.HelpFaqEntity;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqCategoryVo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqItemVo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqListVo;
import com.sqx.app.lvyou.help.enums.HelpFaqCategory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FAQ 分类分组工具
 */
public final class HelpFaqGroupUtil {

    private HelpFaqGroupUtil() {
    }

    public static HelpFaqListVo groupByCategory(List<HelpFaqEntity> entities) {
        HelpFaqListVo listVo = new HelpFaqListVo();
        if (CollectionUtils.isEmpty(entities)) {
            listVo.setCategories(new ArrayList<>());
            return listVo;
        }

        Map<String, HelpFaqCategoryVo> categoryMap = new LinkedHashMap<>();
        for (HelpFaqEntity entity : entities) {
            String categoryCode = StringUtils.defaultString(entity.getCategoryCode());
            HelpFaqCategoryVo categoryVo = categoryMap.computeIfAbsent(categoryCode, code -> {
                HelpFaqCategoryVo vo = new HelpFaqCategoryVo();
                vo.setCategoryCode(code);
                vo.setCategoryName(resolveCategoryName(entity));
                vo.setItems(new ArrayList<>());
                return vo;
            });

            HelpFaqItemVo itemVo = new HelpFaqItemVo();
            itemVo.setId(entity.getId());
            itemVo.setQuestion(entity.getQuestion());
            itemVo.setAnswer(entity.getAnswer());
            categoryVo.getItems().add(itemVo);
        }

        listVo.setCategories(new ArrayList<>(categoryMap.values()));
        return listVo;
    }

    private static String resolveCategoryName(HelpFaqEntity entity) {
        if (StringUtils.isNotBlank(entity.getCategoryName())) {
            return entity.getCategoryName();
        }
        return HelpFaqCategory.resolveLabel(entity.getCategoryCode());
    }
}
