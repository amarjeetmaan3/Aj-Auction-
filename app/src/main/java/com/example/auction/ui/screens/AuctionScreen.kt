package com.example.auction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auction.AuctionRules
import com.example.auction.data.AuctionConfig
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus
import com.example.auction.ui.components.GradeChip
import com.example.auction.ui.components.ModernStatCard
import com.example.auction.ui.components.PlayerRoleBadge
import com.example.auction.ui.components.TeamTag
import com.example.auction.ui.components.formatPoints
import com.example.auction.ui.components.parseColorSafe
import com.example.auction.ui.components.getContrastTextColor

@Composable
fun AuctionScreen(
    players: SnapshotStateList<AuctionPlayer>,
    teams: SnapshotStateList<AuctionTeam>,
    config: AuctionConfig,
    canUndo: Boolean,
    roundNumber: Int,
    saveSnapshot: () -> Unit,
    undoLastAction: () -> Unit,
    onBid: () -> Int,
    onNextRound: () -> Unit
) {
    var selectedNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedTeamId by rememberSaveable { mutableStateOf<String?>(null) }
    val defaultBid = if (config.basePrice > 0.0) formatPoints(config.basePrice) else "1"
    var bidText by rememberSaveable(config.basePrice, selectedNumber) { mutableStateOf(defaultBid) }
    var notice by remember { mutableStateOf("") }
    var showAllTeams by rememberSaveable { mutableStateOf(false) }
    var showManualPlayers by rememberSaveable { mutableStateOf(false) }
    var showRoundDialog by rememberSaveable { mutableStateOf(false) }

    val pending = players.filter { it.status == PlayerStatus.PENDING }
    val selectedPlayer = pending.firstOrNull { it.number == selectedNumber }
    val currentBid = bidText.toDoubleOrNull()
    val selectedTeam = teams.firstOrNull { it.id == selectedTeamId }
    val soldCount = players.count { it.status == PlayerStatus.SOLD }
    val unsoldCount = players.count { it.status == PlayerStatus.UNSOLD }
    val retainedCount = players.count { it.status == PlayerStatus.RETAINED }
    val poolTotal = players.size - retainedCount

    val canSellSelected = selectedPlayer != null && selectedTeam != null && currentBid != null &&
        AuctionRules.canSell(selectedPlayer, selectedTeam, config, currentBid)

    fun resetSelection() {
        selectedNumber = null
        selectedTeamId = null
        bidText = defaultBid
    }

    fun pickRandom() {
        if (pending.isEmpty() && unsoldCount > 0) {
            onNextRound()
            notice = "Next round started automatically."
        } else notice = ""
        val picked = players.filter { it.status == PlayerStatus.PENDING }.randomOrNull()
        if (picked != null) {
            selectedNumber = picked.number
            selectedTeamId = null
            bidText = defaultBid
        } else {
            selectedNumber = null
            notice = "No pending players available."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModernStatCard("ROUND", "$roundNumber", accentColor = Color(0xFFFFD54F), modifier = Modifier.weight(1f))
            ModernStatCard("POOL", "${pending.size}/$poolTotal", accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1.2f))
            ModernStatCard("SOLD", "$soldCount", accentColor = Color(0xFF00E676), modifier = Modifier.weight(1f))
            ModernStatCard("UNSOLD", "$unsoldCount", accentColor = Color(0xFFFF5252), modifier = Modifier.weight(1f))
            if (config.maxRetentions > 0) {
                ModernStatCard("RETAIN", "$retainedCount", accentColor = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
            }
        }

        // Active Player Spotlight Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(
                1.5.dp,
                if (selectedPlayer != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Gavel, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                            Text("ON STAGE · ROUND $roundNumber", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showManualPlayers = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Rounded.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("List", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { pickRandom() },
                            enabled = pending.isNotEmpty() || unsoldCount > 0,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Casino, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Spin", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }

                // Player Details Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = pending.isNotEmpty()) { showManualPlayers = true }
                        .padding(14.dp)
                ) {
                    if (selectedPlayer != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${selectedPlayer.number}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = selectedPlayer.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PlayerRoleBadge(selectedPlayer.role)
                                    if (config.enablePlayerGrades) {
                                        GradeChip(selectedPlayer.grade)
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("BASE PRICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${formatPoints(config.basePrice)} pts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                            Text(
                                text = if (pending.isEmpty()) "No Pending Players Available" else "Tap here or 'Spin' to call player",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (pending.isNotEmpty()) {
                                Text("${pending.size} players in waiting queue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Live Bidding Price Console
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BID AMOUNT (POINTS)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    selectedTeam?.let { t ->
                        Text("Selected: ${t.name}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = parseColorSafe(t.colorHex))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val floor = if (config.basePrice > 0.0) config.basePrice else 1.0

                    // -1 Button
                    OutlinedButton(
                        onClick = { bidText = formatPoints(((bidText.toDoubleOrNull() ?: floor) - 1.0).coerceAtLeast(floor)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("-1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    // Bid Display Field
                    OutlinedTextField(
                        value = bidText,
                        onValueChange = { raw -> bidText = raw.filter { it.isDigit() || it == '.' }.take(6) },
                        modifier = Modifier.width(100.dp).height(52.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFFFD54F)
                        )
                    )

                    // +1 Button
                    Button(
                        onClick = { bidText = formatPoints((bidText.toDoubleOrNull() ?: floor) + 1.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("+1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    // +2 Button
                    Button(
                        onClick = { bidText = formatPoints((bidText.toDoubleOrNull() ?: floor) + 2.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("+2", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    // +5 Button
                    Button(
                        onClick = { bidText = formatPoints((bidText.toDoubleOrNull() ?: floor) + 5.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("+5", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }

        // Teams Grid: Select bidding team
        Text(
            text = "SELECT BIDDING TEAM (${teams.size} teams)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            teams.chunked(4).forEach { teamRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    teamRow.forEach { t ->
                        val selected = t.id == selectedTeamId
                        val full = t.playerCount >= config.playersPerTeam
                        val maxBid = AuctionRules.maxNextBid(t, config)
                        val tColor = parseColorSafe(t.colorHex)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (full) 0.45f else 1f)
                                .clickable(enabled = !full) {
                                    selectedTeamId = if (selected) null else t.id
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) tColor.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                if (selected) 2.dp else 1.dp,
                                if (selected) Color(0xFFFFD54F) else tColor.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = tColor,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = t.shortName.ifBlank { t.name }.take(4).uppercase(),
                                        color = getContrastTextColor(tColor),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${t.playerCount}/${config.playersPerTeam}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${formatPoints(t.pointsRemaining.coerceAtLeast(0.0))} pts",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (t.pointsRemaining > 0) Color(0xFF00E676) else Color(0xFFFF5252),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Max ${formatPoints(maxBid)}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    repeat((4 - teamRow.size).coerceAtLeast(0)) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        // Action Buttons: SOLD and UNSOLD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val p = selectedPlayer
                    val t = selectedTeam
                    val amount = currentBid
                    if (p != null && t != null && amount != null && AuctionRules.canSell(p, t, config, amount)) {
                        val pi = players.indexOfFirst { it.number == p.number }
                        val ti = teams.indexOfFirst { it.id == t.id }
                        if (pi >= 0 && ti >= 0) {
                            saveSnapshot()
                            val turn = onBid()
                            players[pi] = p.copy(
                                status = PlayerStatus.SOLD,
                                teamId = t.id,
                                price = amount,
                                auctionTurn = turn,
                                auctionRound = roundNumber
                            )
                            teams[ti] = t.copy(pointsSpent = t.pointsSpent + amount, playerCount = t.playerCount + 1)
                            notice = "✓ ${p.name} SOLD to ${t.shortName} for ${formatPoints(amount)} points!"
                            resetSelection()
                            if (players.none { it.status == PlayerStatus.PENDING }) showRoundDialog = true
                        }
                    }
                },
                enabled = canSellSelected,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF1E3A2B),
                    disabledContentColor = Color(0xFF4A755D)
                )
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("SOLD", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    val p = selectedPlayer
                    val pi = players.indexOfFirst { it.number == p?.number }
                    if (p != null && pi >= 0) {
                        saveSnapshot()
                        val turn = onBid()
                        players[pi] = p.copy(
                            status = PlayerStatus.UNSOLD,
                            teamId = null,
                            price = 0.0,
                            auctionTurn = turn,
                            auctionRound = roundNumber
                        )
                        notice = "⚠ ${p.name} marked UNSOLD."
                        resetSelection()
                        if (players.none { it.status == PlayerStatus.PENDING }) showRoundDialog = true
                    }
                },
                enabled = selectedPlayer != null,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF3E1E1E),
                    disabledContentColor = Color(0xFF7E4A4A)
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("UNSOLD", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }

        // Secondary controls: Undo & View All Teams
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    undoLastAction()
                    resetSelection()
                    showRoundDialog = false
                    notice = "Last action undone."
                },
                enabled = canUndo,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Undo Last", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showAllTeams = true },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Team Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (notice.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Recent Event Ticker
        val historyItems = players
            .filter { (it.status == PlayerStatus.SOLD || it.status == PlayerStatus.UNSOLD) && it.auctionTurn != null }
            .sortedByDescending { it.auctionTurn }
            .take(15)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT AUCTION EVENTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("${historyItems.size} logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (historyItems.isEmpty()) {
            Text(
                text = "No players sold or unsold yet in this session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        historyItems.forEach { p ->
            val team = teams.firstOrNull { it.id == p.teamId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F1720),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${p.auctionTurn ?: 0}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${p.number} ${p.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Round ${p.auctionRound ?: 1} · " + (if (p.status == PlayerStatus.UNSOLD) "UNSOLD" else (team?.name ?: "—")),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (p.status == PlayerStatus.UNSOLD) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (p.status == PlayerStatus.SOLD) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${formatPoints(p.price)} pts",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "UNSOLD",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Round Completion Dialog
    if (showRoundDialog) {
        val unsoldNow = players.count { it.status == PlayerStatus.UNSOLD }
        if (unsoldNow > 0) {
            AlertDialog(
                onDismissRequest = { showRoundDialog = false },
                title = { Text("Round $roundNumber Complete", fontWeight = FontWeight.Bold) },
                text = { Text("$unsoldNow player(s) went unsold. Would you like to start Round ${roundNumber + 1} with them now?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showRoundDialog = false
                            onNextRound()
                            notice = "Round ${roundNumber + 1} started with unsold players."
                        }
                    ) { Text("Start Round ${roundNumber + 1}") }
                },
                dismissButton = {
                    TextButton(onClick = { showRoundDialog = false }) { Text("Later") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showRoundDialog = false },
                title = { Text("Auction Complete!", fontWeight = FontWeight.Bold) },
                text = { Text("All available players in this pool have been auctioned.") },
                confirmButton = {
                    Button(onClick = { showRoundDialog = false }) { Text("Done") }
                }
            )
        }
    }

    // Manual Player Selection Modal
    if (showManualPlayers) {
        AlertDialog(
            onDismissRequest = { showManualPlayers = false },
            title = { Text("Select Player for Auction", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (pending.isEmpty()) {
                        Text("No pending players left in this round.")
                    }
                    pending.forEach { p ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNumber = p.number
                                    selectedTeamId = null
                                    bidText = defaultBid
                                    notice = ""
                                    showManualPlayers = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.size(30.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("#${p.number}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                    PlayerRoleBadge(p.role)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManualPlayers = false }) { Text("Close") }
            }
        )
    }

    // All Teams Overview Modal
    if (showAllTeams) {
        AlertDialog(
            onDismissRequest = { showAllTeams = false },
            title = { Text("All Teams Overview", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    teams.forEach { t ->
                        val tColor = parseColorSafe(t.colorHex)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, tColor.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(t.name, fontWeight = FontWeight.Black, color = tColor)
                                    Text("${t.playerCount}/${config.playersPerTeam} slots", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text("Remaining Budget: ${formatPoints(t.pointsRemaining.coerceAtLeast(0.0))} pts", fontSize = 12.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                                Text("Max Allowed Next Bid: ${formatPoints(AuctionRules.maxNextBid(t, config))} pts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllTeams = false }) { Text("Close") }
            }
        )
    }
}
