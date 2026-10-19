package mda.pages

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object HostCheck {
    fun status(url: String): Int {
        try {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build()
            val request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build()
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
        } catch (error: Exception) {
            throw AssertionError("The host failed, not the screen. ${error.message}", error)
        }
    }
}
