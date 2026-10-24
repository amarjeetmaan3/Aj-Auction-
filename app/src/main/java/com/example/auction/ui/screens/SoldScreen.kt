package com.example.auction.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus
import com.example.auction.ui.components.ModernStatCard
import com.example.auction.ui.components.PlayerRoleBadge
import com.example.auction.ui.components.PlayerStatusBadge
import com.example.auction.ui.components.TeamTag
import com.example.auction.ui.components.formatPoints

@Composable
fun SoldScreen(
    players: List<AuctionPlayer>,
    teams: List<AuctionTeam>
) {
    var filter by remember { mutableStateOf("ALL") }

    val soldPlayers = players.filter { it.status == PlayerStatus.SOLD }
    val unsoldPlayers = players.filter { it.status == PlayerStatus.UNSOLD }
    val retainedPlayers = players.filter { it.status == PlayerStatus.RETAINED }

    val visiblePlayers = when (filter) {
        "Sold" -> soldPlayers
        "Unsold" -> unsoldPlayers
        "Retained" -> retainedPlayers
        else -> players.filter { it.status in listOf(PlayerStatus.SOLD, PlayerStatus.UNSOLD, PlayerStatus.RETAINED) }
    }

    val totalSpent = soldPlayers.sumOf { it.price } + retainedPlayers.sumOf { it.price }
    val avgPrice = if (soldPlayers.isNotEmpty()) soldPlayers.map { it.price }.average() else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(24.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("AUCTION RESULTS OVERVIEW", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Total Points Spent: ${formatPoints(totalSpent)} pts · Avg Bid: ${formatPoints(avgPrice)} pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to players.count { it.status in listOf(PlayerStatus.SOLD, PlayerStatus.UNSOLD, PlayerStatus.RETAINED) },
                "Sold" to soldPlayers.size,
                "Unsold" to unsoldPlayers.size,
                "Retained" to retainedPlayers.size
            ).forEach { (option, count) ->
                val active = filter == option
                val btnColor = when (option) {
                    "Sold" -> Color(0xFF00E676)
                    "Unsold" -> Color(0xFFFF5252)
                    "Retained" -> Color(0xFFFFD54F)
                    else -> MaterialTheme.colorScheme.primary
                }

                Button(
                    onClick = { filter = option },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) btnColor else btnColor.copy(alpha = 0.12f),
                        contentColor = if (active) Color.Black else btnColor
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "$option ($count)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Players Result List
        if (visiblePlayers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No ${filter.lowercase()} players found", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            visiblePlayers
                .sortedWith(compareBy<AuctionPlayer> { it.auctionTurn ?: Int.MAX_VALUE }.thenBy { it.number })
                .forEach { player ->
                    val team = teams.firstOrNull { it.id == player.teamId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${player.number}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (team != null) {
                                        TeamTag(team.name, team.shortName, team.colorHex)
                                    } else if (player.status == PlayerStatus.UNSOLD) {
                                        Text("UNSOLD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                    }
                                    PlayerRoleBadge(player.role)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                PlayerStatusBadge(player.status)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (player.status == PlayerStatus.UNSOLD) "—" else "${formatPoints(player.price)} pts",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
        }
    }
}
