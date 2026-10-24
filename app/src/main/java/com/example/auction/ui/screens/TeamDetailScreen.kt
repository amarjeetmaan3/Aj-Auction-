package com.example.auction.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.auction.ui.components.ModernStatCard
import com.example.auction.ui.components.PlayerRoleBadge
import com.example.auction.ui.components.PlayerStatusBadge
import com.example.auction.ui.components.formatPoints
import com.example.auction.ui.components.parseColorSafe
import com.example.auction.ui.components.getContrastTextColor

@Composable
fun TeamDetailScreen(
    team: AuctionTeam,
    players: List<AuctionPlayer>,
    config: AuctionConfig,
    onRetain: (Int) -> String,
    onOpenAuction: () -> Unit
) {
    val squad = players.filter { it.teamId == team.id && (it.status == PlayerStatus.SOLD || it.status == PlayerStatus.RETAINED) }
    val retained = squad.count { it.status == PlayerStatus.RETAINED }
    val maxNextBid = AuctionRules.maxNextBid(team, config)
    val teamColor = parseColorSafe(team.colorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Team Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.5.dp, teamColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = teamColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = team.shortName.take(3).uppercase(),
                                color = getContrastTextColor(teamColor),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Squad Code: ${team.shortName} · Starting Budget: ${formatPoints(team.startingPoints)} pts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress Bar for squad slots
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ROSTER FILL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${team.playerCount} / ${config.playersPerTeam} slots filled", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFF1E2E3E), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((team.playerCount.toFloat() / config.playersPerTeam.coerceAtLeast(1)).coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(teamColor, RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        // Stats Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernStatCard("BUDGET LEFT", "${formatPoints(team.pointsRemaining)} pts", accentColor = Color(0xFF00E676), modifier = Modifier.weight(1f))
            ModernStatCard("POINTS SPENT", "${formatPoints(team.pointsSpent)} pts", accentColor = Color(0xFFFFD54F), modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernStatCard("ROSTER COUNT", "${team.playerCount}/${config.playersPerTeam}", accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            ModernStatCard("MAX NEXT BID", "${formatPoints(maxNextBid)} pts", accentColor = Color(0xFF40C4FF), modifier = Modifier.weight(1f))
        }

        // Action: Open Auction
        Button(
            onClick = onOpenAuction,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.Gavel, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Open Live Auction Console", color = Color.Black, fontWeight = FontWeight.Black)
        }

        // Retain Player Card
        if (config.maxRetentions > 0) {
            RetainPlayerCard(team, players, config, onRetain)
        }

        // Squad Listing
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("OFFICIAL SQUAD ROSTER (${squad.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${config.playersPerTeam - team.playerCount} slots open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (squad.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No players assigned yet", fontWeight = FontWeight.Bold)
                    Text("Acquire players in the live auction or retain existing ones.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            squad.forEach { player ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#${player.number}", fontWeight = FontWeight.Black, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(player.name, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PlayerRoleBadge(player.role)
                                Text("Round ${player.auctionRound ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            PlayerStatusBadge(player.status)
                            Spacer(Modifier.height(2.dp))
                            Text("${formatPoints(player.price)} pts", fontWeight = FontWeight.Black, color = Color(0xFFFFD54F), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetainPlayerCard(
    team: AuctionTeam,
    players: List<AuctionPlayer>,
    config: AuctionConfig,
    onRetain: (Int) -> String
) {
    var query by remember(team.id) { mutableStateOf("") }
    var message by remember(team.id) { mutableStateOf("") }
    val pending = players.filter { it.status == PlayerStatus.PENDING }
    val q = query.trim()
    val typedNumber = Regex("^\\d+").find(q)?.value?.toIntOrNull()
    val target = when {
        q.isBlank() -> null
        typedNumber != null -> pending.firstOrNull { it.number == typedNumber }
        else -> pending.firstOrNull { it.name.contains(q, ignoreCase = true) }
    }
    val matches = if (q.isBlank()) emptyList() else pending.filter {
        it.number.toString().startsWith(q) || it.name.contains(q, ignoreCase = true)
    }.take(4)
    val slotsLeft = (config.maxRetentions - team.retainedCount).coerceAtLeast(0)
    val blocked = slotsLeft <= 0 || team.pointsRemaining < config.retentionCost || team.playerCount >= config.playersPerTeam

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                Text("Retain Player (${slotsLeft} slots left)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Text("Cost: ${formatPoints(config.retentionCost)} points deducted from team budget.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(40); message = "" },
                label = { Text("Search player by name or jersey #") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            if (matches.isNotEmpty() && !(matches.size == 1 && q == matches[0].number.toString())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    matches.forEach { m ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { query = m.number.toString() }
                        ) {
                            Text(
                                text = "#${m.number} ${m.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (target != null) {
                        message = onRetain(target.number)
                        query = ""
                    }
                },
                enabled = !blocked && target != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
            ) {
                Text(
                    text = if (target != null) "Retain #${target.number} ${target.name}" else "Select Pending Player",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            if (message.isNotBlank()) {
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
