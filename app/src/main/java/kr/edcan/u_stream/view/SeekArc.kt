package kr.edcan.u_stream.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kr.edcan.u_stream.R

class SeekArc : View {
    private val mAngleOffset = -90

    private var mThumb: Drawable? = null

    private var mMax = 287

    private var mProgress = 0
    private var mSecondProgress = 0

    private var mProgressWidth = 4

    private var mArcWidth = 2

    private var mStartAngle = 0

    private var mSweepAngle = 360

    private var mRotation = 0

    private var mRoundedEdges = false

    private var mTouchInside = true

    private var mClockwise = true

    // Internal variables
    var isTouching = false
        internal set
    private var mArcRadius = 0
    private var mProgressSweep = 0f
    private var mSecondProgressSweep = 0f
    private val mArcRect = RectF()
    private var mDisablePaint: Paint? = null
    private var mArcPaint: Paint? = null
    private var mProgressPaint: Paint? = null
    private var mTranslateX: Int = 0
    private var mTranslateY: Int = 0
    private var mThumbXPos: Int = 0
    private var mThumbYPos: Int = 0
    private var mTouchAngle: Double = 0.toDouble()
    private var mTouchIgnoreRadius: Float = 0.toFloat()
    private var mOnSeekArcChangeListener: OnSeekArcChangeListener? = null

    interface OnSeekArcChangeListener {

        fun onProgressChanged(seekArc: SeekArc, progress: Int, fromUser: Boolean)

        fun onStartTrackingTouch(seekArc: SeekArc)

        fun onStopTrackingTouch(seekArc: SeekArc)
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, R.attr.seekArcStyle)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val res = resources
        val density = context.resources.displayMetrics.density

        // Defaults, may need to link this into theme settings
        val arcColor = ContextCompat.getColor(context, R.color.prgGray)
        var progressColor = ContextCompat.getColor(context, R.color.colorPrimary)
        var thumbHalfheight = 0
        var thumbHalfWidth = 0
        mThumb = ContextCompat.getDrawable(context, R.drawable.seek_arc_control_selector)
        // Convert progress width to pixels for current density
        mProgressWidth = (mProgressWidth * density).toInt()


        if (attrs != null) {
            // Attribute initialization
            val a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekArc, defStyle, 0)

            val thumb = a.getDrawable(R.styleable.SeekArc_thumb)
            if (thumb != null) {
                mThumb = thumb
            }
            val thumbSize = a.getDimension(R.styleable.SeekArc_thumbSize, mMax.toFloat()).toInt()
            thumbHalfheight = thumbSize.toInt() / 2
            thumbHalfWidth = thumbSize.toInt() / 2
            mThumb!!.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight)

            mMax = a.getInteger(R.styleable.SeekArc_max, mMax)
            mProgress = a.getInteger(R.styleable.SeekArc_progress, mProgress)
            mProgressWidth = a.getDimension(
                    R.styleable.SeekArc_progressWidth, mProgressWidth.toFloat()).toInt()
            mArcWidth = a.getDimension(R.styleable.SeekArc_arcWidth,
                    mArcWidth.toFloat()).toInt()
            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle)
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle)
            mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation)
            mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
                    mRoundedEdges)
            mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
                    mTouchInside)
            mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise,
                    mClockwise)

            progressColor = a.getColor(R.styleable.SeekArc_aProgressColor,
                    progressColor)

            a.recycle()
        }

        mProgress = if (mProgress > mMax) mMax else mProgress
        mProgress = if (mProgress < 0) 0 else mProgress

        mSweepAngle = if (mSweepAngle > 360) 360 else mSweepAngle
        mSweepAngle = if (mSweepAngle < 0) 0 else mSweepAngle

        mStartAngle = if (mStartAngle > 360) 0 else mStartAngle
        mStartAngle = if (mStartAngle < 0) 0 else mStartAngle

        mArcPaint = Paint()
        mArcPaint!!.color = arcColor
        mArcPaint!!.isAntiAlias = true
        mArcPaint!!.style = Paint.Style.STROKE
        mArcPaint!!.strokeWidth = mArcWidth.toFloat()
        //mArcPaint.setAlpha(45);

        mDisablePaint = Paint()
        mDisablePaint!!.color = ContextCompat.getColor(context, R.color.prgGray)
        mDisablePaint!!.isAntiAlias = true
        mDisablePaint!!.style = Paint.Style.STROKE
        mDisablePaint!!.strokeWidth = (mArcWidth / 2).toFloat()

        mProgressPaint = Paint()
        mProgressPaint!!.color = progressColor
        mProgressPaint!!.isAntiAlias = true
        mProgressPaint!!.style = Paint.Style.STROKE
        mProgressPaint!!.strokeWidth = mProgressWidth.toFloat()

        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!mClockwise) {
            canvas.scale(-1f, 1f, mArcRect.centerX(), mArcRect.centerY())
        }
        val arcStart = mStartAngle + mAngleOffset + mRotation
        val arcSweep = mSweepAngle
        canvas.drawArc(mArcRect, arcStart.toFloat(), arcSweep.toFloat(), false, mDisablePaint!!)
        canvas.drawArc(mArcRect, arcStart.toFloat(), mSecondProgressSweep, false, mArcPaint!!)
        canvas.drawArc(mArcRect, arcStart.toFloat(), mProgressSweep, false,
                mProgressPaint!!)

        canvas.translate((mTranslateX - mThumbXPos).toFloat(), (mTranslateY - mThumbYPos).toFloat())
        mThumb!!.draw(canvas)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = View.getDefaultSize(suggestedMinimumHeight,
                heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth,
                widthMeasureSpec)
        val min = Math.min(width, height)
        var top = 0f
        var left = 0f
        var arcDiameter = 0

        mTranslateX = (width * 0.5f).toInt()
        mTranslateY = (height * 0.5f).toInt()

        arcDiameter = min - paddingLeft
        mArcRadius = arcDiameter / 2
        top = (height / 2 - arcDiameter / 2).toFloat()
        left = (width / 2 - arcDiameter / 2).toFloat()
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter)

        val arcStart = mProgressSweep.toInt() + mStartAngle + mRotation + 90
        mThumbXPos = (mArcRadius * Math.cos(Math.toRadians(arcStart.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * Math.sin(Math.toRadians(arcStart.toDouble()))).toInt()

        setTouchInSide(mTouchInside)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onStartTrackingTouch()
                updateOnTouch(event)
            }
            MotionEvent.ACTION_MOVE -> updateOnTouch(event)
            MotionEvent.ACTION_UP -> {
                onStopTrackingTouch()
                isPressed = false
            }
            MotionEvent.ACTION_CANCEL -> {
                onStopTrackingTouch()
                isPressed = false
            }
        }
        return true
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (mThumb != null && mThumb!!.isStateful) {
            val state = drawableState
            mThumb!!.state = state
        }
        invalidate()
    }

    fun setSecondaryProgress(progress: Int) {
        this.mSecondProgress = progress
        this.mSecondProgressSweep = (progress + 0.1).toFloat() * mSweepAngle / 100.0f
    }

    private fun onStartTrackingTouch() {
        isTouching = true
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        isTouching = false
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStopTrackingTouch(this)
        }
    }

    private fun updateOnTouch(event: MotionEvent) {
        val ignoreTouch = ignoreTouch(event.x, event.y)
        if (ignoreTouch) {
            return
        }
        isPressed = true
        mTouchAngle = getTouchDegrees(event.x, event.y)
        val progress = getProgressForAngle(mTouchAngle)
        onProgressRefresh(progress, true)
    }

    private fun ignoreTouch(xPos: Float, yPos: Float): Boolean {
        var ignore = false
        val x = xPos - mTranslateX
        val y = yPos - mTranslateY

        val touchRadius = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true
        }
        return ignore
    }

    private fun getTouchDegrees(xPos: Float, yPos: Float): Double {
        var x = xPos - mTranslateX
        val y = yPos - mTranslateY
        x = if (mClockwise) x else -x
        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()) + Math.PI / 2 // 중심에서 부터 연계되는 연장선상의 좌표
                - Math.toRadians(mRotation.toDouble()))
        if (angle < 0) {
            angle = 360 + angle
        }
        angle -= mStartAngle.toDouble()
        return angle
    }

    private fun getProgressForAngle(angle: Double): Int {
        var touchProgress = Math.round(valuePerDegree() * angle).toInt()

        touchProgress = if (touchProgress < 0)
            INVALID_PROGRESS_VALUE
        else
            touchProgress
        touchProgress = if (touchProgress > mMax)
            INVALID_PROGRESS_VALUE
        else
            touchProgress
        return touchProgress
    }

    fun setMax(max: Int) {
        this.mMax = max
    }

    private fun valuePerDegree(): Float {
        return mMax.toFloat() / mSweepAngle
    }

    private fun onProgressRefresh(progress: Int, fromUser: Boolean) {
        updateProgress(progress, fromUser)
    }

    private fun updateThumbPosition() {
        val thumbAngle = (mStartAngle.toFloat() + mProgressSweep + mRotation.toFloat() + 90f).toInt()
        mThumbXPos = (mArcRadius * Math.cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * Math.sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
    }

    private fun updateProgress(progress: Int, fromUser: Boolean) {
        var progress = progress
        if (progress == INVALID_PROGRESS_VALUE) {
            return
        }

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!
                    .onProgressChanged(this, progress, fromUser)
        }

        progress = if (progress > mMax) mMax else progress
        progress = if (mProgress < 0) 0 else progress

        mProgress = progress
        mProgressSweep = (progress + 0.1).toFloat() / mMax * mSweepAngle
        updateThumbPosition()

        invalidate()
    }

    fun setOnSeekArcChangeListener(l: OnSeekArcChangeListener) {
        mOnSeekArcChangeListener = l
    }

    var progress: Int
        get() = mProgress
        set(progress) = updateProgress(progress, false)

    var progressWidth: Int
        get() = mProgressWidth
        set(mProgressWidth) {
            this.mProgressWidth = mProgressWidth
            mProgressPaint!!.strokeWidth = mProgressWidth.toFloat()
        }

    var arcWidth: Int
        get() = mArcWidth
        set(mArcWidth) {
            this.mArcWidth = mArcWidth
            mArcPaint!!.strokeWidth = mArcWidth.toFloat()
        }

    var arcRotation: Int
        get() = mRotation
        set(mRotation) {
            this.mRotation = mRotation
            updateThumbPosition()
        }

    var startAngle: Int
        get() = mStartAngle
        set(mStartAngle) {
            this.mStartAngle = mStartAngle
            updateThumbPosition()
        }

    var sweepAngle: Int
        get() = mSweepAngle
        set(mSweepAngle) {
            this.mSweepAngle = mSweepAngle
            updateThumbPosition()
        }

    fun setRoundedEdges(isEnabled: Boolean) {
        mRoundedEdges = isEnabled
        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        } else {
            mArcPaint!!.strokeCap = Paint.Cap.SQUARE
            mProgressPaint!!.strokeCap = Paint.Cap.SQUARE
        }
    }

    fun setTouchInSide(isEnabled: Boolean) {
        val thumbHalfheight = mThumb!!.intrinsicHeight.toInt() / 2
        val thumbHalfWidth = mThumb!!.intrinsicWidth.toInt() / 2
        mTouchInside = isEnabled
        if (mTouchInside) {
            mTouchIgnoreRadius = mArcRadius.toFloat() / 4
        } else {
            mTouchIgnoreRadius = (mArcRadius - Math.min(thumbHalfWidth, thumbHalfheight)).toFloat()
        }
    }

    fun setClockwise(isClockwise: Boolean) {
        mClockwise = isClockwise
    }

    companion object {

        private val TAG = SeekArc::class.java.simpleName
        private val INVALID_PROGRESS_VALUE = -1
    }
}

