package com.sqx.app.lvyou.transport.util;

import com.sqx.app.lvyou.transport.domain.vo.TransportDirectionVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportTrackInfoVo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 公交 JSON 工具测试
 */
public class TransportJsonUtilTest {

    @Test
    public void parseDirections_shouldReturnItems() {
        String raw = "[{\"label\":\"上行\",\"from\":\"A\",\"to\":\"B\",\"firstBus\":\"06:30\",\"lastBus\":\"21:00\",\"interval\":\"8-12分钟\"}]";
        List<TransportDirectionVo> directions = TransportJsonUtil.parseDirections(raw);
        Assert.assertEquals(1, directions.size());
        Assert.assertEquals("上行", directions.get(0).getLabel());
        Assert.assertEquals("A", directions.get(0).getFrom());
    }

    @Test
    public void parseTrackInfo_shouldReturnVehicleInfo() {
        String raw = "{\"currentStation\":\"荣昌北站\",\"nextStation\":\"昌州大道口\",\"eta\":\"约 5 分钟\",\"speed\":\"28 km/h\",\"vehicleNo\":\"渝C·A8821\"}";
        TransportTrackInfoVo trackInfo = TransportJsonUtil.parseTrackInfo(raw);
        Assert.assertNotNull(trackInfo);
        Assert.assertEquals("荣昌北站", trackInfo.getCurrentStation());
        Assert.assertEquals("渝C·A8821", trackInfo.getVehicleNo());
    }

    @Test
    public void parseTimes_shouldReturnTimeList() {
        List<String> times = TransportJsonUtil.parseTimes("[\"06:30\",\"06:42\",\"06:54\"]");
        Assert.assertEquals(3, times.size());
        Assert.assertEquals("06:30", times.get(0));
    }
}
