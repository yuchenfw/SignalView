package com.yuchenfw.signalview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat

/**
 * 动态旋转到指定角度，仿万能钥匙的信号检测View，
 * 初次会转动到最大角度，然后转动到指定角度
 * Created by yuchenfw on 2018/3/19.
 * @author yuchenfw
 */
class SignalView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    companion object {
        //初始最大角度
        private const val MAX_ANGLE_DEFAULT = 270
    }

    private var mPaint: Paint = Paint()
    //最大角度
    private var mMaxAngle = 270
    //转速
    private var mAngleSpeed = 5
    //当前角度
    private var mCurrentAngle = 0
    //指针图片
    private var mSpeedPointerBitmapSource: Bitmap? = null
    //进度图片源文件
    private var mSpeedProcessBitmapSource: Bitmap? = null
    //进度图片处理后图片
    private var mSpeedProcessBitmap: Bitmap? = null
    //刻度盘图片
    private var mSpeedDialBitmap: Bitmap? = null
    //最终绘制图片的matrix
    private var mSpeedBitmapMatrix: Matrix? = null
    //图片宽度
    private var mViewWidth = 0
    //图片高度
    private var mViewHeight = 0
    //warp_content时最大宽度及高度
    private val mMaxWidth = DensityUtils.dp2px(context!!, 300f)
    private val mMaxHeight = DensityUtils.dp2px(context!!, 300f)
    //是否第一次启动
    private var firstCheck = false
    //View中心x坐标
    private var mViewCenterX = 0f
    //View中心y坐标
    private var mViewCenterY = 0f
    //View下部显示名称
    private var mTargetName = ""

    init {
        mSpeedPointerBitmapSource = BitmapFactory.decodeResource(resources, R.drawable.signal_check_pointer)
        mSpeedProcessBitmapSource = BitmapFactory.decodeResource(resources, R.drawable.signal_check_process)
        mSpeedDialBitmap = BitmapFactory.decodeResource(resources, R.drawable.signal_check_dial)
        mSpeedBitmapMatrix = Matrix()
    }

    private fun calculateDrawableMatrix() {
        mSpeedBitmapMatrix!!.reset()
        mViewCenterX = mViewWidth / 2.toFloat() + paddingLeft.toFloat()
        mViewCenterY = mViewHeight / 2.toFloat() + paddingTop.toFloat()
        //缩放倍数
        val scaleX = mViewWidth / mSpeedPointerBitmapSource!!.width.toFloat()
        val scaleY = mViewHeight / mSpeedPointerBitmapSource!!.height.toFloat()
        //缩放
        mSpeedBitmapMatrix!!.postScale(scaleX, scaleY)
        //平移
        mSpeedBitmapMatrix!!.postTranslate(paddingLeft.toFloat(), paddingTop.toFloat())
        //绕中心旋转
        mSpeedBitmapMatrix!!.postRotate(mCurrentAngle.toFloat(), mViewCenterX, mViewCenterY)

        //外层
        val matrix = Matrix()
        matrix.postScale(mViewWidth.toFloat() / mSpeedProcessBitmapSource!!.width, mViewHeight.toFloat() / mSpeedProcessBitmapSource!!.height.toFloat())
        mSpeedProcessBitmap = Bitmap.createBitmap(mSpeedProcessBitmapSource!!, 0, 0, mSpeedProcessBitmapSource!!.width, mSpeedProcessBitmapSource!!.height, matrix, true)
        //刻度盘
        mSpeedDialBitmap = Bitmap.createScaledBitmap(mSpeedDialBitmap, mViewWidth, mViewHeight, false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mViewWidth = width - paddingLeft - paddingRight
        mViewHeight = height - paddingTop - paddingBottom
        calculateDrawableMatrix()
        mPaint.reset()
        mPaint.isAntiAlias = true
        mPaint.color = ContextCompat.getColor(context, android.R.color.white)
        //绘制刻度盘
        canvas?.drawBitmap(mSpeedDialBitmap, paddingLeft.toFloat(), paddingTop.toFloat(), null)

        //绘制圆环
        canvas?.drawBitmap(mSpeedProcessBitmap, paddingLeft.toFloat(), paddingTop.toFloat(), null)
        //绘制指针
        canvas?.drawBitmap(mSpeedPointerBitmapSource, mSpeedBitmapMatrix, null)
        //绘制中间指示数值
        mPaint.textSize = DensityUtils.sp2px(context!!, 20f).toFloat()
        mPaint.textAlign = Paint.Align.CENTER
        canvas?.drawText("${(mCurrentAngle / MAX_ANGLE_DEFAULT.toFloat() * 100).toInt()}%", mViewCenterX, mViewCenterY + DensityUtils.sp2px(context!!, 6f).toFloat(), mPaint)
        mPaint.color = ContextCompat.getColor(context, android.R.color.darker_gray)
        //绘制底部名称
        canvas?.drawText(mTargetName, mViewCenterX, height - paddingBottom - DensityUtils.sp2px(context!!, 20f).toFloat(), mPaint)
        //设置遮挡圆环的参数
        mPaint.style = Paint.Style.STROKE
        mPaint.alpha = 240
        //环形的宽度
        mPaint.strokeWidth = DensityUtils.dp2px(context, 15f).toFloat()
        val x = mViewWidth / 2f - DensityUtils.dp2px(context, 41.5f)//41.5dp是图片边缘距离圆环处的距离
        val y = mViewHeight / 2f - DensityUtils.dp2px(context, 41.5f)
        //绘制圆环遮挡层
        val rectF = RectF(mViewCenterX - x, mViewCenterY - y, mViewCenterY + x, mViewCenterY + y)
        canvas?.drawArc(rectF, 135f + mCurrentAngle, MAX_ANGLE_DEFAULT - mCurrentAngle.toFloat(), false, mPaint)
        //初次会转动到最大角度后，返回到指定角度
        if (!firstCheck) {
            mCurrentAngle += mAngleSpeed
            if (mCurrentAngle >= MAX_ANGLE_DEFAULT) {
                firstCheck = true
            }
        } else {
            if (mCurrentAngle >= mMaxAngle) {
                mCurrentAngle -= mAngleSpeed
            }
        }
        postInvalidateDelayed(100)
    }

    //支持wrap_content
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMeasureSpecMode == MeasureSpec.AT_MOST && heightMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mMaxWidth, mMaxHeight)
        } else if (widthMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mMaxWidth, heightMeasureSpecSize)
        } else if (heightMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthMeasureSpecSize, mMaxHeight)
        }
    }

    //设置最大角度
    fun setAngle(angle: Int) {
        mMaxAngle = angle
        postInvalidate()
    }

    //设置View下名称
    fun setName(name: String) {
        mTargetName = name
    }

    //设置转动速度
    fun setAngleSpeed(speed: Int) {
        mAngleSpeed = speed
    }

    //设置，最大角度，转速，名称
    fun setParameters(maxAngle: Int, angleSpeed: Int, name: String) {
        mMaxAngle = maxAngle
        mAngleSpeed = angleSpeed
        mTargetName = name
        postInvalidate()
    }
}