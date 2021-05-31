package com.test.grocerieslist.websocket

import com.test.grocerieslist.websocket.WebSocketManager.Companion.NORMAL_CLOSURE_STATUS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@ExperimentalCoroutinesApi
class WebSocketListener : WebSocketListener() {

    val socketEventChannel: Channel<SocketUpdate> = Channel(10)

    override fun onOpen(webSocket: WebSocket, response: Response) {}

    override fun onMessage(webSocket: WebSocket, text: String) {
        GlobalScope.launch {
            if (!socketEventChannel.isClosedForSend) {
                socketEventChannel.send(SocketUpdate(text))
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        GlobalScope.launch {
            if (!socketEventChannel.isClosedForSend) {
                socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
            }
        }
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        socketEventChannel.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        GlobalScope.launch {
            if (!socketEventChannel.isClosedForSend) {
                socketEventChannel.send(SocketUpdate(exception = t))
            }
        }
    }
}