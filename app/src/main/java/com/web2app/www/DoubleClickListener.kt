package com.web2app.www

import android.view.MotionEvent
import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}
abstract class DoubleClick2 :View.OnTouchListener{
    var lastClickTime:Long = 0
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        if(event==null)return false

        if(event.eventTime - event.downTime < 120 && event.actionMasked == MotionEvent.ACTION_UP) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA2) {
                onDoubleClick2(p0)
            }
            lastClickTime = clickTime
        }
        return false
    }
    abstract fun onDoubleClick2(v: View?)
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA2: Long = 320 //milliseconds
    }
}