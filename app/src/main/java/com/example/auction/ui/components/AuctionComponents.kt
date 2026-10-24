package com.example.auction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auction.data.PlayerStatus

val DEFAULT_TEAM_PALETTE = listOf(
    "#1E88E5", // Electric Blue
    "#E53935", // Crimson Red
    "#43A047", // Vibrant Green
    "#FB8C00", // Bright Amber-Orange
    "#8E24AA", // Vivid Purple
    "#00ACC1", // Bright Teal
    "#FDD835", // Radiant Yellow
    "#D81B60", // Deep Magenta
    "#3949AB", // Indigo
    "#00897B", // Emerald Teal
    "#F4511E", // Flame Orange
    "#6D4C41", // Rich Copper
    "#546E7A", // Slate Steel
    "#C0CA33", // Electric Lime
    "#7CB342", // Fresh Olive
    "#0288D1"  // Sky Sapphire
)

fun getDefaultTeamColor(index: Int): String {
    return DEFAULT_TEAM_PALETTE[index % DEFAULT_TEAM_PALETTE.size]
}

/**
 * Calculates accessible high-contrast text color (Dark Slate vs Pure White)
 * based on relative luminance of the background color.
 */
fun getContrastTextColor(backgroundColor: Color): Color {
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return if (luminance > 0.55f) Color(0xFF101820) else Color.White
}

fun parseColorSafe(hex: String, fallback: Color = Color(0xFF1976D2)): Color =
    runCatching {
        val clean = hex.trim()
        val formatted = if (clean.startsWith("#")) clean else "#$clean"
        Color(android.graphics.Color.parseColor(formatted))
    }.getOrDefault(fallback)

fun formatPoints(value: Double): String =
    if (value.isFinite() && value % 1.0 == 0.0) value.toLong().toString()
    else if (value.isFinite()) "%.1f".format(value) else "0"

@Composable
fun PlayerRoleBadge(role: String?, modifier: Modifier = Modifier) {
    if (role.isNullOrBlank()) return
    val r = role.trim().lowercase()
    val (bg, fg) = when {
        r.contains("bat") -> Color(0xFF1E3A8A) to Color(0xFF93C5FD)
        r.contains("bowl") -> Color(0xFF065F46) to Color(0xFF6EE7B7)
        r.contains("all") || r.contains("ar") -> Color(0xFF581C87) to Color(0xFFD8B4FE)
        r.contains("keep") || r.contains("wk") -> Color(0xFF831843) to Color(0xFFF9A8D4)
        else -> Color(0xFF374151) to Color(0xFFE5E7EB)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = role.uppercase(),
            color = fg,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

@Composable
fun PlayerStatusBadge(status: PlayerStatus, modifier: Modifier = Modifier) {
    val (color, icon, label) = when (status) {
        PlayerStatus.SOLD -> Triple(Color(0xFF00E676), Icons.Rounded.CheckCircle, "SOLD")
        PlayerStatus.UNSOLD -> Triple(Color(0xFFFF5252), Icons.Rounded.HighlightOff, "UNSOLD")
        PlayerStatus.RETAINED -> Triple(Color(0xFFFFD54F), Icons.Rounded.Lock, "RETAINED")
        PlayerStatus.PENDING -> Triple(Color(0xFF8193A5), Icons.Rounded.HourglassEmpty, "PENDING")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = label,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun GradeChip(grade: String?, modifier: Modifier = Modifier) {
    if (grade.isNullOrBlank()) return
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFD54F).copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = "GRADE $grade",
            color = Color(0xFFFFE082),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ModernStatCard(
    label: String,
    value: String,
    subValue: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TeamTag(
    teamName: String,
    shortName: String,
    colorHex: String,
    solidBackground: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tColor = parseColorSafe(colorHex)
    val textColor = if (solidBackground) getContrastTextColor(tColor) else Color.White
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (solidBackground) tColor else tColor.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (solidBackground) Color.Transparent else tColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!solidBackground) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tColor, CircleShape)
                )
            }
            Text(
                text = shortName.ifBlank { teamName },
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SetupProgressHeader(
    currentStep: Int, // 1 for Rules, 2 for Teams, 3 for Players
    modifier: Modifier = Modifier
) {
    val steps = listOf("1. Rules & Budget", "2. Team Rosters", "3. Player Pool")
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LEAGUE SETUP WIZARD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "STEP $currentStep OF 3",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                steps.forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isDone = stepNum < currentStep
                    val isCurrent = stepNum == currentStep
                    val stepColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isDone -> Color(0xFF00E676)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = stepColor.copy(alpha = if (isCurrent) 0.25f else 0.1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) stepColor else stepColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isDone) "✓ $title" else title,
                            color = if (isCurrent) MaterialTheme.colorScheme.onSurface else stepColor,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
