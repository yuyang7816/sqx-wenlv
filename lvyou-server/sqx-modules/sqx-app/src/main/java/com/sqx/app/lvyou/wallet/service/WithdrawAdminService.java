package com.sqx.app.lvyou.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminAuditBo;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminPageBo;
import com.sqx.app.lvyou.wallet.domain.vo.WithdrawAdminVo;

/**
 * 管理端提现审核服务
 */
public interface WithdrawAdminService {

    IPage<WithdrawAdminVo> page(WithdrawAdminPageBo pageBo);

    void audit(WithdrawAdminAuditBo auditBo);
}
