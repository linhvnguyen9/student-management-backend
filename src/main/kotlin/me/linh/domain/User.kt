package me.linh.domain

data class User(
    val uid: String,
    val email: String?,
    val photoUrl: String?,
    val isAdmin : Boolean = false
)