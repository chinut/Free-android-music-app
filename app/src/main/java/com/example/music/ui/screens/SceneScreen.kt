package com.example.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.example.music.data.SceneMode

@Composable
fun SceneScreen(vm: SceneViewModel = viewModel()) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        Text(
            "🎬 情景模式",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            "根据当前场景，自动为你播放合适的歌",
            color = Color(0x99FFFFFF),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (vm.loading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        } else if (vm.message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                vm.message,
                color = Color(0xFF8EC5FF),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(SceneMode.entries) { scene ->
                SceneCard(
                    scene = scene,
                    isPlaying = vm.lastScene == scene && vm.message.startsWith(scene.emoji),
                    onClick = {
                        vm.playScene(scene) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SceneCard(
    scene: SceneMode,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val gradient = when (scene) {
        SceneMode.DRIVING -> listOf(Color(0xFF3A7BFF), Color(0xFF6B9FFF))
        SceneMode.SAD -> listOf(Color(0xFF5B6B8E), Color(0xFF8E5BFF))
        SceneMode.PARTY -> listOf(Color(0xFFFF6B9F), Color(0xFF8E5BFF))
        SceneMode.STUDY -> listOf(Color(0xFF00B8C4), Color(0xFF3A7BFF))
        SceneMode.SPORT -> listOf(Color(0xFFFF8C42), Color(0xFFFF4D6D))
        SceneMode.SLEEP -> listOf(Color(0xFF4A3570), Color(0xFF1A1240))
        SceneMode.LOVE -> listOf(Color(0xFFFF4D6D), Color(0xFFFF9A6B))
        SceneMode.MEMORY -> listOf(Color(0xFF8B6F47), Color(0xFF4A3A2E))
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(colors = gradient)
            )
            .border(
                width = if (isPlaying) 2.dp else 0.dp,
                color = if (isPlaying) Color.White else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(scene.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    scene.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                scene.description,
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}