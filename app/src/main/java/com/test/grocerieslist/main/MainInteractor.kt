package com.test.grocerieslist.main

import com.test.grocerieslist.data.repositories.MainRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainInteractor constructor(private val repository: MainRepository) {

    fun startSocket() = repository.startSocket()

    fun stopSocket() = repository.closeSocket()

}