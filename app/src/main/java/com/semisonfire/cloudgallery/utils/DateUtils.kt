package com.semisonfire.cloudgallery.utils

import android.text.format.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
  const val DATE_FORMAT = "dd.MM.yyyy"
  const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

  val currentDate: String
    get() {
      val millis = System.currentTimeMillis()
      return getDateString(Date(millis), DATE_TIME_FORMAT)
    }

  fun getDateString(date: String?, format: String?): String? {
    try {
      val simpleDateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
      val newDate = simpleDateFormat.parse(date)
      return getDateString(newDate, format)
    } catch (e: ParseException) {
      e.printThrowable()
    }
    return null
  }

  fun getDateString(date: Date?, format: String?): String {
    return DateFormat.format(format, date).toString()
  }
}