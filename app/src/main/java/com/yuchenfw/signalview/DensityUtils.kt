package com.yuchenfw.signalview

import android.content.Context
import android.util.TypedValue


/**
 * Created by yuchenfw on 2018/1/29.
 */
class DensityUtils {
    companion object {
        fun sp2px(context: Context, spVal: Float): Int {

            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,

                    spVal, context.resources.displayMetrics).toInt()
        }

        fun px2sp(context: Context, pxVal: Float): Float {

            return pxVal / context.resources.displayMetrics.scaledDensity

        }

        fun dp2px(context: Context, dpVal: Float): Int {

            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,

                    dpVal, context.resources.displayMetrics).toInt()

        }

        fun px2dp(context: Context, pxVal: Float): Float {

            val scale = context.resources.displayMetrics.density

            return pxVal / scale
        }
    }
}