package com.yesjnet.gwanak.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.yesjnet.gwanak.core.ConstsApp
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.NavScreenNotDefiendException
import com.yesjnet.gwanak.extension.slideTransitionStart
import com.yesjnet.gwanak.extension.slideTransitionUpStart
import com.yesjnet.gwanak.ui.base.BaseActivity
import com.yesjnet.gwanak.ui.base.BaseFragment
import com.yesjnet.gwanak.ui.main.LoginActivity
import com.yesjnet.gwanak.ui.main.MainActivity
import com.yesjnet.gwanak.ui.main.SettingActivity

/**
 *  화면전환 관리 네비게이터 Class
 *  1. 네비게이션 화면 정의 - NavScreen SealedClass
 *  2. 화면 데이터 정의 - ScreenInfo 기반
 *  3. 공통 구현 - startScreen 내 분기처리 및 공통 프로세스 등
 *  4. 이동처리 구현 - Intent,Flag,Transition 등
 */

/**
 *  1. 네비게이션 화면 정의
 */
sealed class NavScreen<out T> {

    //  Login Auth
    class Intro<out T>(val screenInfo: T? = null) : NavScreen<T>()
    class Main<out T>(val screenInfo: T? = null) : NavScreen<T>()
    class Login<out T>(val screenInfo: T? = null) : NavScreen<T>()
    class Setting<out T>(val screenInfo: T? = null) : NavScreen<T>()

    // system 이동처리
    class SystemMove<out T>(val screenInfo: T? = null) : NavScreen<T>()
}

/**
 *  2. 네비게이션 화면 정의
 */
data class ScreenInfo(
    val intent: Intent? = null,
    val requestCode:Int = 0,
    val transType: EnumApp.TransitionType ?= null,
    val options: ActivityOptionsCompat? = null,
    val resultLauncher: ActivityResultLauncher<Intent>? = null
) {
    // Do nothing
}

/**
 *  3. 화면 이동 기본 처리 메소드
 *  => Navigator Major Method
 */
fun BaseActivity<*>.startScreen(navSc: NavScreen<ScreenInfo?>) {

    when (navSc) {
        is NavScreen.Intro -> startIntroActivity(navSc.screenInfo)
        is NavScreen.Main -> startMainActivity(navSc.screenInfo)
        is NavScreen.Login -> startLoginActivity(navSc.screenInfo)
        is NavScreen.Setting -> startSettingActivity(navSc.screenInfo)

        is NavScreen.SystemMove -> startSystemMove(navSc.screenInfo)

        else -> throw NavScreenNotDefiendException("Navigator UnDefined NavScreen !! ")
    }
}

fun BaseFragment<*>.startScreen(navSc: NavScreen<ScreenInfo?>) {
    (activity as? BaseActivity<*>)?.startScreen(navSc)
}

fun BaseActivity<*>.startActivityCommon(intent: Intent, screenInfo: ScreenInfo?) {
    screenInfo?.also {screen ->
        screen.intent?.let {
            intent.putExtras(it)
            intent.flags = it.flags
            intent.action = it.action
        }
        screen.transType?.let {
            intent.putExtra(ConstsApp.IntentCode.UI_TRANSITION_TYPE,it)
        }
        if(screen.requestCode > 0) {
            screen.resultLauncher?.launch(screen.intent) ?: startActivityForResult(intent,screen.requestCode)
        } else {
            screen.options?.let { options ->
                ActivityCompat.startActivity(this, intent, options.toBundle())
            } ?: run {
                startActivity(intent)
            }
        }
        screen.transType?.let {
            when(it) {
                EnumApp.TransitionType.SLIDE -> slideTransitionStart()
                EnumApp.TransitionType.UP -> slideTransitionUpStart()
                else -> {}
            }
        }
    } ?: startActivity(intent)

}

/**
 *  4. 각 화면 실제 이동 처리 메소드
 */
fun BaseActivity<*>.startIntroActivity(screenInfo: ScreenInfo?) {
    val intent = Intent(this, IntroActivity::class.java)
    startActivityCommon(intent,screenInfo)
}
fun BaseActivity<*>.startMainActivity(screenInfo: ScreenInfo?) {
    val intent = Intent(this, MainActivity::class.java)
    startActivityCommon(intent,screenInfo)
}
fun BaseActivity<*>.startLoginActivity(screenInfo: ScreenInfo?) {
    val intent = Intent(this, LoginActivity::class.java)
    startActivityCommon(intent,screenInfo)
}
fun BaseActivity<*>.startSettingActivity(screenInfo: ScreenInfo?) {
    val intent = Intent(this, SettingActivity::class.java)
    startActivityCommon(intent,screenInfo)
}

fun BaseActivity<*>.startSystemMove(screenInfo: ScreenInfo?) {
    startActivityCommon(Intent(), screenInfo)
}



