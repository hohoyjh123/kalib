package com.yesjnet.gwanak.ui.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.extension.gone

/**
 * base ListAdapter
 *
 * @param T model
 */
abstract class BaseListAdapter<T>(diffUtil: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, RecyclerView.ViewHolder>(diffUtil) {
    abstract fun onCreateViewHolderImpl(
        parent: ViewGroup,
        adapter: BaseListAdapter<T>,
        viewType: Int
    ): RecyclerView.ViewHolder

    abstract fun onBindViewHolderImpl(
        viewHolder: RecyclerView.ViewHolder,
        adapter: BaseListAdapter<T>,
        position: Int
    )

    abstract fun getItemViewTypeImpl(
        position: Int
    ): Int

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return try {
            // ViewHolder Create Abstract
            onCreateViewHolderImpl(parent, this, viewType)
        } catch (e: Exception) {
            e.printStackTrace()
            BaseRecyclerViewAdapter.BaseViewHolder(View(parent.context))
        }
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            onBindViewHolderImpl(holder, this, position)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.d("onBindViewHolder gone")
            holder.itemView.gone()
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return try {
            getItemViewTypeImpl(position)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getItemCount() = currentList.size

    fun addData(data: T, isFirst: Boolean = true) {
        val newList = currentList.toMutableList()
        if (isFirst) {
            newList.add(0, data)
            submitList(newList)
        } else {
            newList.add(data)
            submitList(newList)
        }
    }

    fun removeData(data: T) {
        val newList = currentList.toMutableList()
        val index = newList.indexOf(data)
        if (index != -1) {
            newList.remove(data)
            submitList(newList)
        }
    }

}