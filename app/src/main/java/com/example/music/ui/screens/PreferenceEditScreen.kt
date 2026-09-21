package com.example.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.music.data.RecommendEngine
import com.example.music.data.UserPreferencesStore

private val AccentBlue = Color(0xFF6B9FFF)
private val AccentPurple = Color(0xFF8E5BFF)
private val WarnRed = Color(0xFFFF6B6B)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferenceEditScreen(nav: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferencesStore(context) }

    var tab by remember { mutableStateOf(0) }

    val likedGenres = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getSelectedGenres()) }
    }
    val likedArtists = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getSelectedArtists()) }
    }
    // ★ 自定义喜欢的歌手
    val customLikedArtists = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getCustomLikedArtists()) }
    }

    val blockedGenres = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getBlacklistGenres()) }
    }
    val blockedArtists = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getBlacklistArtists()) }
    }
    val blockedKeywords = remember {
        mutableStateListOf<String>().apply { addAll(prefs.getBlacklistKeywords()) }
    }

    fun save() {
        prefs.setSelectedGenres(likedGenres.toSet())
        prefs.setSelectedArtists(likedArtists.toSet())
        prefs.setCustomLikedArtists(customLikedArtists.toSet())
        prefs.setBlacklistGenres(blockedGenres.toSet())
        prefs.setBlacklistArtists(blockedArtists.toSet())
        prefs.setBlacklistKeywords(blockedKeywords.toSet())
    }

    BackHandler {
        save()
        nav.popBackStack()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("音乐偏好", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = {
                    save()
                    nav.popBackStack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        PrimaryTabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            contentColor = AccentBlue
        ) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = {
                    Text(
                        "我喜欢",
                        color = if (tab == 0) AccentBlue else Color(0x99FFFFFF)
                    )
                }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = {
                    Text(
                        "黑名单",
                        color = if (tab == 1) AccentBlue else Color(0x99FFFFFF)
                    )
                }
            )
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (tab == 0) {
                LikedSection(
                    likedGenres = likedGenres,
                    likedArtists = likedArtists,
                    customLikedArtists = customLikedArtists,
                )
            } else {
                BlacklistSection(
                    blockedGenres = blockedGenres,
                    blockedArtists = blockedArtists,
                    blockedKeywords = blockedKeywords,
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentBlue, AccentPurple)
                        )
                    )
                    .clickable {
                        save()
                        nav.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "保存",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LikedSection(
    likedGenres: MutableList<String>,
    likedArtists: MutableList<String>,
    customLikedArtists: MutableList<String>,
) {
    var customInput by remember { mutableStateOf("") }

    fun addCustom() {
        val k = customInput.trim()
        if (k.isEmpty()) return
        if (k !in customLikedArtists) {
            customLikedArtists.add(k)
        }
        customInput = ""
    }

    Column {
        // ================= 喜欢的风格 =================
        Text(
            "喜欢的风格",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecommendEngine.ALL_GENRES.forEach { genre ->
                val selected = genre in likedGenres
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) likedGenres.remove(genre)
                        else likedGenres.add(genre)
                    },
                    label = { Text(genre, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x22FFFFFF),
                        labelColor = Color(0xCCFFFFFF),
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = Color(0xFF0A0B10)
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ================= ★ 自定义喜欢的歌手 =================
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "自定义喜欢的歌手",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "输入任意歌手名，可直接加入推荐池（无需在下方列表里）",
            color = Color(0x77FFFFFF),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        // 输入框 + 添加按钮
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x22FFFFFF))
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (customInput.isEmpty()) {
                    Text(
                        "输入歌手名，例如：陈鸿宇、房东的猫…",
                        color = Color(0x66FFFFFF),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(AccentBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addCustom() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (customInput.isNotBlank())
                            Brush.linearGradient(colors = listOf(AccentBlue, AccentPurple))
                        else
                            Brush.linearGradient(
                                colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF))
                            )
                    )
                    .clickable { addCustom() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加",
                    tint = if (customInput.isNotBlank()) Color.White else Color(0x66FFFFFF),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 已添加的自定义歌手
        if (customLikedArtists.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x11FFFFFF))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "还没有添加自定义歌手",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp
                )
            }
        } else {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customLikedArtists.toList().forEach { artist ->
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AccentBlue.copy(alpha = 0.2f))
                            .border(
                                width = 1.dp,
                                color = AccentBlue.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            artist,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除",
                            tint = Color(0xAAFFFFFF),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { customLikedArtists.remove(artist) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ================= 预设列表里的歌手 =================
        Text(
            "预设歌手（勾选加入推荐池）",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecommendEngine.ALL_ARTISTS.forEach { artist ->
                val selected = artist in likedArtists
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) likedArtists.remove(artist)
                        else likedArtists.add(artist)
                    },
                    label = { Text(artist, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x22FFFFFF),
                        labelColor = Color(0xCCFFFFFF),
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = Color(0xFF0A0B10)
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlacklistSection(
    blockedGenres: MutableList<String>,
    blockedArtists: MutableList<String>,
    blockedKeywords: MutableList<String>,
) {
    var keywordInput by remember { mutableStateOf("") }

    fun addKeyword() {
        val k = keywordInput.trim()
        if (k.isEmpty()) return
        if (k !in blockedKeywords) {
            blockedKeywords.add(k)
        }
        keywordInput = ""
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                tint = WarnRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "自定义屏蔽",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "输入任意关键词，只要歌手名字里包含它就会被屏蔽（不区分大小写）",
            color = Color(0x77FFFFFF),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x22FFFFFF))
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (keywordInput.isEmpty()) {
                    Text(
                        "输入关键词，例如：Rap、Live、翻唱…",
                        color = Color(0x66FFFFFF),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(AccentBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addKeyword() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (keywordInput.isNotBlank())
                            Brush.linearGradient(colors = listOf(AccentBlue, AccentPurple))
                        else
                            Brush.linearGradient(
                                colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF))
                            )
                    )
                    .clickable { addKeyword() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加",
                    tint = if (keywordInput.isNotBlank()) Color.White else Color(0x66FFFFFF),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (blockedKeywords.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x11FFFFFF))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "还没有添加自定义屏蔽词",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp
                )
            }
        } else {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                blockedKeywords.toList().forEach { kw ->
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(WarnRed.copy(alpha = 0.2f))
                            .border(
                                width = 1.dp,
                                color = WarnRed.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(kw, color = Color.White, fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除",
                            tint = Color(0xAAFFFFFF),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { blockedKeywords.remove(kw) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                tint = WarnRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "屏蔽的风格",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "选中的风格下所有歌手都不会被推荐",
            color = Color(0x77FFFFFF),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecommendEngine.ALL_GENRES.forEach { genre ->
                val selected = genre in blockedGenres
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) blockedGenres.remove(genre)
                        else blockedGenres.add(genre)
                    },
                    label = { Text(genre, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x22FFFFFF),
                        labelColor = Color(0xCCFFFFFF),
                        selectedContainerColor = WarnRed,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                tint = WarnRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "屏蔽的歌手",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "选中的歌手永远不会出现在推荐里",
            color = Color(0x77FFFFFF),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecommendEngine.ALL_ARTISTS.forEach { artist ->
                val selected = artist in blockedArtists
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) blockedArtists.remove(artist)
                        else blockedArtists.add(artist)
                    },
                    label = { Text(artist, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x22FFFFFF),
                        labelColor = Color(0xCCFFFFFF),
                        selectedContainerColor = WarnRed,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}