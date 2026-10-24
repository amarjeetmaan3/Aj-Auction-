package com.example.auction.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Upload
import com.example.auction.ui.components.SetupProgressHeader
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.auction.data.AuctionConfig

@Composable
fun SettingsScreen(
    config: AuctionConfig,
    onSave: (AuctionConfig) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    backupNotice: String
) {
    var basePrice by remember(config.basePrice) { mutableStateOf(config.basePrice.toString()) }
    var budget by remember(config.startingPoints) { mutableStateOf(config.startingPoints.toString()) }
    var squadLimit by remember(config.playersPerTeam) { mutableStateOf(config.playersPerTeam.toString()) }
    var retentionLimit by remember(config.maxRetentions) { mutableStateOf(config.maxRetentions.toString()) }
    var retentionCost by remember(config.retentionCost) { mutableStateOf(config.retentionCost.toString()) }
    var teamCount by remember(config.teamCount) { mutableStateOf(config.teamCount.toString()) }
    var gradesEnabled by remember(config.enablePlayerGrades) { mutableStateOf(config.enablePlayerGrades) }

    val base = basePrice.toDoubleOrNull()
    val points = budget.toDoubleOrNull()
    val squad = squadLimit.toIntOrNull()
    val retention = retentionLimit.toIntOrNull()
    val cost = retentionCost.toDoubleOrNull()
    val count = teamCount.toIntOrNull()

    val valid = base != null && base >= 0 && points != null && points > 0 &&
        squad != null && squad > 0 && retention != null && retention >= 0 &&
        cost != null && cost >= 0 && count != null && count > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SetupProgressHeader(currentStep = 1)

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LEAGUE RULES & SETTINGS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure auction economy, squad size limits & retention costs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Auction Economy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("AUCTION ECONOMY", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                        label = { Text("Base Price (pts)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                        label = { Text("Starting Budget (pts)") },
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
                        value = squadLimit,
                        onValueChange = { squadLimit = it.filter(Char::isDigit).take(3) },
                        label = { Text("Players Per Team") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = teamCount,
                        onValueChange = { teamCount = it.filter(Char::isDigit).take(2) },
                        label = { Text("Number of Teams") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Retention & Grades Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("RETENTION & PLAYER GRADES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = retentionLimit,
                        onValueChange = { retentionLimit = it.filter(Char::isDigit).take(2) },
                        label = { Text("Max Retentions") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = retentionCost,
                        onValueChange = { retentionCost = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                        label = { Text("Cost Per Retain") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Enable Player Grades", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Categorize players by tier (A+, A, B, etc.)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Switch(
                        checked = gradesEnabled,
                        onCheckedChange = { gradesEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Save Settings Button
        Button(
            onClick = {
                if (valid) {
                    onSave(
                        AuctionConfig(
                            playersPerTeam = squad!!,
                            basePrice = base!!,
                            startingPoints = points!!,
                            maxRetentions = retention!!,
                            retentionCost = cost!!,
                            teamCount = count!!,
                            enablePlayerGrades = gradesEnabled
                        )
                    )
                }
            },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(6.dp))
            Text("Save Rules & Continue to Teams ➔", color = Color.Black, fontWeight = FontWeight.Black)
        }

        // Backup & Restore Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("LEAGUE BACKUP & RESTORE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                Text("Export this entire league state to a JSON file or import a previously exported backup.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export JSON")
                    }

                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Restore JSON")
                    }
                }

                if (backupNotice.isNotBlank()) {
                    Text(backupNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
