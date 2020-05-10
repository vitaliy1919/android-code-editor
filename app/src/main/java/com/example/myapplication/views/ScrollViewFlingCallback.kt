package com.example.myapplication.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.widget.ScrollView


class ScrollViewFlingCallback @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : ScrollView(context, attrs, defStyle) {
    interface OnFlingListener {
        fun onFlingStarted()
        fun onFlingStopped()
    }

    var onFlingListener: OnFlingListener? = null
    private var mScrollChecker: Runnable? = null
    private var mPreviousPosition = 0
    override fun fling(velocityY: Int) {
        super.fling(velocityY)
        if (onFlingListener != null) {
            onFlingListener!!.onFlingStarted()
            post(mScrollChecker)
        }
    }

    companion object {
        private const val DELAY_MILLIS = 100
    }

    init {
        mScrollChecker = Runnable {
            val position = scrollY
            if (mPreviousPosition - position == 0) {
                onFlingListener!!.onFlingStopped()
                removeCallbacks(mScrollChecker)
            } else {
                mPreviousPosition = scrollY
                postDelayed(mScrollChecker, DELAY_MILLIS.toLong())
            }
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        Log.d("Size", "$w $h")

    }
}
