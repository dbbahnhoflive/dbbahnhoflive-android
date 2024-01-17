/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R

abstract class RecyclerFragment<A : RecyclerView.Adapter<*>?>(@param:LayoutRes private val layout: Int) :
    Fragment() {
    protected var recyclerView: RecyclerView? = null
        private set
    var adapter: A? = null
        private set

    @JvmField
    protected val titleResourceLiveData = MutableLiveData<Int>()
    private val titleLiveData: MutableLiveData<CharSequence>

    init {
        val titleMediatorLiveData = MediatorLiveData<CharSequence>()
        titleMediatorLiveData.addSource(viewLifecycleOwnerLiveData.switchMap { lifecycleOwner: LifecycleOwner? ->
            if (lifecycleOwner == null) {
                return@switchMap null
            }
            titleResourceLiveData.map { resId: Int -> getText(resId) }
        }) { title: CharSequence? ->
            title?.let {
                titleMediatorLiveData.value = it
            }
        }
        titleLiveData = titleMediatorLiveData
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(layout, container, false)
        titleLiveData.observe(viewLifecycleOwner) { title: CharSequence? ->
            ToolbarViewHolder(view).setTitle(
                title
            )
        }
        recyclerView = view.findViewById(R.id.recycler)
        prepareRecycler(inflater, recyclerView)
        applyAdapter()
        return view
    }

    protected open fun prepareRecycler(inflater: LayoutInflater?, recyclerView: RecyclerView?) {}
    protected fun setTitle(@StringRes title: Int) {
        titleResourceLiveData.value = title
    }

    protected fun setTitle(title: CharSequence) {
        titleLiveData.value = title
    }

    override fun onDestroyView() {
        recyclerView = null
        super.onDestroyView()
    }

    private fun applyAdapter() {
        if (recyclerView != null && adapter != null) {
            recyclerView!!.adapter = adapter
        }
    }

    protected fun setAdapter(adapter: A) {
        this.adapter = adapter
        applyAdapter()
    }
}
