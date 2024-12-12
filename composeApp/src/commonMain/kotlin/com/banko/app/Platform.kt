package com.banko.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform