package com.cs407.finalproject

data class Post(
    val id: Int,
    val userId: Int,
    val imageUri: String,
    val caption: String?,
    var likes: Int,
    val timestamp: String,
    val latitude: Double?,
    val longitude: Double?,
    val isShenanigan: Boolean = false
) {
    override fun toString(): String {
        return "Post(id=$id, imageUri=$imageUri, caption=$caption, timestamp=$timestamp)"
    }
}