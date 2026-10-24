package com.example.auction.ui.screens

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AppToolsScreen() {
    val context = LocalContext.current
    val packageInfo = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()
    }
    val currentVersionName = packageInfo?.versionName ?: "1.0.0"
    val currentVersionCode = packageInfo?.let {
        if (android.os.Build.VERSION.SDK_INT >= 28) it.longVersionCode else {
            @Suppress("DEPRECATION")
            it.versionCode.toLong()
        }
    } ?: 1L

    var showPrivacy by remember { mutableStateOf(false) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var availableApkUrl by remember { mutableStateOf<String?>(null) }
    var availableVersion by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val manifestUrl = "https://raw.githubusercontent.com/amarjeetmaan3/Ajupdates/main/apps/aj-auction.json"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column {
                        Text("AJ AUCTION PRO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("Version $currentVersionName (Build $currentVersionCode)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    text = "A fully native, offline-first cricket league auction management tool. Designed for seamless live bidding and team roster building.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Privacy Policy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF00E676).copy(alpha = 0.15f)
                ) {
                    Icon(Icons.Rounded.Policy, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.padding(8.dp).size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Privacy & Security", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Zero analytics, zero tracking. All auction data stays local.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = { showPrivacy = true }, shape = RoundedCornerShape(10.dp)) {
                    Text("View Policy", fontSize = 11.sp)
                }
            }
        }

        // Update Checking Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                    ) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Software Updates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Check for new native APK releases", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Button(
                    enabled = !checkingUpdate,
                    onClick = {
                        checkingUpdate = true
                        updateMessage = null
                        availableApkUrl = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    val connection = (URL(manifestUrl + "?t=" + System.currentTimeMillis()).openConnection() as HttpURLConnection).apply {
                                        connectTimeout = 8000
                                        readTimeout = 8000
                                        requestMethod = "GET"
                                    }
                                    try {
                                        if (connection.responseCode !in 200..299) error("Server response code: ${connection.responseCode}")
                                        val data = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                                        val code = data.optLong("versionCode", 0L)
                                        val version = data.optString("latestVersion", "")
                                        val url = data.optString("apkUrl", "")
                                        if (code <= 0L || version.isBlank() || !url.startsWith("https://")) error("Invalid manifest payload")
                                        Triple(code, version, url)
                                    } finally {
                                        connection.disconnect()
                                    }
                                }
                            }
                            checkingUpdate = false
                            result.onSuccess { (code, version, url) ->
                                if (code > currentVersionCode) {
                                    availableVersion = version
                                    availableApkUrl = url
                                    updateMessage = "Version $version is available!"
                                } else {
                                    updateMessage = "You are on the latest version ($currentVersionName)."
                                }
                            }.onFailure {
                                updateMessage = "Update check: " + (it.message ?: "Unable to connect")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (checkingUpdate) "Checking Updates…" else "Check for Updates")
                }

                updateMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                availableApkUrl?.let { apkUrl ->
                    Button(
                        onClick = {
                            runCatching {
                                val request = DownloadManager.Request(Uri.parse(apkUrl))
                                    .setTitle("AJ Auction $availableVersion")
                                    .setDescription("Downloading APK update")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AJ-Auction-$availableVersion.apk")
                                    .setMimeType("application/vnd.android.package-archive")
                                val manager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as DownloadManager
                                manager.enqueue(request)
                                updateMessage = "Download started. Check download notification to install."
                            }.onFailure {
                                updateMessage = "Download failed: ${it.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("Download Update APK", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "AJ Auction is built as an offline-first, standalone tool for running live in-person cricket auctions.\n\n" +
                        "• Local Data: All leagues, players, team rosters, and live bidding history remain on this device.\n\n" +
                        "• No Accounts Needed: The app requires no login or personal credentials to operate.\n\n" +
                        "• Permissions: Does not use microphone, camera, contacts, or background location.\n\n" +
                        "For issues or feedback, reach out via the official repository."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text("Close") }
            }
        )
    }
}
