package com.yesjnet.gwanak.data.repository

import com.yesjnet.gwanak.data.api.MemberAPI
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import retrofit2.Call

/**
 * 회원정보 repository
 */
class MemberRepository constructor(
        private val memberAPI: MemberAPI
) {
    // 로그인
    fun postAppLogin(params: MutableMap<String, String>): Call<MemberInfo> {
        return memberAPI.postAppLogin(params)
    }

    // 로그아웃
    fun postAppLogout(params: MutableMap<String, String>): Call<DeviceInfo> {
        return memberAPI.postAppLogout(params)
    }

}