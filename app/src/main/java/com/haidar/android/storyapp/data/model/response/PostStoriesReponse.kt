package com.haidar.android.storyapp.data.model.response

import com.google.gson.annotations.SerializedName

data class PostStoriesResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)
