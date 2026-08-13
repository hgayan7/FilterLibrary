package com.uvstudio.him.photofilterlibrary

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uvstudio.him.photofilterlibrary.sample.databinding.ItemFilterThumbnailBinding

class FilterAdapter(
    private val onItemClick: (FilterType) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val items = mutableListOf<FilterThumbnailItem>()
    private var selectedIndex = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<FilterThumbnailItem>, selectedFilter: FilterType) {
        items.clear()
        items.addAll(newItems)
        selectedIndex = items.indexOfFirst { it.filterType == selectedFilter }.coerceAtLeast(0)
        items.forEachIndexed { index, item ->
            item.isSelected = (index == selectedIndex)
        }
        notifyDataSetChanged()
    }

    fun getSelectedItem(): FilterType? = items.getOrNull(selectedIndex)?.filterType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterThumbnailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(items[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class FilterViewHolder(private val binding: ItemFilterThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilterThumbnailItem, isSelected: Boolean) {
            binding.txtFilterName.text = item.filterType.displayName
            if (item.thumbnailBitmap != null) {
                binding.imgThumbnail.setImageBitmap(item.thumbnailBitmap)
            }

            if (isSelected) {
                binding.cardFilter.strokeWidth = 6
                binding.cardFilter.setCardBackgroundColor(0x1F6200EE)
            } else {
                binding.cardFilter.strokeWidth = 0
                binding.cardFilter.setCardBackgroundColor(Color.TRANSPARENT)
            }

            binding.root.setOnClickListener {
                val previousIndex = selectedIndex
                selectedIndex = bindingAdapterPosition
                if (previousIndex != RecyclerView.NO_POSITION && previousIndex < items.size) {
                    items[previousIndex].isSelected = false
                    notifyItemChanged(previousIndex)
                }
                if (selectedIndex != RecyclerView.NO_POSITION && selectedIndex < items.size) {
                    items[selectedIndex].isSelected = true
                    notifyItemChanged(selectedIndex)
                    onItemClick(item.filterType)
                }
            }
        }
    }
}
