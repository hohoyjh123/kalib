package com.yesjnet.gwanak.core

import com.yesjnet.gwanak.R
import java.util.Locale

class EnumApp {

    /**
     * 로그인 타입
     * email(이메일), facebook(페이스북), google(구글), naver(네이버)
     */
    enum class LoginType {
        EMAIL, FACEBOOK, GOOGLE, NAVER;

        companion object {
            fun valueOfType(status:String) : LoginType {
                return values().firstOrNull {
                    it.toString().lowercase(Locale.getDefault()) == status
                } ?: EMAIL
            }
        }
    }

    /**
     * 공통 Flag YN 처리 이넘.
     * Y(), N()
     */
    enum class FlagYN(val flagYN: String) {
        YES("Y"), NO("N");

        companion object {
            fun valueOfStatus(flagYN: String?) : FlagYN {
                return values().firstOrNull {
                    it.flagYN == flagYN
                } ?: NO
            }
            fun booleanByStatus(flagYN: String?) : Boolean {
                return YES.flagYN == flagYN
            }
            fun flagByBoolean(boolean: Boolean) : String {
                return if(boolean) YES.flagYN else NO.flagYN
            }
        }
    }

    /**
     *  화면전환 타입 / 방향??? 등
     */
    enum class TransitionType { SLIDE, UP , FADE, NONE }

    /**
     * glide 코너 타입
     */
    enum class CornerType { ALL, TOP, BOTTOM}

    /**
     *  앱바 레이아웃 스타일
     */
    enum class AppBarStyle(val leftView: AppBarComponent, val centerView: AppBarComponent, val rightView: AppBarComponent) {
        NONE(AppBarComponent.NONE, AppBarComponent.NONE, AppBarComponent.NONE),
        TITLE(AppBarComponent.NONE, AppBarComponent.TEXT, AppBarComponent.NONE),
        BACK(AppBarComponent.BACK, AppBarComponent.NONE, AppBarComponent.NONE),
        CLOSE(AppBarComponent.NONE, AppBarComponent.NONE, AppBarComponent.CLOSE),
        BACK_TITLE(AppBarComponent.BACK, AppBarComponent.TEXT, AppBarComponent.NONE),
        BACK_TITLE_TEXT(AppBarComponent.BACK, AppBarComponent.TEXT, AppBarComponent.TEXT),
        BACK_TITLE_CLOSE(AppBarComponent.BACK, AppBarComponent.TEXT, AppBarComponent.CLOSE),
        TITLE_CLOSE(AppBarComponent.NONE, AppBarComponent.TEXT, AppBarComponent.CLOSE),
        TITLE_TEXT(AppBarComponent.NONE, AppBarComponent.TEXT, AppBarComponent.TEXT);

    }

    /**
     * 앱바 컴포넌트 =Next=> View or Drawable
     */
    enum class AppBarComponent(val defaultResId:Int, val onResId:Int) {
        NONE(0,0),
        BACK(R.drawable.menu_left_arrow,0),
        TEXT(0,0),
        CLOSE(R.drawable.menu_left_arrow,0);
    }

    /**
     *  메인 화면 하단 메뉴
     */
    enum class MainPage(
        val position: Int
    ) {
        HOME(0),
        BOOK_SEARCH(1),
        MY_LIBRARY(2),
        SETTING(3),
        MENU(4);

        companion object {
            fun getPage(page: String): MainPage {
                return values().firstOrNull {
                    it.toString().lowercase(Locale.getDefault()) == page
                } ?: HOME
            }
        }
    }

    enum class PushLandingPage(val page: String) {
        SPLASH("splash"),
        HOME("home"),
        HOME_DETAIL("home.detail"),
        SHOP("shop"),
        CAMPING("camping"),
        MYPAGE("mypage");

        companion object {
            fun valueOfLanding(landing: String) : PushLandingPage {
                return values().firstOrNull {
                    it.page == landing
                } ?: HOME
            }
        }
    }

    enum class MemberClass(val type: String) {
        ASSOCIATE_MEMBER("2"), // 준회원
        SUSPENDED_MEMBER("1"), // 정지회원
        FULL_MEMBER("0"); // 정회원

        companion object {
            fun valueOfType(type: String) : MemberClass {
                return MemberClass.values().firstOrNull {
                    it.type == type
                } ?: FULL_MEMBER
            }
        }
    }

    /**
     * 웹뷰 타입
     */
    enum class WebType(val webViewUrl: String) {
        HOME("/mobile/index.do"), // 메인 하단메뉴 > 홈으로
        BOOK_SEARCH("/mobile/search/searchSimple.do"), // 메인 하단메뉴 > 도서검색
        MY_LIBRARY("/mobile/member/mypage/myInfo.do"), // 메인 하단메뉴 > 나의 도서관
        RESOURCE_SEARCH("/mobile/search/searchSimple.do"), // 전체메뉴 > 통합자료검색
        SEARCH_NEW_INFO("/mobile/search/searchNew.do"), // 전체메뉴 > 신착자료검색
        U_LIBRARY_SEARCH("/mobile/search/searchULibrary.do"), // 전체메뉴 > U-도서관검색
        SMART_LIBRARY_SEARCH("/mobile/search/searchSmartLibrary.do"), // 전체메뉴 > 스마트도서관검색
        FREQUENTLY_READ_BOOKS("/mobile/loanbest/loanBestList.do"), // 전체메뉴 > 많이보는책
        NOTICE("/mobile/bbs/noticeList.do"), // 전체메뉴 > 공지사항
        FACILITY_INFO("/mobile/liblocation/libLocationList.do"), // 전체메뉴 > 시설정보
        READING_CULTURE_PROGRAM("/mobile/lecture/lectureList.do"), // 전체메뉴 > 독서문화프로그램
        REQUEST_DESIRED_BOOK("/mobile/hopebook/hopeBookInfo.do"), // 전체메뉴 > 희망도서신청
        FREE_DELIVERY("/mobile/parcel/parcelInfo.do"), // 전체메뉴 > 맘대로택배
        LOAN_FROM_LOCAL_BOOKSTORES("/mobile/contents/baroLoan.do"), // 전체메뉴 > 동네서점바로대출제
        E_BOOKS("/mobile/contents/ebook.do"), // 전체메뉴 > 전자책
        AUDIO_BOOKS("/mobile/contents/audioBook.do"), // 전체메뉴 > 오디오북
        MY_INFO("/mobile/member/mypage/myInfo.do"), // 전체메뉴 > 내정보
        BOOK_USAGE_HISTORY("/mobile/loan/mypage/loanStatusList.do"), // 전체메뉴 > 도서이용내역
        INTERLIBRARY_LOAN_REQUEST_HISTORY("/mobile/doorae/mypage/dooraeLillStatusList.do"), // 전체메뉴 > 상호대차신청내역
        MY_BOOKS_OF_INTEREST("/mobile/basket/mypage/basketList.do"), // 전체메뉴 > 나의관심도서
        MY_REQUEST_HISTORY("/mobile/lecture/mypage/lectureApplyList.do"), // 전체메뉴 > 나의신청내역
        REQUEST_FOR_DESIRED_BOOKS("/mobile/hopebook/mypage/hopeBookList.do"), // 전체메뉴 > 희망도서신청내역
        DELIVERY_REQUEST_DETAILS("/mobile/parcel/mypage/parcelBookApplyList.do"), // 전체메뉴 > 맘대로택배신청내역
        LOGIN(""), // 전체메뉴 > 로그인
        MOBILE_MEMBERSHIP_CARD(""), // 전체메뉴 > 모바일회원증
        EDIT_MEMBERSHIP_INFOMATION("/mobile/member/memberModifyCheck.do"), // 전체메뉴 > 회원정보수정
        CHANGE_PASSWORD("/mobile/member/memberPwdModifyCheck.do"), // 전체메뉴 > 비밀번호변경
        RE_AGREE_PERSONAL_INFOMATION("/mobile/member/mypage/memberReAgree.do"), // 전체메뉴 > 개인정보재동의
        APP_SETTINGS(""), // 전체메뉴 > 앱설정
    }
    enum class WebScheme(val scheme: String) {
        OPEN_LOGIN("kr.co.jnet.gwanak://openLogin"),
        OPEN_BARCODE("kr.co.jnet.gwanak://openBarcode"),
        APP_SETTING("kr.co.jnet.gwanak://appSetting");

    }
}