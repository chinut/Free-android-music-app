package com.example.music

import com.example.music.data.tv.YanhuoRemote
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 焰火TV 网络遥控协议层的回归测试。
 *
 * 触发点：口令编码写错会导致「填了口令却一直连不上」，键码写错会导致按错键。
 * 这些都不用连真电视就能测。
 */
class YanhuoRemoteTest {

    // ---------- 地址拼接 ----------

    @Test
    fun `不带口令时地址就是主机加路径`() {
        assertEquals(
            "http://192.168.1.23:8899/api/remote/ping",
            YanhuoRemote.buildUrl("192.168.1.23", 8899, "", "/api/remote/ping"),
        )
    }

    @Test
    fun `路径没有查询串时用问号接口令`() {
        assertEquals(
            "http://10.0.0.5:8899/api/remote/ping?token=abc123",
            YanhuoRemote.buildUrl("10.0.0.5", 8899, "abc123", "/api/remote/ping"),
        )
    }

    @Test
    fun `路径已有查询串时用与号接口令`() {
        assertEquals(
            "http://10.0.0.5:8899/api/remote/events?since=0&wait=15000&token=abc",
            YanhuoRemote.buildUrl("10.0.0.5", 8899, "abc", "/api/remote/events?since=0&wait=15000"),
        )
    }

    @Test
    fun `口令里的特殊字符会被正确编码`() {
        // 空格要变 +，& 要变 %26，否则会把参数截断
        assertEquals(
            "http://h:8899/api/remote/ping?token=a+b%26c%3Dd",
            YanhuoRemote.buildUrl("h", 8899, "a b&c=d", "/api/remote/ping"),
        )
    }

    @Test
    fun `口令是空白时不拼参数`() {
        assertEquals(
            "http://h:8899/api/remote/ping",
            YanhuoRemote.buildUrl("h", 8899, "   ", "/api/remote/ping"),
        )
    }

    @Test
    fun `自定义端口生效`() {
        assertEquals(
            "http://192.168.0.9:9000/api/remote/status",
            YanhuoRemote.buildUrl("192.168.0.9", 9000, "", "/api/remote/status"),
        )
    }

    // ---------- 键码必须与协议文档一致 ----------

    @Test
    fun `方向键与确定返回的键码与协议一致`() {
        assertEquals(19, YanhuoRemote.Key.UP.code)
        assertEquals(20, YanhuoRemote.Key.DOWN.code)
        assertEquals(21, YanhuoRemote.Key.LEFT.code)
        assertEquals(22, YanhuoRemote.Key.RIGHT.code)
        assertEquals(23, YanhuoRemote.Key.OK.code)
        assertEquals(4, YanhuoRemote.Key.BACK.code)
        assertEquals(82, YanhuoRemote.Key.MENU.code)
    }

    @Test
    fun `音量键码与协议一致`() {
        assertEquals(24, YanhuoRemote.Key.VOL_UP.code)
        assertEquals(25, YanhuoRemote.Key.VOL_DOWN.code)
        assertEquals(164, YanhuoRemote.Key.MUTE.code)
    }

    @Test
    fun `媒体键码与协议一致`() {
        assertEquals(126, YanhuoRemote.Key.PLAY.code)
        assertEquals(127, YanhuoRemote.Key.PAUSE.code)
        assertEquals(85, YanhuoRemote.Key.PLAY_PAUSE.code)
        assertEquals(86, YanhuoRemote.Key.STOP.code)
        assertEquals(87, YanhuoRemote.Key.NEXT.code)
        assertEquals(88, YanhuoRemote.Key.PREV.code)
        assertEquals(90, YanhuoRemote.Key.FORWARD.code)
        assertEquals(89, YanhuoRemote.Key.REWIND.code)
    }

    @Test
    fun `每个键的 wire 形式都是 keyCode 等号数字`() {
        YanhuoRemote.Key.entries.forEach { k ->
            assertEquals("keyCode=${k.code}", k.wire)
        }
    }
}
