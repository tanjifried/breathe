package com.breathe.data.remote.ws

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.math.min

sealed interface WsEvent {
  data object Connected : WsEvent
  data object Closed : WsEvent
  data class Message(val type: String?, val raw: String) : WsEvent
  data class Failure(val reason: String) : WsEvent
}

class WebSocketManager(
  private val okHttpClient: OkHttpClient
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 32)
  private val _connectionState = MutableStateFlow(false)

  private var socket: WebSocket? = null
  private var reconnectJob: Job? = null
  private var reconnectAttempt = 0
  private var lastServerUrl: String? = null
  private var lastToken: String? = null

  val events: SharedFlow<WsEvent> = _events.asSharedFlow()
  val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

  fun connect(serverUrl: String, token: String) {
    lastServerUrl = serverUrl
    lastToken = token
    openSocket(serverUrl, token)
  }

  fun disconnect() {
    reconnectJob?.cancel()
    reconnectJob = null
    socket?.close(1000, "manual_disconnect")
    socket = null
    _connectionState.value = false
  }

  private fun openSocket(serverUrl: String, token: String) {
    socket?.cancel()

    val request = Request.Builder()
      .url(buildWebSocketUrl(serverUrl, token))
      .build()

    socket = okHttpClient.newWebSocket(
      request,
      object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
          reconnectAttempt = 0
          _connectionState.value = true
          scope.launch { _events.emit(WsEvent.Connected) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
          val type = Regex("\"type\"\\s*:\\s*\"([^\"]+)\"")
            .find(text)
            ?.groupValues
            ?.getOrNull(1)

          scope.launch { _events.emit(WsEvent.Message(type = type, raw = text)) }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
          _connectionState.value = false
          scope.launch { _events.emit(WsEvent.Closed) }
          scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
          _connectionState.value = false
          scope.launch { _events.emit(WsEvent.Failure(t.message ?: "unknown_websocket_failure")) }
          scheduleReconnect()
        }
      }
    )
  }

  private fun scheduleReconnect() {
    if (reconnectJob?.isActive == true) {
      return
    }

    val serverUrl = lastServerUrl ?: return
    val token = lastToken ?: return
    val delayMs = min(1000L shl reconnectAttempt.coerceAtMost(5), 30_000L)
    reconnectAttempt += 1

    reconnectJob = scope.launch {
      delay(delayMs)
      openSocket(serverUrl, token)
    }
  }

  private fun buildWebSocketUrl(serverUrl: String, token: String): String {
    val normalized = serverUrl.trim().removeSuffix("/")
    val parsed = normalized.toHttpUrl()

    return okhttp3.HttpUrl.Builder()
      .scheme(if (parsed.isHttps) "wss" else "ws")
      .host(parsed.host)
      .port(parsed.port)
      .addPathSegment("ws")
      .addQueryParameter("token", token)
      .build()
      .toString()
  }
}
