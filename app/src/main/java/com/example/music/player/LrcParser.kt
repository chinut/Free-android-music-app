package com.example.music.player

import com.example.music.data.LrcLine

object LrcParser {

    // 匹配时间标签：[mm:ss]、[mm:ss.xx]、[mm:ss:xx]
    private val TIME_RE = Regex("\\[(\\d{1,2}):(\\d{1,2})(?:[.:](\\d{1,3}))?\\]")

    // 匹配其他元数据标签：[ti:xxx]、[ar:xxx]、[al:xxx]、[by:xxx]、[offset:xxx] 等
    private val META_TAG_RE = Regex("\\[[a-zA-Z]{1,10}:[^\\]]*\\]")

    fun parse(raw: String?): List<LrcLine> {
        if (raw.isNullOrBlank()) return emptyList()

        // ★ 统一换行符为 \n（处理 \r\n、\r、\n 各种情况）
        val normalized = raw
            .replace("\r\n", "\n")
            .replace('\r', '\n')

        val out = ArrayList<LrcLine>()

        normalized.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            // 找出这一行里所有的时间标签
            val matches = TIME_RE.findAll(line).toList()
            if (matches.isEmpty()) return@forEach  // 没有时间标签 → 不是歌词行

            // ★ 去掉所有时间标签和元数据标签，剩下的就是纯歌词文本
            val text = line
                .replace(TIME_RE, "")
                .replace(META_TAG_RE, "")
                .trim()

            if (text.isEmpty()) return@forEach

            // 这一行可能带多个时间标签（同一句歌词出现在多个时间点）
            matches.forEach { m ->
                val min = m.groupValues[1].toLongOrNull() ?: 0L
                val sec = m.groupValues[2].toLongOrNull() ?: 0L
                val msStr = m.groupValues[3]
                val ms = when (msStr.length) {
                    0 -> 0L
                    1 -> msStr.toLong() * 100
                    2 -> msStr.toLong() * 10
                    else -> msStr.take(3).toLong()
                }
                out.add(LrcLine(min * 60_000 + sec * 1_000 + ms, text))
            }
        }

        return out.sortedBy { it.time }
    }
}