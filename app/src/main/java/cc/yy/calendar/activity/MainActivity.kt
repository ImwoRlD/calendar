package cc.yy.calendar.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import cc.yy.calendar.R
import cc.yy.calendar.util.Constant
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.startActivity
import java.util.*

class MainActivity : BaseActivity() {
    override fun getLayout(): Int = R.layout.activity_main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initCalendarView()
        initListener()
        val intentFilterSetBackground = IntentFilter(Constant.SP_ACTION_SET_BACKGROUND)
        registerReceiver(broadcastReceiverChangeBackground, intentFilterSetBackground)
        val path = app.getSpValue(Constant.SP_PATH_FOR_BACKGROUND, "")
        if (!TextUtils.isEmpty(path)) {
            val drawable = BitmapDrawable.createFromPath(path)
            calendarView.backgroundDrawable = drawable
        }

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

    private fun initListener() {
        iv_setting.setOnClickListener {
            startActivity<SettingActivity>()
        }
    }

    private val broadcastReceiverChangeBackground = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val path = intent.getStringExtra("path")
            val drawable = BitmapDrawable.createFromPath(path)
            calendarView.backgroundDrawable = drawable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiverChangeBackground)
    }
}

fun Activity.logUtil(error: String) {
    Log.e(localClassName, error)
}
