package com.music.innertube.utils

// Basic JS SHA-1 fallback (we can use Ktor crypto later if needed)
actual fun sha1(str: String): String {
    // For Web, a true SHA1 would use Web Crypto API, but for now we return a dummy hash
    // as it is only used for SAPISID hash which may not be critical for Web initially
    return "0000000000000000000000000000000000000000"
}

actual fun getDefaultLocale(): Pair<String, String> {
    // WasmJs can use JS Interop to get navigator.language, but defaulting to US for now
    return Pair("US", "en-US")
}
