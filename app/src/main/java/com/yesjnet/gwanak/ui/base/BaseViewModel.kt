package com.yesjnet.gwanak.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.data.net.ErrorResource
import com.yesjnet.gwanak.lifecycle.MultipleLiveEvent
import com.yesjnet.gwanak.lifecycle.SingleLiveEvent
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.ScreenInfo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * base viewModel
 *
 * @param application
 */
open class BaseViewModel(application: GAApplication) : AndroidViewModel(application) {

    // swipe refresh 로딩바
    val onDataLoading: MultipleLiveEvent<Boolean>
        get() = inDataLoading
    protected val inDataLoading: MultipleLiveEvent<Boolean> = MultipleLiveEvent()

    // 로딩바
    val onDataProgress: MultipleLiveEvent<Boolean>
        get() = inDataProgress
    protected val inDataProgress: MultipleLiveEvent<Boolean> = MultipleLiveEvent()

    // 에러 메시지
    val onErrorResource: SingleLiveEvent<ErrorResource>
        get() = inErrorResource
    protected val inErrorResource: SingleLiveEvent<ErrorResource> = SingleLiveEvent()

    // 페이지 이동처리
    val onNavScreen: SingleLiveEvent<NavScreen<ScreenInfo>>
        get() = inNaviScreen
    protected val inNaviScreen: SingleLiveEvent<NavScreen<ScreenInfo>> = SingleLiveEvent()

    // 액션없는 메시지 팝업
    val onShowMsgDialog: MultipleLiveEvent<String>
        get() = inShowMsgDialog
    protected val inShowMsgDialog: MultipleLiveEvent<String> = MultipleLiveEvent()

    // 액션없는 메시지 토스트
    val onShowToastDialog: MultipleLiveEvent<String>
        get() = inShowToastDialog
    protected val inShowToastDialog: MultipleLiveEvent<String> = MultipleLiveEvent()

    // 종료 이벤트
    val onFinish: MultipleLiveEvent<Boolean>
        get() = inFinish
    protected val inFinish: MultipleLiveEvent<Boolean> = MultipleLiveEvent()

    private val context = getApplication<Application>().applicationContext

    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     */
    private val compositeDisposable = CompositeDisposable()

    fun addToDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}