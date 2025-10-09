package com.mycollege.schedule.core.network.dto.groups

import androidx.compose.runtime.Immutable

@Immutable
data class Groups(
    val groups: List<Group>
)

@Immutable
data class Group(
    val id: Long,
    val name: String,
    val course: Int,
    val level: String
)