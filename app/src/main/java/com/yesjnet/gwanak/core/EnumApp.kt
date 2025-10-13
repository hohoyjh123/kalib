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
        BACK(R.drawable.icon_back,0),
        TEXT(0,0),
        CLOSE(R.drawable.icon_back,0);
    }

    /**
     *  메인 화면 하단 메뉴
     */
    enum class MainPage(
        val position: Int
    ) {
        HOME(0),
        BOOK_SEARCH(1),
        MEMBERSHIP_CARD(2),
        LOAN_STATUS(3),
        BOOK_INTEREST(4);

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
        ASSOCIATE_MEMBER("2"),
        FULL_MEMBER("0");

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
        BOOK_SEARCH("/mobile/search/plusSearchSimple.do"), // 메인 하단메뉴 > 도서검색
        LOAN_STATUS("/mobile/mylib/reservationStatusList.do"), // 메인 하단메뉴 > 대출예약현황
        BOOK_INTEREST("/mobile/mylib/basketList.do"); // 메인 하단메뉴 > 관심도서
    }
    enum class WebScheme(val scheme: String) {
        OPEN_LOGIN("kr.co.jnet.gjlib://openLogin"),
        OPEN_BARCODE("kr.co.jnet.gjlib://openBarcode"),
        APP_SETTING("kr.co.jnet.gjlib://appSetting");

    }
}