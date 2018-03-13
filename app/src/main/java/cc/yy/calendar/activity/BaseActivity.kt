package cc.yy.calendar.activity

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import cc.yy.calendar.App
import kotlin.properties.Delegates

/**
 * Created by zpy on 2018/3/13.
 */
abstract class BaseActivity : AppCompatActivity() {
    @LayoutRes
    abstract fun getLayout(): Int

    var app: App by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
        app = App.instance
    }
}