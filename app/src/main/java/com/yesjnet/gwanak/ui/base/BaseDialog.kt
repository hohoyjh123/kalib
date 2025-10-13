package com.yesjnet.gwanak.ui.base

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * base 팝업
 */
open class BaseDialogFragment : DialogFragment() {
    interface MyOnClickListener {
        fun onClick(obj: Any?)
    }

    var okClickListener: MyOnClickListener? = null
    internal var cancelClickListener: MyOnClickListener? = null
    internal var twoClickListener: MyOnClickListener? = null
    private val compositeDisposable = CompositeDisposable()

    fun setMyOnClickListener(onClickListener: MyOnClickListener?) {
        onClickListener?.let { okClickListener = it }
    }

    fun setCancelClickListener(cancelClickListener: MyOnClickListener?) {
        cancelClickListener?.let { this.cancelClickListener = it }
    }

    fun setTwoClickListener(twoClickListener: MyOnClickListener?) {
        twoClickListener?.let { this.twoClickListener = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BaseDialog)
    }

    override fun show(
        manager: FragmentManager,
        tag: String?
    ) {
        showAllowingStateLoss(manager, tag)
    }

    fun showAllowingStateLoss(
        manager: FragmentManager,
        tag: String?
    ) {
        val ft = manager.beginTransaction()
        val prev = manager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    private fun hideIme(view: View?) {
        val context = context
        context?.let {
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    override fun dismiss() {
        compositeDisposable.clear()
        hideIme(view)
        super.dismiss()
    }

    override fun onPause() {
        hideIme(view)
        super.onPause()
    }

    /**
     * 팝업 영역 외 터치시 후처리
     * @param dialog dialog
     */
    override fun onCancel(dialog: DialogInterface) {
        Logger.d("BaseDialogFragment onCancel")
        if (cancelClickListener != null) {
            cancelClickListener!!.onClick(null)
        } else if (okClickListener != null) {
            okClickListener!!.onClick(null)
        }
        super.onCancel(dialog)
    }

    fun addToDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
}
