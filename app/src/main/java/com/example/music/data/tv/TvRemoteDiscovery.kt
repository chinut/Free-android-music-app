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
 * 在局域网里找电视上的「焰火TV」。
 *
 * 目的是让用户不用手填 IP。
 *
 * 注意要支持**一个家庭有多台电视**：这里会把网段里所有响应 8899 的设备都收集回来，
 * 交给上层决定是用保存过的那台、唯一一台直接连、还是让用户挑。
 */
object TvRemoteDiscovery {

    /** 扫描并发度。太高会被系统限制，太低又慢。 */
    private const val CONCURRENCY = 48

    /** 单机端口探测超时（毫秒）。局域网内很快，300ms 足够。 */
    private const val PROBE_TIMEOUT_MS = 300

    /** 一次发现的结果。 */
    data class Found(
        val host: String,
        val app: String,
        val protocol: Int,
        val screen: String,
    ) {
        /** 列表里展示的标题；设备没自报名字时退回 IP。 */
        val title: String get() = app.ifBlank { "焰火TV" }

        /** 副标题，始终显示 IP，多台同型号电视时靠它区分。 */
        val subtitle: String get() = host
    }

    /**
     * 扫描局域网，返回**所有**找到的电视（按 IP 升序）。
     *
     * 端口通只是候选，还会再走一次协议 ping 确认真是焰火TV，
     * 避免把恰好占用 8899 的其它服务当成电视。
     */
    suspend fun discoverAll(
        context: Context,
        port: Int = TvRemotePrefs.DEFAULT_PORT,
    ): List<Found> = withContext(Dispatchers.IO) {
        val local = localIpv4() ?: return@withContext emptyList()
        val prefix = local.substringBeforeLast('.', "")
        if (prefix.isEmpty()) return@withContext emptyList()

        val hosts = (1..254).map { "$prefix.$it" }.filter { it != local }

        // 1) 先并发找端口通的候选
        val candidates = coroutineScope {
            hosts.chunked(CONCURRENCY).flatMap { batch ->
                batch.map { ip -> async { if (portOpen(ip, port)) ip else null } }
                    .awaitAll()
                    .filterNotNull()
            }
        }

        // 2) 再逐个协议确认，并读回设备标识
        coroutineScope {
            candidates.map { ip ->
                async {
                    val info = YanhuoRemote(host = ip, port = port).pingInfo()
                    if (info == null) null
                    else Found(host = ip, app = info.app, protocol = info.protocol, screen = info.screen)
                }
            }.awaitAll().filterNotNull()
        }.sortedBy { it.host.substringAfterLast('.').toIntOrNull() ?: 0 }
    }

    /** 在扫描结果里找出保存过的那台。 */
    fun findSaved(list: List<Found>, savedHost: String): Found? =
        list.firstOrNull { it.host == savedHost }

    /** 端口能连上就认为可能是电视（真正的确认交给协议 ping）。 */
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
