package com.mycollege.schedule.core.network.dto.teachers

import androidx.compose.runtime.Immutable

@Immutable
data class Teachers(
    val teachers: List<Teacher>
)

@Immutable
data class Teacher(
    val id: Long,
    val label: String,
    val department: String?
)
