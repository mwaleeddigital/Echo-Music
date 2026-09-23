package com.music.innertube.utils

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
