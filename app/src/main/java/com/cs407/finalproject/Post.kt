package com.cs407.finalproject

data class Post(
    val id: Int,
    val userId: Int,
    val imageUri: String,
    val caption: String?,
    var likes: Int,
    val timestamp: String
)