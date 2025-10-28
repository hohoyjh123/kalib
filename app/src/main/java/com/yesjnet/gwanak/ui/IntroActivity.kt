package com.yesjnet.gwanak.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.ConstsApp.IntentCode.Companion.PUSHDATA
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.data.model.PushData
import com.yesjnet.gwanak.databinding.ActivityIntroBinding
import com.yesjnet.gwanak.extension.browse
import com.yesjnet.gwanak.extension.showAlertConfirm
import com.yesjnet.gwanak.extension.showAlertOK
import com.yesjnet.gwanak.fcm.PushNotifyReceiver
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.base.BaseAppBarActivity
import com.yesjnet.gwanak.ui.base.BaseDialogFragment
import com.yesjnet.gwanak.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class IntroActivity: BaseAppBarActivity<ActivityIntroBinding>(R.layout.activity_intro) {
    private val pref: SecurePreference by inject()

    override fun onInitView() {
        binding.viewModel = getViewModel()
        binding.lifecycleOwner = this
        binding.viewModel?.saveFirebaseInstanceToken()
        startFadeInAnimation(binding.clFade)
//        startFlowerAnimation()
        binding.viewModel?.setShow(true)
    }

    override fun onSubscribeUI() {
        binding.viewModel?.apply {
            onErrorResource.observe(this@IntroActivity) {
                showAlertOK(message = it.message)
            }
            onNavScreen.observe(this@IntroActivity) {
//                if (!fcmMessageCheck(intent))
                startScreen(it)
                finish()
            }
            onIsForceUpdate.observe(this@IntroActivity) { isForceUpdate ->
                if (isForceUpdate) {
                    showAlertConfirm(
                        message = getString(R.string.force_update_message),
                        okListener = object : BaseDialogFragment.MyOnClickListener {
                            override fun onClick(obj: Any?) {
                                browse("market://details?id=$packageName", true)
                                finish()
                            }
                        }, cancelListener = object : BaseDialogFragment.MyOnClickListener {
                            override fun onClick(obj: Any?) {
                                finish()
                            }
                        })
                } else {
                    // Launcher에서 받은 Intent 안에 push 데이터 여부 확인
                    val pushData = intent.getSerializableExtra(PUSHDATA)

                    // 이게 있으면 → 바로 MainActivity 이동
                    if (pushData != null) {
                        Logger.d("jihoon pushData = $pushData")
                        val actIntent = Intent(this@IntroActivity, MainActivity::class.java)
                        actIntent.putExtra(PUSHDATA, pushData)
                        actIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(actIntent)
                        finish()
                    } else if (pref.getConfigBool(ConstsData.PrefCode.AUTO_LOGIN, false)) {
                        Logger.d("jihoon AUTO_LOGIN = true")
                        val loginInfo = binding.viewModel?.appInfo?.getLoginInfo()
                        loginInfo?.let {
                            Logger.d("jihoon AUTO_LOGIN loginInfo != null")
                            binding.viewModel?.postAppLogin(it.userId, it.userPwd)
                        } ?: run {
                            Logger.d("jihoon AUTO_LOGIN loginInfo == null")
                            processLogout()
                        }
                    } else {
                        Logger.d("jihoon BIOMETRIC_LOGIN & AUTO_LOGIN loginInfo == false")
                        processLogout()
                    }
                }
            }
            // 자동로그인 실패 팝업
            onAutoLoginDialog.observe(this@IntroActivity) {
                showAlertOK(message = it, okListener = object : BaseDialogFragment.MyOnClickListener {
                    override fun onClick(obj: Any?) {
                        processLogout()
                    }
                })
            }
        }
    }

    fun processLogout() {
        val memberInfo = binding.viewModel?.userInfo?.getMember()
        memberInfo?.let {
            binding.viewModel?.postAppLogout(memberInfo)
        } ?: run {
            binding.viewModel?.logout()
            binding.viewModel?.updateNavScreen(NavScreen.Main())
        }
    }

    /**
     * fcm 체크
     * 앱 종료시 푸시 수신시 onMessageReceived 수신되지 않기에 체크필요
     */
    private fun fcmMessageCheck(actIntent: Intent): Boolean {
        val pushData = PushData(null, null, null)
        val intent = Intent(this, PushNotifyReceiver::class.java)
        intent.putExtra(PUSHDATA, pushData)
        intent.putExtras(actIntent)
        sendBroadcast(intent)
        finish()
        return true
    }

    private fun startFadeInAnimation (view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.interpolator = LinearInterpolator() // 일정 속도
            animator.duration = 1000 // 1초 동안 서서히 나타남
            animator.start()
        }
    }

}