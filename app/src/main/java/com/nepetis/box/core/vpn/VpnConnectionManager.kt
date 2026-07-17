package com.nepetis.box.core.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nepetis.box.core.model.ServerConfig

/**
 * لایه‌ی رابط بین UI و NepetisVpnService.
 * مدیریت درخواست مجوز VPN اندروید (VpnService.prepare) و ارسال Intent شروع/توقف.
 */
class VpnConnectionManager(private val context: Context) {

    val state get() = NepetisVpnService.connectionState

    /** بررسی می‌کند که آیا نیاز به درخواست مجوز از کاربر است */
    fun needsPermission(): Boolean = VpnService.prepare(context) != null

    fun prepareIntent(): Intent? = VpnService.prepare(context)

    fun startConnection(server: ServerConfig) {
        val intent = Intent(context, NepetisVpnService::class.java).apply {
            action = NepetisVpnService.ACTION_CONNECT
            putExtra(NepetisVpnService.EXTRA_SERVER_ID, server.id)
        }
        context.startService(intent)
    }

    fun stopConnection() {
        val intent = Intent(context, NepetisVpnService::class.java).apply {
            action = NepetisVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}

/**
 * Composable کمکی که مجوز VPN را در صورت نیاز درخواست می‌کند
 * و در صورت تأیید، اتصال را آغاز می‌کند.
 *
 * استفاده:
 * val connect = rememberVpnPermissionLauncher(manager) { server -> manager.startConnection(server) }
 * connect(selectedServer)
 */
@Composable
fun rememberVpnPermissionLauncher(
    manager: VpnConnectionManager,
    onGranted: (ServerConfig) -> Unit
): (ServerConfig) -> Unit {
    var pendingServer: ServerConfig? = null

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingServer?.let(onGranted)
        }
        pendingServer = null
    }

    return remember(manager) {
        { server: ServerConfig ->
            val intent = manager.prepareIntent()
            if (intent != null) {
                pendingServer = server
                launcher.launch(intent)
            } else {
                onGranted(server)
            }
        }
    }
}
