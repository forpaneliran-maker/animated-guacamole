package com.nepetis.box

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.nepetis.box.components.AnimatedParticleBackground
import com.nepetis.box.core.model.ServerConfig
import com.nepetis.box.core.vpn.VpnConnectionManager
import com.nepetis.box.core.vpn.VpnConnectionState
import com.nepetis.box.core.vpn.rememberVpnPermissionLauncher
import com.nepetis.box.ui.screens.ServerListScreen
import com.nepetis.box.ui.theme.NepetisBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NepetisBoxTheme {
                NepetisBoxApp()
            }
        }
    }
}

@Composable
fun NepetisBoxApp() {
    val context = LocalContext.current
    val vpnManager = remember { VpnConnectionManager(context) }

    var servers by remember { mutableStateOf(listOf<ServerConfig>()) }
    var selectedServerId by remember { mutableStateOf<String?>(null) }

    val connectionState by vpnManager.state.collectAsState()
    val isConnected = connectionState is VpnConnectionState.Connected

    val requestConnect = rememberVpnPermissionLauncher(vpnManager) { server ->
        selectedServerId = server.id
        vpnManager.startConnection(server)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // پس‌زمینه متحرک با ذرات رنگی روی زمینه مشکی مات
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        // صفحه اصلی: مدیریت سرورها + دکمه اتصال
        ServerListScreen(
            servers = servers,
            selectedServerId = selectedServerId,
            isConnected = isConnected,
            onServerSelected = { selectedServerId = it.id },
            onAddServer = { newServer -> servers = servers + newServer },
            onConnectToggle = {
                if (isConnected) {
                    vpnManager.stopConnection()
                } else {
                    servers.find { it.id == selectedServerId }?.let { requestConnect(it) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
