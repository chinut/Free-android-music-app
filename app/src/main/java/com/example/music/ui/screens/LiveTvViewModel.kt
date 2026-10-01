package com.example.music.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.data.tv.TvCatalog
import com.example.music.data.tv.TvCategory
import com.example.music.data.tv.TvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 在线电视：频道目录 + 当前播出的频道 + 自动换源。 */
class LiveTvViewModel(
    app: Application,
    /** 可选：启动即播放的频道地址（用于外部拉起指定频道）。 */
    private val autoPlayUrl: String? = null,
) : AndroidViewModel(app) {

    private val _categories = MutableStateFlow<List<TvCategory>>(emptyList())
    val categories: StateFlow<List<TvCategory>> = _categories.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _selectedCategory = MutableStateFlow(0)
    val selectedCategory: StateFlow<Int> = _selectedCategory.asStateFlow()

    /** 正在播放的频道；为 null 表示停留在频道列表。 */
    private val _playing = MutableStateFlow<TvChannel?>(null)
    val playing: StateFlow<TvChannel?> = _playing.asStateFlow()

    /** 换源状态：同名频道的所有源、当前用的是第几个。 */
    data class SourceState(
        val all: List<TvChannel> = emptyList(),
        val index: Int = 0,
    ) {
        val total: Int get() = all.size
        val hasMore: Boolean get() = index < all.size - 1
    }

    private val _source = MutableStateFlow(SourceState())
    val source: StateFlow<SourceState> = _source.asStateFlow()

    /** 全部源都失败后的提示。 */
    private val _allSourcesFailed = MutableStateFlow(false)
    val allSourcesFailed: StateFlow<Boolean> = _allSourcesFailed.asStateFlow()

    init {
        viewModelScope.launch {
            val ctx: Context = getApplication()
            val list = withContext(Dispatchers.IO) { TvCatalog.load(ctx) }
            _categories.value = list
            _loading.value = false

            // 支持从外部指定直接开播某个频道：
            //   adb shell am start -n com.example.music/.MainActivity -e tvUrl <频道地址>
            autoPlayUrl?.let { target ->
                list.forEachIndexed { i, cat ->
                    val ch = cat.channels.firstOrNull { it.url == target }
                    if (ch != null) {
                        _selectedCategory.value = i
                        startPlayback(ch, list)
                        return@launch
                    }
                }
            }
        }
    }

    fun selectCategory(index: Int) {
        if (index in _categories.value.indices) _selectedCategory.value = index
    }

    fun play(channel: TvChannel) {
        startPlayback(channel, _categories.value)
    }

    private fun startPlayback(channel: TvChannel, categories: List<TvCategory>) {
        val sources = collectSources(channel, categories)
        _source.value = SourceState(all = sources, index = 0)
        _allSourcesFailed.value = false
        _playing.value = channel
    }

    /**
     * 播放失败 → 自动换到下一个同名源。
     * 返回 true 表示已切到备用源（调用方无需报错），false 表示所有源都试完了。
     */
    fun retryWithNextSource(): Boolean {
        val st = _source.value
        if (!st.hasMore) {
            _allSourcesFailed.value = true
            return false
        }
        val next = st.index + 1
        _source.value = st.copy(index = next)
        _allSourcesFailed.value = false
        _playing.value = st.all[next]
        return true
    }

    /** 手动换源（用户点「换个源试试」）。 */
    fun switchSource(): Boolean = retryWithNextSource()

    /** 关闭播放页，回到频道列表。 */
    fun stop() {
        _playing.value = null
        _source.value = SourceState()
        _allSourcesFailed.value = false
    }

    /**
     * 找出同名的所有源。
     *
     * 频道目录里同一家电视台常在多个分类下出现，且指向不同源
     * （例如 CCTV-6 在「央视」是 tv.cctv.com，在「央视源2」是央视频）。
     * 具体匹配规则见 [com.example.music.data.tv.TvSourceMatcher]。
     */
    private fun collectSources(channel: TvChannel, categories: List<TvCategory>): List<TvChannel> =
        com.example.music.data.tv.TvSourceMatcher.sourcesOf(channel, categories)
}
