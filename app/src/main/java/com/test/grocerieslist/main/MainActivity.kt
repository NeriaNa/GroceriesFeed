package com.test.grocerieslist.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.test.grocerieslist.R
import com.test.grocerieslist.data.repositories.MainRepository
import com.test.grocerieslist.databinding.ActivityMainBinding
import com.test.grocerieslist.websocket.WebSocketManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    //TODO: Add DI, inject dependencies
    private val mainRepository by lazy { MainRepository(WebSocketManager(getString(R.string.url_groceries))) }
    private val mainInteractor by lazy { MainInteractor(mainRepository) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Companion.MainViewModelFactory(mainInteractor)
    }

    private lateinit var binding: ActivityMainBinding
    private var groceriesAdapter = GroceriesAdapter()
    private var currentMenuItemId = startMenuItemId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.groceriesRecyclerView.adapter = groceriesAdapter
    }

    override fun onStart() {
        super.onStart()
        groceriesAdapter.swap(viewModel.groceryItemsLiveData.value!!)
        viewModel.groceryItemsLiveData.observe(this) { groceriesAdapter.swap(it) }
    }

    override fun onStop() {
        super.onStop()
        viewModel.groceryItemsLiveData.removeObservers(this)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(STATE_MENU_ID, binding.toolbar.menu[0].itemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentMenuItemId = savedInstanceState.getInt(STATE_MENU_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        setMenuItem(currentMenuItemId)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            startMenuItemId -> {
                setMenuItem(stopMenuItemId)
                viewModel.start()
            }
            stopMenuItemId -> {
                setMenuItem(startMenuItemId)
                viewModel.stop()
            }
        }
        return true
    }

    private fun setMenuItem(itemId: Int) {
        binding.toolbar.menu.clear()
        val menuItem = when (itemId) {
            stopMenuItemId -> binding.toolbar.menu.add(Menu.NONE, stopMenuItemId, Menu.NONE, R.string.action_stop)
            startMenuItemId -> binding.toolbar.menu.add(Menu.NONE, startMenuItemId, Menu.NONE, R.string.action_start)
            else -> throw IllegalArgumentException("setMenuItem received an unexpected menu item ID")
        }
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    companion object {
        const val startMenuItemId = 101
        const val stopMenuItemId = 102
        const val STATE_MENU_ID = "menu_id"
    }
}