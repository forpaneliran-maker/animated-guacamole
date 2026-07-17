package com.nepetis.box.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepetis.box.core.model.ProtocolType
import com.nepetis.box.core.model.ServerConfig
import com.nepetis.box.core.model.ConfigParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

@Composable
fun ServerListScreen(
    servers: List<ServerConfig>,
    selectedServerId: String?,
    isConnected: Boolean,
    onServerSelected: (ServerConfig) -> Unit,
    onAddServer: (ServerConfig) -> Unit,
    onConnectToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pingResults by remember { mutableStateOf<Map<String, Long?>>(emptyMap()) }

    Column(modifier = modifier.fillMaxSize()) {

        // هدر با دکمه اتصال بزرگ
        ConnectionHeader(
            isConnected = isConnected,
            serverName = servers.find { it.id == selectedServerId }?.name,
            onToggle = onConnectToggle
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سرورها (${servers.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            GlassIconButton(icon = Icons.Filled.Add) {
                showAddDialog = true
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                ServerRow(
                    server = server,
                    isSelected = server.id == selectedServerId,
                    pingMs = pingResults[server.id],
                    onClick = { onServerSelected(server) },
                    onPingClick = {
                        scope.launch {
                            pingResults = pingResults + (server.id to null) // در حال تست
                            val result = testPing(server.address, server.port)
                            pingResults = pingResults + (server.id to result)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { link ->
                ConfigParser.parse(link)?.let { onAddServer(it) }
                showAddDialog = false
            },
            onAddSubscription = { content ->
                ConfigParser.parseSubscription(content).forEach { onAddServer(it) }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ConnectionHeader(
    isConnected: Boolean,
    serverName: String?,
    onToggle: () -> Unit
) {
    val statusColor = if (isConnected) Color(0xFF00FF88) else Color(0xFF888888)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x1AFFFFFF))
            .clickable { onToggle() }
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnected) Color(0x3300FF88) else Color(0x22FFFFFF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF00FF88) else Color(0xFF444444))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isConnected) "متصل" else "قطع شده",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = serverName ?: "سروری انتخاب نشده",
                fontSize = 13.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ServerRow(
    server: ServerConfig,
    isSelected: Boolean,
    pingMs: Long?,
    onClick: () -> Unit,
    onPingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0x2A00D4FF) else Color(0x14FFFFFF)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${server.protocol.name} · ${server.address}:${server.port}",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable { onPingClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when {
                        pingMs == null && server.pingMs == null -> "تست"
                        pingMs == null -> "..."
                        else -> "${pingMs} ms"
                    },
                    fontSize = 12.sp,
                    color = pingColor(pingMs),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun pingColor(ms: Long?): Color = when {
    ms == null -> Color(0xFFCCCCCC)
    ms < 150 -> Color(0xFF00FF88)
    ms < 400 -> Color(0xFFFFAA00)
    else -> Color(0xFFFF4444)
}

@Composable
private fun GlassIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x22FFFFFF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun AddServerDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onAddSubscription: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isSubscription by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                text = if (isSubscription) "افزودن اشتراک" else "افزودن سرور",
                color = Color.White
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            if (isSubscription) "لینک اشتراک یا محتوای base64"
                            else "vmess:// یا vless:// یا trojan:// یا ss://",
                            color = Color(0xFF777777)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00D4FF),
                        unfocusedBorderColor = Color(0xFF444444)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSubscription,
                        onCheckedChange = { isSubscription = it }
                    )
                    Text("این یک لینک اشتراک (Subscription) است", color = Color(0xFFCCCCCC), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) {
                    if (isSubscription) onAddSubscription(text) else onAdd(text)
                }
            }) {
                Text("افزودن", color = Color(0xFF00D4FF))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color(0xFF999999))
            }
        }
    )
}

/** تست ساده TCP handshake برای سنجش تأخیر تقریبی به سرور (نه پینگ ICMP واقعی) */
private suspend fun testPing(host: String, port: Int): Long? = withContext(Dispatchers.IO) {
    try {
        val start = System.currentTimeMillis()
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), 3000)
        }
        System.currentTimeMillis() - start
    } catch (e: Exception) {
        null
    }
}
