package com.yesjnet.gwanak.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.databinding.DialogCustomBinding
import com.yesjnet.gwanak.extension.gone
import com.yesjnet.gwanak.extension.show
import com.yesjnet.gwanak.ui.base.BaseDialogFragment

/**
 *
 * Description: 커스텀 다이얼로그
 */
class CustomDialog(
    private var cancelLable: Boolean,
    private var title: String,
    private var message: String,
    private var desc: String
) :
    BaseDialogFragment(), View.OnClickListener {
    private lateinit var binding: DialogCustomBinding
    private var hasOKButton = false
    private var hasCancelButton = false
    private var cancel: String? = null
    private var ok: String? = null
    private var cancelTextColor = -1
    private var okTextColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_custom, container, false)
        binding.dialog = this
        return binding.root
    }

    override fun onViewCreated(contentView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(contentView, savedInstanceState)

        // 타이틀
        if (!title.isNullOrBlank()) {
            binding.tvTitle.text = title
            binding.tvTitle.show()
        } else {
            binding.tvTitle.gone()
        }

        // 메시지
        if (!message.isNullOrBlank()) {
            binding.tvMessage.text = message
            binding.tvMessage.show()
        } else {
            binding.tvMessage.gone()
        }


        // 디스크립션
        if (!desc.isNullOrBlank()) {
            binding.tvDesc.text = desc
            binding.tvDesc.show()
        } else {
            binding.tvDesc.gone()
        }

        // 버튼
        if (!cancel.isNullOrBlank()) {
            if (cancelTextColor != -1) binding.btCancel.setTextColor(cancelTextColor)
            binding.btCancel.text = cancel
        }

        // 취소 버튼
        if (hasCancelButton) {
            binding.btCancel.show()
            binding.viewMargin.show()
        } else {
            binding.btCancel.gone()
            binding.viewMargin.gone()
        }

        // ok 버튼
        if (!ok.isNullOrBlank()) {
            binding.btOk.text = ok
            if (okTextColor != -1) binding.btOk.setTextColor(okTextColor)
        }

        if (hasOKButton) {
            binding.btOk.show()
        } else {
            binding.btOk.gone()
        }

        // 영역외 터치
        isCancelable = cancelLable

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btCancel -> {
                cancelClickListener?.onClick(null)
                dismiss()
            }
            R.id.btOk -> {
                okClickListener?.onClick(null)
                dismiss()
            }
            else -> {
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        Logger.d("CustomDialog onCancel")
        if (hasCancelButton) cancelClickListener?.onClick(null) else okClickListener?.onClick(null)
        //        super.onCancel(dialog);
    }

    fun setCancel(cancelable: Boolean) {
        this.cancelLable = cancelable
    }

    fun setCancelBtn(cancel: String?, listener: MyOnClickListener?) {
        if (!TextUtils.isEmpty(cancel)) this.cancel = cancel
        if (listener != null) this.cancelClickListener = listener
    }

    fun setOkBtn(ok: String?, listener: MyOnClickListener?) {
        if (!TextUtils.isEmpty(ok)) this.ok = ok
        if (listener != null) this.okClickListener = listener
        hasOKButton = true
    }

    fun setCancelBtn(
        cancel: String?,
        cancelTextColor: Int,
        listener: MyOnClickListener?
    ) {
        if (!TextUtils.isEmpty(cancel)) this.cancel = cancel
        if (listener != null) this.cancelClickListener = listener
        this.cancelTextColor = cancelTextColor
        hasCancelButton = true
    }

    fun setOkBtn(ok: String?, okTextColor: Int, listener: MyOnClickListener?) {
        if (!TextUtils.isEmpty(ok)) this.ok = ok
        if (listener != null) this.okClickListener = listener
        this.okTextColor = okTextColor
        hasOKButton = true
    }

    companion object {
        fun newInstance(
            cancelLable: Boolean = true,
            title: String,
            message: String,
            desc: String
        ): CustomDialog {
            return CustomDialog(cancelLable, title, message, desc)
        }
    }
}