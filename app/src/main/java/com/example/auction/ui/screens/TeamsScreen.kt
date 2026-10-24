package com.example.auction.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auction.AuctionRules
import com.example.auction.data.AuctionConfig
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus
import com.example.auction.ui.components.formatPoints
import com.example.auction.ui.components.parseColorSafe
import com.example.auction.ui.components.getDefaultTeamColor
import com.example.auction.ui.components.getContrastTextColor
import com.example.auction.ui.components.DEFAULT_TEAM_PALETTE
import com.example.auction.ui.components.SetupProgressHeader

@Composable
fun TeamsScreen(
    teams: List<AuctionTeam>,
    players: List<AuctionPlayer>,
    config: AuctionConfig,
    onContinue: () -> Unit,
    onAdd: (AuctionTeam) -> Unit,
    onUpdate: (id: String, name: String, shortName: String, colorHex: String) -> Unit,
    onDelete: (id: String) -> Unit
) {
    val teamPalette = DEFAULT_TEAM_PALETTE
    var notice by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SetupProgressHeader(currentStep = 2)

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TEAM SETUP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure names and short codes for all ${config.teamCount} teams",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (teams.size == config.teamCount) Color(0xFF00E676).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (teams.size == config.teamCount) Color(0xFF00E676) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "${teams.size} / ${config.teamCount} READY",
                    color = if (teams.size == config.teamCount) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        repeat(config.teamCount) { slotIndex ->
            val team = teams.getOrNull(slotIndex)
            var name by remember(team?.id) { mutableStateOf(team?.name ?: "") }
            var shortName by remember(team?.id) { mutableStateOf(team?.shortName ?: "") }
            var selectedColorHex by remember(team?.id) {
                mutableStateOf(team?.colorHex ?: getDefaultTeamColor(slotIndex))
            }
            val assigned = team?.let { t ->
                players.count { it.teamId == t.id && (it.status == PlayerStatus.SOLD || it.status == PlayerStatus.RETAINED) }
            } ?: 0

            val cardColor = parseColorSafe(selectedColorHex)
            val contrastText = getContrastTextColor(cardColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.2.dp, cardColor.copy(alpha = if (team != null) 0.6f else 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = cardColor,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${slotIndex + 1}", color = contrastText, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                            Text("Team ${slotIndex + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (team != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00E676).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                                    Text("Configured", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(40) },
                            label = { Text("Full Team Name") },
                            placeholder = { Text("e.g. Royal Strikers") },
                            modifier = Modifier.weight(1.8f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = shortName,
                            onValueChange = { shortName = it.take(5).uppercase() },
                            label = { Text("Short Code") },
                            placeholder = { Text("RS") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Color Palette Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Theme Color:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        teamPalette.take(8).forEach { hex ->
                            val c = parseColorSafe(hex)
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(c, CircleShape)
                                    .border(if (isSelected) 2.5.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }

                    if (team != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Budget: ${formatPoints(team.pointsRemaining)} pts", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFD54F))
                                Text("Roster: ${team.playerCount}/${config.playersPerTeam}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Retained: ${team.retainedCount}/${config.maxRetentions}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cleanName = name.trim()
                                val cleanShort = shortName.trim().uppercase()
                                val duplicateName = teams.any { it.id != team?.id && it.name.equals(cleanName, true) }
                                val duplicateShort = teams.any { it.id != team?.id && it.shortName.equals(cleanShort, true) }
                                if (cleanName.isBlank() || cleanShort.isBlank()) {
                                    notice = "Enter both team name and short name for Team ${slotIndex + 1}."
                                } else if (duplicateName || duplicateShort) {
                                    notice = "Team name and short code must be unique."
                                } else if (team == null) {
                                    onAdd(
                                        AuctionTeam(
                                            id = "team-${java.util.UUID.randomUUID()}",
                                            name = cleanName,
                                            shortName = cleanShort,
                                            colorHex = selectedColorHex,
                                            startingPoints = config.startingPoints
                                        )
                                    )
                                    notice = "Team ${slotIndex + 1} saved."
                                } else {
                                    onUpdate(team.id, cleanName, cleanShort, selectedColorHex)
                                    notice = "Team ${slotIndex + 1} updated."
                                }
                            },
                            enabled = name.isNotBlank() && shortName.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                if (team == null) "Save Team ${slotIndex + 1}" else "Update Team ${slotIndex + 1}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (team != null) {
                            OutlinedButton(
                                onClick = {
                                    if (assigned == 0 && team.pointsSpent == 0.0) {
                                        onDelete(team.id)
                                        notice = "Team ${slotIndex + 1} deleted."
                                    } else {
                                        notice = "Cannot delete: this team already has auction bids or retained players."
                                    }
                                },
                                enabled = assigned == 0 && team.pointsSpent == 0.0,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        if (notice.isNotBlank()) {
            Text(notice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        // Continue Button
        Button(
            onClick = onContinue,
            enabled = teams.size == config.teamCount && teams.all { it.name.isNotBlank() && it.shortName.isNotBlank() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(6.dp))
            Text(
                if (teams.size == config.teamCount) "Save Teams & Continue to Players ➔" else "Please configure all ${config.teamCount} teams",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}
