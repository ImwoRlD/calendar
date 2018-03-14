package cc.yy.calendar

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import calendar.yy.cc.greendaolib.bean.DaoMaster
import calendar.yy.cc.greendaolib.bean.NoteDao
import calendar.yy.cc.greendaolib.helper.GreenDaoHelper
import kotlin.properties.Delegates

/**
 * Created by zpy on 2018/3/13.
 */
class App : Application() {
    var noteDao by Delegates.notNull<NoteDao>()
    var sp: SharedPreferences by Delegates.notNull()

    override fun onCreate() {
        super.onCreate()
        instance = this
        sp = getSharedPreferences("memo-data", MODE_PRIVATE)
        //创建数据库
        initGreenDao()
    }

    private fun initGreenDao() {
        if (isMainProcess(applicationContext)) {
            val helper = GreenDaoHelper(this, "calendar-db", DaoMaster.SCHEMA_VERSION)
            val db = helper.writableDb
            val daoSession = DaoMaster(db).newSession()
            noteDao = daoSession.noteDao
        }
    }

    companion object {
        var instance: App by Delegates.notNull()
            private set
    }

    @SuppressLint("CommitPrefEdits")
    fun putSpValue(name: String, value: Any) = with(sp.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("exception")
        }.apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSpValue(name: String, default: T): T = with(sp) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("exception")
        }
        res as T
    }
}

fun isMainProcess(context: Context): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val processInfo = am.runningAppProcesses
    val mainProcessName = context.packageName
    val myPid = android.os.Process.myPid()
    if (null == processInfo) {
        return false
    }
    for (info in processInfo) {
        if (info.pid == myPid && mainProcessName == info.processName) {
            return true
        }
    }
    return false
}