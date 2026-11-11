package com.sqx.app.lvyou.merchant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyAuditBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantInfoBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantUpdateBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantAdminVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantApplyVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStatusVo;

/**
 * 商户业务服务
 */
public interface MerchantService {

    /**
     * 提交入驻申请
     */
    void apply(Long userId, MerchantApplyBo applyBo);

    /**
     * 查询当前用户入驻状态
     */
    MerchantStatusVo getStatus(Long userId);

    /**
     * 管理端分页列表
     */
    IPage<MerchantApplyVo> pageApply(MerchantApplyPageBo pageBo);

    /**
     * 管理端申请详情
     */
    MerchantApplyVo getApplyDetail(Long id);

    /**
     * 管理端审核
     */
    void audit(MerchantApplyAuditBo auditBo);

    /**
     * 管理端商户分页列表
     */
    IPage<MerchantAdminVo> pageMerchant(MerchantPageBo pageBo);

    /**
     * 管理端商户详情
     */
    MerchantAdminVo getMerchantDetail(Long id);

    /**
     * 管理端商户状态调整
     */
    MerchantAdminVo adjust(MerchantAdjustBo adjustBo);

    /**
     * 管理端新增商户
     */
    void saveMerchant(MerchantSaveBo saveBo);

    /**
     * 管理端更新商户
     */
    void updateMerchant(MerchantUpdateBo updateBo);

    /**
     * 管理端删除商户
     */
    void deleteMerchant(MerchantInfoBo infoBo);
}
