package com.yesjnet.gwanak.ui.widget

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorInt

/**
 * 커스텀 뷰 만들기 (원형, 라운드, 테두리)
 */
open class DrawableMaker {
    var shape: DrawableShape = DrawableShape.RECTANGLE

    @ColorInt
    var color: Int = 0
    var cornerRadius: Int = 0
    var cornerRadii: CornerRadii? = null
    var gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM
    var gradientColors: IntArray? = null
    var dashHeight: Int = 1
    var dashColor: Int = 0
    var dashGap: Int = 0
    var dashWidth: Int = 0
    var gradientType: GradientType = GradientType.LINEAR

    fun build(): GradientDrawable {
        val gradientDrawable: GradientDrawable =
            if (gradientColors != null) {
                GradientDrawable(gradientOrientation, gradientColors).also {
                    it.gradientType = gradientType.ordinal
                }
            } else {
                GradientDrawable()
            }
        gradientDrawable.shape = shape.ordinal

        if (color != 0 && gradientColors == null) gradientDrawable.setColor(color)
        if (cornerRadius != 0) gradientDrawable.cornerRadius = cornerRadius.toFloat()

        cornerRadii?.run {
            gradientDrawable.cornerRadii = floatArrayOf(
                leftTop.toFloat(),
                leftTop.toFloat(),
                rightTop.toFloat(),
                rightTop.toFloat(),
                rightBottom.toFloat(),
                rightBottom.toFloat(),
                leftBottom.toFloat(),
                leftBottom.toFloat()
            )
        }

        if (dashColor != 0) {
            gradientDrawable.setStroke(
                dashHeight,
                dashColor,
                dashWidth.toFloat(),
                dashGap.toFloat()
            )
        }
        gradientDrawable.mutate()
        return gradientDrawable
    }

}

fun View.setDrawable(lambda: DrawableMaker.() -> Unit) = apply {
    backgroundTintList = null
    background = DrawableMaker().apply(lambda).build()
}

enum class DrawableShape {
    RECTANGLE,
    OVAL,
    LINE,
    RING
}


enum class GradientType {
    LINEAR,
    RADIAL,
    SWEEP
}


data class CornerRadii(
    var leftTop: Int = 0,
    var leftBottom: Int = 0,
    var rightTop: Int = 0,
    var rightBottom: Int = 0,
)