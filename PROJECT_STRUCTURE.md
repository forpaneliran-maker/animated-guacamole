# 📁 ساختار کامل پروژه NEPETIS BOX

## درختِ دایرکتوری

```
NEPETIS_BOX/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nepetis/box/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MainActivityAdvanced.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── AnimatedParticleBackground.kt
│   │   │   │   │   ├── LiquidGlassBackground.kt
│   │   │   │   │   └── GlassmorphicCard.kt
│   │   │   │   └── ui/
│   │   │   │       └── theme/
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Typography.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── styles.xml
│   │   │   │   ├── mipmap/
│   │   │   │   │   └── ic_launcher.png
│   │   │   │   └── drawable/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── androidTest/...
│   │
│   ├── build.gradle
│   └── proguard-rules.pro
│
├── build.gradle (project level)
├── settings.gradle
├── local.properties
│
├── GUIDE_FA.md (راهنمای فارسی)
├── PROJECT_STRUCTURE.md (این فایل)
└── README.md

```

## شرح فایل‌های کلیدی

### 🎯 MainActivity.kt
- فعالیت اصلی برنامه
- راه‌اندازی اسکرول
- نمایش کارت‌های Glassmorphic
- بستِ پس‌زمینه متحرک

### 🎨 Components

#### AnimatedParticleBackground.kt
- ذرات رنگی متحرک
- حرکت موجی با ریاضیات مثلثاتی
- هاله‌های درخشنده به دور ذرات
- 7 رنگ مختلف

#### LiquidGlassBackground.kt
- اثرات Liquid Glass
- Blob‌های متحرک
- خطوط انیمیشن‌دار
- افزایش بصری

#### GlassmorphicCard.kt
- کارت‌های شفاف
- حاشیه‌های روشن
- تایپوگرافی سفید
- دکمه‌های Glassmorphic

### 🎨 Theme

#### Theme.kt
- رنگ‌های تیم Dark
- رنگ اصلی: آبی روشن
- رنگ‌های فرعی: مجنتا و سبز

#### Typography.kt
- اندازه‌های مختلف متن
- وزن‌های مختلف فونت
- سبک‌های استاندارد

## دستورات مفید

### Build & Run
```bash
# Sync Gradle
./gradlew sync

# Build APK
./gradlew assembleDebug

# Run on Device
adb install app/build/outputs/apk/debug/app-debug.apk

# Clean
./gradlew clean
```

### Testing
```bash
# Unit Tests
./gradlew test

# Instrumented Tests
./gradlew connectedAndroidTest
```

## Dependencies

### Core
- `androidx.compose.ui:ui:1.5.4`
- `androidx.compose.material3:material3:1.1.2`
- `androidx.compose.foundation:foundation:1.5.4`

### Lifecycle
- `androidx.activity:activity-compose:1.8.0`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.6.2`

### Async
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`

## کوئیک‌ استارت

### 1️⃣ آماده‌سازی محیط
```bash
# نصب Android SDK
# نصب Kotlin Plugin
# نصب Gradle
```

### 2️⃣ ایجاد پروژه
```bash
# Android Studio → New Project
# Select: Empty Compose Activity
# Min SDK: API 24
```

### 3️⃣ کپی فایل‌ها
```bash
# کپی تمام فایل‌های Kotlin
# کپی build.gradle
# کپی AndroidManifest.xml
```

### 4️⃣ Sync & Build
```bash
# File → Sync with Gradle Files
# Build → Make Project
# Run → Run 'app'
```

## نکات بهینه‌سازی

✅ **کاهش Recomposition**
- استفاده از `remember`
- استفاده از `mutableStateOf`
- Composable‌های کوچک

✅ **عملکرد بهتر**
- Canvas استفاده شده برای رسم
- LaunchedEffect برای انیمیشن
- Modifier‌های مشروط

✅ **مصرف انرژی کم**
- دقت 60 FPS
- بدون Garbage Collection اضافی
- انیمیشن‌های هوشمند

## گسترش و تطویر

### اضافه کردن ویژگی‌های جدید

#### 1. نمایش تصویر
```kotlin
@Composable
fun ImageCard(imageResId: Int) {
    GlassmorphicCard(
        content = {
            Image(
                painter = painterResource(imageResId),
                contentDescription = "تصویر"
            )
        }
    )
}
```

#### 2. اضافه کردن دکمه
```kotlin
@Composable
fun InteractiveCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier.clickable { onClick() }
    ) {
        // محتوا
    }
}
```

#### 3. انیمیشن کاستم
```kotlin
val infiniteTransition = rememberInfiniteTransition()
val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(...)
)
```

## مشکلات رایج و حل‌ها

| مشکل | علت | حل |
|------|-----|-----|
| ذرات دیده نمی‌شوند | alpha = 0 | مقدار alpha را افزایش دهید |
| اسکرول کار نمی‌کند | غیاب scrollState | اطمینان حاصل کنید scrollState موجود است |
| انیمیشن یخ می‌زند | بار CPU بالا | تعداد ذرات را کاهش دهید |
| Gradle Sync ناکام | dependencies مفقود | Refresh Gradle |

## لینک‌های مفید

- 📚 [Compose Documentation](https://developer.android.com/jetpack/compose/documentation)
- 🎨 [Material 3 Colors](https://m3.material.io/styles/color/dynamic-color/overview)
- 🔧 [Android Studio Guide](https://developer.android.com/studio/intro)
- 📖 [Kotlin Documentation](https://kotlinlang.org/docs/home.html)

---

**ایجاد شده با ❤️ برای NEPETIS BOX**
