package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onThemeClick: (() -> Unit)? = null,
    onCloudClick: (() -> Unit)? = null,
    badgeCount: Int = 0,
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            actions()
            if (onCloudClick != null) {
                IconButton(
                    onClick = onCloudClick,
                    modifier = Modifier.testTag("top_bar_cloud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Firebase Cloud Status",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (onThemeClick != null) {
                IconButton(onClick = onThemeClick) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Switch Theme"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
