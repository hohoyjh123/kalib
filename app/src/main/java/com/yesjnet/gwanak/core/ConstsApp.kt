package com.yesjnet.gwanak.core

/**
 * Description : 프로젝트 공통 전역 상수 및 기타 클래스명에 맞는 상수 정의
 */

class ConstsApp {

    /**
     * Description : Intent Key
     */
    class IntentCode {
        companion object {
            const val UI_TRANSITION_TYPE = "ui_transition_type"
            const val DATA = "data"
            const val SEARCH = "search"
            const val TYPE = "type"
            const val MAIN_PAGE = "main_page"
            const val WEBVIEW_URL = "webview_url"
            const val PUSHDATA = "PushData"
            const val NOTIFICATION_TAG = "NOTIFICATION_TAG"
        }
    }

    /**
     * Description : activity result request code
     */
    class ReqCode {
        companion object {
            const val REQ_CODE_PICK_FROM_ALBUM = 101
            const val REQ_CODE_TAKE_PHOTO = 102
            const val REQ_CODE_CONTACT = 103
            const val PHOTO_GALLERY = 104
            const val SEND_SMS = 105
            const val REQ_CODE_NOTIFICATION = 106
        }
    }

    /**
     * Description : permission code
     */
    class PermissionCode {
        companion object {
            const val PERMISSION_CAMERA = 1
            const val PERMISSION_ALBUM = 2
            const val PERMISSION_CONTACT = 3
            const val PERMISSION_NOTIFICATION = 4
        }
    }


}