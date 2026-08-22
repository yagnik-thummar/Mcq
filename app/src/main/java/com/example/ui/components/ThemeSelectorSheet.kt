package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.SunsetPrimary
import com.example.ui.theme.SunsetSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorSheet(
    activeTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("theme_selector_sheet")
        ) {
            Text(
                text = "Select Palette Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Dynamic Compose Material 3 color schemes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            ThemeOptionItem(
                title = "Indigo Slate",
                description = "Modern deep indigo with sky blue accents",
                primaryColor = IndigoPrimary,
                secondaryColor = IndigoSecondary,
                isSelected = activeTheme == AppThemeMode.INDIGO_SLATE,
                onClick = {
                    onSelectTheme(AppThemeMode.INDIGO_SLATE)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeOptionItem(
                title = "Emerald Forest",
                description = "Vibrant organic green with cyber teal highlights",
                primaryColor = EmeraldPrimary,
                secondaryColor = EmeraldSecondary,
                isSelected = activeTheme == AppThemeMode.EMERALD_FOREST,
                onClick = {
                    onSelectTheme(AppThemeMode.EMERALD_FOREST)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeOptionItem(
                title = "Sunset Copper",
                description = "Warm orange tone with hot pink highlights",
                primaryColor = SunsetPrimary,
                secondaryColor = SunsetSecondary,
                isSelected = activeTheme == AppThemeMode.SUNSET_COPPER,
                onClick = {
                    onSelectTheme(AppThemeMode.SUNSET_COPPER)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeOptionItem(
                title = "Cyber Neon",
                description = "High-contrast tech dark theme with electric cyan",
                primaryColor = CyberPrimary,
                secondaryColor = CyberSecondary,
                isSelected = activeTheme == AppThemeMode.CYBER_NEON,
                onClick = {
                    onSelectTheme(AppThemeMode.CYBER_NEON)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    description: String,
    primaryColor: Color,
    secondaryColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("theme_option_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(primaryColor, secondaryColor))
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
