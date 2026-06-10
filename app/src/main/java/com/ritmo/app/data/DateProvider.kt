package com.ritmo.app.data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

interface DateProvider {
    fun today(): LocalDate
    fun nowMillis(): Long
}

class SystemDateProvider @Inject constructor() : DateProvider {
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
