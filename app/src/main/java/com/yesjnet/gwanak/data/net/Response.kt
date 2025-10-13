package com.yesjnet.gwanak.data.net

import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.BuildConfig
import com.yesjnet.gwanak.core.ConstsData.Companion.TAG_NET
import com.yesjnet.gwanak.core.ConstsData.ResCode.Companion.APP_BAD_REQUEST
import com.yesjnet.gwanak.core.ConstsData.ResCode.Companion.APP_CLIENT_ERROR
import com.yesjnet.gwanak.core.ConstsData.ResCode.Companion.APP_HTTP_ERROR
import com.yesjnet.gwanak.core.ConstsData.ResCode.Companion.APP_NOCONNECTIVITY
import com.yesjnet.gwanak.core.ConstsData.ResCode.Companion.COMM_LOGIN_NEED
import com.yesjnet.gwanak.core.NeedReloginException
import com.yesjnet.gwanak.core.NoConnectivityException
import com.yesjnet.gwanak.data.model.response.ResBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback

/**
 * response
 */
class Response<V> private constructor(private val apiCall: Call<V>, private val apiResult: APIResult<V>) :
        Callback<V> {

    private var isRetryed = false
    override fun onResponse(
            call: Call<V>,
            response: retrofit2.Response<V>
    ) {
        try {
            if(response.isSuccessful){
                response.body()?.let {
                    apiResult.onSuccess(APIResource(it))
//                    if(body.isSuccess()){
//                        Logger.i("[${TAG_NET}] onSuccess = %s %s", call.request(), response)
//                        apiResult.onSuccess(APIResource(body))
//                    } else {
//                        apiResult.onError(ErrorResource(body.code, body.message,null,this@Response))
//                    }
                }
                apiResult.onLoading(false)
            } else {
//                val errorResource = createServerErrorResource(
//                        ServerErrorException(
//                                "HTTP status Error [${response.code()}]",
//                                Throwable(response.errorBody()?.string())
//                        ),this@Response)
                val resBase = Gson().fromJson(response.errorBody()?.string(), ResBase::class.java)


                val errorResource = ErrorResource(APP_BAD_REQUEST,resBase.message, Throwable(response.errorBody()?.string()),this@Response)
                apiResult.onError(errorResource)
                apiResult.onLoading(false)
            }
        } catch (e:Exception){
            apiResult.onLoading(false)
            if (BuildConfig.DEBUG) {
                val errorResource = ErrorResource(APP_CLIENT_ERROR, "${response.code()} ERROR\n${e.message}", Throwable(e), this@Response)
                Logger.e("[${TAG_NET}] onError = %s %s", call.request(), errorResource)
                apiResult.onError(errorResource)
            }
            e.printStackTrace()
        }
    }

    private fun retryCallWithDelay(){
        CoroutineScope(Main).launch {
            delay(696)
            retryCall()
        }
    }

    fun retryCall(){
        isRetryed = true
        Logger.i("[${TAG_NET}] retryCall API = %s", apiCall.request())
        apiResult.onLoading(true)
        apiCall.clone().enqueue(this@Response)
    }

    override fun onFailure(call: Call<V>, throwable: Throwable) {
        Logger.e("[${TAG_NET}] onFailure API = %s %s", call.request(), throwable)
        when (throwable) {
            is NeedReloginException -> {
                apiResult.onError(ErrorResource(COMM_LOGIN_NEED, throwable.message ?: "",throwable,this@Response))
            }
            is NoConnectivityException -> {
                apiResult.onError(ErrorResource(APP_NOCONNECTIVITY, throwable.message ?: "",throwable,this@Response))
            }
            else -> {
                apiResult.onError(createServerErrorResource(throwable,this@Response))
            }
        }
        apiResult.onLoading(false)
    }

    companion object {

        private const val NOTIFY_SERVER_ERROR = "서버통신 오류가 발생하였습니다.\n잠시 후 다시 시도해 주세요"

        fun <V> create(call: Call<V>,resource: APIResult<V>?): Response<V> {
            if (resource == null) throw NullPointerException("Resource interface can not be null")
            val response =
                    Response(call, resource)
            resource.onLoading(true)
            return response
        }
        fun createServerErrorResource(throwable: Throwable, response: Response<*>): ErrorResource {
            return ErrorResource(APP_HTTP_ERROR, NOTIFY_SERVER_ERROR,throwable,response)
        }
    }

}