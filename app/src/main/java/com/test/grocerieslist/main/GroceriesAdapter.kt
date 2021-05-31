package com.test.grocerieslist.main

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.test.grocerieslist.databinding.ItemGroceryBinding
import com.test.grocerieslist.data.model.GroceryItem

class GroceriesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<GroceryItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroceryItemViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as GroceryItemViewHolder
        val item = data[position]
        ImageViewCompat.setImageTintMode(viewHolder.binding.circleImageView, PorterDuff.Mode.SRC_OUT)
        ImageViewCompat.setImageTintList(viewHolder.binding.circleImageView, ColorStateList.valueOf(item.bagColor.toColorInt()))
        viewHolder.binding.title.text = item.name
        viewHolder.binding.weight.text = item.weight
    }

    fun swap(data: List<GroceryItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    class GroceryItemViewHolder(val binding: ItemGroceryBinding) : RecyclerView.ViewHolder(binding.root)
}
