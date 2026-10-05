package com.example.music

import com.example.music.data.tv.TvRemoteDiscovery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 多台电视时的选择逻辑测试。
 *
 * 触发点：
 * - 家里有两台电视时如果「保存的那台」匹配不上，会连错电视或反复弹选择框；
 * - 电视端的调试服务在端口被占用时会自动退让（8899 → 8900 …），
 *   所以同一地址上的端口也必须参与匹配，否则第二台电视永远找不到。
 */
class TvRemoteDiscoveryTest {

    private fun tv(host: String, app: String = "焰火TV", port: Int = 8899) =
        TvRemoteDiscovery.Found(host = host, app = app, protocol = 1, screen = "home", port = port)

    private val livingRoom = tv("192.168.1.10")
    private val bedroom = tv("192.168.1.23")
    private val study = tv("192.168.1.57")

    // ---------- 找出保存的那台 ----------

    @Test
    fun `能在多台里找回保存过的那台`() {
        val list = listOf(livingRoom, bedroom, study)
        assertEquals(bedroom, TvRemoteDiscovery.findSaved(list, "192.168.1.23", 8899))
    }

    @Test
    fun `保存的地址不在列表里时返回空`() {
        val list = listOf(livingRoom, study)
        assertNull(TvRemoteDiscovery.findSaved(list, "192.168.1.23", 8899))
    }

    @Test
    fun `列表为空时返回空`() {
        assertNull(TvRemoteDiscovery.findSaved(emptyList(), "192.168.1.23", 8899))
    }

    @Test
    fun `保存的是空地址时返回空`() {
        // 从没选过电视时 prefs.host 为空，不应误匹配到任何一台
        assertNull(TvRemoteDiscovery.findSaved(listOf(livingRoom, bedroom), "", 8899))
    }

    @Test
    fun `地址必须完全相等不能只是前缀相同`() {
        // 192.168.1.1 和 192.168.1.10 是两台不同的设备，不能混
        val one = tv("192.168.1.1")
        val ten = tv("192.168.1.10")
        assertEquals(ten, TvRemoteDiscovery.findSaved(listOf(one, ten), "192.168.1.10", 8899))
        assertEquals(one, TvRemoteDiscovery.findSaved(listOf(one, ten), "192.168.1.1", 8899))
    }

    // ---------- 端口参与匹配（第二台电视通常不在 8899）----------

    @Test
    fun `同一地址不同端口是两台不同的电视`() {
        // 一台占了 8899，另一台退让到 8900 —— 这是真机上第二台电视的典型情况
        val a = tv("192.168.1.10", port = 8899)
        val b = tv("192.168.1.10", port = 8900)
        assertEquals(a, TvRemoteDiscovery.findSaved(listOf(a, b), "192.168.1.10", 8899))
        assertEquals(b, TvRemoteDiscovery.findSaved(listOf(a, b), "192.168.1.10", 8900))
    }

    @Test
    fun `地址对但端口对不上时不能误匹配`() {
        // 电视换过端口（比如原端口的占用者退出了），保存的 8899 不该匹配到 8901 那台
        val moved = tv("192.168.1.10", port = 8901)
        assertNull(TvRemoteDiscovery.findSaved(listOf(moved), "192.168.1.10", 8899))
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
    fun `默认端口时副标题只有地址`() {
        assertEquals("192.168.1.10", livingRoom.subtitle)
    }

    @Test
    fun `非默认端口时副标题带端口`() {
        // 两台同名电视都在 8899 之外时，靠地址+端口区分
        assertEquals("192.168.1.10:8900", tv("192.168.1.10", port = 8900).subtitle)
    }

    @Test
    fun `同名电视靠副标题区分`() {
        val a = tv("192.168.1.10", port = 8899)
        val b = tv("192.168.1.10", port = 8900)
        assertEquals(a.title, b.title)
        assertEquals("192.168.1.10", a.subtitle)
        assertEquals("192.168.1.10:8900", b.subtitle)
    }

    // ---------- 端口扫描范围 ----------

    @Test
    fun `端口扫描范围与电视端的退让范围一致`() {
        // 电视端是「8899~8899+19 都试」，手机端必须覆盖同样的范围
        assertEquals(20, TvRemoteDiscovery.PORT_SCAN_SIZE)
    }
}
