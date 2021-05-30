package me.linh.data

import me.linh.domain.Announcement
import me.linh.domain.Schedule
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.ZoneOffset

class ScheduleLocal(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ScheduleLocal>(Schedules)

    var name by Schedules.name
    var type by Schedules.type
    var timestamp by Schedules.timestamp
    var isCancelled by Schedules.isCancelled
    var roomName by Schedules.roomName
    var teacherName by Schedules.teacherName

    fun toDomain() = Schedule(
        id.value,
        name,
        type,
        timestamp.toInstant(ZoneOffset.UTC).toEpochMilli(),
        isCancelled,
        roomName,
        teacherName
    )
}