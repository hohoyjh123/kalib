package com.yesjnet.gwanak.data.net

import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.ConstsData.Companion.TAG_NET
import okhttp3.Response
import okhttp3.Interceptor
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 *  Token Refresh Process Interceptor
 *  서버 Response HTTP CODE 401시에 토큰이 만료 되었으므로 토큰을 요청하여 토큰을 갱신한다.
 */
class NetInterceptor : Interceptor , KoinComponent {

    private val mAppInfo: AppInfo by inject()
    companion object {
        var isRetryReqAutToken = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val oriRequst = chain.request()
        var response = chain.proceed(oriRequst)

        if(response.code == ConstsData.ResCode.HTTP_CODE_401){
            val bodyString = response.body!!.string()
            response = response.newBuilder()
                .body(ResponseBody.create(response.body!!.contentType(), bodyString)).build()
            val (code,message) = parseTokenErrorBody(bodyString)
            Logger.i("[${TAG_NET}] HTTP_CODE_401 - %s",bodyString)
//            return when (code) {
//                ConstsData.ResCode.COMM_TOKEN_EXPIRED -> {
//                    doRefreshToken(response,chain)
//                }
//                ConstsData.ResCode.COMM_LOGIN_NEED -> {
//                    mAppInfo.removeAuthTokenInfos()
//                    throw NeedReloginException(
//                        code,
//                        message,
//                        Throwable()
//                    )
//                }
//                else -> {
//                    response
//                }
//            }
        }
        return response
    }

    private fun parseTokenErrorBody(body: String?) : Pair<Int,String> {
//        try {
//            body?.let {
//                val resAuthError = Gson().fromJson(it, ResBase::class.java)
//                return Pair(resAuthError.code,resAuthError.message)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        return  Pair(ConstsData.ResCode.COMM_LOGIN_NEED,"empty")
    }

}



