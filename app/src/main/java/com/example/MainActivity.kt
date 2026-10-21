package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisTab
import com.example.ui.MainViewModel
import com.example.ui.components.TacticalConfirmationDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainCoreScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisDarkObsidian
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisCyanSecondary
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val confirmationDialog by viewModel.confirmationDialog.collectAsState()

                // Permission launcher for voice recognition
                val micPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.triggerVoiceInput()
                    } else {
                        viewModel.emitToast("Se requiere permiso de micrófono para la entrada vocal")
                    }
                }

                // Handle back navigation
                if (currentTab != JarvisTab.CORE) {
                    BackHandler {
                        viewModel.selectTab(JarvisTab.CORE)
                    }
                }

                // Listen for Toast notifications
                LaunchedEffect(Unit) {
                    viewModel.toastEvents.collect { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // Listen for intent dispatches (alarms, apps, web search)
                LaunchedEffect(Unit) {
                    viewModel.intentEvents.collect { intent ->
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Fallo al iniciar acción del sistema: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisDarkObsidian),
                    containerColor = JarvisDarkObsidian,
                    bottomBar = {
                        JarvisBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (currentTab) {
                            JarvisTab.CORE -> MainCoreScreen(
                                viewModel = viewModel,
                                onRequestMicrophonePermission = {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            )
                            JarvisTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                            JarvisTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                            JarvisTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onRequestMicrophonePermission = {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            )
                        }

                        // Sensitive Action Confirmation Dialog
                        confirmationDialog?.let { dialog ->
                            TacticalConfirmationDialog(
                                title = dialog.title,
                                message = dialog.message,
                                onConfirm = dialog.onConfirm,
                                onDismiss = { viewModel.dismissConfirmationDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisBottomNav(
    currentTab: JarvisTab,
    onTabSelected: (JarvisTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF030812),
        border = BorderStroke(1.dp, JarvisBorderGlow.copy(alpha = 0.25f))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            val tabs = listOf(
                Triple(JarvisTab.CORE, "INICIO", Icons.Default.Home),
                Triple(JarvisTab.TOOLS, "COMANDOS", Icons.Default.GridView),
                Triple(JarvisTab.HISTORY, "HISTORIAL", Icons.Default.History),
                Triple(JarvisTab.SETTINGS, "AJUSTES", Icons.Default.Settings)
            )

            tabs.forEach { (tab, label, icon) ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(3.dp)
                                        .background(JarvisCyanPrimary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyanPrimary,
                        selectedTextColor = JarvisCyanPrimary,
                        unselectedIconColor = JarvisTextMuted,
                        unselectedTextColor = JarvisTextMuted,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                )
            }
        }
    }
}
