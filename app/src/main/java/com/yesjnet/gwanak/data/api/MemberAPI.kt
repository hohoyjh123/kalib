package com.yesjnet.gwanak.data.api

import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 회원정보 api
 */
interface MemberAPI {

    // 로그인
    @FormUrlEncoded
    @POST("mobile2/api/appLogin.do")
    fun postAppLogin(@FieldMap params: MutableMap<String, String>): Call<MemberInfo>

    // 로그아웃
    @FormUrlEncoded
    @POST("mobile2/api/appLogout.do")
    fun postAppLogout(@FieldMap params: MutableMap<String, String>): Call<DeviceInfo>
}