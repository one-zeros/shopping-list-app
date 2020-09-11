package `in`.onenzeros.shoppinglist.utils

import java.text.SimpleDateFormat
import java.util.*

class Utility  {
    companion object {
        private const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss"

        fun getDate(milliSeconds: Long): String? {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(DATETIME_FORMAT, Locale.US)

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }
    }
}