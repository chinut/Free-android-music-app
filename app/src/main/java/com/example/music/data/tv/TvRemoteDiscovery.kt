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
 * **必须支持一个家庭有多台电视**：
 * 1. 每台电视的地址不同；
 * 2. 电视端的调试服务在端口被占用时会自动退让（8899 → 8900 → 8901 …，最多试 20 个），
 *    所以同一台电视的端口也可能是 8899 之外的。
 * 因此扫描要同时覆盖 **地址段 × 端口段**，否则家里第二台电视永远找不到。
 */
object TvRemoteDiscovery {

    /** 端口扫描范围大小，与电视端「8899~port+19」的最大退让范围保持一致。 */
    const val PORT_SCAN_SIZE = 20

    /**
     * 同时扫描多少台主机。
     *
     * 每台主机内部还会并发探测端口，所以实际并发连接数是两者相乘。
     * 局域网里绝大多数地址是「无响应超时」，靠这个数量把总耗气压下来。
     */
    private const val HOST_CONCURRENCY = 96

    /** 单机端口探测超时（毫秒）。局域网内很快，300ms 足够。 */
    private const val PROBE_TIMEOUT_MS = 300

    /** 一次发现的结果。 */
    data class Found(
        val host: String,
        val app: String,
        val protocol: Int,
        val screen: String,
        /** 该电视的端口。电视端可能因为端口被占用而退让到 8899 之外。 */
        val port: Int = TvRemotePrefs.DEFAULT_PORT,
    ) {
        /** 列表里展示的标题；设备没自报名字时退回默认名。 */
        val title: String get() = app.ifBlank { "焰火TV" }

        /** 副标题：地址，非默认端口时带上端口，用来区分同名电视。 */
        val subtitle: String
            get() = if (port == TvRemotePrefs.DEFAULT_PORT) host else "$host:$port"
    }

    /**
     * 扫描局域网，返回**所有**找到的电视。
     *
     * @param onFound 每确认一台就回调一次（含当时已找到的全部），方便边扫边显示
     */
    suspend fun discoverAll(
        context: Context,
        basePort: Int = TvRemotePrefs.DEFAULT_PORT,
        onFound: (List<Found>) -> Unit = {},
    ): List<Found> = withContext(Dispatchers.IO) {
        val local = localIpv4()
        if (local == null) {
            android.util.Log.w("TvRemoteDiscovery", "找不到本机 IPv4，无法扫描")
            return@withContext emptyList()
        }
        val prefix = local.substringBeforeLast('.', "")
        if (prefix.isEmpty()) return@withContext emptyList()
        android.util.Log.i(
            "TvRemoteDiscovery",
            "本机 $local → 扫描 $prefix.1-254 的端口 $basePort..${basePort + PORT_SCAN_SIZE - 1}"
        )

        val hosts = (1..254).map { "$prefix.$it" }.filter { it != local }

        // 一次把「地址 × 端口段」扫完。
        //
        // 为什么端口段也要扫：电视端的调试服务在 8899 被占用时会自动退让到
        // 8900/8901…（最多 20 个），所以家里第二台电视完全可能在 8900，
        // 只扫 8899 就会漏掉它 —— 这正是之前"只识别 1 台"的原因。
        val candidates = scanHosts(hosts, (basePort until basePort + PORT_SCAN_SIZE).toList())
        android.util.Log.i("TvRemoteDiscovery", "端口候选 ${candidates.size} 个：$candidates")

        // 2) 逐个走协议 ping 确认真是焰火TV。每台电视只 ping 它的那个端口一次。
        val verified = coroutineScope {
            candidates.map { (host, port) ->
                async {
                    val info = YanhuoRemote(host = host, port = port).pingInfo()
                    android.util.Log.i("TvRemoteDiscovery", "ping $host:$port → ${info?.app ?: "无响应"}")
                    if (info == null) null
                    else Found(
                        host = host,
                        app = info.app,
                        protocol = info.protocol,
                        screen = info.screen,
                        port = port,
                    )
                }
            }.awaitAll().filterNotNull()
        }

        // 3) 边确认边回调：家里电视多、端口段也要扫，整体耗时会比只扫 8899 长
        val found = ArrayList<Found>()
        verified
            .sortedWith(compareBy({ it.host.substringAfterLast('.').toIntOrNull() ?: 0 }, { it.port }))
            .forEach { tv ->
                found += tv
                onFound(found.toList())
            }

        found
    }

    /**
     * 分批并发扫描：对 [hosts] 里的每台主机，试 [ports] 这些端口，
     * 返回所有能连上的 (地址, 端口)。
     */
    private suspend fun scanHosts(
        hosts: List<String>,
        ports: List<Int>,
    ): List<Pair<String, Int>> {
        val out = ArrayList<Pair<String, Int>>()
        for (batch in hosts.chunked(HOST_CONCURRENCY)) {
            val hits = coroutineScope {
                batch.map { h ->
                    async { openPorts(h, ports).map { p -> h to p } }
                }.awaitAll()
            }
            hits.forEach { out += it }
        }
        return out
    }

    /** 返回这台主机在 [ports] 里**所有**能连上的端口。 */
    private suspend fun openPorts(host: String, ports: List<Int>): List<Int> = coroutineScope {
        ports
            .map { p -> async { if (portOpen(host, p)) p else null } }
            .awaitAll()
            .filterNotNull()
    }

    /** 在扫描结果里找出保存过的那台。地址和端口都要一致。 */
    fun findSaved(list: List<Found>, savedHost: String, savedPort: Int): Found? =
        list.firstOrNull { it.host == savedHost && it.port == savedPort }

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
