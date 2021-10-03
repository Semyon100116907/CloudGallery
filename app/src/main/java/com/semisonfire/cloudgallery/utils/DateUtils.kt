package com.semisonfire.cloudgallery.utils

import android.text.format.DateFormat
import com.semisonfire.cloudgallery.logger.printThrowable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DATE_FORMAT = "dd.MM.yyyy"
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)
    private val dateTimeFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.ROOT)

    val currentDate: String
        get() {
            return millisToDateTime(System.currentTimeMillis())
        }

    fun dateString(date: String): String {
        return dateString(dateTimeFormat.parse(date), DATE_FORMAT)
    }

    fun dateTimeMillis(date: String?): Long {
        try {
            return dateTimeFormat.parse(date.orEmpty())?.time ?: -1L
        } catch (e: ParseException) {
            e.printThrowable()
        }

        return -1L
    }

    fun dateMillis(date: String?): Long {
        try {
            return dateFormat.parse(date.orEmpty())?.time ?: -1L
        } catch (e: ParseException) {
            e.printThrowable()
        }

        return -1L
    }

    fun millisToDate(millis: Long): String {
        return DateFormat.format(DATE_FORMAT, millis).toString()
    }

    fun millisToDateTime(millis: Long): String {
        return DateFormat.format(DATE_TIME_FORMAT, millis).toString()
    }

    private fun dateString(date: Date?, format: String?): String {
        return DateFormat.format(format, date).toString()
    }
}