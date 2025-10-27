package com.mycollege.schedule.app.activity.data.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "schedule",
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["teacher_id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["group_id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
class Schedule {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "schedule_id")
    var id: Long = 0

    // связь с преподавателем
    @ColumnInfo(name = "teacher_id", defaultValue = "NULL")
    var teacher: Long?

    // связь с группой
    @ColumnInfo(name = "group_id")
    var group: Long = 0

    @ColumnInfo(name = "day_week")
    var dayWeek: String = ""

    // четность недели
    @ColumnInfo(name = "week_count")
    var weekCount: Int = 1

    // номер пары по счету
    @ColumnInfo(name = "lesson_count")
    var lessonCount: Int = 1

    // время 08:00 - 09:30
    @ColumnInfo(name = "time")
    var time: String = ""

    // название дисциплины
    @ColumnInfo(name = "name")
    var name: String = ""

    // тип пары: Практика, Лекция
    @ColumnInfo(name = "type")
    var type: String = ""

    // ссылка на eios
    @ColumnInfo(name = "eios")
    var eios: String = ""

    // номер аудитории
    @ColumnInfo(name = "location")
    var location: String = ""

    constructor(
        teacher: Long?,
        group: Long,
        dayWeek: String,
        weekCount: Int,
        lessonCount: Int,
        time: String,
        name: String,
        type: String,
        eios: String,
        location: String
    ) {
        this.teacher = teacher
        this.group = group
        this.dayWeek = dayWeek
        this.weekCount = weekCount
        this.lessonCount = lessonCount
        this.time = time
        this.name = name
        this.type = type
        this.eios = eios
        this.location = location
    }

}