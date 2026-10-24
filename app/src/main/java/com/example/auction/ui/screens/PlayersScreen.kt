package com.example.auction.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewList
import com.example.auction.ui.components.SetupProgressHeader
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auction.data.AuctionPlayer
import com.example.auction.ui.components.GradeChip
import com.example.auction.ui.components.PlayerRoleBadge
import com.example.auction.ui.components.PlayerStatusBadge
import kotlinx.coroutines.launch

private fun parseCsvRow(line: String): List<String> {
    val cells = mutableListOf<String>()
    val cell = StringBuilder()
    var quoted = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        when {
            ch == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> {
                cell.append('"')
                i++
            }
            ch == '"' -> quoted = !quoted
            ch == ',' && !quoted -> {
                cells.add(cell.toString())
                cell.setLength(0)
            }
            else -> cell.append(ch)
        }
        i++
    }
    cells.add(cell.toString())
    return cells
}

@Composable
fun PlayersScreen(
    players: List<AuctionPlayer>,
    gradesEnabled: Boolean,
    onAdd: (name: String, number: Int, grade: String, role: String, phone: String, photoUrl: String) -> Unit,
    onUpdate: (oldNumber: Int, name: String, number: Int, grade: String, role: String, phone: String, photoUrl: String) -> Unit,
    onDelete: (number: Int) -> Unit,
    onContinue: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var editingNumber by remember { mutableStateOf<Int?>(null) }
    var gridMode by remember { mutableStateOf(true) }
    var showAddForm by remember { mutableStateOf(false) }
    var importNotice by remember { mutableStateOf("") }
    var formNotice by remember { mutableStateOf("") }
    var pendingDeleteNumber by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                var invalidRows = 0
                val rows = context.contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
                    lines.toList().mapIndexedNotNull { index, line ->
                        if (line.isBlank()) return@mapIndexedNotNull null
                        val cells = parseCsvRow(line).map { it.trim() }
                        val numberCell = cells.firstOrNull().orEmpty().removePrefix("\uFEFF")
                        val playerName = cells.getOrNull(1).orEmpty()
                        if (index == 0 && (numberCell.contains("number", true) || playerName.contains("name", true))) null
                        else {
                            val n = numberCell.toIntOrNull()
                            if (cells.size < 2 || n == null || n <= 0 || playerName.isBlank()) {
                                invalidRows++
                                null
                            } else Triple(n, playerName, listOf(cells.getOrNull(2).orEmpty(), cells.getOrNull(3).orEmpty(), cells.getOrNull(4).orEmpty(), cells.getOrNull(5).orEmpty()))
                        }
                    }
                } ?: error("Could not read selected file")
                rows to invalidRows
            }.onSuccess { (rows, invalidRows) ->
                var added = 0
                var duplicateInFile = 0
                var alreadyExists = 0
                val seenNumbers = mutableSetOf<Int>()
                rows.forEach { (n, playerName, extra) ->
                    if (!seenNumbers.add(n)) {
                        duplicateInFile++
                    } else if (players.any { it.number == n }) {
                        alreadyExists++
                    } else {
                        onAdd(playerName, n, extra[0], extra[1], extra[2], extra[3])
                        added++
                    }
                }
                val skipped = duplicateInFile + alreadyExists
                importNotice = "CSV import: $added added, $skipped skipped, $invalidRows invalid rows."
            }.onFailure { importNotice = "CSV import failed: " + (it.message ?: "error") }
        }
    }

    val filtered = players.filter {
        it.name.contains(search, ignoreCase = true) ||
            it.number.toString().contains(search) ||
            (it.grade ?: "").contains(search, ignoreCase = true) ||
            (it.role ?: "").contains(search, ignoreCase = true) ||
            (it.phone ?: "").contains(search, ignoreCase = true)
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val startEdit: (AuctionPlayer) -> Unit = { player ->
        editingNumber = player.number
        name = player.name
        number = player.number.toString()
        grade = player.grade.orEmpty()
        role = player.role.orEmpty()
        phone = player.phone.orEmpty()
        photoUrl = player.photoUrl.orEmpty()
        showAddForm = true
        scope.launch { listState.animateScrollToItem(0) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SetupProgressHeader(currentStep = 3)

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PLAYER DIRECTORY",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${players.size} players registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showAddForm = !showAddForm },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (showAddForm) Icons.Rounded.Clear else Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Black
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (showAddForm) "Close" else "Add Player", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Add / Edit Player Card
                AnimatedVisibility(visible = showAddForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (editingNumber == null) "Add New Player" else "Edit Player #${editingNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Player Name") },
                                    modifier = Modifier.weight(1.8f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = number,
                                    onValueChange = { number = it.filter(Char::isDigit).take(4) },
                                    label = { Text("Number") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = role,
                                    onValueChange = { role = it.take(40) },
                                    label = { Text("Role (Batsman/Bowler/All-Rounder)") },
                                    modifier = Modifier.weight(1.4f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                if (gradesEnabled) {
                                    OutlinedTextField(
                                        value = grade,
                                        onValueChange = { grade = it.take(20) },
                                        label = { Text("Grade (A/B/C)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it.take(24) },
                                label = { Text("Phone / Contact (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = photoUrl,
                                onValueChange = { photoUrl = it.take(1000) },
                                label = { Text("Player Photo URL (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val n = number.toIntOrNull()
                                        val oldNumber = editingNumber
                                        when {
                                            name.isBlank() -> formNotice = "Please enter player name."
                                            n == null || n <= 0 -> formNotice = "Enter a valid jersey/player number."
                                            players.any { it.number == n && it.number != oldNumber } -> formNotice = "Jersey #$n is already assigned."
                                            else -> {
                                                if (oldNumber == null) {
                                                    onAdd(name.trim(), n, grade.trim(), role.trim(), phone.trim(), photoUrl.trim())
                                                    formNotice = "Player #${n} added successfully."
                                                } else {
                                                    onUpdate(oldNumber, name.trim(), n, grade.trim(), role.trim(), phone.trim(), photoUrl.trim())
                                                    formNotice = "Player #${n} updated successfully."
                                                }
                                                name = ""; number = ""; grade = ""; role = ""; phone = ""; photoUrl = ""; editingNumber = null
                                                showAddForm = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(if (editingNumber == null) "Save Player" else "Update Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                if (editingNumber != null) {
                                    OutlinedButton(
                                        onClick = {
                                            name = ""; number = ""; grade = ""; role = ""; phone = ""; photoUrl = ""; editingNumber = null
                                            showAddForm = false
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("Cancel") }
                                }
                            }

                            if (formNotice.isNotBlank()) {
                                Text(formNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // CSV Import Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF00E676).copy(alpha = 0.15f)) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bulk Player Import", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Upload a CSV file (number,name,grade,role,phone,photoUrl)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { csvImportLauncher.launch(arrayOf("text/*", "text/csv", "application/csv", "application/octet-stream")) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (importNotice.isNotBlank()) {
                    Text(importNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                // Search & View Mode row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search player, role, number…", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (search.isNotBlank()) {
                                IconButton(onClick = { search = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row {
                            IconButton(onClick = { gridMode = true }) {
                                Icon(
                                    Icons.Rounded.GridView,
                                    contentDescription = "Grid",
                                    tint = if (gridMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { gridMode = false }) {
                                Icon(
                                    Icons.Rounded.ViewList,
                                    contentDescription = "List",
                                    tint = if (!gridMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Action: Continue to live auction
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Gavel, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text("Save Players & Start Live Auction ➔", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Text(
                    text = "Showing ${filtered.size} of ${players.size} players",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(if (players.isEmpty()) "No players added yet" else "No matching players found", fontWeight = FontWeight.Bold)
                        Text(if (players.isEmpty()) "Add players individually or import a CSV file." else "Try searching with a different name or number.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else if (gridMode) {
            items(filtered.chunked(2), key = { row -> row.first().number }) { rowPlayers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPlayers.forEach { player ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("#${player.number}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    PlayerStatusBadge(player.status)
                                }

                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PlayerRoleBadge(player.role)
                                    if (gradesEnabled) GradeChip(player.grade)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { startEdit(player) },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(13.dp))
                                    }

                                    OutlinedButton(
                                        onClick = { pendingDeleteNumber = player.number },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (rowPlayers.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            items(filtered, key = { it.number }) { player ->
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
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#${player.number}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PlayerRoleBadge(player.role)
                                if (gradesEnabled) GradeChip(player.grade)
                            }
                        }

                        PlayerStatusBadge(player.status)

                        IconButton(onClick = { startEdit(player) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { pendingDeleteNumber = player.number }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete Player Confirmation Dialog
    val pendingPlayer = players.firstOrNull { it.number == pendingDeleteNumber }
    if (pendingDeleteNumber != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteNumber = null },
            title = { Text("Delete Player #${pendingPlayer?.number}?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${pendingPlayer?.name}? If this player was already sold or retained, the team's balance and squad count will be automatically updated.") },
            confirmButton = {
                Button(
                    onClick = {
                        val num = pendingDeleteNumber
                        if (num != null) onDelete(num)
                        pendingDeleteNumber = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteNumber = null }) { Text("Cancel") }
            }
        )
    }
}
