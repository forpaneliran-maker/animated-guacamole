# 🔌 راهنمای اتصال هسته واقعی Xray/V2Ray

آنچه تا الان ساخته شده **کاملاً کاربردی** است برای:
- ✅ مدیریت لیست سرورها (افزودن با لینک vmess/vless/trojan/ss یا subscription)
- ✅ تست تأخیر (TCP handshake) به هر سرور
- ✅ درخواست مجوز VPN از اندروید و روشن/خاموش کردن رابط TUN
- ✅ نوتیفیکیشن Foreground Service و مدیریت وضعیت اتصال

آنچه **باقی مانده** و باید خودتان اضافه کنید (چون نیاز به کامپایل باینری Go دارد):

## مرحله ۱: گرفتن هسته Xray برای اندروید

```bash
git clone https://github.com/2dust/AndroidLibXrayLite.git
cd AndroidLibXrayLite
# نیاز به نصب Go و gomobile دارد
gomobile bind -target=android -o libv2ray.aar ./
```

خروجی `libv2ray.aar` را در پوشه `app/libs/` پروژه قرار دهید و در `build.gradle` اضافه کنید:

```gradle
dependencies {
    implementation files('libs/libv2ray.aar')
}
```

## مرحله ۲: ساخت JSON کانفیگ از ServerConfig

باید یک تابع بنویسید که `ServerConfig` (که همین الان دارید) را به فرمت JSON استاندارد Xray تبدیل کند. نمونه ساختار برای Vmess:

```json
{
  "outbounds": [{
    "protocol": "vmess",
    "settings": {
      "vnext": [{
        "address": "SERVER_ADDRESS",
        "port": 443,
        "users": [{"id": "UUID", "alterId": 0, "security": "auto"}]
      }]
    },
    "streamSettings": { "network": "ws", "wsSettings": {"path": "/path"} }
  }]
}
```

## مرحله ۳: اتصال به NepetisVpnService

در فایل `core/vpn/NepetisVpnService.kt`، متد `startXrayCore()` را این‌طور تکمیل کنید:

```kotlin
private suspend fun startXrayCore(serverId: String?) {
    val config = buildXrayJsonConfig(server) // تابعی که در مرحله ۲ نوشتید
    Libv2ray.startLoop(config) // از AAR اضافه‌شده در مرحله ۱
    
    // هدایت بسته‌ها بین vpnInterface (fd) و پورت local Xray (معمولاً SOCKS روی 127.0.0.1:1080)
    // این بخش نیاز به یک tun2socks دارد؛ می‌توانید از پروژه‌ی hev-socks5-tunnel
    // یا tun2socks گوگل استفاده کنید (این‌ها هم native library جدا هستند)
}
```

## نکته مهم: چرا این بخش قابل تولید خودکار نیست؟

هسته Xray به زبان **Go** نوشته شده و باید برای معماری‌های ARM/ARM64/x86 اندروید
جداگانه **کامپایل** شود (نه فقط کدنویسی). این خروجی یک فایل باینری (`.aar`) است
که نمی‌توان آن را مثل یک فایل کد متنی نوشت — باید با ابزار `gomobile` روی سیستم
شما (یا CI/CD) build شود. تمام اپ‌های مشابه V2Box/v2rayNG هم دقیقاً همین مسیر
رو طی می‌کنن.

## خلاصه مسیر باقی‌مانده

| مرحله | ابزار لازم | خروجی |
|-------|-----------|-------|
| 1. Build هسته Xray | Go + gomobile | `libv2ray.aar` |
| 2. ساخت JSON config | Kotlin (خودتان) | تابع generator |
| 3. اتصال tun ↔ socks | tun2socks یا hev-socks5-tunnel | routing بسته‌ها |
| 4. اتصال به VpnService | این پروژه (آماده) | ✅ انجام شد |
