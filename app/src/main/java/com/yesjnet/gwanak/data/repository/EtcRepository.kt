package com.yesjnet.gwanak.data.repository

import com.yesjnet.gwanak.data.api.EtcAPI
import com.yesjnet.gwanak.data.model.AppVersionItem
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.PushInfo
import retrofit2.Call

/**
 * 기타 repository
 */
class EtcRepository constructor(
        private val etcAPI: EtcAPI
) {

    // 앱 버전 체크
    fun postAppVersionCheck(params: MutableMap<String, String>): Call<AppVersionItem> {
        return etcAPI.postAppVersionCheck(params)
    }

    // 회원별 설정조회
    fun postPushKeyInfo(params: MutableMap<String, String>): Call<PushInfo> {
        return etcAPI.postPushKeyInfo(params)
    }

    // 디바이스 정보저장
    fun postPushKey(params: MutableMap<String, String>): Call<DeviceInfo> {
        return etcAPI.postPushKey(params)
    }

    // 알림설정
    fun postUpdatePushKey(params: MutableMap<String, String>): Call<DeviceInfo> {
        return etcAPI.postUpdatePushKey(params)
    }

}