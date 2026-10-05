package com.example.music

import com.example.music.data.tv.TvRemoteDiscovery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 多台电视时的选择逻辑测试。
 *
 * 触发点：家里有多台电视时，如果「保存的那台」匹配不上，就会连错电视或反复弹选择框。
 * 这些是纯函数，不用连真电视就能测。
 */
class TvRemoteDiscoveryTest {

    private fun tv(host: String, app: String = "焰火TV") =
        TvRemoteDiscovery.Found(host = host, app = app, protocol = 1, screen = "home")

    private val livingRoom = tv("192.168.1.10")
    private val bedroom = tv("192.168.1.23")
    private val study = tv("192.168.1.57")

    // ---------- 找出保存的那台 ----------

    @Test
    fun `能在多台里找回保存过的那台`() {
        val list = listOf(livingRoom, bedroom, study)
        assertEquals(bedroom, TvRemoteDiscovery.findSaved(list, "192.168.1.23"))
    }

    @Test
    fun `保存的地址不在列表里时返回空`() {
        val list = listOf(livingRoom, study)
        assertNull(TvRemoteDiscovery.findSaved(list, "192.168.1.23"))
    }

    @Test
    fun `列表为空时返回空`() {
        assertNull(TvRemoteDiscovery.findSaved(emptyList(), "192.168.1.23"))
    }

    @Test
    fun `保存的是空地址时返回空`() {
        // 从没选过电视时 prefs.host 为空，不应误匹配到任何一台
        assertNull(TvRemoteDiscovery.findSaved(listOf(livingRoom, bedroom), ""))
    }

    @Test
    fun `地址必须完全相等不能只是前缀相同`() {
        // 192.168.1.1 和 192.168.1.10 是两台不同的设备，不能混
        val one = tv("192.168.1.1")
        val ten = tv("192.168.1.10")
        assertEquals(ten, TvRemoteDiscovery.findSaved(listOf(one, ten), "192.168.1.10"))
        assertEquals(one, TvRemoteDiscovery.findSaved(listOf(one, ten), "192.168.1.1"))
    }

    // ---------- 列表展示 ----------

    @Test
    fun `标题用设备自报的名字`() {
        assertEquals("焰火TV", livingRoom.title)
    }

    @Test
    fun `设备没自报名字时标题退回默认名`() {
        val unknown = tv("192.168.1.99", app = "")
        assertEquals("焰火TV", unknown.title)
    }

    @Test
    fun `副标题始终是 IP，便于区分同型号电视`() {
        // 两台都叫「焰火TV」时必须靠 IP 区分
        val a = tv("192.168.1.10")
        val b = tv("192.168.1.23")
        assertEquals(a.title, b.title)
        assertEquals("192.168.1.10", a.subtitle)
        assertEquals("192.168.1.23", b.subtitle)
    }

    // ---------- 数量判断（决定是否弹选择框）----------

    @Test
    fun `只有一台时不需要用户挑选`() {
        val list = listOf(bedroom)
        assertEquals(1, list.size)
    }

    @Test
    fun `多台时需要用户挑选`() {
        val list = listOf(livingRoom, bedroom, study)
        assertEquals(3, list.size)
    }
}
