package com.yesjnet.gwanak.ui.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.extension.gone
import java.util.*

/**
 * base recyclerViewAdapter
 */

abstract class BaseRecyclerViewAdapter<T> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var dataList: MutableList<T> = mutableListOf()
    protected var hasMore = false
    protected var page = 1

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
    }

    abstract fun onCreateViewHolderImpl(
        parent: ViewGroup,
        adapter: BaseRecyclerViewAdapter<T>,
        viewType: Int
    ): RecyclerView.ViewHolder

    abstract fun onBindViewHolderImpl(
        viewHolder: RecyclerView.ViewHolder,
        adapter: BaseRecyclerViewAdapter<T>,
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
            BaseViewHolder(View(parent.context))
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

    override fun getItemCount() = dataList.size

    final override fun getItemViewType(position: Int): Int {
        return try {
            getItemViewTypeImpl(position)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    open fun setData(data: List<T>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    open fun setDataNullable(data: List<T>?) {
        dataList.clear()
        data?.let {  dataList.addAll(it) }
        notifyDataSetChanged()
    }

    open fun setNextPage(hasMore: Boolean, page: Int) {
        Logger.d("hasMore = $hasMore page = $page")
        this.hasMore = hasMore
        this.page = page
    }

    fun getData(): List<T> {
        return dataList
    }

    fun addDataAll(data: List<T>) {
        val startIndex = dataList.size

        dataList.addAll(data)
        Logger.d("addData startIndex = $startIndex baseRecy size = ${dataList.size}")
        notifyItemRangeInserted(startIndex, data.size)
    }

    fun addData(data: T, isFirst: Boolean = true) {
        if (isFirst) {
            dataList.add(0,data)
            Logger.d("addData first size = ${dataList.size}")
            notifyItemInserted(0)
        } else {
            dataList.add(data)
            Logger.d("addData size = ${dataList.size}")
            notifyItemInserted(dataList.size-1)
        }
    }

    fun removeData(data: T) {
        val index = dataList.indexOf(data)
        if (index != -1) {
            dataList.remove(data)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, dataList.size)
        }
    }

    open fun sortData(comparator: Comparator<in T?>) {
        Collections.sort(dataList, comparator)
        notifyItemRangeChanged(0, itemCount)
    }

    class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)
}