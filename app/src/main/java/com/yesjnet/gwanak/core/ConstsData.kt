package com.yesjnet.gwanak.core

import com.yesjnet.gwanak.BuildConfig
import java.net.HttpURLConnection

/**
 * Description : Response Code Define
 */
class ConstsData {
    companion object {

        // Server API
        private val DEV_MODE: Boolean = BuildConfig.DEBUG
        val SERVER_HOST = BuildConfig.SERVER_HOST
        private const val SERVER_PATH = "/"
        val SERVER_URL_FULL = SERVER_HOST + SERVER_PATH

        const val SIGNUP_URL = "https://www.gwangjinlib.seoul.kr/gjinfo/memberJoinIntro.do"
        const val FIND_ID_URL = "https://www.gwangjinlib.seoul.kr/gjinfo/memberFindId.do"
        const val FIND_PWD_URL = "https://www.gwangjinlib.seoul.kr/gjinfo/memberFindPwd.do"

        // Logger
        const val TAG_COMM = "KJLog"
        const val TAG_NET = TAG_COMM + "_NET"
        const val TAG_IAB = TAG_COMM + "_IAB"

        const val API_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
        const val PATTERN_YYYY_MM_DD = "yyyy-MM-dd"
        const val TEMP_PATH = "temp"
        const val PHOTO_TEMP_FILE_NAME = "temp_photo_file"
        const val PHOTO_TEMP_FILE_EXT = ".jpg"

        // BillingKey
        const val BILLING_KEY: String = ""

        const val THROTTLE_TIME = 369L

        fun isDevMode():Boolean{
            return DEV_MODE
        }
    }

    /**
     * 입력 제한 필터
     */
    class PatternData {
        companion object {
            const val KO_EN_NUM = "^[0-9a-zA-Zㄱ-ㅎ가-힣]*\$"
        }
    }

    class ResCode {

        companion object {

            /**
             *  HTTP Status Code
             */
            const val HTTP_CODE_401 = HttpURLConnection.HTTP_UNAUTHORIZED // Not Authorization
            const val HTTP_CODE_404 = HttpURLConnection.HTTP_NOT_FOUND // Not Found
            const val HTTP_CODE_408 = HttpURLConnection.HTTP_CLIENT_TIMEOUT // Request Time Out

            /**
             *  Server Response Code
             *  @sample 'Type_Noun_Verb'
             */
            // Common
            const val APP_NOCONNECTIVITY = -111 // 인터넷 컨넥션 에러
            const val APP_CLIENT_ERROR = -112 // 클라이언트 자체 에러
            const val APP_HTTP_ERROR = -113 // Retrofit Successfull 300 이상의  HTTP ERROR
            const val APP_BAD_REQUEST = -114 // HTTP_BAD_REQUEST 400 에러 (api 정상이 아닌 경우)

            const val COMM_SUCCESS = 0 // 200 정상 데이터 송수신
            const val COMM_LOGIN_NEED = 3 // 401 : 로그인이 필요합니다
            const val COMM_TOKEN_EXPIRED = 4 // 401 : AccessToken 만료 / 갱신필요


        }
    }

    /**
     * Description : Request Parameter Key
     */
    class ReqParam {
        companion object {

            // Server API Param
            const val AGENT = "agent"
            const val APP_VERSION = "appVersion"
            const val LIB_TYPE = "libType"
            const val USER_ID = "userId"
            const val PASSWORD = "password"
            const val FCM = "fcm"
            const val USER_KEY = "userKey"
            const val SWITCH1 = "switch1"
            const val PUSH_KEY = "pushkey"
            const val APP_USER_NO = "appUserNo"
            const val APP_USER_NM = "appUserNm"

        }
    }

    /**
     * Description : SharedPreference Key
     */
    class PrefCode {
        companion object {
            const val FCM_DEVICE_TOKEN = "pref.pushkey"
            const val SNS_TYPE = "sns_type"
            const val SNS_TOKEN = "sns_token"
            const val USER_NO = "USER_NO"
            const val USER_NM = "USER_NM"
            const val USER_MEMBER_INFO = "user_member_info"
            const val USER_ID = "USER_ID" // 로그인 아이디
            const val USER_PWD = "USER_PWD" // 로그인 비밀번호
            const val DEFAULT_USER_ID = "DEFAULT_USER_ID" // 이전 로그인 한 아이디
            const val DEFAULT_USER_PWD = "DEFAULT_USER_PWD" // 이전 로그인 한 비밀번호
            const val SHAKE_FLAG = "SHAKE_FLAG" // 흔들어열기 캐시저장
            const val APP_VERSION = "APP_VERSION" // 앱 최신버전 (서버)
            const val AUTO_LOGIN = "AUTO_LOGIN" // 자동 로그인 (true, false)
            const val BIOMETRIC_LOGIN = "BIOMETRIC_LOGIN" // 생체 인증로그인
            const val PUSH_ALARM = "PUSH_ALARM" // 푸시 알림 설정
        }
    }

    /**
     * 트래킹 키 추가
     */
    class AnalyticsCode {
        companion object {
            // 기존 트래킹 (facebook, adjust, firebase)
            const val FIRST_SIGNUP = "first_signup" // 최초 회원가입

        }
    }
}