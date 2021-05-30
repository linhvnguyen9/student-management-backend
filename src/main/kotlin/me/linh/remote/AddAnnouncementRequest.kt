package me.linh.remote

import me.linh.domain.Announcement

data class AddAnnouncementRequest(
    val imageUrl: String,
    val title: String,
    val content: String,
    val timestamp: Long
) {
    fun toAnnouncement() = Announcement(0, imageUrl, title, content, timestamp)
}
