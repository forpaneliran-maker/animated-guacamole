package com.nepetis.box.core.model

import android.util.Base64
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder

enum class ProtocolType {
    VMESS, VLESS, TROJAN, SHADOWSOCKS, UNKNOWN
}

data class ServerConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val protocol: ProtocolType,
    val address: String,
    val port: Int,
    val uuidOrPassword: String,
    val network: String = "tcp",       // tcp, ws, grpc, h2
    val path: String = "",
    val host: String = "",
    val tls: Boolean = false,
    val sni: String = "",
    val alterId: Int = 0,
    val security: String = "auto",     // auto, aes-128-gcm, chacha20 و ...
    val rawLink: String = "",
    var pingMs: Long? = null            // نتیجه آخرین تست پینگ (میلی‌ثانیه) یا null = تست نشده
)

/**
 * پارسر لینک‌های اشتراک (vmess/vless/trojan/ss).
 * این کلاس فقط ساختار کانفیگ رو استخراج می‌کنه؛ اتصال واقعی توسط هسته Xray انجام می‌شه.
 */
object ConfigParser {

    fun parse(link: String): ServerConfig? {
        return try {
            when {
                link.startsWith("vmess://") -> parseVmess(link)
                link.startsWith("vless://") -> parseVless(link)
                link.startsWith("trojan://") -> parseTrojan(link)
                link.startsWith("ss://") -> parseShadowsocks(link)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** پارس لیست اشتراک: هر خط base64 کل، یا خطوط جدا از لینک‌ها */
    fun parseSubscription(content: String): List<ServerConfig> {
        val decoded = try {
            String(Base64.decode(content.trim(), Base64.DEFAULT))
        } catch (e: Exception) {
            content
        }
        return decoded.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { parse(it) }
    }

    private fun parseVmess(link: String): ServerConfig {
        val b64 = link.removePrefix("vmess://")
        val json = JSONObject(String(Base64.decode(b64, Base64.DEFAULT)))
        return ServerConfig(
            name = json.optString("ps", "Vmess Server"),
            protocol = ProtocolType.VMESS,
            address = json.optString("add"),
            port = json.optString("port").toIntOrNull() ?: 443,
            uuidOrPassword = json.optString("id"),
            network = json.optString("net", "tcp"),
            path = json.optString("path", ""),
            host = json.optString("host", ""),
            tls = json.optString("tls", "") == "tls",
            sni = json.optString("sni", ""),
            alterId = json.optString("aid").toIntOrNull() ?: 0,
            security = json.optString("scy", "auto"),
            rawLink = link
        )
    }

    private fun parseVless(link: String): ServerConfig {
        val uri = URI(link)
        val params = parseQuery(uri.rawQuery ?: "")
        return ServerConfig(
            name = URLDecoder.decode(uri.fragment ?: "Vless Server", "UTF-8"),
            protocol = ProtocolType.VLESS,
            address = uri.host,
            port = uri.port,
            uuidOrPassword = uri.userInfo ?: "",
            network = params["type"] ?: "tcp",
            path = params["path"] ?: "",
            host = params["host"] ?: "",
            tls = (params["security"] ?: "") == "tls",
            sni = params["sni"] ?: "",
            security = params["encryption"] ?: "none",
            rawLink = link
        )
    }

    private fun parseTrojan(link: String): ServerConfig {
        val uri = URI(link)
        val params = parseQuery(uri.rawQuery ?: "")
        return ServerConfig(
            name = URLDecoder.decode(uri.fragment ?: "Trojan Server", "UTF-8"),
            protocol = ProtocolType.TROJAN,
            address = uri.host,
            port = uri.port,
            uuidOrPassword = uri.userInfo ?: "",
            network = params["type"] ?: "tcp",
            path = params["path"] ?: "",
            host = params["host"] ?: "",
            tls = true,
            sni = params["sni"] ?: uri.host,
            rawLink = link
        )
    }

    private fun parseShadowsocks(link: String): ServerConfig {
        val body = link.removePrefix("ss://")
        val hashIdx = body.indexOf('#')
        val name = if (hashIdx >= 0) URLDecoder.decode(body.substring(hashIdx + 1), "UTF-8") else "SS Server"
        val main = if (hashIdx >= 0) body.substring(0, hashIdx) else body

        // فرمت: base64(method:password)@host:port  یا  کل رشته base64
        val atIdx = main.indexOf('@')
        return if (atIdx >= 0) {
            val userInfo = String(Base64.decode(main.substring(0, atIdx), Base64.DEFAULT))
            val (method, password) = userInfo.split(":", limit = 2)
            val hostPort = main.substring(atIdx + 1)
            val (host, port) = hostPort.split(":", limit = 2)
            ServerConfig(
                name = name,
                protocol = ProtocolType.SHADOWSOCKS,
                address = host,
                port = port.toIntOrNull() ?: 8388,
                uuidOrPassword = password,
                security = method,
                rawLink = link
            )
        } else {
            val decoded = String(Base64.decode(main, Base64.DEFAULT))
            val (methodPass, hostPort) = decoded.split("@", limit = 2)
            val (method, password) = methodPass.split(":", limit = 2)
            val (host, port) = hostPort.split(":", limit = 2)
            ServerConfig(
                name = name,
                protocol = ProtocolType.SHADOWSOCKS,
                address = host,
                port = port.toIntOrNull() ?: 8388,
                uuidOrPassword = password,
                security = method,
                rawLink = link
            )
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isEmpty()) return emptyMap()
        return query.split("&").mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) URLDecoder.decode(parts[0], "UTF-8") to URLDecoder.decode(parts[1], "UTF-8") else null
        }.toMap()
    }
}
