package com.yesjnet.gwanak.ui.main

import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.PushData
import com.yesjnet.gwanak.data.model.eventbus.EBFinish
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.ScreenInfo
import com.yesjnet.gwanak.ui.base.BaseViewModel
import org.greenrobot.eventbus.EventBus
import java.net.URLEncoder

class AllMenuViewModel(
    private val application: GAApplication,
    private val appInfo: AppInfo,
    private val userInfo: UserInfo,
    private val pref: SecurePreference,
) : BaseViewModel(application) {

    // 로그인 정보
    val onMemberInfo: MutableLiveData<MemberInfo>
        get() = inMemberInfo
    private val inMemberInfo: MutableLiveData<MemberInfo> = MutableLiveData()

    // 미로그인
    val onLoginError: MutableLiveData<String>
        get() = inLoginError
    private val inLoginError: MutableLiveData<String> = MutableLiveData()

    init {
        inMemberInfo.value = userInfo.getMember()
    }

    fun updateMemberInfo(memberInfo: MemberInfo) {
        inMemberInfo.value = memberInfo
    }

    /**
     * 종료 클릭 이벤트
     */
    fun onClickFinish() {
        EventBus.getDefault().post(EBFinish(true))
    }

    /**
     * 로그인 클릭 이벤트
     */
    fun onClickLogin() {
        Logger.d("onClickLogin")
        inNaviScreen.value = NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
    }

    /**
     * 마이페이지 클릭 이벤트
     */
    fun onClickMyPage(memberInfo: MemberInfo) {
        Logger.d("onClickMyPage = ${memberInfo.userId}")
        inNaviScreen.value = NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
    }

//    /**
//     * 통합자료검색 클릭이벤트
//     */
//    fun onClickResourceSearch() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/searchSimple.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.INTEGRATED_RESOURCE_SEARCH)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 신착자료검색 클릭이벤트
//     */
//    fun onClickSearchNewInfo() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/accessionBookList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.NEW_INFO_SEARCH)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 희망도서신청 클릭이벤트
//     */
//    fun onClickRequestDesiredBook() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/hopeBook/contents.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.REQUEST_DESIRED_BOOK)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 사서추천도서 클릭이벤트
//     */
//    fun onClickLibRecommendBooks() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/recommendBookList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.LIB_RECOMMEND_BOOKS)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 최다대출도서 클릭이벤트
//     */
//    fun onClickMaximumLoanBooks() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/popularBookMonthList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.MAXIMUN_LOAN_BOOKS)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 전자책 클릭이벤트
//     */
//    fun onClickEbook(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/ebook.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.EBOOK)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 오디오북 클릭이벤트
//     */
//    fun onClickAudioBooks() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/audien.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.AUDIO_BOOK)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//
//    }
//
//    /**
//     * 중랑구도서관현황 클릭이벤트
//     */
//    fun onClickJrLibStatus() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/libInfoList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.JUNGRANG_LIB_STATUS)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 열람좌석정보 클릭이벤트
//     */
//    fun onClickViewSeatInfo() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/maSeatStatus.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.VIEW_SEAT_INFO)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 문화행사신청 클릭이벤트
//     */
//    fun onClickApplyCulturalEvent() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/eventLectureList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.APPLY_CULTURAL_EVENT)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 중랑북클럽 사용신청 클릭이벤트
//     */
//    fun onClickJrBookClub() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/space/contents.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.JUNGRANG_BOOK_CLUB)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 문화행사신청 작은 클릭이벤트
//     */
//    fun onClickSmallCultural() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/smallLectureList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.APPLY_CULTURAL_EVENT_SMALL)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 도서관소식 클릭이벤트
//     */
//    fun onClickLibNews() {
//        val intent = Intent()
//        val memberInfo = userInfo.getMember()
//        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/20005/1/bbsPostList.do", "utf-8")}"
//        intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.LIB_NEWS)
//        intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//        inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//    }
//
//    /**
//     * 내주변 도서관 클릭이벤트
//     */
//    fun onClickLibNearMe() {
//        val isTrue = application.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
//        if (isTrue) {
//            val isEnabledLocationService = GpsUtil.instance.isEnabledGPS()
//            if (!isEnabledLocationService) {
//                GpsUtil.instance.requestTurnOnGPS()
//            }
//
//            val intent = Intent()
//            val memberInfo = userInfo.getMember()
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/libraryMap.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.LIB_NEAR_ME)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        } else {
//            PermissionUtil.requestLocation {
//                val isEnabledLocationService = GpsUtil.instance.isEnabledGPS()
//                if (!isEnabledLocationService) {
//                    GpsUtil.instance.requestTurnOnGPS()
//                }
//
//                val intent = Intent()
//                val memberInfo = userInfo.getMember()
//                val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/libraryMap.do", "utf-8")}"
//                intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.LIB_NEAR_ME)
//                intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//                inNaviScreen.value = NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//            }
//        }
//    }
//
//    /**
//     * 대출현황조회 클릭이벤트
//     */
//    fun onClickLoanStatusInquiry(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/mypage/loanStatusList.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.LOAN_STATUS)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 희망도서신청조회 클릭이벤트
//     */
//    fun onClickDesiredBook(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/mypage/hopeBookList.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.INQUIRY_REQUEST_DESIRED_BOOKS)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 상호대차 클릭이벤트
//     */
//    fun onClickInterLibLoan(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/mypage/mutualLoanStatusList.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.INTERLIB_LOAN)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 관심도서 클릭이벤트
//     */
//    fun onClickBooksInterest(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/mypage/bookcaseMain.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.BOOKS_INTEREST)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 중랑북클럽신청조회 클릭이벤트
//     */
//    fun onClickMyAppInquiry(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/member/appReLogin.do?userId=${URLEncoder.encode(memberInfo.userId, "utf-8")}&returnUrl=${URLEncoder.encode("/mobile/mypage/spaceApplyList.do", "utf-8")}"
//            intent.putExtra(ConstsApp.IntentCode.TYPE, EnumApp.WebType.MY_APPLICATION_INQUIRY)
//            intent.putExtra(ConstsApp.IntentCode.WEBVIEW_URL, webUrl)
//            NavScreen.CustomWebView(ScreenInfo(intent, transType = EnumApp.TransitionType.UP))
//        }
//    }
//
//    /**
//     * 알림설정 클릭이벤트
//     */
//    fun onClickNotificationSetting(memberInfo: MemberInfo?) {
//        val intent = Intent()
//        inNaviScreen.value = if (memberInfo?.userId.isNullOrEmpty()) {
//            NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        } else {
//            NavScreen.AlarmSetting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
//        }
//    }
    fun onClickMenu(webType: EnumApp.WebType?) {
        webType?.let { type ->
            Logger.d("webType = ${type.name} url = ${type.webViewUrl}" )
            when (type) {
                // 로그인
                EnumApp.WebType.LOGIN -> {
                    inNaviScreen.value = NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
                }
                // 모바일 회원증
                EnumApp.WebType.MOBILE_MEMBERSHIP_CARD -> {
                    inNaviScreen.value = NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
                }
                // 앱설정
                EnumApp.WebType.APP_SETTINGS -> {
                    inNaviScreen.value = NavScreen.Setting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
                }
                else -> {
                    val memberInfo = onMemberInfo.value
//                    if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
//                        inLoginError.value = GAApplication.app.getString(R.string.available_after_logging_in)
//                    } else {
                        val webUrl = "${ConstsData.SERVER_URL_FULL}mobile/api/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId, "utf-8")}&returnUrl=${URLEncoder.encode(type.webViewUrl, "utf-8")}"
                        val pushData = PushData(title = "", message = "", url = webUrl)
                        EventBus.getDefault().post(pushData)
                        EventBus.getDefault().post(EBFinish(true))
//                    }

                }
            }
        }

    }

}