package com.sqx.app.lvyou.transport.domain.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 公交线路分页（含附近站点）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公交线路分页（含附近站点）")
public class TransportRoutePageVo extends Page<TransportRouteAppVo> {

    @Schema(description = "附近公交站点（按距离升序）")
    private List<TransportNearbyStationVo> nearbyStations = new ArrayList<>();
}
