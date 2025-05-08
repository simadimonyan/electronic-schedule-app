package com.mycollege.schedule.core.db.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "teachers")
class Teacher {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "teacher_id")
    var id: Long = 0

    @ColumnInfo(name = "name")
    var name: String = ""

    constructor(name: String) {
        this.name = name
    }

}

