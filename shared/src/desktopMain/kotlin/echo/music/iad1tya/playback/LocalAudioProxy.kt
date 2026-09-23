package echo.music.iad1tya.playback

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors

object LocalAudioProxy {
    private var server: HttpServer? = null
    private var port: Int = 0
    @Volatile
    private var currentTargetUrl: String? = null
    @Volatile
    private var currentUserAgent: String? = null

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(15))
        .build()

    init {
        try {
            val s = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            s.createContext("/audio.mp4", Handler())
            s.executor = Executors.newCachedThreadPool { r ->
                Thread(r, "AudioProxyThread").apply { isDaemon = true }
            }
            s.start()
            server = s
            port = s.address.port
            println("LocalAudioProxy started on port $port")
        } catch (e: Exception) {
            System.err.println("Failed to start LocalAudioProxy: ${e.message}")
        }
    }

    fun getProxyUrl(targetUrl: String, userAgent: String? = null): String {
        currentTargetUrl = targetUrl
        currentUserAgent = userAgent ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        return "http://127.0.0.1:$port/audio.mp4"
    }

    private class Handler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val target = currentTargetUrl
            if (target == null) {
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
                return
            }

            val method = exchange.requestMethod.uppercase()
            val isHead = method == "HEAD"
            val clientRange = exchange.requestHeaders.getFirst("Range")
            val ua = currentUserAgent ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

            try {
                if (isHead) {
                    // Probe with a 1-byte GET to retrieve headers without downloading full stream
                    val probeReq = HttpRequest.newBuilder(URI.create(target))
                        .GET()
                        .header("User-Agent", ua)
                        .header("Range", "bytes=0-0")
                        .header("Accept", "*/*")
                        .timeout(Duration.ofSeconds(10))
                        .build()

                    val response = httpClient.send(probeReq, HttpResponse.BodyHandlers.discarding())
                    val contentRange = response.headers().firstValue("Content-Range").orElse(null)
                    val totalLength = contentRange?.substringAfterLast('/')?.toLongOrNull() ?: -1L

                    exchange.responseHeaders.set("Content-Type", "audio/mp4")
                    exchange.responseHeaders.set("Accept-Ranges", "bytes")
                    if (totalLength > 0) {
                        exchange.responseHeaders.set("Content-Length", totalLength.toString())
                    }
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                } else {
                    val reqBuilder = HttpRequest.newBuilder(URI.create(target))
                        .GET()
                        .header("User-Agent", ua)
                        .header("Accept", "*/*")
                        .header("Range", clientRange ?: "bytes=0-")
                        .timeout(Duration.ofSeconds(30))

                    val response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream())
                    val responseCode = response.statusCode()
                    val responseHeaders = response.headers()

                    exchange.responseHeaders.set("Content-Type", "audio/mp4")
                    exchange.responseHeaders.set("Accept-Ranges", "bytes")

                    responseHeaders.firstValue("Content-Range").ifPresent {
                        exchange.responseHeaders.set("Content-Range", it)
                    }

                    val contentLength = responseHeaders.firstValueAsLong("Content-Length").orElse(0L)
                    val responseLength = if (contentLength > 0) contentLength else 0L

                    exchange.sendResponseHeaders(responseCode, responseLength)

                    val output: OutputStream = exchange.responseBody
                    val inputStream: InputStream = response.body()
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    try {
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    } catch (e: Exception) {
                        // Playback completed, seeked or interrupted
                    } finally {
                        try { inputStream.close() } catch (_: Exception) {}
                        try { exchange.close() } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                System.err.println("[LocalAudioProxy] Error: ${e.message}")
                try {
                    exchange.sendResponseHeaders(500, -1)
                    exchange.close()
                } catch (_: Exception) {}
            }
        }
    }
}
