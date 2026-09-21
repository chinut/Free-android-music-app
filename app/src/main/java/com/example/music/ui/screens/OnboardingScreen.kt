package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music.data.RecommendEngine

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: (genres: Set<String>, artists: Set<String>) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val selectedGenres = remember { mutableStateListOf<String>() }
    val selectedArtists = remember { mutableStateListOf<String>() }

    // 根据选中的类型聚合出候选歌手；若没选类型，则展示所有类型下的歌手
    val candidateArtists = remember(selectedGenres.joinToString(",")) {
        if (selectedGenres.isEmpty()) {
            RecommendEngine.GENRE_ARTISTS.values.flatten().distinct()
        } else {
            selectedGenres
                .flatMap { RecommendEngine.GENRE_ARTISTS[it] ?: emptyList() }
                .distinct()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1240),
                        Color(0xFF10132A),
                        Color(0xFF0A0B10)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1080f, 1920f)
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(60.dp))

            // 顶部标题
            Text(
                "👋 欢迎使用",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (step == 0) "先告诉我们你喜欢什么类型的音乐" else "再选几个你喜欢的歌手",
                color = Color(0x99FFFFFF),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { (step + 1) / 2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF8EC5FF),
                trackColor = Color(0x22FFFFFF)
            )

            Spacer(Modifier.height(24.dp))

            // 内容区（可滚动）
            Box(Modifier.weight(1f)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (step == 0) {
                        // 第一步：类型
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RecommendEngine.ALL_GENRES.forEach { genre ->
                                val selected = genre in selectedGenres
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedGenres.remove(genre)
                                        else selectedGenres.add(genre)
                                        // 换类型时清空已选歌手
                                        selectedArtists.clear()
                                    },
                                    label = { Text(genre, fontSize = 14.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(0x22FFFFFF),
                                        labelColor = Color(0xCCFFFFFF),
                                        selectedContainerColor = Color(0xFF8EC5FF),
                                        selectedLabelColor = Color(0xFF0A0B10)
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    } else {
                        // 第二步：歌手
                        if (candidateArtists.isEmpty()) {
                            Text(
                                "暂无可选歌手",
                                color = Color(0x99FFFFFF),
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            FlowRow(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                candidateArtists.forEach { artist ->
                                    val selected = artist in selectedArtists
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            if (selected) selectedArtists.remove(artist)
                                            else selectedArtists.add(artist)
                                        },
                                        label = { Text(artist, fontSize = 14.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0x22FFFFFF),
                                            labelColor = Color(0xCCFFFFFF),
                                            selectedContainerColor = Color(0xFF8EC5FF),
                                            selectedLabelColor = Color(0xFF0A0B10)
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // 底部操作栏
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(onClick = { step = 0 }) {
                        Text("上一步", color = Color(0x99FFFFFF))
                    }
                }
                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (step == 0) {
                            if (selectedGenres.isEmpty()) {
                                // 允许跳过：都为空时也给一个默认
                                selectedGenres.add("流行")
                            }
                            step = 1
                        } else {
                            onFinish(
                                selectedGenres.toSet(),
                                selectedArtists.toSet()
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8EC5FF),
                        contentColor = Color(0xFF0A0B10)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        if (step == 0) "下一步" else "开始使用",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}