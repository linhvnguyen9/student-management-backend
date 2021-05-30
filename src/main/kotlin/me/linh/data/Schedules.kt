package me.linh.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Schedules : IntIdTable() {
    val name = varchar("name", 255)
    val type = varchar("type", 255)
    val timestamp = datetime("timestamp")
    val isCancelled = bool("is_cancelled")
    val roomName = varchar("room_name", 255)
    val teacherName = varchar("teacherName", 255)
}