package com.google.habitus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform