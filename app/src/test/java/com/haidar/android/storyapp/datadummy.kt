package com.haidar.android.storyapp

import com.haidar.android.storyapp.data.model.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Android_logo_2019.png/599px-Android_logo_2019.png",
                createdAt = "2022-01-01T00:00:00Z",
                name = "test",
                description = "testing",
                lat = 0.0,
                lon = 0.0,
                id = i.toString()
            )
            items.add(story)
        }
        return items
    }
}