# 📱 راهنمای NEPETIS BOX

## ویژگی‌ها

✨ **اثرات Liquid Glass**
- کارت‌های شیشه‌ای با حاشیه‌های روشن
- پس‌زمینه متحرک با ذرات رنگی
- انیمیشن‌های نرم و سریع

🎨 **رنگ‌های جذاب**
- آبی روشن (#00D4FF)
- مجنتا (#FF00FF)
- سبز آبی (#00FF88)
- نارنجی (#FFAA00)
- و بسیاری رنگ‌های دیگر

📜 **اسکرول نرم**
- اسکرول عمودی بدون مزاحمت
- پس‌زمینه ثابت در حین اسکرول
- عناصر شفاف و منسجم

## نحوه نصب

### مراحل

1. **نصب Android Studio**
   - دانلود از: https://developer.android.com/studio

2. **ایجاد پروژه جدید**
   ```bash
   - File → New → New Project
   - انتخاب "Empty Compose Activity"
   ```

3. **کپی فایل‌ها**
   - کپی تمام فایل‌های Kotlin به پوشه `app/src/main/java/com/nepetis/box/`
   - کپی `build.gradle` به پوشه پروژه

4. **Sync and Build**
   - Gradle Sync
   - Build → Make Project

5. **اجرا در Emulator یا دستگاه**
   - Run → Run 'app'

## سازی و شخصی‌سازی

### تغییر رنگ‌ها

در فایل `AnimatedParticleBackground.kt`:
```kotlin
val colors = listOf(
    Color(0xFF00D4FF), // رنگ 1
    Color(0xFFFF00FF), // رنگ 2
    // رنگ‌های بیشتر اضافه کنید
)
```

### تغییر سرعت انیمیشن

در فایل `MainActivity.kt`:
```kotlin
LaunchedEffect(Unit) {
    while (true) {
        kotlinx.coroutines.delay(16) // تقلیل عدد برای سرعت بیشتر
        animationTime += 0.016f
    }
}
```

### تغییر تعداد ذرات

در فایل `AnimatedParticleBackground.kt`:
```kotlin
private fun generateParticles(count: Int = 30): List<Particle> {
    // count را تغییر دهید (مثال: 50 برای ذرات بیشتر)
}
```

### تغییر اثر Glassmorphic

در فایل `GlassmorphicCard.kt`:
```kotlin
.background(
    color = Color(0x1AFFFFFF), // شفافیت را تغییر دهید
)
.border(
    width = 1.5.dp, // ضخامت حاشیه را تغییر دهید
)
```

## ساختار پروژه

```
com.nepetis.box/
├── MainActivity.kt           # فعالیت اصلی
├── components/
│   ├── AnimatedParticleBackground.kt
│   ├── LiquidGlassBackground.kt
│   └── GlassmorphicCard.kt
└── ui/
    └── theme/
        ├── Theme.kt
        └── Typography.kt
```

## نکات مهم

⚡ **عملکرد**
- از انیمیشن‌های Compose استفاده می‌کند
- تحت بار CPU پایین
- مناسب برای اکثر دستگاه‌ها

🎯 **بهینه‌سازی**
- استفاده از `LaunchedEffect` برای انیمیشن
- Composable Recomposition کم
- بدون Garbage Collection غیر ضروری

📲 **سازگاری**
- API 24+
- Jetpack Compose
- تمام سایز صفحه

## حل مشکلات

### اگر اسکرول کار نمی‌کند
- بررسی کنید `verticalScroll(scrollState)` موجود است
- اطمینان حاصل کنید Column قد کافی دارد

### اگر انیمیشن سریع/آهسته است
- `delay` در `LaunchedEffect` را تنظیم کنید
- `animationTime += 0.016f` را تغییر دهید

### اگر ذرات دیده نمی‌شوند
- بررسی کنید `alpha` مقادیر از 0 بیشتر هستند
- رنگ‌ها روی پس‌زمینه سیاه قابل رویت هستند

## پیوند‌های مفید

- 📚 [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- 🎨 [Material Design 3](https://m3.material.io/)
- 🔧 [Android Developer](https://developer.android.com/)

---

طراحی شده با ❤️ برای NEPETIS BOX
