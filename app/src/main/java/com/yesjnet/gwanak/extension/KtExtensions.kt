package com.yesjnet.gwanak.extension

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import androidx.annotation.RequiresApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.orhanobut.logger.Logger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Description : 코틀린 익스텐션정의 클래스
 */

/**
 * 폰번호 유형 체크 정규식
 *  @see '테스트안되었음 테스트필요 정규식 수정될수도있음'
 */
fun String.isPhoneNum(): Boolean {
    val p = "^01[016789][0-9]{7,8}\$".toRegex()
    return matches(p)
}

fun String.isNameValid(): Boolean {
    val p = "^[a-zA-Zㄱ-ㅎ가-힣]*\$".toRegex()
    return matches(p)
}

fun String.isPassValid(): Boolean {
    val p = "^[A-Za-z[0-9]]{8,20}$".toRegex() // 영문, 숫자
    return matches(p)
}

/**
 * 이메일 정규식
 * @param email
 * @return
 */
fun String.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isNumeric(): Boolean {
    val p = "^[0-9]+$".toRegex()
    return matches(p)
}

fun String.replace(replaceStr: String): String {
    return Regex("""$replaceStr""").replace(this, "")
}

fun getDecimalFormat(str: Int): String {
    var myFormatter = DecimalFormat("###,###")
    return myFormatter.format(str)
}

/**
 * 전화번호 숫자만 추출
 * @param phone 전화번호
 */
fun getNumeric(phone: String) :String? {
    var answer = phone
    val re = Regex("[^0-9]")
    answer = re.replace(answer, "")

    return answer
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun getMobileFormat(phone: String?) : String {
    if(phone == null) return ""
    return PhoneNumberUtils.formatNumber(phone, Locale.getDefault().country)
}

fun getLocaleCalendar(): Calendar {
    return GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)
}

/**
 * 지정된 시간 초과 여부
 * @param date 지정된 시간
 * @return boolean ( true - 초과함, false - 초과하지 않음 )
 */
fun isOverTime(date: Date): Boolean {
    val checkCalendar: Calendar = Calendar.getInstance()
    checkCalendar.time = date
    val checkDay: Long = checkCalendar.timeInMillis
    val today: Long = Calendar.getInstance().timeInMillis
    var result = checkDay - today
    Logger.d("result = $result")
    return result < 0
}

/**
 * 지정된 시간 차이 계산
 * @param date 지정된 시간
 * @return Long
 */
fun overTime(date: Date): Long {
    val checkCalendar: Calendar = Calendar.getInstance()
    checkCalendar.time = date
    val checkDay: Long = checkCalendar.timeInMillis
    val today: Long = Calendar.getInstance().timeInMillis
    var result = checkDay - today
    Logger.d("overTime result = $result")
    return result
}

fun diffTIme(date: Date): Long {
    val checkCalendar: Calendar = Calendar.getInstance()
    checkCalendar.time = date
    val checkDay: Long = checkCalendar.timeInMillis
    val today: Long = Calendar.getInstance().timeInMillis
    var result = today - checkDay
    Logger.d("diffTIme result = $result")
    return result
}

/**
 * 문자열 -> 날짜로 변환
 *
 * @param str 날짜 문자열 (yyyy-MM-dd HH:mm:ss 등등)
 * @param format 변환할 날짜 패턴
 * @return date
 */
@SuppressLint("SimpleDateFormat")
fun getStringToDate(str: String, format: String): Date {
    val format = SimpleDateFormat(format)
    return format.parse(str)
}

fun getDateFormat(date: Date?, foramt: String): String {
    date?.let { return SimpleDateFormat(foramt,Locale.KOREAN).format(it) } ?: return ""
}

inline fun <T: Any> ifLets(vararg elements: T?, closure: (List<T>) -> Unit) {
    if (elements.all { it != null }) {
        closure(elements.filterNotNull())
    }
}

inline fun <T: Any> ifLet(element: T?, closure: (T) -> Unit) {
    if (element != null ) {
        closure(element)
    }
}

/**
 * 날짜 포멧형식
 * calendar, date 제거하고 이걸로 다 바꿔야함...
 *
 * @param localDateTime os 26 이상에서 사용
 * @param localDateTimeBp os 26 이하에서 지원
 * @param format 포멧형식
 * @return string
 */
fun getLocalDateFormat(localDateTime: LocalDateTime? = null, localDateTimeBp: org.threeten.bp.LocalDateTime? = null, format: String): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern(format)
        localDateTime?.format(formatter)

    } else {
        val formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern(format)
        localDateTimeBp?.format(formatter)
    }
}

fun <T> updateItems(items: ArrayList<T>, beforeItems: ArrayList<T>?, nextPage: Int): ArrayList<T> {
//    if (nextPage == 1) {
//        returnItems.addAll(items)
//    } else {
//        beforeItems?.let { beforeArr ->
//            val afterItems = ArrayList<T>()
//            afterItems.addAll(beforeArr)
//            if (items.isNotEmpty()) {
//                afterItems.addAll(items)
//            }
//            returnItems.addAll(afterItems)
//        }
//    }

    val newList = arrayListOf<T>()
    if (nextPage == 1) {
        newList.addAll(items)
    } else {
        beforeItems?.let { newList.addAll(it) }
        newList.addAll(items)
    }

    return newList
}

fun <T> deleteItem(item: T, beforeItems: ArrayList<T>?): ArrayList<T> {
    beforeItems?.remove(item)

    return beforeItems ?: arrayListOf()
}

fun generateBarcode(contents: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(contents, BarcodeFormat.CODE_128, width, height)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// UTF-8 인코딩을 적용한 QR 코드를 생성하는 함수
fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
    val qrCodeWriter = QRCodeWriter()
    return try {
        // UTF-8 인코딩을 위한 설정 추가
        val hints = Hashtable<EncodeHintType, String>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        // QR 코드 생성
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // 비트맵 생성
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)  // 검정과 흰색 픽셀 설정
            }
        }
        bitmap
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}