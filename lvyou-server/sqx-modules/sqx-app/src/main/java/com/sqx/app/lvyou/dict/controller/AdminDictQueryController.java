package com.sqx.app.lvyou.dict.controller;

import com.sqx.app.lvyou.dict.domain.vo.DictItemVo;
import com.sqx.app.lvyou.dict.service.DictQueryService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理端业务字典项（筛选项下拉）
 */
@Tag(name = "管理端-业务字典", description = "管理端-业务字典")
@RestController
@RequestMapping("/admin/base/dict")
public class AdminDictQueryController {

    @Resource
    private DictQueryService dictQueryService;

    @Operation(summary = "按 type 查询字典项")
    @RequiresPermissions("sys:dict:list")
    @GetMapping("items")
    public Result<List<DictItemVo>> items(@RequestParam("type") String type) {
        return Result.success(dictQueryService.listByType(type));
    }
}
