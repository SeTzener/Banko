package com.banko.app.utils

fun String.tailDisplay(maxLength: Int = 120): String {
    if (length <= maxLength) return this
    val tail = takeLast(maxLength)
    val firstSpace = tail.indexOf(' ')
    return if (firstSpace > 0) {
        "\u2026" + tail.substring(firstSpace + 1)
    } else {
        "\u2026" + tail
    }
}
