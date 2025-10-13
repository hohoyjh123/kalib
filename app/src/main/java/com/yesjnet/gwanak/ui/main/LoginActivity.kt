package com.yesjnet.gwanak.ui.main

import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBFinish
import com.yesjnet.gwanak.data.model.eventbus.EBMainPageEvent
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.databinding.ActivityLoginBinding
import com.yesjnet.gwanak.extension.OnSingleClickListener
import com.yesjnet.gwanak.extension.copyClipboard
import com.yesjnet.gwanak.extension.hideKeyboard
import com.yesjnet.gwanak.extension.showAlertOK
import com.yesjnet.gwanak.extension.showToast
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.base.BaseAppBarActivity
import com.yesjnet.gwanak.ui.base.BaseDialogFragment
import com.yesjnet.gwanak.ui.startScreen
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * 로그인&회원정보 activity
 */
class LoginActivity: BaseAppBarActivity<ActivityLoginBinding>(R.layout.activity_login), OnSingleClickListener {
    private val userInfo: UserInfo by inject()
    private val appInfo: AppInfo by inject()
    private val pref: SecurePreference by inject()

    override fun onInitView() {
        binding.viewModel = getViewModel()
        binding.lifecycleOwner = this
        binding.activity = this
        EventBus.getDefault().register(this)
//        initIdPwdRegular()
        initHeader(EnumApp.AppBarStyle.BACK_TITLE, getString(R.string.login_title), backClick = { onBackPressed() })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        EventBus.getDefault().post(EBMainPageEvent(EnumApp.MainPage.HOME))
    }

    override fun onSubscribeUI() {
        binding.viewModel?.apply {
            onErrorResource.observe(this@LoginActivity) {
                showAlertOK(message = it.message)
            }
            onNavScreen.observe(this@LoginActivity) {
                startScreen(it)
            }
            onShowMsgDialog.observe(this@LoginActivity) {
                showAlertOK(message = it)
            }
            // 로그인 성공 팝업
            onShowLoginDialog.observe(this@LoginActivity) {
                showAlertOK(message = it, okListener = object : BaseDialogFragment.MyOnClickListener {
                    override fun onClick(obj: Any?) {
                        EventBus.getDefault().post(EBFinish(true))
                        finish()
                    }
                })

                // 자동 로그인 체크
                if (binding.icAutoLogin.checkbox.isActivated) {
                    pref.setConfigBool(ConstsData.PrefCode.AUTO_LOGIN, true)
                } else {
                    pref.setConfigBool(ConstsData.PrefCode.AUTO_LOGIN, false)
                }
            }

            onMemberInfo.observe(this@LoginActivity) {
//                updateTitle(it.userNo)
                binding.etId.hideKeyboard()
                binding.etPwd.hideKeyboard()
            }

            onCopyFcmToken.observe(this@LoginActivity) {
                copyClipboard("label", it)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    showToast("복사 성공")
                }
            }

            // TextWatcher로 실시간 공백 제거
            binding.etId.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        val textWithoutSpaces = it.toString().replace(" ", "")
                        if (textWithoutSpaces != it.toString()) {
                            binding.etId.setText(textWithoutSpaces)
                            binding.etId.setSelection(textWithoutSpaces.length)
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            binding.etPwd.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        val textWithoutSpaces = it.toString().replace(" ", "")
                        if (textWithoutSpaces != it.toString()) {
                            binding.etPwd.setText(textWithoutSpaces)
                            binding.etPwd.setSelection(textWithoutSpaces.length)
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.viewModel?.onFcmToken?.postValue(appInfo.getFCMDeviceToken())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        // Stop the updates when the activity is destroyed
//        for (i in 0 until (binding.vpMain.adapter as MembershipProfileAdapter).itemCount) {
//            ((binding.vpMain[0] as RecyclerView).findViewHolderForAdapterPosition(i) as? MembershipProfileAdapter.ItemViewHolder)?.stopUpdates(i)
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMemberInfo(event: EBMemberInfo) {
        if (event.memberInfo.userId.isNullOrEmpty()) {
            binding.viewModel?.updateMemberInfo(MemberInfo())
        } else {
            binding.viewModel?.updateMemberInfo(event.memberInfo)
        }
    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.icAutoLogin -> {
                if (binding.icAutoLogin.checkbox.isActivated) {
                    binding.viewModel?.updateAutoLogin(false)
                } else {
                    binding.viewModel?.updateAutoLogin(true)
                }
//                binding.icAutoLogin.checkbox.isActivated = binding.icAutoLogin.checkbox.isActivated.not()
            }
        }
    }

    fun initIdPwdRegular() {
        // ID 입력 필터
        val idFilter = InputFilter { source, _, _, _, _, _ ->
            val pattern = Regex("^[a-zA-Z0-9!@#\$%^&*()_+\\-=]*$") // 영문, 숫자, 특수문자만 허용
            if (source.isBlank() || pattern.matches(source)) {
                source
            } else {
                ""
            }
        }

        // PW 입력 필터 (공백 입력 제한)
        val pwFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) {
                ""
            } else {
                source
            }
        }

        // EditText에 필터 적용
        binding.etId.filters = arrayOf(idFilter)
        binding.etPwd.filters = arrayOf(pwFilter)
    }

    companion object {
        @BindingAdapter("bindActivated")
        @JvmStatic
        fun ImageView.bindActivated(activated: Boolean?) {
            activated?.let {
                this.isActivated = it
            }
        }
    }

}