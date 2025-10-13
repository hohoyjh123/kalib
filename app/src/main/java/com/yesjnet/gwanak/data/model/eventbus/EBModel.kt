package com.yesjnet.gwanak.data.model.eventbus

import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.data.model.MemberInfo

/**
 * EventBus 이벤트 공통 데이터 모델 정리 클래스
 */
data class EBMainPageEvent(
    val page: EnumApp.MainPage,
    val isReselected: Boolean = false,
    val isResetLayout: Boolean = true
)

/**
 * 액티비티 종료
 */
data class EBFinish(val finish: Boolean)

// 새로고침
data class EBRefresh(val isRefresh: Boolean)
data class EBMemberInfo(val memberInfo: MemberInfo)
// 로그아웃 이벤트
data class EBLogout(val userId: String, val userPwd: String)
