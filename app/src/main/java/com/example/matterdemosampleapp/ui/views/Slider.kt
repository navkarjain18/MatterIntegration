/*
 * Copyright 2023 DigiValet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.matterdemosampleapp.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.matterdemosampleapp.R
import com.example.matterdemosampleapp.utils.ktx.px

class Slider(context: Context, attrs: AttributeSet) : View(context, attrs) {
    /**
     * colors
     */
    private var colorEmpty = context.getColor(R.color.slider_color_empty)
    private var colorFill = context.getColor(R.color.slider_color_fill)
    private var colorThumb = context.getColor(R.color.slider_thumb_color)

    /**
     * paints
     */
    private val paintFill: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colorFill
    }

    private val paintEmpty: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colorEmpty
    }

    private val paintThumb: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colorThumb
    }

    /**
     * progress
     */
    private var minProgress: Int = 0
    private var currentProgress: Int = 50
    private var maxProgress: Int = 100

    private var splitValue = 10000

    /**
     * dimensions
     */
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var cornerRadius: Float = 8.px.toFloat()

    /**
     * flags
     */
    private var isTouchEnabled: Boolean = true
    private var isDecimalSupported: Boolean = false
    private var decimalValue: Int = 10

    /**
     * sizes of borders and thumb
     */
    private var edge: Int = 3

    /**
     * listener
     */
    private var progressListener: ProgressListener? = null

    init {
        attrs.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SliderView, 0, 0)
            colorEmpty = ContextCompat.getColor(
                context,
                typedArray.getInt(R.styleable.SliderView_emptyColor, R.color.slider_color_empty)
            )
            colorFill = ContextCompat.getColor(
                context,
                typedArray.getInt(R.styleable.SliderView_progressColor, R.color.slider_color_fill)
            )
            colorThumb = ContextCompat.getColor(
                context,
                typedArray.getInt(R.styleable.SliderView_thumbColor, R.color.slider_thumb_color)
            )
            maxProgress =
                typedArray.getInt(R.styleable.SliderView_maxProgress, maxProgress) * splitValue

            typedArray.recycle()
        }
        currentProgress = 0
        cornerRadius = 8.px.toFloat()
        isTouchEnabled = true
        paintEmpty.color = colorEmpty
        paintFill.color = colorFill
        paintThumb.color = colorThumb
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //calculates horizontal position in pixel
        val positionProgress =
            viewWidth.toFloat() * this.currentProgress.toFloat() / this.maxProgress.toFloat()

        //draws empty progress
        drawEmptyProgress(canvas)

        //draws fill progress
        drawFillProgress(canvas, positionProgress)

        //draws thumb
        drawThumb(canvas, positionProgress)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (isTouchEnabled) {
                this.currentProgress = calculateCurrentProgress(x)
                val progress = this.currentProgress / splitValue
                progressListener?.beforeProgressChange(
                    if (isDecimalSupported) progress - (progress % decimalValue) else progress
                )
                parent.requestDisallowInterceptTouchEvent(true)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                this.currentProgress = calculateCurrentProgress(x)
                val progress = this.currentProgress / splitValue
                progressListener?.onProgressChange(
                    if (isDecimalSupported) progress - (progress % decimalValue) else progress
                )
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
                val progress = this.currentProgress / splitValue
                progressListener?.afterProgressChange(
                    if (isDecimalSupported) progress - (progress % decimalValue) else progress
                )
                invalidate()
            }
        }

        return true
    }

    private fun drawEmptyProgress(canvas: Canvas) {
        val path = Path()
        path.addRoundRect(
            RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat()),
            cornerRadius,
            cornerRadius,
            Path.Direction.CCW
        )
        canvas.clipPath(path)
        canvas.drawRect(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat(), paintEmpty)
    }

    private fun drawFillProgress(canvas: Canvas, positionProgress: Float) {
        canvas.drawRoundRect(
            RectF(
                edge.px.toFloat(),
                edge.px.toFloat(),
                (positionProgress + (4 * edge).px.toFloat() - ((5 * edge * currentProgress) / maxProgress).px.toFloat()),
                viewHeight.toFloat() - edge.px.toFloat()
            ), cornerRadius * 3 / 4, cornerRadius * 3 / 4, paintFill
        )
    }

    private fun drawThumb(canvas: Canvas, positionProgress: Float) {
        canvas.drawRoundRect(
            RectF(
                positionProgress + (2 * edge).px.toFloat() - ((5 * edge * currentProgress) / maxProgress).px.toFloat(),
                (canvas.height / 2) - (3 * edge).px.toFloat(),
                positionProgress + (3 * edge).px.toFloat() - ((5 * edge * currentProgress) / maxProgress).px.toFloat(),
                (canvas.height / 2) + (3 * edge).px.toFloat()
            ), cornerRadius, cornerRadius, paintThumb
        )
    }

    /**
     * calculates current progress
     */
    private fun calculateCurrentProgress(x: Float): Int {
        return (this.maxProgress * x / viewWidth).toInt().coerceIn(0, maxProgress)
    }

    fun setProgressListener(progressListener: ProgressListener) {
        this.progressListener = progressListener
    }

    fun setCurrentProgress(currentProgress: Int) {
        this.currentProgress =
            ((currentProgress - this.minProgress) * splitValue).coerceIn(0, this.maxProgress)
        invalidate()
    }

    fun getCurrentProgress(): Int {
        return this.currentProgress
    }

    fun setDecimal(isDecimalSupported: Boolean, decimalValue: Int = 5) {
        this.isDecimalSupported = isDecimalSupported
        this.decimalValue = decimalValue.coerceAtLeast(1)
    }

    fun getDecimalSupport(): Boolean {
        return this.isDecimalSupported
    }

    fun getDecimalValue(): Int {
        return this.decimalValue
    }

    fun setMinProgress(minProgress: Int) {
        this.minProgress = minProgress
    }

    fun getMinProgress(): Int {
        return this.minProgress
    }

    fun setMaxProgress(maxProgress: Int) {
        this.maxProgress = (maxProgress - this.minProgress) * splitValue
    }

    fun getMaxProgress(): Int {
        return this.maxProgress
    }

    fun setCornerRadius(cornerRadius: Float) {
        this.cornerRadius = cornerRadius
        postInvalidate()
    }

}

interface ProgressListener {
    fun beforeProgressChange(progress: Int)
    fun onProgressChange(progress: Int)
    fun afterProgressChange(progress: Int)
}