package com.yesjnet.gwanak.ui.main

import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.BuildConfig
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.Family
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBFinish
import com.yesjnet.gwanak.data.model.eventbus.EBMainPageEvent
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.databinding.ActivityLoginBinding
import com.yesjnet.gwanak.extension.OnSingleClickListener
import com.yesjnet.gwanak.extension.bindChangeTextStyle
import com.yesjnet.gwanak.extension.bindSetDrawable
import com.yesjnet.gwanak.extension.copyClipboard
import com.yesjnet.gwanak.extension.getColorCompat
import com.yesjnet.gwanak.extension.getString
import com.yesjnet.gwanak.extension.gone
import com.yesjnet.gwanak.extension.hideKeyboard
import com.yesjnet.gwanak.extension.pixelFromDP
import com.yesjnet.gwanak.extension.show
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
        binding.viewModel?.saveFirebaseInstanceToken()
        EventBus.getDefault().register(this)
        initAdapter()
//        initHeader(EnumApp.AppBarStyle.BACK_TITLE, getString(R.string.login_title), backClick = { onBackPressed() })

        val memberInfo = userInfo.getMember()
        val headerStr = if (memberInfo?.userId.isNullOrEmpty()) {
            getString(R.string.login_title)
        } else {
            getString(R.string.mobile_membership_card)
        }

        if (BuildConfig.DEBUG) {
            binding.clFcm.show()
        } else {
            binding.clFcm.gone()
        }

        initHeader(EnumApp.AppBarStyle.BACK_TITLE, headerStr)

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
                updateTitle(it.userId)
                binding.etId.hideKeyboard()
                binding.etPwd.hideKeyboard()
            }

            // 오른쪽 화살표 클릭
            onLeftClick.observe(this@LoginActivity) {
                binding.vpMain.currentItem -= 1
            }
            // 왼쪽 화살표 클릭
            onRightClick.observe(this@LoginActivity) {
                binding.vpMain.currentItem += 1
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMemberInfo(event: EBMemberInfo) {
        if (event.memberInfo.userId.isNullOrEmpty()) {
            binding.viewModel?.updateMemberInfo(MemberInfo())
        } else {
            binding.viewModel?.updateMemberInfo(event.memberInfo)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventFinish(ebFinish: EBFinish) {
        Logger.d("allMenuActivity finish")
        finish()
    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.icAutoLogin -> {
                if (binding.icAutoLogin.checkbox.isActivated) {
                    binding.viewModel?.updateAutoLogin(false)
                } else {
                    binding.viewModel?.updateAutoLogin(true)
                }
            }
        }
    }

    private fun initAdapter() {
        binding.vpMain.apply {
            adapter = LoginAdapter(binding.viewModel, this@LoginActivity)
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

    /**
     * title 업데이트
     */
    private fun updateTitle(title: String? = null) {
        if (title.isNullOrEmpty()) {
            setTitle(getString(R.string.login_title))
        } else {
            setTitle(getString(R.string.mobile_membership_card))
        }
    }

    companion object {
        @BindingAdapter("bindActivated")
        @JvmStatic
        fun ImageView.bindActivated(activated: Boolean?) {
            activated?.let {
                this.isActivated = it
            }
        }

        @BindingAdapter("bindCardStatus", "position")
        @JvmStatic
        fun TextView.bindCardStatus(item: Family?, position: Int) {
            item?.let {
                if (it.isMy) {
                    this.text = this.context.getString(R.string.my)
                    this.bindSetDrawable(bgColor = this.context.getColorCompat(R.color.primary_color), cornerAll = this.context.pixelFromDP(9))
                } else {
                    this.text = String.format(this.context.getString(R.string.family_format), position)
                    this.bindSetDrawable(bgColor = this.context.getColorCompat(R.color.color_6150C5), cornerAll = this.context.pixelFromDP(9))
                }
            }
        }

        @BindingAdapter("bindCardName")
        @JvmStatic
        fun TextView.bindCardName(item: Family?) {
            item?.let {
                val fullStr = String.format(this.getString(R.string.login_after_mobile_card_format), it.familyName)
                bindChangeTextStyle(
                    fullStr = fullStr,
                    changeStr = it.familyName,
                    changeColor = this.context.getColorCompat(R.color.white),
                    changeSize = this.context.pixelFromDP(20),
                    styleType = Typeface.BOLD
                )
            }
        }

        @BindingAdapter("bindStatusCount", "bindStatusTotal")
        @JvmStatic
        fun TextView.bindStatusCount(count: Int?, total: Int) {
            count?.let {
                this.text = String.format(this.context.getString(R.string.current_situation_format), it, total)
            }
        }

        @BindingAdapter("bindMobileStatus")
        @JvmStatic
        fun TextView.bindMobileStatus(item: MemberInfo?) {
            item?.let {
                /*
                userClass = '1' → 대출정지 (~loanStopDate 표시)
                → 대출정지
                userClass = '2' → 제적회원
                → 제적회원
                userClass ≠ '1' && userClass ≠ '2' && memberClass = '0' && illAplStopDate 있음 → 정회원 + 이용제한 (상호대차 신청 제한)
                → 상호대차정지
                userClass ≠ '1' && userClass ≠ '2' && memberClass = '0' && uLibraryVO.endDate 있음 → 정회원 + 이용제한 (U-도서관 신청 제한)
                → U도서관정지
                userClass ≠ '1' && userClass ≠ '2' && memberClass = '0' && 제한 없음 → 정회원 (정상)
                → 대출가능
                userClass ≠ '1' && userClass ≠ '2' && memberClass = '2' → 준회원
                → 준회원
                 */
                val userClass = it.userClass
                val memberClass = it.memberClass

                if ("1" == userClass) {
                    this.text = this.context.getString(R.string.loan_suspension)
                } else if ("2" == userClass) {
                    this.text = this.context.getString(R.string.expulsion_of_member)
                } else if ("1" != userClass && "2" != userClass && "0" == memberClass && !item.illAplStopDate.isNullOrEmpty()) {
                    this.text = this.context.getString(R.string.interlibrary_loan_suspension)
                } else if ("1" != userClass && "2" != userClass && "0" == memberClass && item.uLibraryEndDate.isNullOrEmpty()) {
                    this.text = this.context.getString(R.string.u_library_suspension)
                } else if ("1" != userClass && "2" != userClass && "0" == memberClass) {
                    this.text = this.context.getString(R.string.loan_available)
                } else if ("1" != userClass && "2" != userClass && "2" == memberClass) {
                    this.text = this.context.getString(R.string.associate_member)
                }
            }
        }
    }

}