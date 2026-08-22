package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ThemeSelectorSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OcrScannerScreen
import com.example.ui.screens.PaperConfigScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.theme.McqGeneratorTheme
import com.example.ui.viewmodel.McqViewModel

enum class AppScreen(val title: String, val icon: ImageVector, val tabLabel: String) {
    DASHBOARD("Dashboard", Icons.Default.Home, "Overview"),
    OCR_SCANNER("OCR Scanner", Icons.Default.CameraAlt, "OCR Scan"),
    QUESTION_BANK("Question Bank", Icons.Default.FormatListNumbered, "Bank"),
    PAPER_CONFIG("Paper Config", Icons.Default.Description, "Exam Paper"),
    PDF_PREVIEW("PDF Preview", Icons.Default.PictureAsPdf, "Preview")
}

class MainActivity : ComponentActivity() {

    private val viewModel: McqViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val activeTheme by viewModel.appThemeMode.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
            var showThemeSheet by remember { mutableStateOf(false) }

            McqGeneratorTheme(appThemeMode = activeTheme) {
                Scaffold(
                    bottomBar = {
                        // Only show bottom navigation on primary 4 screens (hide during full-page PDF preview)
                        if (currentScreen != AppScreen.PDF_PREVIEW) {
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("bottom_navigation_bar"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                val navTabs = listOf(
                                    AppScreen.DASHBOARD,
                                    AppScreen.OCR_SCANNER,
                                    AppScreen.QUESTION_BANK,
                                    AppScreen.PAPER_CONFIG
                                )

                                navTabs.forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.tabLabel,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                            when (screen) {
                                AppScreen.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToOcr = { currentScreen = AppScreen.OCR_SCANNER },
                                    onNavigateToBank = { currentScreen = AppScreen.QUESTION_BANK },
                                    onNavigateToPaperConfig = { currentScreen = AppScreen.PAPER_CONFIG },
                                    onNavigateToPdfPreview = { _ ->
                                        currentScreen = AppScreen.PDF_PREVIEW
                                    },
                                    onThemeClick = { showThemeSheet = true }
                                )

                                AppScreen.OCR_SCANNER -> OcrScannerScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = AppScreen.DASHBOARD },
                                    onQuestionSaved = { currentScreen = AppScreen.QUESTION_BANK }
                                )

                                AppScreen.QUESTION_BANK -> QuestionBankScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = AppScreen.DASHBOARD },
                                    onNavigateToOcr = { currentScreen = AppScreen.OCR_SCANNER },
                                    onNavigateToPaperConfig = { currentScreen = AppScreen.PAPER_CONFIG }
                                )

                                AppScreen.PAPER_CONFIG -> PaperConfigScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = AppScreen.DASHBOARD },
                                    onNavigateToBank = { currentScreen = AppScreen.QUESTION_BANK },
                                    onNavigateToPdfPreview = { _ ->
                                        currentScreen = AppScreen.PDF_PREVIEW
                                    }
                                )

                                AppScreen.PDF_PREVIEW -> PdfPreviewScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = AppScreen.PAPER_CONFIG },
                                    onNavigateHome = { currentScreen = AppScreen.DASHBOARD }
                                )
                            }
                        }
                    }

                    if (showThemeSheet) {
                        ThemeSelectorSheet(
                            activeTheme = activeTheme,
                            onSelectTheme = { viewModel.appThemeMode.value = it },
                            onDismiss = { showThemeSheet = false }
                        )
                    }
                }
            }
        }
    }
}
