package com.nepetis.box.core.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.nepetis.box.MainActivity
import com.nepetis.box.core.model.ServerConfig
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * سرویس اصلی VPN.
 * این کلاس فقط لایه‌ی رابط سیستم‌عامل اندروید (tun interface) رو مدیریت می‌کنه.
 * ترافیک واقعی رمزنگاری‌شده باید توسط هسته Xray/V2Ray پردازش بشه —
 * محل اتصال آن در متد `startXrayCore()` مشخص شده (TODO).
 */
class NepetisVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.nepetis.box.CONNECT"
        const val ACTION_DISCONNECT = "com.nepetis.box.DISCONNECT"
        const val EXTRA_SERVER_ID = "extra_server_id"
        const val NOTIFICATION_CHANNEL_ID = "nepetis_vpn_channel"
        const val NOTIFICATION_ID = 1

        private val _state = MutableStateFlowHolder()
        val connectionState get() = _state.flow
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trafficJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                connect(serverId)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    private fun connect(serverId: String?) {
        _state.update(VpnConnectionState.Connecting)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("در حال اتصال..."))

        serviceScope.launch {
            try {
                // 1) ساخت رابط TUN
                vpnInterface = establishVpnInterface()

                // 2) TODO: راه‌اندازی هسته Xray/V2Ray و هدایت بسته‌ها از/به رابط TUN
                //    مثال معمول در پروژه‌هایی مثل v2rayNG:
                //    val config = buildXrayJsonConfig(server)
                //    Libv2ray.startLoop(config)   // <-- از AndroidLibXrayLite (باید جدا اضافه شود)
                startXrayCore(serverId)

                _state.update(VpnConnectionState.Connected(serverId))
                updateNotification("متصل شد ✅")
            } catch (e: Exception) {
                _state.update(VpnConnectionState.Error(e.message ?: "خطای ناشناخته"))
                disconnect()
            }
        }
    }

    /**
     * محل اتصال به هسته Xray. در نسخه فعلی فقط یک placeholder است.
     * وقتی فایل AAR هسته (مثلاً libv2ray.aar) به پروژه اضافه شد،
     * این متد باید JSON کانفیگ را بسازد و Libv2ray.startLoop(...) را صدا بزند.
     */
    private suspend fun startXrayCore(serverId: String?) {
        // Placeholder: فعلاً فقط تاخیر شبیه‌سازی شده
        delay(500)
        // trafficJob = serviceScope.launch { pumpPackets() } // بعد از اتصال هسته واقعی فعال شود
    }

    private fun establishVpnInterface(): ParcelFileDescriptor {
        val builder = Builder()
            .setSession("NEPETIS BOX")
            .addAddress("10.10.10.2", 24)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .setMtu(1500)

        // اجازه‌ی مسیریابی از طریق برنامه‌های دیگر (به جز خودمان، برای جلوگیری از حلقه)
        builder.addDisallowedApplication(packageName)

        return builder.establish()
            ?: throw IllegalStateException("امکان ساخت رابط VPN وجود ندارد")
    }

    private fun disconnect() {
        trafficJob?.cancel()
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null
        _state.update(VpnConnectionState.Disconnected)
        stopForeground(true)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("NEPETIS BOX")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NEPETIS BOX VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onRevoke() {
        disconnect()
        super.onRevoke()
    }
}

sealed class VpnConnectionState {
    object Disconnected : VpnConnectionState()
    object Connecting : VpnConnectionState()
    data class Connected(val serverId: String?) : VpnConnectionState()
    data class Error(val message: String) : VpnConnectionState()
}

/** Holder ساده برای StateFlow تا بدون DI هم قابل استفاده باشد */
private class MutableStateFlowHolder {
    private val _flow = kotlinx.coroutines.flow.MutableStateFlow<VpnConnectionState>(VpnConnectionState.Disconnected)
    val flow: kotlinx.coroutines.flow.StateFlow<VpnConnectionState> get() = _flow
    fun update(state: VpnConnectionState) {
        _flow.value = state
    }
}
