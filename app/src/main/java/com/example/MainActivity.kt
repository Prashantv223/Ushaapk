package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.screens.*
import com.example.ui.theme.ClrAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeStyle by viewModel.themeStyle.collectAsState()
            val darkModeSetting by viewModel.darkModeSetting.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val statusMessage by viewModel.statusMessage.collectAsState()
            val activeMediaItem by viewModel.activeMediaItem.collectAsState()

            var showVoiceDialog by remember { mutableStateOf(false) }

            // Back handler
            BackHandler(enabled = true) {
                when {
                    activeMediaItem != null -> viewModel.closeMedia()
                    currentScreen == AppScreen.FILE_MANAGER && !viewModel.navigateUp() -> finish()
                    currentScreen != AppScreen.FILE_MANAGER -> viewModel.navigateTo(AppScreen.FILE_MANAGER)
                    else -> finish()
                }
            }

            ClrAppTheme(
                themeStyle = themeStyle,
                darkModeSetting = darkModeSetting
            ) {
                Scaffold(
                    topBar = {
                        AppTopBar(
                            viewModel = viewModel,
                            currentScreen = currentScreen,
                            onVoiceClick = { showVoiceDialog = true }
                        )
                    },
                    bottomBar = {
                        AppBottomNav(
                            currentScreen = currentScreen,
                            onSelectScreen = { screen ->
                                viewModel.navigateTo(screen)
                            }
                        )
                    },
                    snackbarHost = {
                        statusMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 70.dp, start = 16.dp, end = 16.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Snackbar(
                                    action = {
                                        TextButton(onClick = { viewModel.showMessage("") }) {
                                            Text("OK", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                ) {
                                    Text(msg)
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.FILE_MANAGER -> FileManagerScreen(viewModel)
                                AppScreen.AI_CONVERTER -> AiFileConverterScreen(viewModel)
                                AppScreen.PRIVATE_VAULT -> PrivateVaultScreen(viewModel)
                                AppScreen.MAPS -> MapScreen(viewModel)
                                AppScreen.HINDI_CALENDAR -> HindiCalendarScreen(viewModel)
                                AppScreen.CLOUD_SYNC -> CloudSyncScreen(viewModel)
                                AppScreen.QR_SHARE -> QrScreen(viewModel)
                                AppScreen.SETTINGS -> SettingsScreen(viewModel)
                            }
                        }

                        // Voice Command Modal
                        if (showVoiceDialog) {
                            VoiceCommandDialog(
                                viewModel = viewModel,
                                onDismiss = { showVoiceDialog = false }
                            )
                        }

                        // Media Player Modal
                        activeMediaItem?.let { mediaItem ->
                            MediaPlayerDialog(
                                item = mediaItem,
                                viewModel = viewModel,
                                onDismiss = { viewModel.closeMedia() }
                            )
                        }
                    }
                }
            }
        }
    }
}
