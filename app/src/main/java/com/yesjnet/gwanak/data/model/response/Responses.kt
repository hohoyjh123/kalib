package com.yesjnet.gwanak.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Server Response Body Data Modelling
 */
data class ResBase(
    @SerializedName("field") val field: String?,
    @SerializedName("error_type") val error_type: String,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
) {

//    fun isSuccess(): Boolean {
//        return code == ConstsData.ResCode.COMM_SUCCESS
//    }
}

data class Debug(
    @SerializedName("exception") val exception: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("file") val file: String,
    @SerializedName("line") val line: Int
)

// 삭제 api response
data class ResSuccess(val success: Boolean)

data class ResMessage(val message: String)

data class ResMapList<T>(val mapList: ArrayList<T>)