@file:JvmName("AndroidUtils")
package com.music.innertube.utils

import java.security.MessageDigest
import java.util.Locale

actual fun sha1(str: String): String = MessageDigest.getInstance("SHA-1").digest(str.toByteArray()).toHex()

actual fun getDefaultLocale(): Pair<String, String> {
    val locale = Locale.getDefault()
    return Pair(locale.country, locale.toLanguageTag())
}
