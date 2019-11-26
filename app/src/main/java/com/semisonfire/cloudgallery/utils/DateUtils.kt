package com.semisonfire.cloudgallery.utils

import android.text.format.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
  const val ONLY_DATE_FORMAT = "dd.MM.yyyy"
  const val DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

  val currentDate: String
    get() {
      val millis = System.currentTimeMillis()
      return getDateString(Date(millis), DEFAULT_FORMAT)
    }

  fun getDateString(date: String?, format: String?): String? {
    try {
      val simpleDateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault())
      simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")

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