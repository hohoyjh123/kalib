package com.yesjnet.gwanak.extension

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Build
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.data.model.CulturalProgram
import com.yesjnet.gwanak.data.model.Family
import com.yesjnet.gwanak.data.model.HolidayItem
import com.yesjnet.gwanak.data.model.NoticeItem
import com.yesjnet.gwanak.data.model.RecommendBook
import com.yesjnet.gwanak.ui.widget.CornerRadii
import com.yesjnet.gwanak.ui.widget.DrawableShape
import com.yesjnet.gwanak.ui.widget.setDrawable
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.util.concurrent.atomic.AtomicBoolean


/**
 *  Description : Databinding BindingAdapter Class Collection
 */

/**
 * 뷰 / 아이템뷰 더블클릭 방지 리스너 바인딩 in XML
 */
interface OnSingleClickListener {
    fun onSingleClick(view: View)
}

interface OnSingleItemClickListener {
    fun onSingleItemClick(view: View, position: Int = -1)
}

fun View.setOnSingleClickListener(listener: (view: View) -> Unit) {
    this.setOnClickListener { v ->
        v.isClickable = false
        postDelayed({ v.isClickable = true }, 1000L)
        listener.invoke(v)
    }
}

@BindingAdapter("onSingleClick")
fun View.setOnNewSingleClickListener(clickListener: OnSingleClickListener?) {
    clickListener?.also {
        setOnClickListener(OnNewSingleClickListener(it))
    } ?: setOnClickListener(null)
}

class OnNewSingleClickListener(
    private val clickListener: OnSingleClickListener,
    private val intervalMs: Long = 250,
) : View.OnClickListener {
    private var canClick = AtomicBoolean(true)

    override fun onClick(v: View?) {
        if (canClick.getAndSet(false)) {
            v?.run {
                postDelayed({
                    canClick.set(true)
                }, intervalMs)
                clickListener.onSingleClick(v)
            }
        }
    }
}

@BindingAdapter("bindShowRefresh")
fun SwipeRefreshLayout.isRefreshing(visible: Boolean) {
    isRefreshing = visible
}

@BindingAdapter("bindShowProgressViewGroup")
fun ViewGroup.isProgress(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("bindShowProgress")
fun ProgressBar.isProgress(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("bindStartAnimation")
fun startAnimation(imageView: ImageView, isStart: Boolean) {
    val rocketAnimation = imageView.background as AnimationDrawable
    if (isStart) rocketAnimation.start() else rocketAnimation.stop()
}

@BindingAdapter("setImage", "centerCrop")
fun setImageView(imageView: ImageView, photo: String?, centerCrop: Boolean) {
    if (photo == null) {
        imageView.setImageResource(0)
        return
    }

    val options = RequestOptions()
    if (centerCrop) options.centerCrop()

    Glide.with(imageView.context)
            .load(photo)
            .apply(options)
            .into(imageView)
}

/**
 * 사진
 * @param uri 사진 uri
 * @param photoStr 사진 url
 * @param roundValue 라운드 수치
 * @param cornerType 라운드 타입
 * @param isVisibleFlag 뷰 활성/비활성 사용여부(true - 사용, false - 미사용)
 * @param pl
 */
@BindingAdapter(
    value = ["photo", "roundValue", "cornerType"],
    requireAll = false
)
fun<T> ImageView.bindImageView(
    photo: T? = null,
    roundValue: Int? = null,
    cornerType: EnumApp.CornerType? = null
) {
    val glideCornerType = when (cornerType) {
        EnumApp.CornerType.TOP -> RoundedCornersTransformation.CornerType.TOP
        EnumApp.CornerType.BOTTOM -> RoundedCornersTransformation.CornerType.BOTTOM
        else -> RoundedCornersTransformation.CornerType.ALL
    }

    val transformation = if (roundValue == null) {
        MultiTransformation(CenterCrop())
    } else {
        MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(roundValue, 0, glideCornerType)
        )
    }

    when {
        photo != null -> {
            Glide.with(context).load(photo).transition(DrawableTransitionOptions.withCrossFade()).apply(RequestOptions.bitmapTransform(transformation)).into(this)
            show()
        }
        else -> {
           gone()
        }
    }
}

@BindingAdapter("bindRoundImage", "roundValue", "cornerType")
fun bindRoundImage(imageView: ImageView, photo: String?, roundValue: Int, cornerType: EnumApp.CornerType) {
    photo?.let { photoUrl ->
        val round: Int = imageView.context.pixelFromDP(roundValue)
        var glideCornerType = when(cornerType) {
            EnumApp.CornerType.TOP -> RoundedCornersTransformation.CornerType.TOP
            EnumApp.CornerType.BOTTOM -> RoundedCornersTransformation.CornerType.BOTTOM
            else -> RoundedCornersTransformation.CornerType.ALL
        }
        val transformation = MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(
                round,
                0,
                glideCornerType
            )
        )

        Glide.with(imageView.context).load(photoUrl)
            .apply(RequestOptions.bitmapTransform(transformation)).into(imageView)
    } ?: run { imageView.setImageResource(R.drawable.btn_toast) }
}

@BindingAdapter("bindRoundImageUri", "roundValue", "cornerType", "thumbnail")
fun bindRoundImageUri(imageView: ImageView, uri: Uri?, roundValue: Int, cornerType: EnumApp.CornerType, thumbnail: Float) {
    uri?.let { uri ->
        val round: Int = imageView.context.pixelFromDP(roundValue)
        var glideCornerType = when(cornerType) {
            EnumApp.CornerType.TOP -> RoundedCornersTransformation.CornerType.TOP
            EnumApp.CornerType.BOTTOM -> RoundedCornersTransformation.CornerType.BOTTOM
            else -> RoundedCornersTransformation.CornerType.ALL
        }
        val transformation = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(
                        round,
                        0,
                        glideCornerType
                )
        )

        if (thumbnail > 0.0f) {
            Glide.with(imageView.context).load(uri)
                    .apply(RequestOptions.bitmapTransform(transformation)).thumbnail(thumbnail).into(imageView)
        } else {
            Glide.with(imageView.context).load(uri)
                    .apply(RequestOptions.bitmapTransform(transformation)).into(imageView)
        }
    }
}

@BindingAdapter("setImageCircle", "defaultImg", "placeholder")
fun setImageCircle(imageView: ImageView, photo: String? = null, defaultImg: Drawable? = null, placeholder: Drawable? = null) {
    photo?.let {
        val options = RequestOptions()
        options.centerCrop()
        options.circleCrop()
        placeholder?.let { options.placeholder(placeholder) }

        Glide.with(imageView.context)
                .load(photo)
                .apply(options)
                .into(imageView)
    } ?: run {
        defaultImg?.let { defaultDrawable ->
            imageView.setImageDrawable(defaultDrawable)
        } ?: run {
            imageView.setImageResource(0)
        }
    }
}

@BindingAdapter("bindProfilePhoto", "photoUri", "isCircle", "roundValue", "cornerType")
fun bindProfilePhoto(imageView: ImageView, item: String?, photoUri: Uri?, isCircle: Boolean, roundValue: Int, cornerType: EnumApp.CornerType) {

    val round: Int = imageView.context.pixelFromDP(roundValue)
    var glideCornerType = when(cornerType) {
        EnumApp.CornerType.TOP -> RoundedCornersTransformation.CornerType.TOP
        EnumApp.CornerType.BOTTOM -> RoundedCornersTransformation.CornerType.BOTTOM
        else -> RoundedCornersTransformation.CornerType.ALL
    }

    val transformation = if (isCircle) {
        MultiTransformation(
                CenterCrop(), CircleCrop(),
                RoundedCornersTransformation(
                        round,
                        0,
                        glideCornerType
                )
        )
    } else {
        MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(
                        round,
                        0,
                        glideCornerType
                )
        )
    }

    item?.let {
        Glide.with(imageView.context).load(it)
                .apply(RequestOptions.bitmapTransform(transformation)).into(imageView)
    } ?: run {
        photoUri?.let {
            Glide.with(imageView.context).load(it)
                    .apply(RequestOptions.bitmapTransform(transformation)).into(imageView)
        }
    }
}

/**
 * 버튼 라운드, 테두리, 그라데이션 설정
 * xml 그만만들고 이걸로 쓰자.
 */
@BindingAdapter(
    value = ["shapeType", "bgColor", "cornerAll", "cornerLeftTop", "cornerLeftBottom", "cornerRightTop", "cornerRightBottom", "stroke", "strokeColor", "gradientOri", "gradientArr", "rippleColor"],
    requireAll = false
)
fun View.bindSetDrawable(
    shapeType: DrawableShape? = null,
    bgColor: Int? = null,
    cornerAll: Int? = null,
    cornerLeftTop: Int? = null,
    cornerLeftBottom: Int? = null,
    cornerRightTop: Int? = null,
    cornerRightBottom: Int? = null,
    stroke: Int? = null,
    strokeColor: Int? = null,
    gradientOri: GradientDrawable.Orientation? = null,
    gradientArr: IntArray? = null,
    rippleColor: Int? = null,
) {
    setDrawable {
        // 모양 타입
        ifLet(shapeType) {
            shape = it
        }
        // 백그라운드 색상
        ifLet(bgColor) {
            color = it
        }
        // 라운드 코너 전체
        ifLet(cornerAll) {
            cornerRadius = this@bindSetDrawable.context.pixelFromDP(it)
        }
        // 라운드 코너 개별
        if (cornerLeftTop.isNotNull() || cornerRightTop.isNotNull() || cornerLeftBottom.isNotNull() || cornerRightBottom.isNotNull()) {
            val corner = CornerRadii(
                this@bindSetDrawable.context.pixelFromDP(cornerLeftTop ?: 0),
                this@bindSetDrawable.context.pixelFromDP(cornerLeftBottom ?: 0),
                this@bindSetDrawable.context.pixelFromDP(cornerRightTop ?: 0),
                this@bindSetDrawable.context.pixelFromDP(cornerRightBottom ?: 0)
            )
            cornerRadii = corner
        }
        // 테두리 높이
        ifLet(stroke) {
            dashHeight = this@bindSetDrawable.context.pixelFromDP(it)
        }
        // 테두리 색상
        ifLet(strokeColor) {
            dashColor = it
        }
        // 그라데이션 방향
        ifLet(gradientOri) {
            gradientOrientation = it
        }
        // 그라데이션 색상
        ifLet(gradientArr) {
            gradientColors = it
        }
    }

    // 클릭 이팩트 효과주기
    // 라운드 코너 전체를 줬을때 리플 이펙트도 동일하게 반영.(라운드 코너 개별로 줬을때도 처리해야하나 흠..)
    ifLet(rippleColor) { color ->
        val contentOuterRadii = floatArrayOf(
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat(),
            this@bindSetDrawable.context.pixelFromDP(cornerAll ?: 0).toFloat()
        )

        val buttonShape = RoundRectShape(contentOuterRadii, null, null)
        val maskShapeDrawable = ShapeDrawable(buttonShape).apply {
            paint.color = color
        }
        val contentShapeDrawable = ShapeDrawable(buttonShape).apply {
            paint.color = 0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = RippleDrawable(ColorStateList.valueOf(color), contentShapeDrawable, maskShapeDrawable)
        }
    }
}

// 특정 문자열 색상 변경
@BindingAdapter("bindChangeTextColor", "startIndex", "changeColor")
fun bindChangeTextColor(textView: TextView, changeStr: String?, startIndex: Int = -1, changeColor: Int) {
    changeStr?.let { changeStr ->
        textView.setTextViewCustom(textView.text.toString(), changeStr, startIndex, color = changeColor, styleType = Typeface.BOLD)
    }
}

// 특정 문자열 색상 변경
@BindingAdapter("bindStringForamt", "formatStr")
fun bindStringForamt(textView: TextView, fullStr: String, formatStr: String?) {
    formatStr?.let { formatStr ->
        textView.text = String.format(fullStr, formatStr)
    }
}

// 특정 문자열 색상 & 스타일 변경
@BindingAdapter(
    value = ["bindChangeTextStyle", "isFormat", "changeStr", "startIndex", "changeColor", "styleType", "isUnderLine", "fullUnderLine"],
    requireAll = false
)
fun TextView.bindChangeTextStyle(
    fullStr: String,
    isFormat: Boolean = false,
    changeStr: String?,
    startIndex: Int = -1,
    changeColor: Int,
    styleType: Int,
    isUnderLine: Boolean? = null,
    fullUnderLine: Boolean? = null,
) {
    changeStr?.let { str ->
        val fullStr = if (isFormat) String.format(fullStr, str) else fullStr
        val isUnder = isUnderLine ?: false
        val fullUnder = fullUnderLine ?: false
        setTextViewCustom(
            fullStr,
            str,
            startIndex,
            color = changeColor,
            styleType = styleType,
            isUnderLine = isUnder,
            fullUnderLine = fullUnder
        )
    }
}

/**
 * 텍스트뷰 비밀번호 형식 on/off
 */
@BindingAdapter("bindPwdShow")
fun TextView.bindPwdShow(isShow: Boolean?) {
    isShow?.let {
        if (it) {
            this.transformationMethod = null
        } else {
            this.transformationMethod = PasswordTransformationMethod()
        }
    }
}

/**
 * 숫자로 바코드 생성
 */
@BindingAdapter("bindCreateBarcode")
fun ImageView.bindCreateBarcode(barcode: String?) {
    barcode?.let {
        if (it.isNotEmpty()) {
            val bitmap = generateBarcode(it, this.context.pixelFromDP(246), this.context.pixelFromDP(55))
            this.setImageBitmap(bitmap)
        }
    }
}

/**
 * 도서관 휴관일
 */
@BindingAdapter("bindLibClose")
fun TextView.bindLibClose(item: HolidayItem?) {
    item?.let {
        this.text = it.libAlias
        this.bindSetDrawable(bgColor = Color.parseColor(item.libColor), cornerAll = 14)
    }
}

