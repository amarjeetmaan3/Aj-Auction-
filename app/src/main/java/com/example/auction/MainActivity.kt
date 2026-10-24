package com.example.auction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsCricket
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auction.data.AuctionConfig
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionSnapshot
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus
import com.example.auction.ui.screens.AppToolsScreen
import com.example.auction.ui.screens.AuctionScreen
import com.example.auction.ui.screens.LeagueSelectScreen
import com.example.auction.ui.screens.PlayersScreen
import com.example.auction.ui.screens.SettingsScreen
import com.example.auction.ui.screens.SoldScreen
import com.example.auction.ui.screens.TeamDetailScreen
import com.example.auction.ui.screens.TeamStatusScreen
import com.example.auction.ui.screens.TeamsScreen
import com.example.auction.ui.theme.AuctionTheme
import org.json.JSONArray
import org.json.JSONObject

enum class AppTab(val title: String, val icon: ImageVector) {
    AUCTION("Auction", Icons.Rounded.Gavel),
    TEAMS("Teams", Icons.Rounded.Shield),
    PLAYERS("Players", Icons.Rounded.SportsCricket),
    SOLD("Results", Icons.Rounded.EmojiEvents),
    HOME("Status", Icons.Rounded.BarChart),
    SETTINGS("Rules", Icons.Rounded.Tune),
    APP("App Info", Icons.Rounded.Info),
    TEAM_DETAIL("Team", Icons.Rounded.Shield)
}

data class LeagueInfo(val id: String, val name: String)

object LeagueDirectory {
    private const val PREFS = "aj_auction_league_directory"
    private const val KEY = "leagues"

    fun load(context: android.content.Context): List<LeagueInfo> {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null)
        if (raw != null) return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val item = array.optJSONObject(i) ?: return@mapNotNull null
                val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val name = item.optString("name").takeIf { it.isNotBlank() } ?: "Untitled league"
                LeagueInfo(id, name)
            }
        }.getOrDefault(emptyList())
        val initial = listOf(LeagueInfo("legacy-default", "My League"))
        save(context, initial)
        return initial
    }

    fun save(context: android.content.Context, leagues: List<LeagueInfo>) {
        val array = JSONArray()
        leagues.forEach { league -> array.put(JSONObject().put("id", league.id).put("name", league.name)) }
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit().putString(KEY, array.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuctionTheme(darkTheme = true) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuctionApp()
                }
            }
        }
    }
}

@Composable
fun AuctionApp() {
    val context = LocalContext.current
    var leagues by remember { mutableStateOf(LeagueDirectory.load(context)) }
    var activeLeagueId by rememberSaveable { mutableStateOf<String?>(null) }
    var openLeagueStartsAtAuction by rememberSaveable { mutableStateOf(false) }

    val activeLeague = leagues.firstOrNull { it.id == activeLeagueId }

    if (activeLeague != null) {
        key(activeLeague.id) {
            LeagueWorkspace(
                leagueId = activeLeague.id,
                leagueName = activeLeague.name,
                initialTab = if (openLeagueStartsAtAuction) AppTab.AUCTION else AppTab.SETTINGS,
                onBack = {
                    leagues = LeagueDirectory.load(context)
                    activeLeagueId = null
                }
            )
        }
    } else {
        LeagueSelectScreen(
            onSelectLeague = { leagueId, startAtAuction ->
                openLeagueStartsAtAuction = startAtAuction
                activeLeagueId = leagueId
            }
        )
    }
}

@Composable
fun LeagueWorkspace(
    leagueId: String,
    leagueName: String,
    initialTab: AppTab,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val restored = remember(leagueId) { AuctionLocalStore.load(context, leagueId) }
    var selectedTab by rememberSaveable(leagueId, initialTab) { mutableStateOf(initialTab) }
    var selectedTeamId by rememberSaveable { mutableStateOf<String?>(null) }
    var appMenuExpanded by remember { mutableStateOf(false) }
    var config by remember { mutableStateOf(restored.config) }

    val players = remember { mutableStateListOf<AuctionPlayer>().apply { addAll(restored.players) } }
    val teams = remember { mutableStateListOf<AuctionTeam>().apply { addAll(restored.teams) } }
    val history = remember(leagueId) {
        mutableStateListOf<AuctionSnapshot>().apply { addAll(AuctionLocalStore.loadHistory(context, leagueId)) }
    }
    var bidCounter by remember { mutableStateOf(restored.bidCounter) }
    var retentionCounter by remember { mutableStateOf(restored.retentionCounter) }
    var roundNumber by remember { mutableStateOf(restored.roundNumber) }
    var backupNotice by remember { mutableStateOf("") }
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val backup = pendingBackupJson
        if (uri != null && backup != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(backup) }
                    ?: error("Could not write backup file")
            }.onSuccess { backupNotice = "Backup file saved successfully." }
                .onFailure { backupNotice = "Could not save backup file." }
        } else if (uri == null) {
            backupNotice = "Backup export cancelled."
        }
        pendingBackupJson = null
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read backup file")
            }.onSuccess { raw ->
                val imported = AuctionLocalStore.parseBackup(raw)
                if (imported == null) {
                    backupNotice = "Backup file is invalid or unsupported."
                } else {
                    val duplicatePlayerNumbers = imported.players.groupingBy { it.number }.eachCount().filterValues { it > 1 }.keys
                    val duplicateTeamIds = imported.teams.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
                    val teamIds = imported.teams.map { it.id }.toSet()
                    val invalidPlayers = imported.players.any {
                        it.number <= 0 || it.name.isBlank() || !it.price.isFinite() || it.price < 0 ||
                            (it.teamId != null && it.teamId !in teamIds) ||
                            ((it.status == PlayerStatus.SOLD || it.status == PlayerStatus.RETAINED) && it.teamId == null)
                    }
                    val invalidTeams = imported.teams.any {
                        it.id.isBlank() || it.name.isBlank() || it.shortName.isBlank() ||
                            !it.startingPoints.isFinite() || it.startingPoints < 0 ||
                            !it.pointsSpent.isFinite() || it.pointsSpent < 0 ||
                            it.playerCount < 0 || it.retainedCount < 0
                    }
                    val invalidConfig = imported.config.playersPerTeam <= 0 || imported.config.teamCount <= 0 ||
                        !imported.config.basePrice.isFinite() || imported.config.basePrice < 0 ||
                        !imported.config.startingPoints.isFinite() || imported.config.startingPoints < 0 ||
                        imported.config.maxRetentions < 0 || !imported.config.retentionCost.isFinite() ||
                        imported.config.retentionCost < 0

                    if (duplicatePlayerNumbers.isNotEmpty()) {
                        backupNotice = "Backup rejected: duplicate player numbers (${duplicatePlayerNumbers.sorted().joinToString()})."
                    } else if (duplicateTeamIds.isNotEmpty()) {
                        backupNotice = "Backup rejected: duplicate team IDs."
                    } else if (invalidPlayers || invalidTeams || invalidConfig) {
                        backupNotice = "Backup rejected: invalid player, team, or auction settings."
                    } else {
                        history.add(AuctionSnapshot(players.toList(), teams.toList(), bidCounter, retentionCounter, roundNumber))
                        if (history.size > 50) history.removeAt(0)
                        AuctionLocalStore.saveHistory(context, leagueId, history.toList())
                        players.clear(); players.addAll(imported.players)
                        teams.clear(); teams.addAll(imported.teams)
                        config = imported.config
                        bidCounter = imported.bidCounter
                        retentionCounter = imported.retentionCounter
                        roundNumber = imported.roundNumber
                        backupNotice = "Backup restored successfully. Prior state saved in Undo."
                    }
                }
            }.onFailure { backupNotice = "Could not restore backup file: ${it.message}" }
        }
    }

    LaunchedEffect(players.toList(), teams.toList(), config, bidCounter, retentionCounter, roundNumber) {
        AuctionLocalStore.save(context, leagueId, players.toList(), teams.toList(), config, bidCounter, retentionCounter, roundNumber)
    }

    fun saveSnapshot() {
        history.add(AuctionSnapshot(players.toList(), teams.toList(), bidCounter, retentionCounter, roundNumber))
        if (history.size > 50) history.removeAt(0)
        AuctionLocalStore.saveHistory(context, leagueId, history.toList())
    }

    fun undoLastAction() {
        val snapshot = history.removeLastOrNull() ?: return
        AuctionLocalStore.saveHistory(context, leagueId, history.toList())
        players.clear()
        players.addAll(snapshot.players)
        teams.clear()
        teams.addAll(snapshot.teams)
        bidCounter = snapshot.bidCounter
        retentionCounter = snapshot.retentionCounter
        roundNumber = snapshot.roundNumber
    }

    fun startNextRound() {
        if (players.none { it.status == PlayerStatus.UNSOLD }) return
        saveSnapshot()
        players.indices.forEach { index ->
            val p = players[index]
            if (p.status == PlayerStatus.UNSOLD) {
                players[index] = p.copy(status = PlayerStatus.PENDING, auctionTurn = null)
            }
        }
        roundNumber += 1
    }

    fun retainPlayer(teamId: String, playerNumber: Int): String {
        val ti = teams.indexOfFirst { it.id == teamId }
        val pi = players.indexOfFirst { it.number == playerNumber }
        if (ti < 0 || pi < 0) return "Player not found."
        val team = teams[ti]
        val p = players[pi]
        if (p.status != PlayerStatus.PENDING) return "Player is not available in pending pool."
        if (team.retainedCount >= config.maxRetentions) return "No retain slots left for ${team.name}."
        if (team.pointsRemaining < config.retentionCost) return "${team.name} cannot afford retention cost."
        if (team.playerCount >= config.playersPerTeam) return "${team.name}'s squad roster is full."
        if (!AuctionRules.canRetain(p, team, config)) return "Retain action is not allowed under current rules."

        saveSnapshot()
        retentionCounter += 1
        players[pi] = p.copy(
            status = PlayerStatus.RETAINED,
            teamId = team.id,
            price = config.retentionCost,
            auctionTurn = retentionCounter,
            auctionRound = null
        )
        teams[ti] = team.copy(
            pointsSpent = team.pointsSpent + config.retentionCost,
            playerCount = team.playerCount + 1,
            retainedCount = team.retainedCount + 1
        )
        return "✓ ${p.name} retained by ${team.name}."
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = Color(0xFF0A1017),
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Leagues", tint = Color.White)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E676),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Text(
                                text = "AJ AUCTION PRO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Text(
                            text = leagueName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD54F),
                            maxLines = 1,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { appMenuExpanded = true }) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = appMenuExpanded,
                            onDismissRequest = { appMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("App Information") },
                                onClick = {
                                    appMenuExpanded = false
                                    selectedTab = AppTab.APP
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("League Settings") },
                                onClick = {
                                    appMenuExpanded = false
                                    selectedTab = AppTab.SETTINGS
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D1620),
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val primaryTabs = listOf(
                    AppTab.AUCTION,
                    AppTab.TEAMS,
                    AppTab.PLAYERS,
                    AppTab.SOLD,
                    AppTab.HOME,
                    AppTab.SETTINGS
                )

                primaryTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                maxLines = 1,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF00E676),
                            indicatorColor = Color(0xFF00E676),
                            unselectedIconColor = Color(0xFF7E92A2),
                            unselectedTextColor = Color(0xFF7E92A2)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 12.dp)
        ) {
            Crossfade(targetState = selectedTab, label = "ScreenTransition") { currentTab ->
                when (currentTab) {
                    AppTab.AUCTION -> AuctionScreen(
                        players = players,
                        teams = teams,
                        config = config,
                        canUndo = history.isNotEmpty(),
                        roundNumber = roundNumber,
                        saveSnapshot = ::saveSnapshot,
                        undoLastAction = ::undoLastAction,
                        onBid = { bidCounter += 1; bidCounter },
                        onNextRound = ::startNextRound
                    )

                    AppTab.TEAMS -> TeamsScreen(
                        teams = teams,
                        players = players,
                        config = config,
                        onContinue = {
                            selectedTab = AppTab.PLAYERS
                        },
                        onAdd = { newTeam: AuctionTeam ->
                            if (AuctionRules.canAddTeam(teams.size, config)) {
                                saveSnapshot()
                                teams.add(newTeam)
                            }
                        },
                        onUpdate = { id: String, name: String, shortName: String, colorHex: String ->
                            val index = teams.indexOfFirst { it.id == id }
                            if (index >= 0) {
                                saveSnapshot()
                                teams[index] = teams[index].copy(name = name, shortName = shortName, colorHex = colorHex)
                            }
                        },
                        onDelete = { id: String ->
                            val hasActivity = players.any { it.teamId == id && it.status in listOf(PlayerStatus.SOLD, PlayerStatus.RETAINED) }
                            if (!hasActivity) {
                                saveSnapshot()
                                teams.removeAll { it.id == id }
                                if (selectedTeamId == id) selectedTeamId = null
                            }
                        }
                    )

                    AppTab.PLAYERS -> PlayersScreen(
                        players = players,
                        gradesEnabled = config.enablePlayerGrades,
                        onAdd = { name, number, grade, role, phone, photoUrl ->
                            if (players.none { it.number == number }) {
                                saveSnapshot()
                                players.add(
                                    AuctionPlayer(
                                        number = number,
                                        name = name.trim(),
                                        grade = grade.trim().ifBlank { null },
                                        role = role.trim().ifBlank { null },
                                        phone = phone.trim().ifBlank { null },
                                        photoUrl = photoUrl.trim().ifBlank { null }
                                    )
                                )
                            }
                        },
                        onUpdate = { oldNumber, name, number, grade, role, phone, photoUrl ->
                            val index = players.indexOfFirst { it.number == oldNumber }
                            if (index >= 0 && players.none { it.number == number && it.number != oldNumber }) {
                                saveSnapshot()
                                players[index] = players[index].copy(
                                    number = number,
                                    name = name.trim(),
                                    grade = grade.trim().ifBlank { null },
                                    role = role.trim().ifBlank { null },
                                    phone = phone.trim().ifBlank { null },
                                    photoUrl = photoUrl.trim().ifBlank { null }
                                )
                            }
                        },
                        onDelete = { number ->
                            val index = players.indexOfFirst { it.number == number }
                            if (index >= 0) {
                                saveSnapshot()
                                val player = players[index]
                                val teamIndex = teams.indexOfFirst { it.id == player.teamId }
                                if (teamIndex >= 0 && player.status in listOf(PlayerStatus.SOLD, PlayerStatus.RETAINED)) {
                                    val team = teams[teamIndex]
                                    val cost = if (player.status == PlayerStatus.RETAINED) config.retentionCost else player.price
                                    teams[teamIndex] = team.copy(
                                        pointsSpent = (team.pointsSpent - cost).coerceAtLeast(0.0),
                                        playerCount = (team.playerCount - 1).coerceAtLeast(0),
                                        retainedCount = if (player.status == PlayerStatus.RETAINED) (team.retainedCount - 1).coerceAtLeast(0) else team.retainedCount
                                    )
                                }
                                players.removeAt(index)
                            }
                        },
                        onContinue = { selectedTab = AppTab.AUCTION }
                    )

                    AppTab.SOLD -> SoldScreen(players = players, teams = teams)

                    AppTab.HOME -> TeamStatusScreen(
                        teams = teams,
                        players = players,
                        config = config,
                        onTeamClick = { team ->
                            selectedTeamId = team.id
                            selectedTab = AppTab.TEAM_DETAIL
                        }
                    )

                    AppTab.SETTINGS -> SettingsScreen(
                        config = config,
                        onSave = { updated ->
                            config = updated
                            teams.indices.forEach { index ->
                                val t = teams[index]
                                if (t.playerCount == 0 && t.pointsSpent == 0.0) {
                                    teams[index] = t.copy(startingPoints = updated.startingPoints)
                                }
                            }
                            selectedTab = AppTab.TEAMS
                        },
                        onExportBackup = {
                            val json = AuctionLocalStore.createBackup(
                                leagueName = leagueName,
                                players = players.toList(),
                                teams = teams.toList(),
                                config = config,
                                bidCounter = bidCounter,
                                retentionCounter = retentionCounter,
                                roundNumber = roundNumber
                            )
                            pendingBackupJson = json
                            exportLauncher.launch("aj-auction-${leagueName.replace(' ', '_').lowercase()}-backup.json")
                        },
                        onImportBackup = {
                            restoreLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        backupNotice = backupNotice
                    )

                    AppTab.TEAM_DETAIL -> {
                        val selectedTeam = teams.firstOrNull { it.id == selectedTeamId }
                        if (selectedTeam == null) {
                            Text("Please choose a team to view.")
                        } else {
                            TeamDetailScreen(
                                team = selectedTeam,
                                players = players,
                                config = config,
                                onRetain = { number -> retainPlayer(selectedTeam.id, number) },
                                onOpenAuction = { selectedTab = AppTab.AUCTION }
                            )
                        }
                    }

                    AppTab.APP -> AppToolsScreen()
                }
            }
        }
    }
}

data class StoredAuctionData(
    val players: List<AuctionPlayer>,
    val teams: List<AuctionTeam>,
    val config: AuctionConfig,
    val bidCounter: Int = 0,
    val retentionCounter: Int = 0,
    val roundNumber: Int = 1
)

object AuctionLocalStore {
    private const val PREFS = "aj_auction_local_data"
    private const val LEGACY_KEY_DATA = "auction_state"

    fun deleteLeagueData(context: android.content.Context, leagueId: String) {
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).edit()
            .remove("auction_state_" + leagueId)
            .remove("auction_history_" + leagueId)
            .apply()
    }

    fun save(
        context: android.content.Context,
        leagueId: String,
        players: List<AuctionPlayer>,
        teams: List<AuctionTeam>,
        config: AuctionConfig,
        bidCounter: Int,
        retentionCounter: Int,
        roundNumber: Int
    ) {
        val root = JSONObject()
        root.put("config", JSONObject().apply {
            put("playersPerTeam", config.playersPerTeam)
            put("basePrice", config.basePrice)
            put("startingPoints", config.startingPoints)
            put("maxRetentions", config.maxRetentions)
            put("retentionCost", config.retentionCost)
            put("teamCount", config.teamCount)
            put("enablePlayerGrades", config.enablePlayerGrades)
        })
        root.put("players", JSONArray().apply {
            players.forEach { p ->
                put(JSONObject().apply {
                    put("number", p.number)
                    put("name", p.name)
                    put("grade", p.grade ?: JSONObject.NULL)
                    put("role", p.role ?: JSONObject.NULL)
                    put("phone", p.phone ?: JSONObject.NULL)
                    put("photoUrl", p.photoUrl ?: JSONObject.NULL)
                    put("status", p.status.name)
                    put("teamId", p.teamId ?: JSONObject.NULL)
                    put("price", p.price)
                    put("auctionTurn", p.auctionTurn ?: JSONObject.NULL)
                    put("auctionRound", p.auctionRound ?: JSONObject.NULL)
                })
            }
        })
        root.put("bidCounter", bidCounter)
        root.put("retentionCounter", retentionCounter)
        root.put("roundNumber", roundNumber.coerceAtLeast(1))
        root.put("teams", JSONArray().apply {
            teams.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("shortName", t.shortName)
                    put("colorHex", t.colorHex)
                    put("startingPoints", t.startingPoints)
                    put("pointsSpent", t.pointsSpent)
                    put("playerCount", t.playerCount)
                    put("retainedCount", t.retainedCount)
                })
            }
        })
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit().putString("auction_state_" + leagueId, root.toString()).apply()
    }

    fun createBackup(
        leagueName: String,
        players: List<AuctionPlayer>,
        teams: List<AuctionTeam>,
        config: AuctionConfig,
        bidCounter: Int,
        retentionCounter: Int,
        roundNumber: Int
    ): String {
        val root = JSONObject()
        root.put("format", "aj-auction-backup-v1")
        root.put("leagueName", leagueName)
        root.put("config", JSONObject().apply {
            put("playersPerTeam", config.playersPerTeam)
            put("basePrice", config.basePrice)
            put("startingPoints", config.startingPoints)
            put("maxRetentions", config.maxRetentions)
            put("retentionCost", config.retentionCost)
            put("teamCount", config.teamCount)
            put("enablePlayerGrades", config.enablePlayerGrades)
        })
        root.put("players", JSONArray().apply {
            players.forEach { p ->
                put(JSONObject().apply {
                    put("number", p.number)
                    put("name", p.name)
                    put("grade", p.grade ?: JSONObject.NULL)
                    put("role", p.role ?: JSONObject.NULL)
                    put("phone", p.phone ?: JSONObject.NULL)
                    put("photoUrl", p.photoUrl ?: JSONObject.NULL)
                    put("status", p.status.name)
                    put("teamId", p.teamId ?: JSONObject.NULL)
                    put("price", p.price)
                    put("auctionTurn", p.auctionTurn ?: JSONObject.NULL)
                    put("auctionRound", p.auctionRound ?: JSONObject.NULL)
                })
            }
        })
        root.put("teams", JSONArray().apply {
            teams.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("shortName", t.shortName)
                    put("colorHex", t.colorHex)
                    put("startingPoints", t.startingPoints)
                    put("pointsSpent", t.pointsSpent)
                    put("playerCount", t.playerCount)
                    put("retainedCount", t.retainedCount)
                })
            }
        })
        root.put("bidCounter", bidCounter)
        root.put("retentionCounter", retentionCounter)
        root.put("roundNumber", roundNumber)
        return root.toString(2)
    }

    fun parseBackup(raw: String): StoredAuctionData? = runCatching {
        val root = JSONObject(raw)
        if (root.optString("format") != "aj-auction-backup-v1") return null
        val c = root.optJSONObject("config") ?: JSONObject()
        val config = AuctionConfig(
            c.optInt("playersPerTeam", 11),
            c.optDouble("basePrice", 2.0),
            c.optDouble("startingPoints", 100.0),
            c.optInt("maxRetentions", 2),
            c.optDouble("retentionCost", 10.0),
            c.optInt("teamCount", 4),
            c.optBoolean("enablePlayerGrades", false)
        )
        val players = mutableListOf<AuctionPlayer>()
        val pa = root.optJSONArray("players") ?: return null
        for (i in 0 until pa.length()) {
            val p = pa.optJSONObject(i) ?: continue
            players.add(
                AuctionPlayer(
                    p.optInt("number"),
                    p.optString("name"),
                    if (p.isNull("grade")) null else p.optString("grade"),
                    if (p.isNull("role")) null else p.optString("role"),
                    if (p.isNull("phone")) null else p.optString("phone"),
                    if (p.isNull("photoUrl")) null else p.optString("photoUrl"),
                    runCatching { PlayerStatus.valueOf(p.optString("status", "PENDING")) }.getOrDefault(PlayerStatus.PENDING),
                    if (p.isNull("teamId")) null else p.optString("teamId"),
                    p.optDouble("price", 0.0),
                    if (p.isNull("auctionTurn")) null else p.optInt("auctionTurn"),
                    if (p.isNull("auctionRound")) null else p.optInt("auctionRound")
                )
            )
        }
        val teams = mutableListOf<AuctionTeam>()
        val ta = root.optJSONArray("teams") ?: return null
        for (i in 0 until ta.length()) {
            val t = ta.optJSONObject(i) ?: continue
            teams.add(
                AuctionTeam(
                    t.optString("id"),
                    t.optString("name"),
                    t.optString("shortName"),
                    t.optString("colorHex", "#00E676"),
                    t.optDouble("startingPoints", config.startingPoints),
                    t.optDouble("pointsSpent", 0.0),
                    t.optInt("playerCount", 0),
                    t.optInt("retainedCount", 0)
                )
            )
        }
        StoredAuctionData(
            players,
            teams,
            config,
            root.optInt("bidCounter", 0).coerceAtLeast(0),
            root.optInt("retentionCounter", 0).coerceAtLeast(0),
            root.optInt("roundNumber", 1).coerceAtLeast(1)
        )
    }.getOrNull()

    fun saveHistory(context: android.content.Context, leagueId: String, history: List<AuctionSnapshot>) {
        val array = JSONArray()
        history.takeLast(50).forEach { snap ->
            val state = JSONObject()
            state.put("bidCounter", snap.bidCounter)
            state.put("retentionCounter", snap.retentionCounter)
            state.put("roundNumber", snap.roundNumber)
            state.put("players", JSONArray().apply {
                snap.players.forEach { p ->
                    put(JSONObject().apply {
                        put("number", p.number)
                        put("name", p.name)
                        put("grade", p.grade ?: JSONObject.NULL)
                        put("role", p.role ?: JSONObject.NULL)
                        put("phone", p.phone ?: JSONObject.NULL)
                        put("photoUrl", p.photoUrl ?: JSONObject.NULL)
                        put("status", p.status.name)
                        put("teamId", p.teamId ?: JSONObject.NULL)
                        put("price", p.price)
                        put("auctionTurn", p.auctionTurn ?: JSONObject.NULL)
                        put("auctionRound", p.auctionRound ?: JSONObject.NULL)
                    })
                }
            })
            state.put("teams", JSONArray().apply {
                snap.teams.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("name", t.name)
                        put("shortName", t.shortName)
                        put("colorHex", t.colorHex)
                        put("startingPoints", t.startingPoints)
                        put("pointsSpent", t.pointsSpent)
                        put("playerCount", t.playerCount)
                        put("retainedCount", t.retainedCount)
                    })
                }
            })
            array.put(state)
        }
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit().putString("auction_history_" + leagueId, array.toString()).apply()
    }

    fun loadHistory(context: android.content.Context, leagueId: String): List<AuctionSnapshot> {
        return runCatching {
            val raw = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
                .getString("auction_history_" + leagueId, null) ?: return emptyList()
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val state = array.optJSONObject(index) ?: return@mapNotNull null
                val ps = mutableListOf<AuctionPlayer>()
                val pa = state.optJSONArray("players") ?: JSONArray()
                for (i in 0 until pa.length()) {
                    val p = pa.optJSONObject(i) ?: continue
                    ps.add(
                        AuctionPlayer(
                            number = p.optInt("number"),
                            name = p.optString("name"),
                            grade = if (p.isNull("grade")) null else p.optString("grade"),
                            role = if (p.isNull("role")) null else p.optString("role"),
                            phone = if (p.isNull("phone")) null else p.optString("phone"),
                            photoUrl = if (p.isNull("photoUrl")) null else p.optString("photoUrl"),
                            status = runCatching { PlayerStatus.valueOf(p.optString("status", "PENDING")) }.getOrDefault(PlayerStatus.PENDING),
                            teamId = if (p.isNull("teamId")) null else p.optString("teamId"),
                            price = p.optDouble("price", 0.0),
                            auctionTurn = if (p.isNull("auctionTurn")) null else p.optInt("auctionTurn"),
                            auctionRound = if (p.isNull("auctionRound")) null else p.optInt("auctionRound")
                        )
                    )
                }
                val ts = mutableListOf<AuctionTeam>()
                val ta = state.optJSONArray("teams") ?: JSONArray()
                for (i in 0 until ta.length()) {
                    val t = ta.optJSONObject(i) ?: continue
                    ts.add(
                        AuctionTeam(
                            id = t.optString("id"),
                            name = t.optString("name"),
                            shortName = t.optString("shortName"),
                            colorHex = t.optString("colorHex", "#00E676"),
                            startingPoints = t.optDouble("startingPoints", 100.0),
                            pointsSpent = t.optDouble("pointsSpent", 0.0),
                            playerCount = t.optInt("playerCount", 0),
                            retainedCount = t.optInt("retainedCount", 0)
                        )
                    )
                }
                AuctionSnapshot(
                    ps,
                    ts,
                    state.optInt("bidCounter", 0),
                    state.optInt("retentionCounter", 0),
                    state.optInt("roundNumber", 1).coerceAtLeast(1)
                )
            }.takeLast(50)
        }.getOrDefault(emptyList())
    }

    fun load(context: android.content.Context, leagueId: String): StoredAuctionData {
        val fallback = StoredAuctionData(emptyList(), emptyList(), AuctionConfig())
        return try {
            val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            val raw = (prefs.getString("auction_state_" + leagueId, null)
                ?: if (leagueId == "legacy-default") prefs.getString(LEGACY_KEY_DATA, null) else null)
                ?: return fallback
            val root = JSONObject(raw)
            val c = root.optJSONObject("config") ?: JSONObject()
            val config = AuctionConfig(
                playersPerTeam = c.optInt("playersPerTeam", 11),
                basePrice = c.optDouble("basePrice", 2.0),
                startingPoints = c.optDouble("startingPoints", 100.0),
                maxRetentions = c.optInt("maxRetentions", 2),
                retentionCost = c.optDouble("retentionCost", 10.0),
                teamCount = c.optInt("teamCount", 4),
                enablePlayerGrades = c.optBoolean("enablePlayerGrades", false)
            )
            val players = mutableListOf<AuctionPlayer>()
            val playerArray = root.optJSONArray("players") ?: JSONArray()
            for (i in 0 until playerArray.length()) {
                val p = playerArray.optJSONObject(i) ?: continue
                players.add(
                    AuctionPlayer(
                        number = p.optInt("number"),
                        name = p.optString("name"),
                        grade = p.optNullableString("grade"),
                        role = p.optNullableString("role"),
                        phone = p.optNullableString("phone"),
                        photoUrl = p.optNullableString("photoUrl"),
                        status = runCatching { PlayerStatus.valueOf(p.optString("status", "PENDING")) }.getOrDefault(PlayerStatus.PENDING),
                        teamId = p.optNullableString("teamId"),
                        price = p.optDouble("price", 0.0),
                        auctionTurn = if (p.isNull("auctionTurn")) null else p.optInt("auctionTurn"),
                        auctionRound = if (p.isNull("auctionRound")) null else p.optInt("auctionRound")
                    )
                )
            }
            val teams = mutableListOf<AuctionTeam>()
            val teamArray = root.optJSONArray("teams") ?: JSONArray()
            for (i in 0 until teamArray.length()) {
                val t = teamArray.optJSONObject(i) ?: continue
                teams.add(
                    AuctionTeam(
                        id = t.optString("id"),
                        name = t.optString("name"),
                        shortName = t.optString("shortName"),
                        colorHex = t.optString("colorHex", "#00E676"),
                        startingPoints = t.optDouble("startingPoints", config.startingPoints),
                        pointsSpent = t.optDouble("pointsSpent", 0.0),
                        playerCount = t.optInt("playerCount", 0),
                        retainedCount = t.optInt("retainedCount", 0)
                    )
                )
            }
            StoredAuctionData(
                players,
                teams,
                config,
                root.optInt("bidCounter", 0).coerceAtLeast(0),
                root.optInt("retentionCounter", 0).coerceAtLeast(0),
                root.optInt("roundNumber", 1).coerceAtLeast(1)
            )
        } catch (_: Exception) { fallback }
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
