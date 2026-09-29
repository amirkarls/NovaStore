package com.novastore.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class PingResult(
    val host: String,
    val success: Boolean,
    val latencyMs: Long,
    val message: String
)

object PingTest {

    suspend fun test(host: String = "github.com"): PingResult = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(5000)
            val elapsed = System.currentTimeMillis() - start

            if (reachable) {
                PingResult(
                    host = host,
                    success = true,
                    latencyMs = elapsed,
                    message = "OK"
                )
            } else {
                PingResult(
                    host = host,
                    success = false,
                    latencyMs = elapsed,
                    message = "Host unreachable"
                )
            }
        } catch (e: Exception) {
            PingResult(
                host = host,
                success = false,
                latencyMs = 0,
                message = e.message ?: "Error"
            )
        }
    }
}
