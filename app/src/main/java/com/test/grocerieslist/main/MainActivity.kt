package com.test.grocerieslist.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.test.grocerieslist.R
import com.test.grocerieslist.data.repositories.MainRepository
import com.test.grocerieslist.databinding.ActivityMainBinding
import com.test.grocerieslist.websocket.WebSocketManager
import kotlinx.coroutines.ExperimentalCoroutinesApi


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuItem = binding.toolbar.menu.add(Menu.NONE, startMenuItemId, Menu.NONE, getString(R.string.action_start))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            startMenuItemId -> {
                binding.toolbar.menu.removeItem(startMenuItemId)
                val menuItem = binding.toolbar.menu.add(Menu.NONE, stopMenuItemId, Menu.NONE, R.string.action_stop)
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                viewModel.start()
            }
            stopMenuItemId -> {
                binding.toolbar.menu.removeItem(stopMenuItemId)
                val menuItem = binding.toolbar.menu.add(Menu.NONE, startMenuItemId, Menu.NONE, R.string.action_start)
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                viewModel.stop()
            }
        }
        return true
    }

    companion object {
        const val startMenuItemId = 101
        const val stopMenuItemId = 102
    }
}