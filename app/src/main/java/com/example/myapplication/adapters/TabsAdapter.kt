package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class TabsAdapter: RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {
    var tabsNames: ArrayList<String> = ArrayList(arrayListOf("Untitled"))
    var onItemChangeListeners: ArrayList<OnItemChange> = ArrayList()
    var activePosition = 0;
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.tab_item, parent, false)
        return TabViewHolder(rootView, this)
    }

    override fun getItemCount(): Int {
        return tabsNames.size
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.active = (position == activePosition)
        holder.title.text = tabsNames[position]
    }

    fun addTab(name: String) {
        tabsNames.add(name)
        notifyItemInserted(tabsNames.size - 1)
    }

    fun addOnItemListener(listener: OnItemChange) {
        onItemChangeListeners.add(listener)
    }

    fun removeTab(index: Int) {
        if (tabsNames.size == 1) {
            tabsNames[0] = "Untitled"
            notifyItemChanged(0);
            return
        }
        tabsNames.removeAt(index)
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, tabsNames.size);
        notifyDataSetChanged()
    }

    interface OnItemChange {
        fun onItemClosed(position: Int)
        fun onItemActive(position: Int)
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
                adapter.activePosition = adapterPosition
                adapter.notifyDataSetChanged()
            }
            close.setOnClickListener {
                adapter.removeTab(adapterPosition)
            }
        }

    }
}