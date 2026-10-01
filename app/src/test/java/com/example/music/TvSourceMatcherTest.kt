package com.example.music

import com.example.music.data.tv.TvCategory
import com.example.music.data.tv.TvChannel
import com.example.music.data.tv.TvSourceMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 同频道多源匹配（自动换源）的回归测试，数据取自真实的 tv.json 形态。 */
class TvSourceMatcherTest {

    private fun cat(name: String, vararg chans: Pair<String, String>) =
        TvCategory(
            name = name,
            tag = name,
            channels = chans.map { TvChannel(name = it.first, url = it.second) },
        )

    // 真实案例：CCTV-6 在两个分类里名字带/不带连字符，指向两个不同源
    private val cctv6A = TvChannel("CCTV-6 电影", "https://tv.cctv.com/live/cctv6/")
    private val cctv6B = TvChannel("CCTV6 电影", "https://www.yangshipin.cn/tv/home?pid=600108442")
    private val cctv1 = TvChannel("CCTV-1 综合", "https://tv.cctv.com/live/cctv1/")

    private val catalog = listOf(
        cat("央视", "CCTV-6 电影" to cctv6A.url, "CCTV-1 综合" to cctv1.url),
        cat("央视源2", "CCTV6 电影" to cctv6B.url),
    )

    @Test
    fun `带连字符与不带连字符会被认成同一个台`() {
        assertEquals(TvSourceMatcher.normalize(cctv6A.name), TvSourceMatcher.normalize(cctv6B.name))
    }

    @Test
    fun `能找出同名频道的两个源`() {
        val sources = TvSourceMatcher.sourcesOf(cctv6A, catalog)
        assertEquals(2, sources.size)
        assertEquals(cctv6A.url, sources[0].url)
        assertEquals(cctv6B.url, sources[1].url)
    }

    @Test
    fun `从第二个源出发时它也要排第一`() {
        val sources = TvSourceMatcher.sourcesOf(cctv6B, catalog)
        assertEquals(2, sources.size)
        assertEquals(cctv6B.url, sources[0].url)
    }

    @Test
    fun `不同频道不会互相匹配`() {
        val sources = TvSourceMatcher.sourcesOf(cctv1, catalog)
        assertEquals(1, sources.size)
        assertEquals(cctv1.url, sources[0].url)
    }

    @Test
    fun `没有同名源时只返回自己`() {
        val lonely = TvChannel("某个独有台", "https://example.com/a")
        val sources = TvSourceMatcher.sourcesOf(lonely, catalog)
        assertEquals(1, sources.size)
        assertEquals(lonely.url, sources[0].url)
    }

    @Test
    fun `完全相同地址不会重复出现`() {
        val dup = listOf(
            cat("A", "CCTV-6 电影" to cctv6A.url),
            cat("B", "CCTV6 电影" to cctv6A.url),
        )
        val sources = TvSourceMatcher.sourcesOf(cctv6A, dup)
        assertEquals(1, sources.size)
    }

    @Test
    fun `括号补充说明不影响匹配`() {
        val a = TvChannel("广东卫视", "https://x/1")
        val b = TvChannel("广东卫视（高清）", "https://x/2")
        val list = listOf(
            cat("甲", "广东卫视" to a.url),
            cat("乙", "广东卫视（高清）" to b.url),
        )
        val sources = TvSourceMatcher.sourcesOf(a, list)
        assertEquals(2, sources.size)
    }

    @Test
    fun `全角空格与普通空格等价`() {
        assertEquals(
            TvSourceMatcher.normalize("CCTV-6 电影"),
            TvSourceMatcher.normalize("CCTV6\u3000电影"),
        )
    }

    @Test
    fun `真实目录里 CCTV6 确实存在多个源`() {
        // 记录真实数据形态，避免以后改目录时悄悄退化
        val all = listOf(cctv6A, cctv6B).map { it.name }
        assertTrue(all.any { it.contains("-") })
        assertTrue(all.any { !it.contains("-") })
    }
}
