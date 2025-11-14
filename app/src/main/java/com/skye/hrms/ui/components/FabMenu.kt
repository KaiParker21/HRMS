package com.skye.hrms.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.skye.hrms.R

data class DocumentFabItem(
    val text: String,
    val icon: ImageVector,
    val mimeType: String
)

// 2. The list of items for this specific menu
val documentFabItems = listOf(
    DocumentFabItem(
        text = "Image",
        icon = Icons.Outlined.Image,
        mimeType = "image/*"
    ),
    DocumentFabItem(
        text = "PDF",
        icon = Icons.Outlined.PictureAsPdf,
        mimeType = "application/pdf"
    ),
    DocumentFabItem(
        text = "Any File",
        icon = Icons.AutoMirrored.Outlined.Article,
        mimeType = "*/*"
    )
)

// 3. The reusable FabMenu Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentFabMenu(
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    onItemClicked: (DocumentFabItem) -> Unit
) {
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = onToggle
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Upload a file"
                )
            }
        }
    ) {
        documentFabItems.forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onItemClicked(item)
                },
                text = { Text(text = item.text) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                }
            )
        }
    }
}