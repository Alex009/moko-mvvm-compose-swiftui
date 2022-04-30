package ru.alex009.moko.mvvm.declarativeui

actual class Platform actual constructor() {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}