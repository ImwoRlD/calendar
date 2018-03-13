package cc.yy.calendar.activity

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import calendar.yy.cc.greendaolib.bean.Note
import calendar.yy.cc.greendaolib.bean.NoteDao
import cc.yy.calendar.R
import kotlinx.android.synthetic.main.activity_edit_note.*
import org.jetbrains.anko.imageResource
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by zpy on 2018/3/13.
 */
class EditNoteActivity : BaseActivity() {
    override fun getLayout(): Int = R.layout.activity_edit_note
    private var date: Date by Delegates.notNull()
    private var isFinish = false
    var keyboardShowChangeListener = KeyboardShowChangeListener()
    private var globalListener by Delegates.notNull<ViewTreeObserver.OnGlobalLayoutListener>()
    private val minKeyboardHeightPx = 150
    var decorView: View? = null
    private var note: Note? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
        date = intent.getSerializableExtra("date") as Date
        iv_time.text = DateFormat.format("yyyy-MM-dd", date).toString()
        val loadAll = app.noteDao.loadAll()
        loadAll.forEach {
            logUtil(it.toString())
        }
        note = app.noteDao.queryBuilder().where(NoteDao.Properties.Date.eq(date)).unique()
        if (null == note) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            inputStatus()
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            finishStatus()
            et_note_content.setText(note!!.text.toString())
        }
        decorView = window.decorView
        globalListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private val windowVisibleDisplayFrame = Rect()
            private var lastVisibleDecorViewHeight: Int = 0

            override fun onGlobalLayout() {
                decorView?.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
                val visibleDecorViewHeight = windowVisibleDisplayFrame.height()
                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + minKeyboardHeightPx) {
                        keyboardShowChangeListener.keyboardShow()
                    } else if (lastVisibleDecorViewHeight + minKeyboardHeightPx < visibleDecorViewHeight) {
                        keyboardShowChangeListener.keyboardHidden()
                    }
                }
                lastVisibleDecorViewHeight = visibleDecorViewHeight
            }
        }
        decorView?.viewTreeObserver?.addOnGlobalLayoutListener(globalListener)
    }

    private fun initListener() {
        iv_back.setOnClickListener {
            saveNote()
            this@EditNoteActivity.finish()
        }
        iv_submit_or_delete.setOnClickListener {
            if (isFinish) {
                //删除
                logUtil("删除了")
                deleteNote()
                this@EditNoteActivity.finish()
            } else {
                finishStatus()
                //保存
                saveNote()
            }
        }
    }

    private fun saveNote() {
        if (null == note) {
            note = Note()
            note!!.text = et_note_content.text.toString()
            note!!.date = date
            val id = app.noteDao.insert(note)
            note!!.id = id
        } else {
            note!!.text = et_note_content.text.toString()
            app.noteDao.update(note)
        }
    }

    private fun deleteNote() {
        note?.let {
            app.noteDao.deleteByKey(it.id)
        }
    }

    inner class KeyboardShowChangeListener : IKeyboardShowChangeListener {
        override fun keyboardShow() {
            inputStatus()
        }

        override fun keyboardHidden() {
            finishStatus()
        }
    }

    fun inputStatus() {
        isFinish = false
        iv_submit_or_delete.imageResource = R.drawable.ic_edit_submit
    }

    fun finishStatus() {
        isFinish = true
        iv_submit_or_delete.imageResource = R.drawable.ic_memo_delete
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val v = currentFocus
        if (v != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        decorView?.viewTreeObserver?.removeGlobalOnLayoutListener(globalListener)
    }
}

interface IKeyboardShowChangeListener {
    fun keyboardShow()
    fun keyboardHidden()
}