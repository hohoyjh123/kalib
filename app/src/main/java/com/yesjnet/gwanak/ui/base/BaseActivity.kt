package com.yesjnet.gwanak.ui.base

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.extension.hideKeyboard
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * base activity
 */
abstract class BaseActivity<B : ViewDataBinding>(@LayoutRes private val layoutResId: Int) : AppCompatActivity(layoutResId) {

    abstract fun onInitView()
    abstract fun onSubscribeUI()

    protected lateinit var binding: B
        private set

    private val compositeDisposable = CompositeDisposable()

    var captureUri : Uri? = null
    lateinit var captureUris : ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutResId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                            or WindowInsetsCompat.Type.ime()
                )
                v.updatePadding(
                    left = bars.left,
                    top = bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                WindowInsetsCompat.CONSUMED
            }

            // statusbar 나오도록 수정
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = true
        }

        onInitView()
        onSubscribeUI()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    fun addToDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun hideIme (view: View?){
        view?.hideKeyboard()
        view?.clearFocus()
    }

    /**
     * 로그인 여부 체크
     * @return boolean (true - 로그인, false - 비 로그인)
     */
    fun isLoginCheck(memberInfo: MemberInfo?): Boolean {
        val userId = memberInfo?.userId ?: ""
        return userId.isNotEmpty()
    }

}