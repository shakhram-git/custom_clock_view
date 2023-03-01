package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.lang.Math.PI
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class CustomAnalogClock @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attr, defStyleAttr) {
    private var mHeight = 0
    private var mWidth = 0

    private var widthCenter = 0
    private var heightCenter = 0
    private var circleRadius = 0f
    private val fontSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)

    private val clockHours = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var clockHoursPositions = emptyList<Pair<Float, Float>>()

    private var padding = 0
    private val numeralSpacing = 0
    private var handTruncation = 0
    private var mHourHandTruncation = 0

    private var radius = 0
    private val paint: Paint = Paint()
    private val rect: Rect = Rect()

    private val initTime: Long = -21600000

    private var timeState = TimeState(initTime, false)

    private val calendar: Calendar = Calendar.getInstance()

    private var listeners = mutableListOf<(TimeState) -> Unit>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = height
        mWidth = width
        widthCenter = mWidth / 2
        heightCenter = mHeight / 2
        padding = numeralSpacing + 50 // spacing from the circle border
        val minAttr = min(mHeight, mWidth)
        radius = minAttr / 2 - padding
        circleRadius = (radius + padding - 10).toFloat()

        // for maintaining different heights among the clock-hands
        handTruncation = minAttr / 20
        mHourHandTruncation = minAttr / 17

        clockHoursPositions = clockHours.map { hour ->
            val angle = PI / 6 * (hour - 3)
            val x = (mWidth / 2 + cos(angle) * radius).toFloat()
            val y = (mHeight / 2 + sin(angle) * radius).toFloat()
            Pair(x, y)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8F
        paint.isAntiAlias = true
        canvas?.drawCircle(
            widthCenter.toFloat(),
            heightCenter.toFloat(), circleRadius, paint
        )
        /** clock-center */
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(
            widthCenter.toFloat(),
            heightCenter.toFloat(),
            12F,
            paint
        )
        paint.textSize = fontSize

        clockHours.forEachIndexed { index, hour ->
            val tmp = hour.toString()
            paint.getTextBounds(tmp, 0, tmp.length, rect) // for circle-wise bounding
            val position = clockHoursPositions[index]
            canvas!!.drawText(
                hour.toString(),
                position.first - rect.width() / 2,
                position.second + rect.height() / 2,
                paint
            )
        }

        calendar.timeInMillis = timeState.time
        drawHandLine(
            canvas!!,
            ((calendar[Calendar.HOUR] + (calendar[Calendar.MINUTE] / 60.0)) * 5),
            isHour = true,
            isSecond = false
        ) // draw hour
        drawHandLine(
            canvas,
            calendar[Calendar.MINUTE].toDouble(),
            isHour = false,
            isSecond = false
        ) // draw minutes
        drawHandLine(
            canvas,
            calendar[Calendar.SECOND].toDouble(),
            isHour = false,
            isSecond = true
        ) // draw seconds
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        listeners.forEach { it.invoke(timeState) }
        runTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listeners.removeAll(listeners)
    }

    private fun drawHandLine(canvas: Canvas, moment: Double, isHour: Boolean, isSecond: Boolean) {
        val angle = PI * moment / 30 - PI / 2
        val handRadius =
            if (isHour) radius - handTruncation - mHourHandTruncation else radius - handTruncation
        if (isSecond) paint.color = Color.GRAY
        canvas.drawLine(
            (mWidth / 2).toFloat(), (mHeight / 2).toFloat(),
            (mWidth / 2 + cos(angle) * handRadius).toFloat(),
            (mHeight / 2 + sin(angle) * handRadius).toFloat(), paint
        )
    }

    fun start() {
        timeState.isPlayed = true
        listeners.forEach { it.invoke(timeState) }
    }

    fun stop() {
        timeState.isPlayed = false
        listeners.forEach { it.invoke(timeState) }
    }

    fun reset() {
        timeState.isPlayed = false
        timeState.time = initTime
        listeners.forEach { it.invoke(timeState) }
        invalidate()
    }


    fun addUpdateListener(listener: (TimeState) -> Unit) {
        listeners.add(listener)
    }

    private fun runTimer() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (timeState.isPlayed) {
                    timeState.time += 1000
                    listeners.forEach {
                        it.invoke(timeState)
                    }
                    invalidate()
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

}