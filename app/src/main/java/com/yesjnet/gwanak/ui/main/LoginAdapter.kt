package com.yesjnet.gwanak.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.yesjnet.gwanak.data.model.Family
import com.yesjnet.gwanak.databinding.RowMembershipBinding
import com.yesjnet.gwanak.ui.base.BaseRecyclerViewAdapter

/**
 * 모바일 회원증 어댑터
 */
class LoginAdapter (
    val viewModel: LoginViewModel?,
    val lifecycleOwner: LifecycleOwner
) :
    BaseRecyclerViewAdapter<Family>(){

    override fun getItemViewTypeImpl(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolderImpl(
        parent: ViewGroup,
        adapter: BaseRecyclerViewAdapter<Family>,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val binding: RowMembershipBinding = RowMembershipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.viewModel = viewModel
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolderImpl(
        viewHolder: RecyclerView.ViewHolder,
        adapter: BaseRecyclerViewAdapter<Family>,
        position: Int
    ) {

        val cert = getData()[position]
        if (viewHolder is ItemViewHolder) {
            viewHolder.onbind(cert,position)
        }
    }

    inner class ItemViewHolder(val binding: RowMembershipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun onbind(item: Family, position: Int) {
            binding.item = item
            binding.pos = position

            binding.executePendingBindings()
        }
    }

}