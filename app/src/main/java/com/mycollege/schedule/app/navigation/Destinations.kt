package com.mycollege.schedule.app.navigation

import kotlinx.serialization.Serializable

/**
 * Screen for choosing groups, courses and specialities
 */
@Serializable
object GroupScreen

/**
 * Screen for checking out new schedule
 */
@Serializable
object ScheduleScreen

/**
 * Nested Navigation key for [GroupScreen] and [ScheduleScreen] menu
 */
@Serializable
object Start

/**
 * Welcome Onboarding Screen
 */
@Serializable
object Onboarding

/**
 * Settings Screen for [ScheduleScreen] params
 */
@Serializable
object Settings

