package com.sqx.app.lvyou.order.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.lvyou.order.domain.bo.OrderGuestBo;
import com.sqx.app.lvyou.order.domain.vo.OrderGuestVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 线路出行人 JSON 解析与展示
 */
public final class OrderGuestInfoSupport {

    private static final String ROLE_BOOKER = "booker";
    private static final String ROLE_COMPANION = "companion";

    private OrderGuestInfoSupport() {
    }

    public static String toJson(List<OrderGuestBo> guestList) {
        if (CollectionUtils.isEmpty(guestList)) {
            return null;
        }
        return JSON.toJSONString(guestList);
    }

    public static List<OrderGuestBo> parseList(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<OrderGuestBo> list = JSON.parseObject(json, new TypeReference<List<OrderGuestBo>>() {
            });
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static List<String> extractNames(List<OrderGuestBo> guestList) {
        if (CollectionUtils.isEmpty(guestList)) {
            return new ArrayList<>();
        }
        return guestList.stream()
                .map(OrderGuestBo::getName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    public static List<OrderGuestVo> toVoList(String json, boolean maskSensitive) {
        List<OrderGuestVo> voList = new ArrayList<>();
        for (OrderGuestBo guest : parseList(json)) {
            OrderGuestVo vo = new OrderGuestVo();
            vo.setRole(StringUtils.defaultIfBlank(guest.getRole(), ROLE_COMPANION));
            vo.setRoleLabel(ROLE_BOOKER.equals(vo.getRole()) ? "预约人" : "同行人");
            vo.setName(StringUtils.trimToNull(guest.getName()));
            vo.setIdCard(maskSensitive ? maskIdCard(guest.getIdCard()) : StringUtils.trimToNull(guest.getIdCard()));
            vo.setPhone(maskSensitive ? maskPhone(guest.getPhone()) : StringUtils.trimToNull(guest.getPhone()));
            voList.add(vo);
        }
        return voList;
    }

    public static void validateGuestList(List<OrderGuestBo> guestList, int guestCount) {
        if (CollectionUtils.isEmpty(guestList) || guestList.size() != guestCount) {
            throw new IllegalArgumentException("出行人数量与出行人数不一致");
        }
        Set<String> idCards = new LinkedHashSet<>();
        for (int i = 0; i < guestList.size(); i++) {
            OrderGuestBo guest = guestList.get(i);
            String role = i == 0 ? ROLE_BOOKER : ROLE_COMPANION;
            guest.setRole(role);
            String name = StringUtils.trimToNull(guest.getName());
            if (name == null || name.length() < 2 || name.length() > 20) {
                throw new IllegalArgumentException("出行人姓名格式不正确");
            }
            String idCard = normalizeIdCard(guest.getIdCard());
            if (!isValidIdCard(idCard)) {
                throw new IllegalArgumentException("身份证号格式不正确");
            }
            if (!idCards.add(idCard)) {
                throw new IllegalArgumentException("同一订单内身份证号不可重复");
            }
            guest.setIdCard(idCard);
            guest.setName(name);
        }
    }

    private static String normalizeIdCard(String idCard) {
        return StringUtils.upperCase(StringUtils.trimToEmpty(idCard));
    }

    private static boolean isValidIdCard(String idCard) {
        return idCard.matches("^[1-9]\\d{16}[\\dX]$");
    }

    private static String maskIdCard(String idCard) {
        String text = StringUtils.trimToEmpty(idCard);
        if (text.length() < 10) {
            return text;
        }
        return text.substring(0, 3) + "********" + text.substring(text.length() - 4);
    }

    private static String maskPhone(String phone) {
        String text = StringUtils.trimToEmpty(phone);
        if (text.length() < 7) {
            return text;
        }
        return text.substring(0, 3) + "****" + text.substring(text.length() - 4);
    }
}
