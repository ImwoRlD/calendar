package cc.yy.calendar.activity

import android.app.Activity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import cc.yy.calendar.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.util.*

class MainActivity : BaseActivity() {
    override fun getLayout(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCalendarView()
    }

    private fun initCalendarView() {
        calendarView.state().edit()
                .setFirstDayOfWeek(Calendar.WEDNESDAY)
                .setMinimumDate(CalendarDay.from(2016, 4, 3))
                .setMaximumDate(CalendarDay.from(2020, 5, 12))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
        calendarView.currentDate = CalendarDay.from(Calendar.getInstance())
        calendarView.selectedDate = CalendarDay.from(Calendar.getInstance())
        calendarView.setOnDateChangedListener { _, date, selected ->
            logUtil(DateFormat.format("yyyy-MM-dd HH:mm:ss", date.date).toString())
            if (selected) startActivity<EditNoteActivity>("date" to date.date)
        }
    }
}

fun Activity.logUtil(error: String) {
    Log.e(localClassName, error)
}
