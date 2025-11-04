package com.yesjnet.gwanak.ui.main

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.eventbus.EBFinish
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.databinding.ActivityAllMenuBinding
import com.yesjnet.gwanak.extension.OnSingleClickListener
import com.yesjnet.gwanak.extension.bindChangeTextStyle
import com.yesjnet.gwanak.extension.getColorCompat
import com.yesjnet.gwanak.extension.getString
import com.yesjnet.gwanak.extension.showAlertOK
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.ScreenInfo
import com.yesjnet.gwanak.ui.base.BaseAppBarActivity
import com.yesjnet.gwanak.ui.base.BaseDialogFragment
import com.yesjnet.gwanak.ui.startScreen
import com.yesjnet.gwanak.util.GpsUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.getViewModel

class AllMenuActivity: BaseAppBarActivity<ActivityAllMenuBinding>(R.layout.activity_all_menu), OnSingleClickListener {

    override fun onInitView() {
        binding.viewModel = getViewModel()
        binding.activity = this
        binding.lifecycleOwner = this
        GpsUtil.instance.init(this@AllMenuActivity)
        EventBus.getDefault().register(this)
    }

    override fun onSubscribeUI() {
        binding.viewModel?.apply {
            onErrorResource.observe(this@AllMenuActivity) {
                showAlertOK(message = it.message)
            }
            onNavScreen.observe(this@AllMenuActivity) {
                startScreen(it)
            }
            onShowMsgDialog.observe(this@AllMenuActivity) {
                showAlertOK(message = it)
            }
            onFinish.observe(this@AllMenuActivity) {
                finish()
            }
            onLoginError.observe(this@AllMenuActivity) {
                showAlertOK(message = getString(R.string.available_after_logging_in), okListener = object : BaseDialogFragment.MyOnClickListener {
                    override fun onClick(obj: Any?) {
                        startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventFinish(ebFinish: EBFinish) {
        Logger.d("allMenuActivity finish")
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMemberInfo(event: EBMemberInfo) {
        if (event.memberInfo.userId.isNullOrEmpty()) {
            binding.viewModel?.updateMemberInfo(MemberInfo())
        } else {
            binding.viewModel?.updateMemberInfo(event.memberInfo)
        }
    }

    companion object {
        @BindingAdapter("bindLoginInfo")
        @JvmStatic
        fun TextView.bindLoginInfo(memberInfo: MemberInfo?) {
            if (memberInfo?.userId.isNullOrEmpty()) {
                // 비 로그인
                val fullStr = this.getString(R.string.login_before_message)
                bindChangeTextStyle(
                    fullStr = fullStr,
                    changeStr = fullStr.substring(0, 4),
                    changeColor = this.context.getColorCompat(R.color.white),
                    styleType = Typeface.BOLD
                )
            // 로그인
            } else {
                val fullStr = String.format(this.getString(R.string.login_after_message_format), memberInfo?.name)
                bindChangeTextStyle(
                    fullStr = fullStr,
                    changeStr = memberInfo?.name,
                    changeColor = this.context.getColorCompat(R.color.white),
                    styleType = Typeface.BOLD
                )
            }
        }

    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.icResourceSearch -> {
                Logger.d("icResourceSearch")
            }
            R.id.icSearchNewInfo -> {
                Logger.d("icSearchNewInfo")
            }
        }
    }
}