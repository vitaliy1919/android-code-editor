package com.example.myapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.room.entities.TabData

class TabsAdapter: RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {
    val initialTab = TabData("Untitled")
    var tabsNames: ArrayList<TabData> = ArrayList(arrayListOf(initialTab))
    var onItemChangeListeners: ArrayList<OnItemChange> = ArrayList()
    var activePosition = 0;


    fun setNewDataSet(dataset: ArrayList<TabData>) {
        if (dataset.isEmpty()) {
            Log.d("Adapter","A new dataset is empty")
            tabsNames = ArrayList(arrayListOf(initialTab))
            return
        }
        tabsNames = dataset
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.tab_item, parent, false)
        return TabViewHolder(rootView, this)
    }

    override fun getItemCount(): Int {
        return tabsNames.size
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.active = (position == activePosition)
        holder.title.text = tabsNames[position].fileName
    }

    fun addTab(name: TabData) {
        tabsNames.add(name)
        notifyItemInserted(tabsNames.size - 1)
    }

    fun addOnItemListener(listener: OnItemChange) {
        onItemChangeListeners.add(listener)
    }

    fun unregisterListener(listener: OnItemChange) {
        onItemChangeListeners.remove(listener)
    }

    fun get(position: Int): TabData {
        return tabsNames[position]
    }
    fun setActive(position: Int) {
        for (listener in onItemChangeListeners)
            listener.beforeItemActive(activePosition, position, false)
        val prevPosition = activePosition
        activePosition = position
        notifyDataSetChanged()

    }
    fun getActive(): Int {
        return activePosition
    }
    fun removeTab(index: Int) {
        for (listener in onItemChangeListeners)
            listener.beforeItemClosed(index)
        if (tabsNames.size == 1) {
            tabsNames[0] = initialTab
            notifyItemChanged(0);
            return
        }
        for (listener in onItemChangeListeners)
            listener.beforeItemActive(activePosition, index - 1, true)
        tabsNames.removeAt(index)
        if (index == activePosition)
            activePosition = index - 1
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, tabsNames.size);
        notifyDataSetChanged()


    }

    interface OnItemChange {
        fun beforeItemClosed(position: Int)
        fun beforeItemActive(prevPosition: Int, position: Int, tabClosed: Boolean)
    }

    class TabViewHolder(var view: View, var adapter: TabsAdapter): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val activeView: View = view.findViewById(R.id.active_status)
        val close: ImageView = view.findViewById(R.id.close_tab)
        var active = false
            get() = field
            set(value) {
                field = value
                if (value)
                    activeView.visibility = View.VISIBLE
                else
                    activeView.visibility = View.GONE
            }
        init {
            view.setOnClickListener {
                adapter.setActive(adapterPosition)
            }
            close.setOnClickListener {
                adapter.removeTab(adapterPosition)
            }
        }

    }
}