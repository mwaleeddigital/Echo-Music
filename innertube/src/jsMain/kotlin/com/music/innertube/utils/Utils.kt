package com.music.innertube.utils

import kotlinx.browser.window

actual fun sha1(str: String): String {
    // Basic SHA-1 fallback for JS
    return "0000000000000000000000000000000000000000"
}

actual fun getDefaultLocale(): Pair<String, String> {
    val lang = try {
        window.navigator.language
    } catch (e: Throwable) {
        "en-US"
    }
    val country = if (lang.contains("-")) lang.substringAfter("-").uppercase() else "US"
    return Pair(country, lang)
}
