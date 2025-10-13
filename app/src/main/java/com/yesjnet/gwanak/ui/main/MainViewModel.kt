package com.yesjnet.gwanak.ui.main

import android.webkit.CookieManager
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.KJApplication
import com.yesjnet.gwanak.core.UserInfo
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
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.base.BaseViewModel
import org.greenrobot.eventbus.EventBus

class MainViewModel(
    private val application: KJApplication,
    private val appInfo: AppInfo,
    private val etcRepo: EtcRepository,
    private val memberRepo: MemberRepository,
    val userInfo: UserInfo,
    private val pref: SecurePreference,
) : BaseViewModel(application) {

    val onMemberInfo: MutableLiveData<MemberInfo>
        get() = inMemberInfo
    private val inMemberInfo: MutableLiveData<MemberInfo> = MutableLiveData()

    val onSelectTab: MutableLiveData<EnumApp.MainPage>
        get() = inSelectTab
    private val inSelectTab: MutableLiveData<EnumApp.MainPage> = MutableLiveData()

    val onAutoLoginError: MutableLiveData<String>
        get() = inAutoLoginError
    private val inAutoLoginError: MutableLiveData<String> = MutableLiveData()

    init {
        userInfo.getMember()?.let { updateMemberInfo(it) } ?: updateMemberInfo(MemberInfo())
    }

    fun updateMemberInfo(memberInfo: MemberInfo?) {
        this.inMemberInfo.value = memberInfo
    }

    /**
     * 하단 탭 클릭 이벤트
     */
    fun setSelectTab(page: EnumApp.MainPage) {
        inSelectTab.value = page
    }

    fun logout() {
        CookieManager.getInstance().removeAllCookie()
        userInfo.clearUserInfo()
        appInfo.removeLoginInfo()
        EventBus.getDefault().post(EBMemberInfo(MemberInfo()))
    }

    /**
     * 자동 로그인 체크
     */
    fun checkAutoLogin() {
        val userId = appInfo.getLoginInfo()?.userId
        val userPwd = appInfo.getLoginInfo()?.userPwd
        if (!userId.isNullOrEmpty() && !userPwd.isNullOrEmpty()) {
            Logger.d("jihoon autologin")
            postAppLogin(userId, userPwd)
        } else {
            Logger.d("jihoon autologin nonono")
        }
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
                    EventBus.getDefault().post(EBMemberInfo(resource.resBase))
                    postPushKey(id, resource.resBase.recKey.toString())
                } else {
                    logout()
                    inAutoLoginError.value = application.getString(R.string.auto_login_fail_error)
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