package com.example.music.data.tv

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

/**
 * 在局域网里自动找电视上的「焰火TV」。
 *
 * 目的是让用户不用手填 IP：打开遥控页就直接连上。
 * 做法是先看本机地址推出 /24 网段，再并发探测该网段的 8899 端口。
 */
object TvRemoteDiscovery {

    /** 扫描并发度。太高会被系统限制，太低又慢。 */
    private const val CONCURRENCY = 48

    /** 单机端口探测超时（毫秒）。局域网内很快，300ms 足够。 */
    private const val PROBE_TIMEOUT_MS = 300

    /** 扫描结果缓存时长，避免每次进页面都全扫一遍。 */
    private const val CACHE_MS = 60_000L

    @Volatile
    private var cachedHost: String? = null

    @Volatile
    private var cachedAt: Long = 0L

    /** 记下成功连上的地址，下次优先用。 */
    fun remember(host: String) {
        cachedHost = host
        cachedAt = System.currentTimeMillis()
    }

    fun cached(): String? {
        val h = cachedHost ?: return null
        if (System.currentTimeMillis() - cachedAt > CACHE_MS) return null
        return h
    }

    /**
     * 扫描局域网，返回第一个能连上的电视地址；找不到返回 null。
     */
    suspend fun discover(context: Context, port: Int = TvRemotePrefs.DEFAULT_PORT): String? {
        cached()?.let { return it }

        val local = localIpv4() ?: return null
        val prefix = local.substringBeforeLast('.', "")
        if (prefix.isEmpty()) return null

        val hosts = (1..254).map { "$prefix.$it" }.filter { it != local }

        return withContext(Dispatchers.IO) {
            coroutineScope {
                hosts.chunked(CONCURRENCY).firstNotNullOfOrNull { batch ->
                    val results = batch.map { ip ->
                        async { if (portOpen(ip, port)) ip else null }
                    }.awaitAll()
                    results.firstOrNull { it != null }?.also { remember(it) }
                }
            }
        }
    }

    /** 端口能连上就认为找到了（真正的协议握手交给 YanhuoRemote.ping）。 */
    private fun portOpen(host: String, port: Int): Boolean = try {
        Socket().use { it.connect(InetSocketAddress(host, port), PROBE_TIMEOUT_MS) }
        true
    } catch (e: Exception) {
        false
    }

    /** 取本机第一个可用的 IPv4 地址（排除回环）。 */
    private fun localIpv4(): String? = runCatching {
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
            ?.hostAddress
    }.getOrNull()
}
