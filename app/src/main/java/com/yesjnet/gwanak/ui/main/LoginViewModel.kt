package com.yesjnet.gwanak.ui.main

import android.os.Build
import android.webkit.CookieManager
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.Family
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.data.net.APIResource
import com.yesjnet.gwanak.data.net.APIResult
import com.yesjnet.gwanak.data.net.ErrorResource
import com.yesjnet.gwanak.data.net.Response
import com.yesjnet.gwanak.data.repository.EtcRepository
import com.yesjnet.gwanak.data.repository.MemberRepository
import com.yesjnet.gwanak.extension.browse
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.base.BaseViewModel
import com.yesjnet.gwanak.util.PermissionUtil
import org.greenrobot.eventbus.EventBus

/**
 * 회원증 뷰모델
 */

class LoginViewModel(
    private val application: GAApplication,
    private val etcRepo: EtcRepository,
    private val memberRepo: MemberRepository,
    val userInfo: UserInfo,
    private val appInfo: AppInfo,
    private val pref: SecurePreference,
) : BaseViewModel(application) {

    // id 입력값
    val onBindId: MutableLiveData<String>
        get() = inBindId
    private val inBindId: MutableLiveData<String> = MutableLiveData()

    // 비밀번호 입력값
    val onBindPwd: MutableLiveData<String>
        get() = inBindPwd
    private val inBindPwd: MutableLiveData<String> = MutableLiveData()

    // 모바일회원증 왼쪽 버튼 클릭
    val onLeftClick: MutableLiveData<Boolean>
        get() = inLeftClick
    private val inLeftClick: MutableLiveData<Boolean> = MutableLiveData()

    // 모바일회원증 오른쪽 버튼 클릭
    val onRightClick: MutableLiveData<Boolean>
        get() = inRightClick
    private val inRightClick: MutableLiveData<Boolean> = MutableLiveData()

    // 로그인 정보
    val onMemberInfo: MutableLiveData<MemberInfo>
        get() = inMemberInfo
    private val inMemberInfo: MutableLiveData<MemberInfo> = MutableLiveData()

    // fcm token 복사 이벤트
    val onCopyFcmToken: MutableLiveData<String>
        get() = inCopyFcmToken
    private val inCopyFcmToken: MutableLiveData<String> = MutableLiveData()

    // fcm token
    val onFcmToken: MutableLiveData<String>
        get() = inFcmToken
    private val inFcmToken: MutableLiveData<String> = MutableLiveData()

    // 로그인 성공 팝업
    val onShowLoginDialog: MutableLiveData<String>
        get() = inShowLoginDialog
    private val inShowLoginDialog: MutableLiveData<String> = MutableLiveData()

    // 바코드 팝업
    val onShowBarcodeDialog: MutableLiveData<Family>
        get() = inShowBarcodeDialog
    private val inShowBarcodeDialog: MutableLiveData<Family> = MutableLiveData()

    // 자동로그인
    val onAutoLogin: MutableLiveData<Boolean>
        get() = inAutoLogin
    private val inAutoLogin: MutableLiveData<Boolean> = MutableLiveData()

    init {
        // todo 자동로그인 캐시 활용
        inAutoLogin.value = false
        userInfo.getMember()?.let { updateMemberInfo(it) }

        val userId = pref.getStrValue(ConstsData.PrefCode.DEFAULT_USER_ID, "")
        val userPwd = pref.getStrValue(ConstsData.PrefCode.DEFAULT_USER_PWD, "")
        inBindId.value = userId
        inBindPwd.value = userPwd
    }

    fun updateMemberInfo(memberInfo: MemberInfo?) {
        this.inMemberInfo.value = memberInfo
    }

    fun updateAutoLogin(autoLogin: Boolean) {
        inAutoLogin.postValue(autoLogin)
    }

    fun clearIdPwd() {
        inBindId.value = ""
        inBindPwd.value = ""
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

    fun logout() {
        CookieManager.getInstance().removeAllCookie()
        userInfo.clearUserInfo()
        appInfo.removeLoginInfo()
        pref.clearAll(SecurePreference.CONFIG_PREF_NAME)
        updateMemberInfo(MemberInfo())
        EventBus.getDefault().post(EBMemberInfo(MemberInfo()))
    }


    /**
     * 아이디 입력 삭제 클릭 이벤트
     */
    fun onClickIdClear() {
        inBindId.value = ""
    }

    /**
     * 비밀번호 입력 삭제 클릭 이벤트
     */
    fun onClickPwdClear() {
        inBindPwd.value = ""
    }

    /**
     * 로그인 클릭 이벤트
     */
    fun onClickLogin(id: String?, pwd: String?) {
        when {
            id.isNullOrEmpty() -> inShowMsgDialog.value = application.getString(R.string.login_fail_error)
            pwd.isNullOrEmpty() -> inShowMsgDialog.value = application.getString(R.string.login_fail_error)
            else -> {
                postAppLogin(id, pwd)
            }
        }
    }

    /**
     * 모바일 회원증 viewpager 왼쪽 버튼 클릭 이벤트
     */
    fun onClickLeft() {
        inLeftClick.value = true
    }

    /**
     * 모바일 회원증 viewpager 오른쪽 버튼 클릭 이벤트
     */
    fun onClickRight() {
        inRightClick.value = true
    }

    /**
     * FCM TOKEN 복사 클릭이벤트
     */
    fun onFcmCopyClick(fcmToken: String) {
        inCopyFcmToken.postValue(fcmToken)
    }

    /**
     * 회원가입 클릭 이벤트
     */
    fun onClickSignup() {
        application.browse(ConstsData.SIGNUP_URL, true)
    }

    /**
     * 아이디 찾기 클릭 이벤트
     */
    fun onClickFindId() {
        application.browse(ConstsData.FIND_ID_URL, true)
    }

    /**
     * 비밀번호 재발급 클릭 이벤트
     */
    fun onClickFindPwd() {
        application.browse(ConstsData.FIND_PWD_URL, true)
    }

    /**
     * 바코드 클릭 이벤트
     */
    fun onClickBarcode(item: Family) {
        inShowBarcodeDialog.value = item
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
                    clearIdPwd()
                    inShowLoginDialog.value = application.getString(R.string.login_success_msg)
                    userInfo.setMember(resource.resBase)
                    appInfo.setLoginInfo(id, pwd)
                    updateMemberInfo(resource.resBase)
                    EventBus.getDefault().post(EBMemberInfo(resource.resBase))
                    postPushKey(id, resource.resBase.recKey.toString())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionUtil.requestNotifications {
                            Logger.d("notification true")
                        }
                    }
                } else {
                    inShowMsgDialog.value = application.getString(R.string.login_fail_error)
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

}