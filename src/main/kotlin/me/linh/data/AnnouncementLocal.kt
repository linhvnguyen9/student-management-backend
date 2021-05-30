package me.linh.data

import me.linh.domain.Announcement
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.ZoneOffset

class AnnouncementLocal(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AnnouncementLocal>(Announcements)
    var imageUrl by Announcements.imageUrl
    var title by Announcements.title
    var content by Announcements.content
    var timestamp by Announcements.timestamp

    fun toDomain() = Announcement(id.value, imageUrl, title, content, timestamp.toInstant(ZoneOffset.UTC).toEpochMilli())
}