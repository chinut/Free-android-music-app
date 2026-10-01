package com.example.music

import com.example.music.data.tv.TvStreamUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 频道地址归一化的回归测试。
 * 覆盖 tv.json 里真实出现的几种地址形态。
 */
class TvStreamUrlTest {

    // ---------- 包装页解包 ----------

    @Test
    fun `解包 tv_utao 包装页取出内层 m3u8`() {
        val raw = "https://tv.utao.tv/tv-web/live.html?url=https://livezzutv.chinamcache.com/live/zb02.m3u8"
        assertEquals("https://livezzutv.chinamcache.com/live/zb02.m3u8", TvStreamUrl.unwrap(raw))
    }

    @Test
    fun `解包后带 usave 参数也要处理`() {
        val raw = "https://www.gstv.com.cn/tv-web/live.html?url=https://hls.gstv.com.cn/skt649/nd6ycc.m3u8&usave=1"
        assertEquals("https://hls.gstv.com.cn/skt649/nd6ycc.m3u8", TvStreamUrl.unwrap(raw))
    }

    @Test
    fun `内层地址做了 URL 编码时能正确还原`() {
        val raw = "https://x.com/tv-web/live.html?url=https%3A%2F%2Fa.com%2Flive.m3u8"
        assertEquals("https://a.com/live.m3u8", TvStreamUrl.unwrap(raw))
    }

    @Test
    fun `普通站点页面绝不改动`() {
        val page = "https://tv.cctv.com/live/cctv13/"
        assertEquals(page, TvStreamUrl.unwrap(page))
        assertFalse(TvStreamUrl.isLocalPlayerPage(page))
    }

    @Test
    fun `站点自身带 url 参数但不是本地播放页时不动它`() {
        val page = "https://example.com/watch?url=https://other.com/a.m3u8"
        assertEquals(page, TvStreamUrl.unwrap(page))
    }

    // ---------- 直连流判定 ----------

    @Test
    fun `m3u8 直连地址判为原生`() {
        assertEquals(
            "https://tmpstream.hyrtv.cn/xwzh/sd/live.m3u8",
            TvStreamUrl.directMediaOf("https://tmpstream.hyrtv.cn/xwzh/sd/live.m3u8"),
        )
    }

    @Test
    fun `包装页里的 m3u8 也能判为原生`() {
        val raw = "https://tv.utao.tv/tv-web/live.html?url=https://vms-x.hbnews.net/video/xwzh/index.m3u8"
        assertEquals(
            "https://vms-x.hbnews.net/video/xwzh/index.m3u8",
            TvStreamUrl.directMediaOf(raw),
        )
    }

    @Test
    fun `无扩展名的 m3u8 接口也认`() {
        // 查询串要完整保留，不能截断
        assertEquals(
            "https://a.com/hls/channel?id=1&format=m3u8",
            TvStreamUrl.directMediaOf("https://a.com/hls/channel?id=1&format=m3u8"),
        )
    }

    @Test
    fun `站点网页不交给原生播放器`() {
        assertNull(TvStreamUrl.directMediaOf("https://tv.cctv.com/live/cctv13/"))
        assertNull(TvStreamUrl.directMediaOf("https://www.yangshipin.cn/tv/home?pid=600108442"))
        assertNull(TvStreamUrl.directMediaOf("https://live.jstv.com/"))
    }

    @Test
    fun `本地播放页本身不会被当成媒体流`() {
        // 没有内层 url 的裸播放页 → 不是媒体流
        assertNull(TvStreamUrl.directMediaOf("https://www.gstv.com.cn/tv-web/live.html"))
    }

    // ---------- 请求头 ----------

    @Test
    fun `gstv 需要补 Referer`() {
        val h = TvStreamUrl.headersFor("https://hls.gstv.com.cn/skt649/nd6ycc.m3u8")
        assertEquals("https://www.gstv.com.cn/", h["Referer"])
        assertTrue(h.containsKey("User-Agent"))
    }

    @Test
    fun `央视源补央视 Referer`() {
        val h = TvStreamUrl.headersFor("https://cctvpic.example/live.m3u8")
        assertEquals("https://tv.cctv.com/", h["Referer"])
    }

    @Test
    fun `未知域名只带 UA 不乱加 Referer`() {
        val h = TvStreamUrl.headersFor("https://unknown-host.example/live.m3u8")
        assertNull(h["Referer"])
        assertTrue(h.containsKey("User-Agent"))
    }

    // ---------- 按站点选 UA ----------

    @Test
    fun `央视频必须用桌面 UA`() {
        // 移动 UA 下央视频会弹「前往央视频客户端」而不给播放器
        assertEquals(
            TvStreamUrl.UA_DESKTOP,
            TvStreamUrl.userAgentFor("https://www.yangshipin.cn/tv/home?pid=600108442"),
        )
    }

    @Test
    fun `央视网必须用移动 UA`() {
        // 桌面 UA 下央视网回「本时段节目暂时无法播放，请移至移动端观看」
        assertEquals(
            TvStreamUrl.UA_MOBILE,
            TvStreamUrl.userAgentFor("https://tv.cctv.com/live/cctv6/"),
        )
    }

    @Test
    fun `其它站点默认桌面 UA`() {
        assertEquals(
            TvStreamUrl.UA_DESKTOP,
            TvStreamUrl.userAgentFor("https://live.jstv.com/"),
        )
        assertEquals(
            TvStreamUrl.UA_DESKTOP,
            TvStreamUrl.userAgentFor("https://www.gstv.com.cn/"),
        )
    }

    @Test
    fun `非法地址不抛异常`() {
        assertEquals(TvStreamUrl.UA_DESKTOP, TvStreamUrl.userAgentFor("not a url"))
    }
}
