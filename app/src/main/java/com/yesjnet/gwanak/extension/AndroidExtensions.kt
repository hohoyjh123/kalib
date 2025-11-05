package com.yesjnet.gwanak.extension

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.InputFilter
import android.text.Spannable
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.ui.base.BaseActivity
import com.yesjnet.gwanak.ui.base.BaseDialogFragment
import com.yesjnet.gwanak.ui.base.BaseFragment
import com.yesjnet.gwanak.ui.dialog.CustomDialog
import com.yesjnet.gwanak.util.FileDownloadHelper.correctMimeType
import java.io.File
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * 특정 리소스 ID와 관련된 색상을 반환
 *  @param color 리소스 id
 *  return color
 */
fun Context.getColorCompat(colorId: Int) = ContextCompat.getColor(this, colorId)

/**
 * 특정 리소스 ID와 관련된 색상을 반환(다중)
 *  @param colorId 리소스 id
 *  return colors
 */
fun Context.getColorStateLists(colorId: Int) = ContextCompat.getColorStateList(this, colorId)

tailrec fun <T : View> View.findParent(parentType: Class<T>): T {
    return if (parent.javaClass == parentType) parent as T else (parent as View).findParent(parentType)
}

/**
 * Extension method to share for Context.
 */
fun Context.share(text: String, subject: String? = ""): Boolean {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/html"
    subject?.let { intent.putExtra(EXTRA_SUBJECT, subject) }
    intent.putExtra(EXTRA_TEXT, text)
    return try {
        startActivity(createChooser(intent, ""))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

/**
 * SMS 보내기
 */
fun Context.sms(phone: String?, body: String = "", requestCode: Int = 0) {
    val smsToUri = Uri.parse("smsto:" + phone)
    val intent = Intent(Intent.ACTION_SENDTO, smsToUri)
    intent.putExtra("sms_body", body)
    if (requestCode > 0) {
        startActivityForResult(this as Activity, intent, requestCode, null)
    } else {
        startActivity(intent)
    }
}

/**
 * 전화걸기
 */
fun Context.dial(tel: String?) = startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))

/**
 * 브라우저 이동
 * @param url 주소
 * @param newTask flag
 *
 */
fun Context.browse(url: String, newTask: Boolean = false): Boolean {
    val marketLaunch = Intent(Intent.ACTION_VIEW)
    if (newTask) marketLaunch.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    marketLaunch.data = Uri.parse(url)
    return try {
        startActivity(marketLaunch)
        true
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }

}

/**
 * View
 */
fun View.getString(stringResId: Int): String = resources.getString(stringResId)

fun View.show() : View {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
    return this
}

fun View.hide() : View {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
    return this
}

fun View.gone() : View {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
    return this
}

fun View.toggleVisible() : View {
    visibility = if (visibility == View.VISIBLE) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
    return this
}

fun View.getBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    draw(canvas)
    canvas.save()
    return bmp
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        clearFocus()
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) { }
    return false
}

fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).show()
}

fun View.doOnLayout(onLayout: (View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int,
                                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            if (onLayout(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}

inline val Context.displayWidth: Int
    get() = resources.displayMetrics.widthPixels

inline val Context.displayHeight: Int
    get() = resources.displayMetrics.heightPixels

inline val Context.displayMetricks: DisplayMetrics
    get() = resources.displayMetrics

/**
 * Activity Fragment
 */

fun Activity.showToast(message:String, duration:Int = Toast.LENGTH_SHORT) {
    var toastView = layoutInflater.inflate(R.layout.layout_toast, null)
    var tvMessage: TextView = toastView.findViewById(R.id.tvToast)
    tvMessage.text = message

    var toast = Toast(this)
    toast.setGravity(Gravity.BOTTOM, 0, pixelFromDP(16))
    toast.view = toastView
    toast.duration = duration
    toast.show()
}

fun Fragment.showToast(message:String, duration:Int = Toast.LENGTH_SHORT) = this?.let { it.activity?.showToast(message, duration) }

/**
 * PemissionUtil
 */

/**
 * 권한 유무 체크
 * @param permission 확인할 권한
 * return boolean
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.checkAndRequestPermission(
    requestCode: Int,
    alertDialog: AlertDialog?,
    vararg permissions: String
): Boolean {
    val deniedPermissions =
        getDeniedPermissions(this,*permissions)
    if (deniedPermissions.isNotEmpty()) {
        for (deniedPermission in deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    deniedPermission
                )
            ) {
                if (alertDialog != null) {
                    alertDialog.show()
                    return false
                }
            }
        }
        ActivityCompat.requestPermissions(this, deniedPermissions, requestCode)
        return false
    }
    return true
}

fun Fragment.checkAndRequestPermission(
    fragment: Fragment,
    requestCode: Int,
    alertDialog: AlertDialog?,
    vararg permissions: String
): Boolean {
    val deniedPermissions =
        getDeniedPermissions(fragment.requireActivity(),*permissions)
    if (deniedPermissions.isNotEmpty()) {
        for (deniedPermission in deniedPermissions) {
            if (fragment.shouldShowRequestPermissionRationale(deniedPermission)) {
                if (alertDialog != null) {
                    alertDialog.show()
                    return false
                }
            }
        }
        fragment.requestPermissions(deniedPermissions, requestCode)
        return false
    }
    return true
}

fun getDeniedPermissions(
    context: Context,
    vararg permissions: String
): Array<String> {
    val deniedPermissions: MutableList<String> =
        ArrayList()
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            deniedPermissions.add(permission)
        }
    }
    return deniedPermissions.toTypedArray()
}

/**
 * 기본 팝업(ok 리스너 적용)
 * @param cancelable 팝업 cancelable
 * @param title 제목
 * @param message 내용
 * @param desc 디스크립션
 * @param okString 확인 버튼명
 * @param okListener 확인 클릭 이벤트
 * @return CustomDialog
 */
fun AppCompatActivity.showAlertOK(
        cancelable: Boolean = true,
        title: String = "",
        message: String = "",
        desc: String = "",
        okString: String? = "",
        okListener: BaseDialogFragment.MyOnClickListener? = null
): CustomDialog {
    val customDialog: CustomDialog = CustomDialog.newInstance(cancelable, title, message, desc)
    customDialog.setOkBtn(okString, okListener)
    customDialog.show(supportFragmentManager, "")
    return customDialog
}
fun BaseFragment<*>.showAlertOK(
        cancelable: Boolean = true,
        title: String = "",
        message: String = "",
        desc: String = "",
        okString: String? = "",
        okListener: BaseDialogFragment.MyOnClickListener? = null
) {
    activity?.let {
        val customDialog: CustomDialog = CustomDialog.newInstance(cancelable, title, message, desc)
        customDialog.setOkBtn(okString, okListener)
        customDialog.show(it.supportFragmentManager, "")
    }
}



/**
 * 기본 팝업(ok, cancel 리스너 적용)
 * @param cancelable 팝업 cancelable
 * @param title 제목
 * @param message 내용
 * @param desc 디스크립션
 * @param okString 확인 버튼명
 * @param cancelString 취소버튼명
 * @param okButtonColor 확인 버튼색상
 * @param cancelButtonColor 취소 버튼색상
 * @param okListener 확인 클릭 이벤트
 * @param cancelListener 취소 클릭 이벤트
 * @return CustomDialog
 */
fun AppCompatActivity.showAlertConfirm(
        cancelable: Boolean = true,
        title: String = "",
        message: String = "",
        desc: String = "",
        okString: String? = "",
        cancelString: String? = "",
        okButtonColor: Int = -1,
        cancelButtonColor: Int = -1,
        okListener: BaseDialogFragment.MyOnClickListener,
        cancelListener: BaseDialogFragment.MyOnClickListener? = null
): CustomDialog {
    val customDialog = CustomDialog.newInstance(cancelable, title, message, desc)
    customDialog.setOkBtn(okString, okButtonColor, okListener)
    customDialog.setCancelBtn(cancelString, cancelButtonColor, cancelListener)
    customDialog.show(supportFragmentManager, "")
    return customDialog
}

fun BaseFragment<*>.showAlertConfirm(
        cancelable: Boolean = true,
        title: String = "",
        message: String = "",
        desc: String = "",
        okString: String? = "",
        cancelString: String? = "",
        okButtonColor: Int = -1,
        cancelButtonColor: Int = -1,
        okListener: BaseDialogFragment.MyOnClickListener,
        cancelListener: BaseDialogFragment.MyOnClickListener? = null
) : CustomDialog? {
    return (activity as? AppCompatActivity)?.showAlertConfirm(
            cancelable,
            title,
            message,
            desc,
            okString,
            cancelString,
            okButtonColor,
            cancelButtonColor,
            okListener,
            cancelListener
    )
}


/**
 * DP로 픽셀 구하기
 * @param dip 픽셀값 구하기 위한 dip 수치
 * return int
 */
fun Context.pixelFromDP(dip: Int): Int {
    return (dip * resources.displayMetrics.density).toInt()
}

/**
 * 커스텀 텍스트뷰
 * @param fulltext 전체 문자열
 * @param subtext 변경할 문자열
 * @param size 변경할 폰트 크기
 * @param color 변경할 폰트 색상
 * @param styleType 변경할 폰트 스타일 (Normal, Bold)
 * @param isUnderLine 변경할 언더라인
 * @param fullUnderLine 전체 언더라인
 */
fun TextView.setTextViewCustom(
    fulltext: String,
    subtext: String,
    startIndex: Int = -1,
    size: Int = -1,
    color: Int = -1,
    styleType: Int = Typeface.NORMAL,
    isUnderLine: Boolean = false,
    fullUnderLine: Boolean = false,
) {
    setText(fulltext, TextView.BufferType.SPANNABLE)
    val str = text as Spannable
    val i = if (startIndex == -1) fulltext.indexOf(subtext) else startIndex

    // 폰트 크기
    if (size != -1) {
        str.setSpan(AbsoluteSizeSpan(size), i, i + subtext.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // 폰트 색상
    if (color != -1) {
        str.setSpan(
            ForegroundColorSpan(color),
            i,
            i + subtext.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    // 폰트 스타일
    str.setSpan(
        StyleSpan(styleType),
        i,
        i + subtext.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    if (isUnderLine) {
        str.setSpan(
            UnderlineSpan(),
            i,
            i + subtext.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // underLine
    } else if (fullUnderLine) {
        str.setSpan(
            UnderlineSpan(),
            0,
            0 + fulltext.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // underLine
    }
}

fun Context.getSelectIndex(selectStr: String, arr: Array<String>): Int {
    var selectIndex = -1

    for(index in arr.indices) {
        if (selectStr == arr[index]) {
            selectIndex = index
            break
        }
    }

    return selectIndex
}

fun Context.getSelectStr(selectIndex: Int, arr: Array<String>): String {
    return arr[selectIndex]
}


fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop*4)       // "8" was obtained experimentally
}

fun View.interceptTouch(id: Int) {
    setOnTouchListener { v, event ->
        if (v.id == id) {
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> v.parent
                    .requestDisallowInterceptTouchEvent(false)
            }
        }
        false
    }
}

fun BaseActivity<*>.slideTransitionStart(){
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun BaseActivity<*>.slideTransitionFinish(){
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

fun BaseActivity<*>.slideTransitionUpStart(){
    overridePendingTransition(R.anim.slide_up_enter, R.anim.no_change)
}

fun BaseActivity<*>.slideTransitionUpFinish(){
    overridePendingTransition(R.anim.no_change, R.anim.slide_down_enter)
}

/**
 * 생년월일 입력 팝업
 * @param year 년도
 * @param month 월
 * @param day 일
 * @param dateSetListener 콜백
 * @param minDate 최소 입력가능 날짜
 * @param maxDate 최대 입력가능 날짜
 */
fun Context.showBirthDayDialog(year: Int, month: Int, day: Int, dateSetListener: DatePickerDialog.OnDateSetListener, minDate: Calendar? = null, maxDate: Calendar? = null) {
    val birthday = DatePickerDialog(
        this,
        dateSetListener,
        year,
        month,
        day)

    minDate?.let { birthday.datePicker.minDate = it.timeInMillis }
    maxDate?.let { birthday.datePicker.maxDate = it.timeInMillis }

    birthday.show()
}

/**
 * 상태바 높이 가져오기
 */
fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

/**
 * 상태바 투명도 적용
 * @param translucent (true - 투명 적용, false - 투명 미적용)
 */
fun Activity.setTranslucent(translucent: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = this.window
        if (translucent) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
}

fun View.preventDoubleClick() {
    isClickable = false
    Handler().postDelayed({ isClickable = true },969L)
}

fun EditText.filterByDataType(pattern: String) {
    this.filters = arrayOf(object : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val ps =
                Pattern.compile(pattern)
            return if (!ps.matcher(source).matches()) "" else null
        }
    })
}

/**
 * 뷰페이저 좌우 투명도
 */
fun ViewPager2.setTransFormer() {
    this.setPageTransformer { view, position ->
        var alpha = 1.0f - abs(position)
        // limit alpha// [-1, 1]

        // view.setAlpha(0.0F);
        // view.setVisibility(View.GONE);
        // [0]
        when {
            alpha < 0.2f -> alpha = 0.2f
        }

        // [-Infinity,-1) OR (1,+Infinity]
        when {
            position <= -1.0f || position >= 1.0f -> {
//                view.setAlpha(0.0F);
//                view.setVisibility(View.GONE);
                // [0]
            }
            position == 0.0f -> {
                view.alpha = 1.0f
                view.visibility = View.VISIBLE
                // [-1, 1]
            }
            else -> {
                view.alpha = alpha
                view.visibility = View.VISIBLE
            }
        }
    }
}

fun ViewPager2.alphaAndScalePageTransformer() {
    val scaleMax = 0.8f
    val alphaMax = 0.5f

    this.setPageTransformer { view, position ->
        val scale: Float = if (position < 0) (1 - scaleMax) * position + 1 else (scaleMax - 1) * position + 1
        val alpha: Float = if (position < 0) (1 - alphaMax) * position + 1 else (alphaMax - 1) * position + 1
        if (position < 0) {
            view.pivotX = view.width.toFloat()
            view.pivotY = view.height / 2.toFloat()
        } else {
            view.pivotX = 0f
            view.pivotY = view.height / 2.toFloat()
        }
        view.scaleX = scale
        view.scaleY = scale
        view.alpha = abs(alpha)
    }
}

/**
 * nestedscroll in recyclerview move
 */
fun NestedScrollView.moveToScroll(recyclerView: RecyclerView, index: Int, delayMillis: Long) {
    recyclerView.postDelayed({
        val y: Float =
            recyclerView.y + recyclerView.getChildAt(index).y
        smoothScrollTo(0, y.toInt())
    }, delayMillis)
}

fun runOnUiThread(action: () -> Unit) = Handler(Looper.getMainLooper()).post(Runnable(action))

fun Context.getCapturedImage(selectedPhotoUri: Uri): Bitmap? {
    return when {
        Build.VERSION.SDK_INT < 29 -> MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedPhotoUri
        )
        Build.VERSION.SDK_INT > 29 -> {
            val source =
                ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
            ImageDecoder.decodeBitmap(source)
        }
        else -> { null }
    }
}

fun <T> T.isNotNull(): Boolean {
    return this != null
}

fun <T> T.isNull(): Boolean {
    return this == null
}

fun <T> T.notNull(block: () -> Unit) {
    if (this != null) block.invoke()
}

/**
 * 클립보드 복사
 */
fun Context.copyClipboard(label: String, copyStr: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    // When setting the clipboard text.
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, copyStr))
}

fun WebView.setupDownloadListener(context: Context) {
    var downloadId: Long = -1L
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    this.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
        try {
            // ✅ 파일명 파싱
            var fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            contentDisposition?.let {
                val regex = Regex("filename\\*?=([^;]+)")
                val match = regex.find(it)
                if (match != null) {
                    val raw = match.groupValues[1]
                        .replace("UTF-8''", "")
                        .replace("\"", "")
                        .trim()
                    fileName = URLDecoder.decode(raw, "UTF-8")
                }
            }

            // ✅ 저장 경로: 앱 전용 다운로드 디렉토리
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) file.delete()

            // mime 타입 교정
            val correctedMimeType = correctMimeType(fileName, mimeType)

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("파일 다운로드 중...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                setMimeType(mimeType.ifEmpty { "application/octet-stream" })
                setMimeType(correctedMimeType)
                val cookie = CookieManager.getInstance().getCookie(url)
                if (cookie != null) addRequestHeader("Cookie", cookie)
                addRequestHeader("User-Agent", userAgent)
                setDestinationUri(Uri.fromFile(file))
            }

            downloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show()

            Logger.d("Download URL: $url")
            Logger.d("Disposition: $contentDisposition")
            Logger.d("MimeType: $mimeType")
            Logger.d("FileName: $fileName")

            // ✅ 다운로드 완료 후 파일 열기 브로드캐스트 등록
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        val mime = mimeType.ifEmpty {
                            // MIME 미확인시 확장자 기반 보정
                            when {
                                fileName.endsWith(".xlsx", true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                fileName.endsWith(".xls", true) -> "application/vnd.ms-excel"
                                fileName.endsWith(".txt", true) -> "text/plain"
                                else -> "application/octet-stream"
                            }
                        }

                        // ✅ FileProvider URI로 변환
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )

                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mime)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            context.startActivity(openIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "파일을 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                        // 리시버 해제
                        context.unregisterReceiver(this)
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "다운로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}