package cc.yy.calendar

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import calendar.yy.cc.greendaolib.bean.DaoMaster
import calendar.yy.cc.greendaolib.bean.NoteDao
import calendar.yy.cc.greendaolib.helper.GreenDaoHelper
import kotlin.properties.Delegates

/**
 * Created by zpy on 2018/3/13.
 */
class App : Application() {
    var noteDao by Delegates.notNull<NoteDao>()
    override fun onCreate() {
        super.onCreate()
        instance = this
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