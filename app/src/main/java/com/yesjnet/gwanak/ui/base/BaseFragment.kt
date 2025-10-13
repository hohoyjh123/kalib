package com.yesjnet.gwanak.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.yesjnet.gwanak.data.model.eventbus.EBMainPageEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * base fragment
 */
abstract class BaseFragment<B : ViewDataBinding>(@LayoutRes private val layoutResId: Int) : Fragment(layoutResId) {

    abstract fun onInitView()
    abstract fun onSubscribeUI()

//    private var page: EnumApp.MainPage? = null
    private val compositeDisposable = CompositeDisposable()

    protected lateinit var binding: B
        private set

//    fun setPage(page: EnumApp.MainPage) {
//        this.page = page
//    }
//
//    fun getPage(): EnumApp.MainPage? {
//        return this.page
//    }

    open fun onEventMainPage(mainPageEvent: EBMainPageEvent){ }
    open fun onEventCardItemRefresh(){ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    fun addToDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ViewBinding 해제
        if (this::binding.isInitialized) {
            // ViewBinding 메모리 해제
            binding.unbind()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}