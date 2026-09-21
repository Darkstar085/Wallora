package com.darkstar.wallora.data

import okhttp3.OkHttpClient

object NetworkClient {
    val client: OkHttpClient by lazy { OkHttpClient() }
}
