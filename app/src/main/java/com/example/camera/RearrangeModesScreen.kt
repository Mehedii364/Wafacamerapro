package com.example.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class ModeItemData(
    val mode: CaptureMode,
    val displayName: String
)

val ALL_SHOOTING_MODES = listOf(
    ModeItemData(CaptureMode.NIGHT, "NIGHT"),
    ModeItemData(CaptureMode.HI_RES, "HI-RES"),
    ModeItemData(CaptureMode.PANO, "PANO"),
    ModeItemData(CaptureMode.MACRO, "MACRO"),
    ModeItemData(CaptureMode.SLO_MO, "SLO-MO"),
    ModeItemData(CaptureMode.TIME_LAPSE, "TIME-LAPSE"),
    ModeItemData(CaptureMode.DUAL_VIDEO, "VIEW VIDEO"),
    ModeItemData(CaptureMode.UNDERWATER, "UNDERWATER"),
    ModeItemData(CaptureMode.STICKER, "STICKER"),
    ModeItemData(CaptureMode.DOC_SCANNER, "DOC SCANNER"),
    ModeItemData(CaptureMode.PORTRAIT, "PORTRAIT"),
    ModeItemData(CaptureMode.PRO, "PRO")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RearrangeModesScreen(
    currentPinnedModes: List<CaptureMode>,
    onSavePinnedModes: (List<CaptureMode>) -> Unit,
    onSelectModeAndClose: (CaptureMode) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var pinnedList by remember { mutableStateOf(currentPinnedModes.distinct()) }
    var selectedForAction by remember { mutableStateOf<CaptureMode?>(null) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hold and drag to rearrange shooting modes.",
                        color = Color(0xFFE2E4E8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("rearrange_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 3-Column Grid of 12 Shooting Modes
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ALL_SHOOTING_MODES) { item ->
                    val isPinned = pinnedList.contains(item.mode)
                    val isTargeted = selectedForAction == item.mode

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isTargeted) Color(0xFF262A36) else Color(0xFF141518)
                            )
                            .border(
                                width = if (isTargeted) 1.5.dp else if (isPinned) 1.dp else 0.5.dp,
                                color = if (isTargeted) Color(0xFF00E5FF) else if (isPinned) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color(0xFF22242A),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!pinnedList.contains(item.mode)) {
                                            pinnedList = (pinnedList + item.mode).distinct()
                                        }
                                    },
                                    onDrag = { _, _ -> },
                                    onDragEnd = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isPinned) {
                                    // Already pinned: tap to launch mode immediately
                                    onSelectModeAndClose(item.mode)
                                } else {
                                    // Add to pinned tray
                                    pinnedList = (pinnedList + item.mode).distinct()
                                }
                            }
                            .testTag("mode_card_${item.mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            ShootingModeIcon(
                                mode = item.mode,
                                tint = if (isPinned) Color.White else Color(0xFFCCCCCC),
                                size = 32.dp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.displayName,
                                color = if (isPinned) Color.White else Color(0xFFAAAAAA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        // Status badge in top-end
                        if (isPinned) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(16.dp)
                                    .background(Color(0xFF00E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Pinned",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Section: Tray and Done Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tray Slots Row (matching screenshot: [VIDEO] [PHOTO] [dashed box])
                Text(
                    text = "Active Bottom Bar Modes (Tap to remove or reorder)",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(pinnedList) { index, mode ->
                        val displayName = when (mode) {
                            CaptureMode.VIDEO -> "VIDEO"
                            CaptureMode.PHOTO -> "PHOTO"
                            CaptureMode.PORTRAIT -> "PORTRAIT"
                            CaptureMode.NIGHT -> "NIGHT"
                            CaptureMode.PRO -> "PRO"
                            CaptureMode.HI_RES -> "HI-RES"
                            CaptureMode.PANO -> "PANO"
                            CaptureMode.MACRO -> "MACRO"
                            CaptureMode.SLO_MO -> "SLO-MO"
                            CaptureMode.TIME_LAPSE -> "TIME-LAPSE"
                            CaptureMode.DUAL_VIDEO -> "VIEW VIDEO"
                            CaptureMode.UNDERWATER -> "UNDERWATER"
                            CaptureMode.STICKER -> "STICKER"
                            CaptureMode.DOC_SCANNER -> "DOC SCANNER"
                            CaptureMode.HDR -> "HDR"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E2024),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333640)),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .testTag("tray_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Move left
                                if (index > 0) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Move left",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val mutable = pinnedList.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index - 1]
                                                mutable[index - 1] = temp
                                                pinnedList = mutable
                                            }
                                    )
                                }

                                Text(
                                    text = displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                // Move right
                                if (index < pinnedList.size - 1) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Move right",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val mutable = pinnedList.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index + 1]
                                                mutable[index + 1] = temp
                                                pinnedList = mutable
                                            }
                                    )
                                }

                                // Remove from tray (keep minimum 2)
                                if (pinnedList.size > 2) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                pinnedList = pinnedList.filter { it != mode }
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Empty dashed slot
                    item {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = 64.dp, height = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF444752),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color(0xFF141518)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add mode",
                                tint = Color(0xFF666A78),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prominent Pill "Done" Button (matching screenshot)
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSavePinnedModes(pinnedList)
                        onBack()
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD6D9E0),
                        contentColor = Color(0xFF111215)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(52.dp)
                        .testTag("rearrange_done_button")
                ) {
                    Text(
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111215)
                    )
                }
            }
        }
    }
}
