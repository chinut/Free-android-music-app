package com.example.music.ui

import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.music.MusicApp
import com.example.music.data.UserPreferencesStore
import com.example.music.data.update.UpdateChecker
import com.example.music.data.update.UpdateInfo
import com.example.music.player.PlayerHolder
import com.example.music.ui.components.ImportShareDialog
import com.example.music.ui.components.MiniPlayer
import com.example.music.ui.components.SidePlayerPanel
import com.example.music.ui.components.UpdateDialog
import com.example.music.ui.screens.EQScreen
import com.example.music.ui.screens.FullPlayerScreen
import com.example.music.ui.screens.HomeScreen
import com.example.music.ui.screens.OnboardingScreen
import com.example.music.ui.screens.PlaylistDetailScreen
import com.example.music.ui.screens.PlaylistScreen
import com.example.music.ui.screens.PreferenceEditScreen
import com.example.music.ui.screens.SceneScreen
import com.example.music.ui.screens.SettingsScreen
import com.example.music.ui.screens.SplashScreen
import com.example.music.ui.theme.DynamicBackground
import kotlinx.coroutines.delay

@Composable
fun MusicApp(
    initialShareText: String? = null,
    onShareConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 1) 开屏
    val app = MusicApp.instance
    var showSplash by remember { mutableStateOf(!app.splashShown) }

    if (showSplash) {
        SplashScreen(onFinish = {
            app.splashShown = true
            showSplash = false
        })
        return
    }

    // 2) 首次引导
    val prefs = remember { UserPreferencesStore(context) }
    var onboarded by remember { mutableStateOf(prefs.isOnboarded()) }
    var forceOnboarding by remember { mutableStateOf(false) }

    if (!onboarded || forceOnboarding) {
        OnboardingScreen(onFinish = { genres, artists ->
            prefs.setSelectedGenres(genres)
            prefs.setSelectedArtists(artists)
            prefs.setOnboarded(true)
            onboarded = true
            forceOnboarding = false
        })
        return
    }

    // 3) 主界面
    val nav = rememberNavController()
    var showFullPlayer by remember { mutableStateOf(false) }
    var incomingShare by remember { mutableStateOf<String?>(null) }
    var pendingUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    val currentSong by PlayerHolder.currentSong.collectAsState()
    val hasSong = currentSong != null

    LaunchedEffect(Unit) {
        delay(3000)
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            pendingUpdate = UpdateChecker.checkForUpdate(currentCode)
        } catch (e: Exception) {
            // 忽略
        }
    }

    LaunchedEffect(initialShareText) {
        if (!initialShareText.isNullOrBlank()) {
            incomingShare = initialShareText
        }
    }

    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: "home"

    Box(Modifier.fillMaxSize()) {
        DynamicBackground {
            if (isLandscape) {
                Row(Modifier.fillMaxSize()) {
                    AppNavigationRail(nav = nav, route = route)

                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        MainNavHost(
                            nav = nav,
                            onReopenOnboarding = { forceOnboarding = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (hasSong) {
                        SidePlayerPanel(
                            modifier = Modifier.fillMaxHeight(),
                            width = 220.dp,
                            onExpand = { showFullPlayer = true }
                        )
                    }
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0x4D000000),
                                            Color(0xB3000000)
                                        )
                                    )
                                )
                        ) {
                            MiniPlayer(onExpand = { showFullPlayer = true })
                            AppBottomBar(nav = nav, route = route)
                        }
                    }
                ) { padding ->
                    MainNavHost(
                        nav = nav,
                        onReopenOnboarding = { forceOnboarding = true },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        if (showFullPlayer) {
            FullPlayerScreen(onClose = { showFullPlayer = false })
        }

        pendingUpdate?.let { info ->
            UpdateDialog(info = info, onDismiss = { pendingUpdate = null })
        }
    }

    incomingShare?.let { text ->
        ImportShareDialog(
            initialText = text,
            onDismiss = {
                incomingShare = null
                onShareConsumed()
            },
            onImported = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
private fun MainNavHost(
    nav: NavHostController,
    onReopenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = nav,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { HomeScreen() }
        composable("scene") { SceneScreen() }
        composable("playlists") { PlaylistScreen(nav) }
        composable("playlists/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            PlaylistDetailScreen(nav, id)
        }
        composable("settings") {
            SettingsScreen(
                onReopenOnboarding = onReopenOnboarding,
                onOpenPreferenceEdit = {
                    nav.navigate("preference_edit")
                },
                onOpenEQ = {
                    nav.navigate("eq")
                }
            )
        }
        composable("preference_edit") {
            PreferenceEditScreen(nav)
        }
        composable("eq") {
            EQScreen(nav)
        }
    }
}

@Composable
private fun AppNavigationRail(
    nav: NavHostController,
    route: String,
) {
    val itemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = Color(0xFF8EC5FF),
        selectedTextColor = Color(0xFF8EC5FF),
        indicatorColor = Color(0x338EC5FF),
        unselectedIconColor = Color(0x99FFFFFF),
        unselectedTextColor = Color(0x99FFFFFF)
    )

    NavigationRail(
        containerColor = Color(0x66000000),
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(Modifier.weight(1f))

        NavigationRailItem(
            selected = route == "home",
            onClick = {
                nav.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "发现") },
            label = { Text("发现") },
            colors = itemColors
        )

        Spacer(Modifier.height(16.dp))

        NavigationRailItem(
            selected = route == "scene",
            onClick = {
                nav.navigate("scene") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "情景") },
            label = { Text("情景") },
            colors = itemColors
        )

        Spacer(Modifier.height(16.dp))

        NavigationRailItem(
            selected = route.startsWith("playlists"),
            onClick = {
                nav.navigate("playlists") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "歌单") },
            label = { Text("歌单") },
            colors = itemColors
        )

        Spacer(Modifier.height(16.dp))

        NavigationRailItem(
            selected = route == "settings" ||
                    route == "preference_edit" ||
                    route == "eq",
            onClick = {
                nav.navigate("settings") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
            label = { Text("设置") },
            colors = itemColors
        )

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun AppBottomBar(
    nav: NavHostController,
    route: String,
) {
    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF8EC5FF),
            selectedTextColor = Color(0xFF8EC5FF),
            indicatorColor = Color(0x338EC5FF),
            unselectedIconColor = Color(0x99FFFFFF),
            unselectedTextColor = Color(0x99FFFFFF)
        )

        NavigationBarItem(
            selected = route == "home",
            onClick = {
                nav.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "发现") },
            label = { Text("发现") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = route == "scene",
            onClick = {
                nav.navigate("scene") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "情景") },
            label = { Text("情景") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = route.startsWith("playlists"),
            onClick = {
                nav.navigate("playlists") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "歌单") },
            label = { Text("歌单") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = route == "settings" ||
                    route == "preference_edit" ||
                    route == "eq",
            onClick = {
                nav.navigate("settings") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
            label = { Text("设置") },
            colors = itemColors
        )
    }
}