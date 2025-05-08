package com.mycollege.schedule.core.db.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "groups")
class Group {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    var id: Long = 0

    // название группы 19-СПО-ПКС-02
    @ColumnInfo(name = "name")
    var name: String = ""

    // курс
    @ColumnInfo(name = "course")
    var course: Int = 1

    // напрвление: СПО, Бакалавриат, Магистратура
    @ColumnInfo(name = "level")
    var level: String = ""

    constructor(name: String, course: Int, level: String) {
        this.name = name
        this.course = course
        this.level = level
    }

}