package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.data.model.AppInfo
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.AppListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CacheHelperTheme
import com.example.ui.viewmodel.AppListViewModel

enum class NavigationScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isBottomBarVisible: Boolean = true
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    APPS("Apps List", Icons.Filled.Apps, Icons.Outlined.Apps),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    APP_DETAIL("App Details", Icons.Filled.Apps, Icons.Outlined.Apps, isBottomBarVisible = false),
    ABOUT("About App", Icons.Filled.Settings, Icons.Outlined.Settings, isBottomBarVisible = false)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val uiState by viewModel.uiState.collectAsState()

            CacheHelperTheme(themeMode = themeMode) {
                var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }

                Scaffold(
                    contentWindowInsets = WindowInsets.navigationBars,
                    bottomBar = {
                        if (currentScreen.isBottomBarVisible) {
                            NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
                                listOf(
                                    NavigationScreen.HOME,
                                    NavigationScreen.APPS,
                                    NavigationScreen.SETTINGS
                                ).forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = { Text(screen.title) },
                                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                NavigationScreen.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        onScanAppsClicked = { viewModel.scanInstalledApps() },
                                        onNavigateToAppsList = { currentScreen = NavigationScreen.APPS },
                                        onNavigateToSettings = { currentScreen = NavigationScreen.SETTINGS },
                                        onAppSelected = { app ->
                                            viewModel.selectApp(app)
                                            currentScreen = NavigationScreen.APP_DETAIL
                                        }
                                    )
                                }

                                NavigationScreen.APPS -> {
                                    AppListScreen(
                                        uiState = uiState,
                                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                        onFilterSelected = { viewModel.onFilterSelected(it) },
                                        onSortSelected = { viewModel.onSortSelected(it) },
                                        onRefresh = { viewModel.scanInstalledApps() },
                                        onAppSelected = { app ->
                                            viewModel.selectApp(app)
                                            currentScreen = NavigationScreen.APP_DETAIL
                                        }
                                    )
                                }

                                NavigationScreen.SETTINGS -> {
                                    SettingsScreen(
                                        currentThemeMode = themeMode,
                                        onThemeModeSelected = { viewModel.setThemeMode(it) },
                                        onNavigateToAbout = { currentScreen = NavigationScreen.ABOUT }
                                    )
                                }

                                NavigationScreen.APP_DETAIL -> {
                                    uiState.selectedApp?.let { app ->
                                        AppDetailScreen(
                                            app = app,
                                            onBackClicked = { currentScreen = NavigationScreen.APPS }
                                        )
                                    } ?: run {
                                        currentScreen = NavigationScreen.APPS
                                    }
                                }

                                NavigationScreen.ABOUT -> {
                                    AboutScreen(
                                        onBackClicked = { currentScreen = NavigationScreen.SETTINGS }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
