package com.yesjnet.gwanak.ui

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
import com.yesjnet.gwanak.data.model.AppVersionItem
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.data.net.APIResource
import com.yesjnet.gwanak.data.net.APIResult
import com.yesjnet.gwanak.data.net.ErrorResource
import com.yesjnet.gwanak.data.net.Response
import com.yesjnet.gwanak.data.repository.EtcRepository
import com.yesjnet.gwanak.data.repository.MemberRepository
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.base.BaseViewModel
import com.yesjnet.gwanak.util.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class IntroViewModel(
    private val application: KJApplication,
    private val etcRepo: EtcRepository,
    private val memberRepo: MemberRepository,
    val appInfo: AppInfo,
    val userInfo: UserInfo,
    private val pref: SecurePreference,
) : BaseViewModel(application) {

    val onShow: MutableLiveData<Boolean>
        get() = inShow
    private val inShow: MutableLiveData<Boolean> = MutableLiveData()

    // 자동로그인 실패(웹사이트 비밀번호 변경)
    val onAutoLoginDialog: MutableLiveData<String>
        get() = inAutoLoginDialog
    private val inAutoLoginDialog: MutableLiveData<String> = MutableLiveData()


    // 로그아웃
    val onLogout: MutableLiveData<Boolean>
        get() = inLogout
    private val inLogout: MutableLiveData<Boolean> = MutableLiveData()

    // 버전 체크
    val onIsForceUpdate: MutableLiveData<Boolean>
        get() = inIsForceUpdate
    private val inIsForceUpdate: MutableLiveData<Boolean> = MutableLiveData()

    fun setShow(isShow: Boolean) {
        inShow.value = isShow

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
//            inNaviScreen.value = NavScreen.Main()
            postAppVersionCheck()
        }
    }

    fun updateNavScreen(navScreen: NavScreen<ScreenInfo>) {
        inNaviScreen.value = navScreen
    }

    fun logout() {
        CookieManager.getInstance().removeAllCookie()
        userInfo.clearUserInfo()
        appInfo.removeLoginInfo()
        pref.clearAll(SecurePreference.CONFIG_PREF_NAME)
        EventBus.getDefault().post(EBMemberInfo(MemberInfo()))
    }

    fun saveFirebaseInstanceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.w("[FCM] FirebaseInstanceId failed", task.exception)
                val params = hashMapOf<String, String>()
                task.exception?.message?.let {
                    params["exception"] = it
                }
                return@OnCompleteListener
            }

            // Get new Instance ID token
            val token = task.result
            token?.let { appInfo.storeFCMDeviceToken(it) }
        })
    }

    /**
     * 앱 버전 체크 api
     */
    fun postAppVersionCheck() {
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.AGENT] = "1"
            this[ConstsData.ReqParam.APP_VERSION] = BuildConfig.VERSION_NAME
        }
        val call = etcRepo.postAppVersionCheck(params)
        val response = Response.create(call, object : APIResult<AppVersionItem> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<AppVersionItem>) {
                pref.setStrValue(ConstsData.PrefCode.APP_VERSION, resource.resBase.version.appVersion)
                inIsForceUpdate.value = resource.resBase.status == "terminated"
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
                // todo 테스트 후 주석
//                inIsForceUpdate.value = false
            }

        })
        call.enqueue(response)
    }

    /**
     * 로그인 api
     * @param id 아이디
     * @param pwd 비밀번호
     */
    fun postAppLogin(id: String, pwd: String) {
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.USER_ID] = id
            this[ConstsData.ReqParam.PASSWORD] = pwd
        }
        val call = memberRepo.postAppLogin(params)
        val response = Response.create(call, object : APIResult<MemberInfo> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<MemberInfo>) {
                Logger.d("resource = $resource")
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.flag)) {
                    userInfo.setMember(resource.resBase)
                    appInfo.setLoginInfo(id, pwd)
                    postPushKey(id, resource.resBase.recKey.toString())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionUtil.requestNotifications {
                            Logger.d("notification true")
                        }
                    }
                } else {
                    inAutoLoginDialog.value = application.getString(R.string.auto_login_fail_error)
                }
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }

    /**
     * 디바이스 정보저장 api
     */
    fun postPushKey(id: String, userKey: String) {
        val deviceToken = appInfo.getFCMDeviceToken()
        if (deviceToken.isEmpty()) {
            Logger.d("fcm deviceToken is empty")
            return
        }
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.AGENT] = "1"
            this[ConstsData.ReqParam.USER_ID] = id
            this[ConstsData.ReqParam.FCM] = deviceToken
            this[ConstsData.ReqParam.USER_KEY] = userKey
        }
        Logger.d("fcm = $deviceToken")
        val call = etcRepo.postPushKey(params)
        val response = Response.create(call, object : APIResult<DeviceInfo> {
            override fun onLoading(isLoading: Boolean) {
                inDataLoading.value = isLoading
            }

            override fun onSuccess(resource: APIResource<DeviceInfo>) {
                Logger.d("resource = $resource")
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.flag)) {
                    inNaviScreen.value = NavScreen.Main()
                } else {
                    inShowMsgDialog.value = application.getString(R.string.device_info_error)
                }
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }

    /**
     * 로그아웃 api
     */
    fun postAppLogout(memberInfo: MemberInfo) {
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
                    inNaviScreen.value = NavScreen.Main()
                }
            }

            override fun onError(errorResource: ErrorResource) {
                inErrorResource.value = errorResource
            }

        })
        call.enqueue(response)
    }

}