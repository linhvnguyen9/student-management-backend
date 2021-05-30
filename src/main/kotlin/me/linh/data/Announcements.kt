package me.linh.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Announcements : IntIdTable() {
    val imageUrl = varchar("image_url", 255)
    val title = varchar("title", 255)
    val content = text("content")
    val timestamp = datetime("timestamp")
}