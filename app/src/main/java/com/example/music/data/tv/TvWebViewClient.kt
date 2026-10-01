package com.example.music.data.tv

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream
import java.text.MessageFormat
import java.util.Collections

/**
 * 移植自 utao 的 `WebViewClientImpl`。
 *
 * 关键职责：
 * 1. 凡地址里含 "tv-web" 的资源，一律从 assets 读取并返回（替代 Firefox 的 moz-extension://）。
 * 2. HTML 里把 `base.js` 换成 `basex.js`（Android 版入口，标记 `_tvIsApp=true`）。
 * 3. 页面加载完成后注入 `end.js` + `load_detail_tv.js`（直播）/ `load_detail_video.js`（点播）。
 * 4. 带 `u-link=1` 的直播页里，捕获 .m3u8 请求写入 sessionStorage，供前端切换播放器。
 * 5. 图片按 utao 的白名单放行，其余（广告等）直接拦截丢弃。
 */
class TvWebViewClient(
    private val context: Context,
    private val liveMode: Boolean,
) : WebViewClient() {

    private companion object {
        const val TAG = "TvWebViewClient"
        const val THEME_COLOR = "#0B1020"
    }

    /** 当前主文档地址，m3u8 捕获时要用它判断是否 `u-link=1`。 */
    @Volatile
    private var currentUrl: String? = null

    /** 是否已经注入过详情脚本（避免同页重复注入）。 */
    @Volatile
    private var injectedForUrl: String? = null

    var onPageTitle: ((String) -> Unit)? = null
    var onPageError: ((String) -> Unit)? = null

    /** 页面（站点自己的网页，不是 tv-web 资源）加载完成。 */
    var onPageLoaded: (() -> Unit)? = null

    // ==================== 页面回调 ====================

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        currentWebView = view
        currentUrl = url
        if (url != null && url != injectedForUrl) injectedForUrl = null
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val wv = view ?: return
        val pageUrl = url ?: return
        onPageTitle?.invoke(wv.title ?: "")

        // 自己的页面（频道浏览器 / 播放器页）不需要注入站点插件
        if (pageUrl.contains("tv-web") && !pageUrl.contains("u-link=1")) {
            onPageLoaded?.invoke()
            return
        }
        if (injectedForUrl == pageUrl) {
            onPageLoaded?.invoke()
            return
        }
        injectedForUrl = pageUrl

        val script = buildInjectionScript() ?: return
        wv.evaluateJavascript(script, null)
        onPageLoaded?.invoke()
    }

    /**
     * 手动重新注入站点插件。切换频道后页面会重新加载，
     * 这里强制再注入一次，避免注入标志把新页面挡掉。
     */
    fun injectSiteScripts(view: WebView?) {
        val wv = view ?: return
        injectedForUrl = null
        val script = buildInjectionScript() ?: return
        wv.evaluateJavascript(script, null)
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // 不少地方台证书配置不规范，按 utao 的做法放行
        Log.i(TAG, "忽略 SSL 错误: ${error?.url}")
        handler?.proceed()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: android.webkit.WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val desc = error?.description?.toString() ?: "加载失败"
            Log.w(TAG, "主文档加载失败: ${request.url} $desc")
            onPageError?.invoke(desc)
        }
    }

    /**
     * 站点若用 target=_blank 或 window.open，直接在同一个 WebView 里打开，避免弹出新窗口。
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        return handleNavigation(view, url)
    }

    @Deprecated("Android 旧版回调，仅为兼容低版本保留")
    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val u = url ?: return false
        return handleNavigation(view, u)
    }

    private fun handleNavigation(view: WebView?, url: String): Boolean {
        val wv = view ?: return false
        return try {
            // 非 http(s) 的 scheme（intent://, market://, weixin:// 等）交给系统
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return false
            }
            wv.loadUrl(url)
            true
        } catch (e: Exception) {
            Log.w(TAG, "导航失败 $url : ${e.message}")
            false
        }
    }

    // ==================== 资源拦截 ====================

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? {
        val req = request ?: return null
        val url = req.url.toString()

        // 1) 带 u-link=1 的直播页：捕获 m3u8，交给前端 sessionStorage
        if (liveMode && (url.contains(".m3u8") || url.contains(".flv"))) {
            val page = currentUrl
            if (page != null && page.contains("u-link=1")) {
                captureStream(view, url, page)
            }
        }

        // 2) 图片：只做登录二维码识别，不再整站屏蔽图片
        //    （utao 的屏蔽表是为电视端广告环境设计的，手机上会把正常内容也挡掉）
        val accept = req.requestHeaders?.get("Accept") ?: ""
        if (accept.startsWith("image/")) {
            handleQrImage(url)
        }

        // 3) tv-web 资源一律走 assets
        if (!url.contains(TvAssets.ROOT)) return null
        return serveAsset(url)
    }

    private fun captureStream(view: WebView?, streamUrl: String, pageUrl: String) {
        val wv = view ?: return
        val js = MessageFormat.format(
            "try{{sessionStorage.setItem(\"{0}\",\"{1}\");sessionStorage.setItem(\"{2}\",\"{3}\");}}catch(e){{}}",
            "u-m3u8", escapeJs(streamUrl),
            "u-loc", escapeJs(pageUrl),
        )
        wv.post { wv.evaluateJavascript(js, null) }
    }

    private fun escapeJs(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")

    /**
     * 从 assets 返回 tv-web 资源。返回 null 表示交给系统正常加载。
     */
    private fun serveAsset(url: String): WebResourceResponse? {
        val relative = TvAssets.normalize(url) ?: return null

        // 图片：保留原始字节
        val mime = TvAssets.mimeOf(relative)

        return when {
            relative.endsWith(".html") -> {
                val html = TvAssets.readText(context, "${TvAssets.ROOT}/$relative")
                if (html.isEmpty()) return null
                val patched = patchHtml(html)
                response("text/html", patched.toByteArray(Charsets.UTF_8))
            }

            relative.endsWith("basex.js") -> {
                val js = TvAssets.readWebText(context, relative)
                val patched = js.replace("\"{version}\"", "\"${appVersionCode()}\"")
                    .replace("{version}", appVersionCode().toString())
                response("text/javascript", patched.toByteArray(Charsets.UTF_8))
            }

            else -> {
                val bytes = TvAssets.readBytes(context, relative) ?: return null
                WebResourceResponse(mime, "utf-8", ByteArrayInputStream(bytes)).apply {
                    addCorsHeaders(this)
                }
            }
        }
    }

    /**
     * HTML 改写：替换 Firefox 版入口 base.js → basex.js，并去掉失效的第三方统计脚本。
     */
    private fun patchHtml(html: String): String {
        var out = html.replace("js/base.js", "js/basex.js")
        // 去掉引用了已删除文件的 script 标签
        out = out.replace(
            Regex("""<script[^>]*js-sdk-pro\.min\.js[^>]*>\s*</script>"""),
            "",
        )
        return out
    }

    private fun response(mime: String, body: ByteArray): WebResourceResponse =
        WebResourceResponse(mime, "utf-8", ByteArrayInputStream(body)).apply {
            addCorsHeaders(this)
        }

    private fun addCorsHeaders(resp: WebResourceResponse) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resp.responseHeaders = Collections.singletonMap(
                "Access-Control-Allow-Origin", "*"
            )
        }
    }

    /**
     * 登录二维码识别。页面上的 `_loginQr(url, type)` 由站点脚本定义，
     * 原生只负责把二维码地址回传给它（对应 utao 的 Util.loginQr）。
     */
    private fun handleQrImage(url: String) {
        val type = when {
            url.contains("open.weixin.qq.com/connect/qrcode") -> "微信"
            url.contains("ptlogin2.qq.com/ssl/ptqrshow") -> "手机端qq"
            url.startsWith("https://img.alicdn.com/imgextra/") && url.endsWith("xcode.png") -> "youkuQr"
            else -> return
        }
        val js = "try{if(window._loginQr){window._loginQr(\"${escapeJs(url)}\",\"$type\");}}catch(e){}"
        currentWebView?.post { currentWebView?.evaluateJavascript(js, null) }
    }

    /** 当前绑定的 WebView，用于主动求值。 */
    @Volatile
    var currentWebView: WebView? = null

    // ==================== 注入脚本 ====================

    /**
     * 拼出与 utao `getFileContent` 等价的注入脚本：
     * `end.js` + （直播模式 ? `load_detail_tv.js` : `load_detail_video.js`）。
     */
    private fun buildInjectionScript(): String? {
        val end = TvAssets.readWebText(context, "js/end.js")
        val detail = TvAssets.readWebText(
            context,
            if (liveMode) "js/load_detail_tv.js" else "js/load_detail_video.js",
        )
        if (end.isEmpty() && detail.isEmpty()) return null
        return buildString {
            append(end)
            append('\n')
            append(detail)
            append('\n')
            // 兜底：把站点里的 _api 暴露到页面上下文（等价于 cloneInto 的效果）
            append(
                """
                (function(){
                  try{
                    if(window._api && !window.wrappedJSObject){ /* 原生桥已是全局 */ }
                    if(!window._tvIsGecko) window._tvIsGecko=false;
                    if(!window._apiX && window._api){ window._apiX = window._api; }
                  }catch(e){}
                })();
                """.trimIndent()
            )
        }
    }

    private fun appVersionCode(): Int = try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    } catch (e: Exception) {
        1
    }

    /** 便于调试：当前主文档地址。 */
    fun currentPageUrl(): String? = currentUrl
}
