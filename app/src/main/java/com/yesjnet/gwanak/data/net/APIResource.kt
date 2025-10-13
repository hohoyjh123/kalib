package com.yesjnet.gwanak.data.net

import com.yesjnet.gwanak.core.ConstsData

/**
 * api resource
 */
data class APIResource<V>(val resBase: V){

}

data class ErrorResource(var code: Int, val message: String, val throwable: Throwable?, private val response: Response<*>){
    fun isAppError(): Boolean = code > ConstsData.ResCode.COMM_SUCCESS && throwable == null
    fun isHTTPError(): Boolean = !isAppError()
    fun getAPIResponse(): Response<*> = response
}