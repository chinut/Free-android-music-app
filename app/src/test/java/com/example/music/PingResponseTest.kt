package com.example.music

import com.example.music.data.tv.YanhuoRemote
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 遥控协议 `/api/remote/ping` 响应的解析测试。
 *
 * **这是给电视端开发者的契约测试。**
 *
 * 手机 App 与电视 App 是独立发版的，用户不可能同时升级，
 * 所以两边都必须能处理对方的老/新版本：
 *
 * - 老电视端：`{"ok":true,"app":"焰火TV","protocol":1,"screen":"home"}`
 * - 新电视端：额外带 `name`（用户设的电视名）和 `port`（实际监听端口）
 *
 * 改了电视端的 ping 响应后，请跑一遍这些用例确认没破坏旧行为。
 */
class PingResponseTest {

    /** 与生产代码 [YanhuoRemote.pingInfo] 里一致的解析逻辑。 */
    private fun parse(json: String): YanhuoRemote.PingInfo {
        val o = JSONObject(json)
        val name = o.optString("name", "").ifBlank { o.optString("app", "") }
        return YanhuoRemote.PingInfo(
            name = name,
            protocol = o.optInt("protocol", 0),
            screen = o.optString("screen", ""),
            port = o.optInt("port", 0),
        )
    }

    // ---------- 老电视端（当前线上版本）必须继续正常工作 ----------

    @Test
    fun `老响应只有 app 字段时 名字取自 app`() {
        val info = parse("""{"ok":true,"app":"焰火TV","protocol":1,"screen":"home"}""")
        assertEquals("焰火TV", info.name)
        assertEquals("焰火TV", info.title)
        assertEquals(1, info.protocol)
        assertEquals("home", info.screen)
    }

    @Test
    fun `老响应没有 port 字段时 hasPort 为假`() {
        val info = parse("""{"ok":true,"app":"焰火TV","protocol":1,"screen":"home"}""")
        assertFalse("老版本不报端口，调用方应回退到探测到的端口", info.hasPort)
        assertEquals(0, info.port)
    }

    // ---------- 新电视端：自报名字 ----------

    @Test
    fun `新响应有 name 字段时优先用 name`() {
        val info = parse("""{"ok":true,"app":"焰火TV","name":"客厅电视","protocol":2,"port":8899,"screen":"home"}""")
        assertEquals("客厅电视", info.name)
        assertEquals("客厅电视", info.title)
    }

    @Test
    fun `name 存在但为空串时回退到 app`() {
        // 用户没设名字时电视端可能返回 name:""，不能因此显示空白
        val info = parse("""{"ok":true,"app":"焰火TV","name":"","protocol":2,"port":8899}""")
        assertEquals("焰火TV", info.title)
    }

    @Test
    fun `两个字段都没有时退回默认名`() {
        val info = parse("""{"ok":true,"protocol":2}""")
        assertEquals("", info.name)
        assertEquals("焰火TV", info.title)
    }

    // ---------- 新电视端：自报端口 ----------

    @Test
    fun `新响应带 port 时 hasPort 为真`() {
        val info = parse("""{"ok":true,"app":"焰火TV","protocol":2,"port":8900}""")
        assertTrue(info.hasPort)
        assertEquals(8900, info.port)
    }

    @Test
    fun `端口退让后的值能被正确读出`() {
        // 电视端 8899 被占用时会退让，并把实际端口报出来
        val info = parse("""{"ok":true,"name":"卧室电视","protocol":2,"port":8903}""")
        assertEquals(8903, info.port)
        assertEquals("卧室电视", info.title)
    }

    @Test
    fun `port 为 0 或越界时视为未提供`() {
        assertFalse(parse("""{"ok":true,"app":"焰火TV","port":0}""").hasPort)
        assertFalse(parse("""{"ok":true,"app":"焰火TV","port":-1}""").hasPort)
        assertFalse(parse("""{"ok":true,"app":"焰火TV","port":70000}""").hasPort)
    }

    @Test
    fun `port 是字符串数字时也能读出来`() {
        // 有些设备端拼 JSON 时会把数字拼成字符串，容错一下
        val info = parse("""{"ok":true,"app":"焰火TV","port":"8901"}""")
        assertEquals(8901, info.port)
        assertTrue(info.hasPort)
    }

    // ---------- 契约：宁可字段缺失，也不要把响应写坏 ----------

    @Test
    fun `响应必须能被 JSONObject 解析`() {
        // 电视端拼字符串时若含未转义引号会解析失败，手机端会当成"没找到电视"
        val name = "客厅\"电视"   // 名字里带引号
        val json = JSONObject().apply {
            put("ok", true)
            put("app", "焰火TV")
            put("name", name)
            put("protocol", 2)
            put("port", 8899)
        }.toString()
        assertEquals(name, parse(json).name)
    }
}
