package com.yesjnet.gwanak.ui.main

import android.os.Build
import android.webkit.CookieManager
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.BuildConfig
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.KJApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.Family
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.PushInfo
import com.yesjnet.gwanak.data.model.eventbus.EBLogout
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.data.net.APIResource
import com.yesjnet.gwanak.data.net.APIResult
import com.yesjnet.gwanak.data.net.ErrorResource
import com.yesjnet.gwanak.data.net.Response
import com.yesjnet.gwanak.data.repository.EtcRepository
import com.yesjnet.gwanak.data.repository.MemberRepository
import com.yesjnet.gwanak.extension.browse
import com.yesjnet.gwanak.lifecycle.MultipleLiveEvent
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.ScreenInfo
import com.yesjnet.gwanak.ui.base.BaseViewModel
import com.yesjnet.gwanak.ui.startScreen
import com.yesjnet.gwanak.util.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * 회원증 뷰모델
 */

class SettingViewModel(
    private val application: KJApplication,
    private val etcRepo: EtcRepository,
    private val memberRepo: MemberRepository,
    val userInfo: UserInfo,
    private val appInfo: AppInfo,
    private val pref: SecurePreference,
) : BaseViewModel(application) {

    val onIsChangeReady: MultipleLiveEvent<Boolean>
        get() = inIsChangeReady
    private val inIsChangeReady: MultipleLiveEvent<Boolean> = MultipleLiveEvent()

    // 로그인 정보
    val onMemberInfo: MutableLiveData<MemberInfo>
        get() = inMemberInfo
    private val inMemberInfo: MutableLiveData<MemberInfo> = MutableLiveData()

    // 자동로그인
    val onAutoLogin: MutableLiveData<Boolean>
        get() = inAutoLogin
    private val inAutoLogin: MutableLiveData<Boolean> = MutableLiveData()

    // 생체인증 로그인
    val onBiometricLogin: MutableLiveData<Boolean>
        get() = inBiometricLogin
    private val inBiometricLogin: MutableLiveData<Boolean> = MutableLiveData()

    // 푸시알림 설정
    val onPushAlarm: MutableLiveData<Boolean>
        get() = inPushAlarm
    private val inPushAlarm: MutableLiveData<Boolean> = MutableLiveData()

    // 흔들어열기 설정
    val onShakeOpen: MutableLiveData<Boolean>
        get() = inShakeOpen
    private val inShakeOpen: MutableLiveData<Boolean> = MutableLiveData()

    // 현재 버전
    val onCurrentVersion: MutableLiveData<String>
        get() = inCurrentVersion
    private val inCurrentVersion: MutableLiveData<String> = MutableLiveData()

    // 앱 최신 버전
    val onLatestVersion: MutableLiveData<String>
        get() = inLatestVersion
    private val inLatestVersion: MutableLiveData<String> = MutableLiveData()

    // fcm token(개발 버전만 노출)
    val onFcmToken: MutableLiveData<String>
        get() = inFcmToken
    private val inFcmToken: MutableLiveData<String> = MutableLiveData()

    // fcm token 복사 이벤트
    val onCopyFcmToken: MutableLiveData<String>
        get() = inCopyFcmToken
    private val inCopyFcmToken: MutableLiveData<String> = MutableLiveData()


    init {
        userInfo.getMember()?.let { updateMemberInfo(it) } ?: updateMemberInfo(MemberInfo())
        inCurrentVersion.postValue(BuildConfig.VERSION_NAME)
        inLatestVersion.postValue(pref.getStrValue(ConstsData.PrefCode.APP_VERSION, ""))
        inAutoLogin.postValue(pref.getConfigBool(ConstsData.PrefCode.AUTO_LOGIN, false))
        inBiometricLogin.postValue(pref.getConfigBool(ConstsData.PrefCode.BIOMETRIC_LOGIN, false))
        inPushAlarm.postValue(pref.getConfigBool(ConstsData.PrefCode.PUSH_ALARM, false))
        inShakeOpen.postValue(pref.getConfigBool(ConstsData.PrefCode.SHAKE_FLAG, false))

    }

    fun updateMemberInfo(memberInfo: MemberInfo?) {
        this.inMemberInfo.value = memberInfo
    }

    fun updateIsChangeReady(isChangeReady: Boolean) {
        inIsChangeReady.value = isChangeReady
    }

    fun getIsChangeReady(): Boolean {
        return inIsChangeReady.value ?: false
    }

    fun logout() {
        CookieManager.getInstance().removeAllCookie()
        userInfo.clearUserInfo()
        appInfo.removeLoginInfo()
        pref.clearAll(SecurePreference.CONFIG_PREF_NAME)
        updateMemberInfo(MemberInfo())
        EventBus.getDefault().post(EBMemberInfo(MemberInfo()))
    }

    fun onClickLoginCheck(memberInfo: MemberInfo?) {
        if (memberInfo?.userId.isNullOrEmpty()) {
            inNaviScreen.value = NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))
        } else {
            postAppLogout(memberInfo!!)
        }
    }

    /**
     * FCM TOKEN 복사 클릭이벤트
     */
    fun onFcmCopyClick(fcmToken: String) {
        inCopyFcmToken.postValue(fcmToken)
    }

    /**
     * 회원별 설정조회
     */
    fun postPushKeyInfo(userId: String) {
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.USER_ID] = userId
        }
        val call = etcRepo.postPushKeyInfo(params)
        val response = Response.create(call, object : APIResult<PushInfo> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<PushInfo>) {
                Logger.d("resource = $resource")
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.push1Yn)) {
                    inPushAlarm.postValue(true)
                } else {
                    inPushAlarm.postValue(false)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(696)
                    inIsChangeReady.value = true
                }
            }

            override fun onError(errorResource: ErrorResource) {
                // todo api 테스트후 삭제
                CoroutineScope(Dispatchers.Main).launch {
                    delay(696)
                    inIsChangeReady.value = true
                }
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }


    /**
     * 로그아웃 api
     */
    private fun postAppLogout(memberInfo: MemberInfo) {
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.FCM] = appInfo.getFCMDeviceToken()
            this[ConstsData.ReqParam.USER_KEY] = memberInfo.recKey.toString()
        }
        Logger.d("params = $params")
        val call = memberRepo.postAppLogout(params)
        val response = Response.create(call, object : APIResult<DeviceInfo> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<DeviceInfo>) {
                Logger.d("resource = $resource")
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.flag)) {
//                    val loginInfo = appInfo.getLoginInfo()
//                    loginInfo?.let {
//                        pref.setStrValue(ConstsData.PrefCode.DEFAULT_USER_ID, it.userId)
//                        pref.setStrValue(ConstsData.PrefCode.DEFAULT_USER_PWD, it.userPwd)
//                        EventBus.getDefault().post(EBLogout(it.userId, it.userPwd))
//                    }
                    logout()
                }
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }

    /**
     * 알림설정
     */
    fun postUpdatePushKey(userId: String, switch1: String) {
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.USER_ID] = userId
            this[ConstsData.ReqParam.SWITCH1] = switch1
        }
        val call = etcRepo.postUpdatePushKey(params)
        val response = Response.create(call, object : APIResult<DeviceInfo> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<DeviceInfo>) {
                Logger.d("resource = $resource")
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.flag)) {

                } else {

                }
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }

}