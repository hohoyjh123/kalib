package com.yesjnet.gwanak.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// start
/**
 * 앱 버전 체크
 */
data class AppVersionItem(
    @SerializedName("version") val version: Version,
    @SerializedName("status")val status: String
): Serializable

data class Version(
    @SerializedName("AGENT") val agent: Int,
    @SerializedName("UPD_DT") val updDt: Long,
    @SerializedName("APP_VERSION") val appVersion: String
): Serializable

data class DeviceInfo(
    @SerializedName("flag") val flag: String
): Serializable

data class PushInfo(
    @SerializedName("agent") val agent: Int,
    @SerializedName("flag") val flag: String,
    @SerializedName("push1Yn") val push1Yn: String,
    @SerializedName("userId") val userId: String
): Serializable

/**
 * 회원 정보
 */
data class MemberInfo(
    @SerializedName("flag") val flag: String = "",
    @SerializedName("klMemberYn") val klMemberYn: String = "",
    @SerializedName("loanStopDate") val loanStopDate: String? = null,
    @SerializedName("manageCode") val manageCode: String = "",
    @SerializedName("memberClass") val memberClass: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("privacyExpireDate") val privacyExpireDate: String = "",
    @SerializedName("reAgreeYn") val reAgreeYn: String = "",
    @SerializedName("recKey") val recKey: Int = 0,
    @SerializedName("userClass") val userClass: String = "",
    @SerializedName("userClassCode") val userClassCode: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("userNo") val userNo: String = "",
    @SerializedName("userPositionCode") val userPositionCode: String = ""
): Serializable

/**
 * 로그인 정보
 */
data class LoginInfo(
    @SerializedName("userId") val userId: String = "",
    @SerializedName("userPwd") val userPwd: String = ""
)

data class Family(
    @SerializedName("loanStopEndDate") val loanStopEndDate: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("userKey") val userKey: String = "",
    @SerializedName("userName") val userName: String = "",
    @SerializedName("userNo") val userNo: String = "",
    @SerializedName("userStatus") val userStatus: String = "",
    @SerializedName("userPositionCode") var loanNoShow: Boolean = false,
    @SerializedName("loanCount") val loanCount: Int = 0,
    @SerializedName("possibleLoanCount") val possibleLoanCount: Int = 0
): Serializable

data class Holiday(
    @SerializedName("mapList") val mapList: ArrayList<HolidayItem>
): Serializable

/**
 * 휴관일 목록 아이템
 */
data class HolidayItem(
    @SerializedName("lib_alias") val libAlias: String,
    @SerializedName("lib_color") val libColor: String
): Serializable

// end

/**
 * 공지사항 아이템
 */
data class NoticeItem(
    @SerializedName("reg_dt") val redDt: String,
    @SerializedName("lib_alias") val libAlias: String,
    @SerializedName("link_url") val linkUrl: String,
    @SerializedName("title") val title: String
): Serializable

/**
 * 문화 프로그램
 */
data class CulturalProgram(
    @SerializedName("lecture_start_ymd") val lectureStartYmd: String,
    @SerializedName("lib_alias") val libAlias: String,
    @SerializedName("lecture_end_ymd") val lectureEndYmd: String,
    @SerializedName("status_nm") val statusNm: String,
    @SerializedName("link_url") val linkUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("target_nm") val targetNm: String,
    @SerializedName("target_desc") val targetDesc: String
): Serializable

/**
 * 공지사항 아이템
 */
data class RecommendBook(
    @SerializedName("img_url") val imgUrl: String,
    @SerializedName("author") val author: String,
    @SerializedName("link_url") val linkUrl: String,
    @SerializedName("title") val title: String
): Serializable

data class PushData(
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("url") val url: String? = null,
): Serializable