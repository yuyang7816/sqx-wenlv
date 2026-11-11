package com.sqx.app.lvyou.order.service;

import com.sqx.app.lvyou.order.domain.bo.ServiceRecordSubmitBo;
import com.sqx.app.lvyou.order.domain.vo.ServiceRecordSubmitVo;

/**
 * 服务预约业务服务
 */
public interface ServiceRecordService {

    ServiceRecordSubmitVo submit(Long userId, ServiceRecordSubmitBo submitBo);
}
