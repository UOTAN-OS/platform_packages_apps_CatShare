/*
 * Copyright (C) 2026 The uwuAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uwuaosp.compose.settingslib

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SettingsToolbarActionButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
internal fun SettingsToolbar(
    title: String,
    showBackButton: Boolean,
    useCollapsingToolbar: Boolean,
    progress: Float,
    toolbarHeight: Dp,
    statusBarPadding: Dp,
    onNavigateUp: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    val actionBarHeight = actionBarSize()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarPadding + toolbarHeight)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = statusBarPadding)
    ) {
        if (showBackButton) {
            SettingsToolbarActionButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                onClick = onNavigateUp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 15.dp)
                    .height(actionBarHeight),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .height(actionBarHeight)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
        if (useCollapsingToolbar) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .alpha(1f - progress)
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(actionBarHeight)
                .padding(start = if (showBackButton) 88.dp else 24.dp, end = 24.dp)
                .alpha(if (useCollapsingToolbar) progress else 1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
