package me.linh.remote

import me.linh.domain.Announcement
import me.linh.domain.Schedule

data class AddScheduleRequest(
    val name: String,
    val type: String,
    val timestamp: Long,
    val isCancelled: Boolean,
    val roomName: String,
    val teacherName: String
) {
    fun toSchedule() = Schedule(0, name, type, timestamp, isCancelled, roomName, teacherName)
}
