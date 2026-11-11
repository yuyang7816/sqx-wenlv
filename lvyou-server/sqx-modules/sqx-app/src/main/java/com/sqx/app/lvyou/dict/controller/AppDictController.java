package com.sqx.app.lvyou.dict.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.dict.domain.vo.DictItemVo;
import com.sqx.app.lvyou.dict.service.DictQueryService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端业务字典（公开白名单）
 */
@Tag(name = "用户端-业务字典", description = "用户端-业务字典")
@RestController
@RequestMapping("/app/base/dict")
public class AppDictController {

    @Resource
    private DictQueryService dictQueryService;

    @Operation(summary = "按 type 查询字典项")
    @PublicAccess
    @GetMapping("items")
    public Result<List<DictItemVo>> items(@RequestParam("type") String type) {
        return Result.success(dictQueryService.listByType(type));
    }
}
