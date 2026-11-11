package com.sqx.app.lvyou.kefu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImMessagePageBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImReplyBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSendBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSessionInfoBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSessionPageBo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImMessageVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSendVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSessionVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImUnreadVo;

import java.util.List;

public interface KefuImService {

    KefuImSessionVo openSession(Long userId);

    KefuImSendVo sendUserMessage(Long userId, KefuImSendBo sendBo);

    List<KefuImMessageVo> listUserMessages(Long userId, KefuImMessagePageBo pageBo);

    IPage<KefuImSessionVo> adminSessionPage(KefuImSessionPageBo pageBo);

    List<KefuImMessageVo> adminListMessages(KefuImMessagePageBo pageBo);

    KefuImSendVo adminReply(Long staffId, KefuImReplyBo replyBo);

    void adminCloseSession(Long sessionId);

    KefuImUnreadVo adminUnreadTotal();
}
