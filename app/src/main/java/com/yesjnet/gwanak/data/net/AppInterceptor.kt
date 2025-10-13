package com.yesjnet.gwanak.data.net

import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.ConstsData.Companion.TAG_NET
import com.yesjnet.gwanak.core.GAApplication
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * app interceptor
 */
class AppInterceptor(private val mApplication: GAApplication) : Interceptor , KoinComponent {

    private val mAppInfo: AppInfo by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
//        val cm = mApplication.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
//        if(!isConnected){
//            throw NoConnectivityException(
//                "Network Connection Error",
//                Throwable()
//            )
//        }

        val oriRequst = chain.request().newBuilder().apply {
//            val accessToken = mAppInfo.getAuthTokenInfo()?.let {
//                "Bearer ${it.access}"
//            }
            val accessToken = ""
            val userAgent = mAppInfo.userAgent
            header("Authorization", accessToken?: "")
            header("User-Agent", userAgent)
            if (ConstsData.isDevMode()) {
                val strRequset = chain.request().url
                val headers = chain.request().headers
                val body = chain.request().body
                Logger.i("[${TAG_NET}] Request : %s \n %s \n %s",strRequset,headers,body)
            }
        }
        var response = chain.proceed(oriRequst.build())
        // Logging
        if (ConstsData.isDevMode() && response.isSuccessful) {
            val bodyString = response.body!!.string()
            response = response.newBuilder()
                .body(ResponseBody.create(response.body!!.contentType(), bodyString)).build()
            Logger.json(bodyString)
        }
        return response
    }

}



