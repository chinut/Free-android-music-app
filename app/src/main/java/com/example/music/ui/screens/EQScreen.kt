package com.example.music.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.music.data.EQManager
import com.example.music.data.EQPreset
import com.example.music.data.EQPresets
import com.example.music.data.UserPreferencesStore

private val AccentBlue = Color(0xFF6B9FFF)
private val AccentPurple = Color(0xFF8E5BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EQScreen(nav: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferencesStore(context) }

    var enabled by remember { mutableStateOf(prefs.isEQEnabled()) }
    var activeId by remember { mutableStateOf(prefs.getActiveEQId()) }

    val customList = remember {
        mutableStateListOf<EQPreset>().apply { addAll(prefs.getCustomEQs()) }
    }
    val bandLevels = remember { mutableStateListOf<Int>() }

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }

    fun findPreset(id: String): EQPreset? =
        EQPresets.byId(id) ?: customList.firstOrNull { it.id == id }

    fun reloadBands() {
        val actual = EQManager.readCurrentBandLevels()
        bandLevels.clear()
        if (actual.isNotEmpty()) {
            bandLevels.addAll(actual)
        } else {
            val preset = findPreset(activeId)
            val fallbackCount = if (EQManager.bandCount > 0) EQManager.bandCount else 5
            bandLevels.addAll(preset?.bands ?: List(fallbackCount) { 0 })
        }
    }

    LaunchedEffect(activeId, enabled) {
        reloadBands()
    }

    fun applyPreset(preset: EQPreset) {
        activeId = preset.id
        prefs.setActiveEQId(preset.id)
        EQManager.applyPreset(preset)
        reloadBands()
    }

    fun setEnabled(v: Boolean) {
        enabled = v
        prefs.setEQEnabled(v)
        EQManager.setEnabled(v)
    }

    BackHandler { nav.popBackStack() }

    Column(Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("均衡器", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // 开关行
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "启用均衡器",
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enabled,
                onCheckedChange = { setEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0A0B10),
                    checkedTrackColor = AccentBlue,
                    uncheckedThumbColor = Color(0x99FFFFFF),
                    uncheckedTrackColor = Color(0x33FFFFFF)
                )
            )
        }

        // 预设横向列表
        Text(
            "预设",
            color = Color(0x99FFFFFF),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
        )
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(EQPresets.ALL, key = { it.id }) { preset ->
                PresetChip(
                    label = preset.name,
                    selected = activeId == preset.id,
                    onClick = { applyPreset(preset) }
                )
            }
            items(customList.toList(), key = { it.id }) { preset ->
                PresetChip(
                    label = preset.name,
                    selected = activeId == preset.id,
                    onClick = { applyPreset(preset) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 频段滑块
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (bandLevels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "播放一首歌后即可调整",
                        color = Color(0x88FFFFFF),
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bandLevels.forEachIndexed { i, level ->
                        VerticalBand(
                            label = EQManager.bandLabel(i),
                            value = level,
                            minValue = EQManager.levelRange.start.toInt(),
                            maxValue = EQManager.levelRange.endInclusive.toInt(),
                            enabled = enabled,
                            onValueChange = { newValue ->
                                bandLevels[i] = newValue
                                EQManager.setBandLevel(i, newValue)
                            }
                        )
                    }
                }
            }
        }

        // 底部操作栏
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 保存为自定义
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(listOf(AccentBlue, AccentPurple)))
                    .clickable(enabled = bandLevels.isNotEmpty()) {
                        saveName = ""
                        showSaveDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("保存为自定义", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // 删除自定义（仅当激活的是自定义 EQ）
            val activeCustom = customList.firstOrNull { it.id == activeId }
            if (activeCustom != null) {
                Box(
                    Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x33FF4D6D))
                        .border(1.dp, Color(0x66FF4D6D), RoundedCornerShape(50))
                        .clickable {
                            prefs.deleteCustomEQ(activeId)
                            customList.removeAll { it.id == activeId }
                            applyPreset(EQPresets.flat)
                            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("删除", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 保存对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存 EQ 预设") },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    placeholder = { Text("预设名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = saveName.isNotBlank(),
                    onClick = {
                        val newPreset = EQPreset(
                            id = "custom_${System.currentTimeMillis()}",
                            name = saveName.trim(),
                            bands = bandLevels.toList(),
                            isBuiltIn = false
                        )
                        prefs.saveCustomEQ(newPreset)
                        customList.add(newPreset)
                        applyPreset(newPreset)
                        showSaveDialog = false
                        Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0x22FFFFFF),
            labelColor = Color(0xCCFFFFFF),
            selectedContainerColor = AccentBlue,
            selectedLabelColor = Color(0xFF0A0B10)
        ),
        shape = RoundedCornerShape(50)
    )
}

/**
 * 竖直滑块：上下拖动改变增益值。
 */
@Composable
private fun VerticalBand(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    var trackHeight by remember { mutableStateOf(0f) }
    val range = (maxValue - minValue).toFloat()

    val fraction = ((value - minValue) / range).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // 顶部数值
        Text(
            text = formatGain(value),
            color = Color(0x99FFFFFF),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // 滑块轨道
        Box(
            Modifier
                .width(40.dp)
                .weight(1f)
                .onSizeChanged { trackHeight = it.height.toFloat() }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectVerticalDragGestures { change, _ ->
                        val y = change.position.y.coerceIn(0f, trackHeight)
                        val f = 1f - (y / trackHeight)
                        val newValue = (minValue + f * range).toInt()
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // 底轨
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
            )
            // 已选填充
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(fraction)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(listOf(AccentPurple, AccentBlue))
                    )
            )
            // 拖动点
            if (trackHeight > 0f) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                0,
                                -(trackHeight * fraction - 9.dp.toPx()).toInt()
                            )
                        }
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (enabled) Color.White else Color(0x88FFFFFF))
                )
            }
        }

        // 底部频率
        Text(
            text = label,
            color = Color(0x99FFFFFF),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private fun formatGain(mb: Int): String {
    if (mb == 0) return "0"
    val sign = if (mb > 0) "+" else ""
    return "$sign${"%.1f".format(mb / 100f)}"
}