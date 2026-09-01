package com.solunis.schedule.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    fun getCurrentWeek(startDateStr: String, maxWeek: Int = 20): Int {
        if (startDateStr.isEmpty()) return 1
        return try {
            val startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(startDate, today)
            (daysBetween / 7 + 1).toInt().coerceIn(1, maxWeek)
        } catch (e: Exception) {
            1
        }
    }

    fun getTodayDayOfWeek(): Int = LocalDate.now().dayOfWeek.value

    fun formatDate(date: LocalDate = LocalDate.now()): String =
        date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    fun getDayOfWeekChinese(dayOfWeek: Int): String {
        val names = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        return names.getOrElse(dayOfWeek - 1) { "周一" }
    }
}
