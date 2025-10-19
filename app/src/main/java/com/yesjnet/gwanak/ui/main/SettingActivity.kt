package com.yesjnet.gwanak.ui.main

import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.BuildConfig
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.databinding.ActivitySettingBinding
import com.yesjnet.gwanak.extension.OnSingleClickListener
import com.yesjnet.gwanak.extension.bindSetDrawable
import com.yesjnet.gwanak.extension.copyClipboard
import com.yesjnet.gwanak.extension.getColorCompat
import com.yesjnet.gwanak.extension.gone
import com.yesjnet.gwanak.extension.showAlertOK
import com.yesjnet.gwanak.extension.showToast
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.base.BaseAppBarActivity
import com.yesjnet.gwanak.ui.startScreen
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.concurrent.TimeUnit

/**
 * 설정 activity
 */
class SettingActivity: BaseAppBarActivity<ActivitySettingBinding>(R.layout.activity_setting),
    OnSingleClickListener {
    private val userInfo: UserInfo by inject()
    private val appInfo: AppInfo by inject()
    private val pref: SecurePreference by inject()

    // 시스템 설정에서 돌아왔을 때 onActivityResult로 결과 확인
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onInitView() {
        binding.viewModel = getViewModel()
        binding.lifecycleOwner = this
        binding.activity = this
        EventBus.getDefault().register(this)

        initHeader(EnumApp.AppBarStyle.BACK_TITLE, getString(R.string.setting))
    }

    override fun onSubscribeUI() {
        binding.viewModel?.apply {
            onErrorResource.observe(this@SettingActivity) {
                showAlertOK(message = it.message)
            }
            onNavScreen.observe(this@SettingActivity) {
                startScreen(it)
            }
            onShowMsgDialog.observe(this@SettingActivity) {
                showAlertOK(message = it)
            }
            onMemberInfo.observe(this@SettingActivity) {
                val userId = it.userId
                if (isLoginCheck(it)) {
                    // fcm 표시
                    if (BuildConfig.DEBUG) {
                        binding.clFcm.gone()
                    } else {
                        binding.clFcm.gone()
                    }
                    binding.viewModel?.postPushKeyInfo(userId)
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(696)
                        binding.viewModel?.updateIsChangeReady(true)
                    }
                }
            }
            onCopyFcmToken.observe(this@SettingActivity) {
                copyClipboard("label", it)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    showToast("복사 성공")
                }
            }
        }

        // 자동 로그인
        addToDisposable(binding.icAutoLogin.swcSwitch.checkedChanges()
            .debounce(100, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (binding.viewModel?.getIsChangeReady() == true) {
                    if (isLoginCheck(binding.viewModel?.onMemberInfo?.value)) {
                        pref.setConfigBool(ConstsData.PrefCode.AUTO_LOGIN, it)
                    } else {
                        // 로그인 전
                        if (it) {
                            binding.icAutoLogin.swcSwitch.isChecked = false
                        } else {
                            showAlertOK(message = getString(R.string.available_after_logging_in))
                        }
                    }
                }

            })

        // 흔들어열기 설정
        addToDisposable(binding.icShakeSetting.swcSwitch.checkedChanges()
            .debounce(100, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (binding.viewModel?.getIsChangeReady() == true) {
                    if (isLoginCheck(binding.viewModel?.onMemberInfo?.value)) {
                        pref.setConfigBool(ConstsData.PrefCode.SHAKE_FLAG, it)
                    } else {
                        // 로그인 전
                        if (it) {
                            binding.icShakeSetting.swcSwitch.isChecked = false
                        } else {
                            showAlertOK(message = getString(R.string.available_after_logging_in))
                        }
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        binding.viewModel?.onFcmToken?.postValue(appInfo.getFCMDeviceToken())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.ivBanner -> {
                Logger.d("ivbanner")
            }
            R.id.ivBarcode -> {
                Logger.d("ivBarcode")
            }
            R.id.icPush1 -> {
                val isPush1Bool = binding.icPush1.checkbox.isActivated
                val isPush2Bool = binding.icPush2.checkbox.isActivated
                val userId = appInfo.getLoginInfo()?.userId ?: ""
                if (isLoginCheck(binding.viewModel?.onMemberInfo?.value)) {
                    if (isPush1Bool) {
                        binding.viewModel?.updatePushAlarm(false)
                    } else {
                        binding.viewModel?.updatePushAlarm(true)
                    }
                    binding.viewModel?.postUpdatePushKey(userId = userId, push1Yn = EnumApp.FlagYN.flagByBoolean(!isPush1Bool), push2Yn = EnumApp.FlagYN.flagByBoolean(isPush2Bool))
                } else {
                    // 로그인 전
                    if (isPush1Bool) {
                        binding.viewModel?.updatePushAlarm(false)
                    } else {
                        showAlertOK(message = getString(R.string.available_after_logging_in))
                    }
                }
            }
            R.id.icPush2 -> {
                val isPush1Bool = binding.icPush1.checkbox.isActivated
                val isPush2Bool = binding.icPush2.checkbox.isActivated
                val userId = appInfo.getLoginInfo()?.userId ?: ""
                if (isLoginCheck(binding.viewModel?.onMemberInfo?.value)) {
                    if (isPush2Bool) {
                        binding.viewModel?.updateReAgreeAlarm(false)
                    } else {
                        binding.viewModel?.updateReAgreeAlarm(true)
                    }
                    binding.viewModel?.postUpdatePushKey(userId = userId, push1Yn = EnumApp.FlagYN.flagByBoolean(isPush1Bool), push2Yn = EnumApp.FlagYN.flagByBoolean(!isPush2Bool))
                } else {
                    // 로그인 전
                    if (isPush2Bool) {
                        binding.viewModel?.updateReAgreeAlarm(false)
                    } else {
                        showAlertOK(message = getString(R.string.available_after_logging_in))
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMemberInfo(event: EBMemberInfo) {
        if (event.memberInfo.userId.isNullOrEmpty()) {
            binding.viewModel?.updateMemberInfo(MemberInfo())
        } else {
            binding.viewModel?.updateMemberInfo(event.memberInfo)
        }
        finish()
    }

    companion object {


        @BindingAdapter("bindLoginUi")
        @JvmStatic
        fun TextView.bindLoginUi(memberInfo: MemberInfo?) {
            memberInfo?.let {
                if (it.userId.isNullOrEmpty()) {
                    text = this.context.getString(R.string.login)
                    setTextColor(this.context.getColorCompat(R.color.white))
                    this.bindSetDrawable(bgColor = this.context.getColorCompat(R.color.primary_color), cornerAll = 3)
                } else {
                    text = this.context.getString(R.string.logout)
                    setTextColor(this.context.getColorCompat(R.color.color_777777))
                    this.bindSetDrawable(bgColor = this.context.getColorCompat(R.color.white), cornerAll = 3, stroke = 1, strokeColor = this.context.getColorCompat(R.color.color_CCCCCC))
                }
            } ?: run {
                text = this.context.getString(R.string.login)
                setTextColor(this.context.getColorCompat(R.color.white))
                this.bindSetDrawable(bgColor = this.context.getColorCompat(R.color.primary_color), cornerAll = 3)
            }
        }

        @BindingAdapter("bindVersion")
        @JvmStatic
        fun TextView.bindVersion(version: String?) {
            version?.let { this.text = version }
        }
    }

}