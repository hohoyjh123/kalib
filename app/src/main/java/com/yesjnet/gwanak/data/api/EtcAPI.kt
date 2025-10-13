package com.yesjnet.gwanak.data.api

import com.yesjnet.gwanak.data.model.AppVersionItem
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.PushInfo
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 기타 api
 */
interface EtcAPI {

    // 앱 버전 체크
    @FormUrlEncoded
    @POST("mobile/api/appVersionCheck.do")
    fun postAppVersionCheck(@FieldMap params: MutableMap<String, String>): Call<AppVersionItem>

    // 회원별 설정조회
    @FormUrlEncoded
    @POST("mobile/api/pushkeyInfo.do")
    fun postPushKeyInfo(@FieldMap params: MutableMap<String, String>): Call<PushInfo>

    // 디바이스 정보저장
    @FormUrlEncoded
    @POST("mobile/api/pushkey.do")
    fun postPushKey(@FieldMap params: MutableMap<String, String>): Call<DeviceInfo>

    // 알림설정
    @FormUrlEncoded
    @POST("mobile/api/updatePushkey.do")
    fun postUpdatePushKey(@FieldMap params: MutableMap<String, String>): Call<DeviceInfo>
}