package com.haidar.android.storyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.databinding.ItemStoryBinding
import com.haidar.android.storyapp.dateFormat

class StoryAdapter(
    private val listStory: List<ListStoryItem>
) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    private lateinit var onItemClickCallBack: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallBack = onItemClickCallback
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listStory.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listStory[position], onItemClickCallBack)
    }
    class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem, clickCallback: OnItemClickCallback) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(ivItemPhoto)

                tvItemName.text = story.name
                tvStoryDate.text = story.createdAt.dateFormat()
                tvStoryDesc.text = story.description
                cvStory.setOnClickListener{
                    clickCallback.onItemClicked(story, arrayOf(
                        Pair(ivItemPhoto, "sharedPhoto"),
                        Pair(tvItemName, "sharedName"),
                        Pair(tvStoryDate, "sharedDate"),
                        Pair(tvStoryDesc, "sharedDesc")
                    ))
                }

            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem, sharedViews: Array<Pair<View, String>>)
    }
}