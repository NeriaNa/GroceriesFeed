package com.test.grocerieslist.data.repositories

import com.test.grocerieslist.websocket.WebSocketManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainRepository constructor(private val webSocketManager: WebSocketManager) {

    fun startSocket() = webSocketManager.startSocket()

    fun closeSocket() = webSocketManager.stopSocket()
}