package com.sqx.app.base.user.cancel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountAuditBo;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountPageBo;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountSubmitBo;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountSubmitVo;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountVo;

public interface CancelAccountService {

    CancelAccountSubmitVo submit(Long userId, CancelAccountSubmitBo submitBo);

    IPage<CancelAccountVo> page(CancelAccountPageBo pageBo);

    void audit(CancelAccountAuditBo auditBo);
}
