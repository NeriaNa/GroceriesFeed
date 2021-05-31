package com.test.grocerieslist.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.test.grocerieslist.utilities.SingleLiveEvent
import com.test.grocerieslist.data.model.GroceryItem
import com.test.grocerieslist.utilities.notifyObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@ExperimentalCoroutinesApi
class MainViewModel constructor(private val interactor: MainInteractor): ViewModel() {

    //TODO: Inject view model dependencies
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val groceryItemJsonAdapter = moshi.adapter(GroceryItem::class.java)

    companion object {
        private const val STATUS_STARTED = 0
        private const val STATUS_STOPPED = 1

        class MainViewModelFactory(private val someString: MainInteractor): ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = MainViewModel(someString) as T
        }
    }

    private var status = STATUS_STOPPED
    private val groceryItemsMutex = Mutex()
    val groceryItemsLiveData: MutableLiveData<MutableList<GroceryItem>> = SingleLiveEvent()

    init {
        groceryItemsLiveData.value = mutableListOf()
        subscribeToSocketEvents()
    }

    fun start() {
        viewModelScope.launch {
            groceryItemsMutex.withLock { groceryItemsLiveData.notifyObserver() }
            this@MainViewModel.status = STATUS_STARTED
        }
    }

    fun stop() {
        this@MainViewModel.status = STATUS_STOPPED
    }

    private fun subscribeToSocketEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.startSocket().consumeEach {
                    when {
                        it.exception != null -> onSocketError(it.exception.message)
                        it.text == null -> return@consumeEach //Ignore items with an empty text
                        else -> {
                            val groceryItem: GroceryItem = groceryItemJsonAdapter.fromJson(it.text)!!
                            groceryItemsMutex.withLock {
                                groceryItemsLiveData.value!!.add(groceryItem)
                                if (status == STATUS_STARTED) {
                                    groceryItemsLiveData.notifyObserver()
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                onSocketError(ex.message)
            }
        }
    }

    private fun onSocketError(message: String?) {
        Log.e("Groceries Feed", "Error while fetching groceries: $message")
    }

    override fun onCleared() {
        interactor.stopSocket()
        super.onCleared()
    }

}