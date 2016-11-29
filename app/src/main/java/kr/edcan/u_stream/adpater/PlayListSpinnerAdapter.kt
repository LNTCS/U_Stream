package kr.edcan.u_stream.adpater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kr.edcan.u_stream.R
import java.util.*

/**
 * Created by LNTCS on 2016-03-18.
 */

class PlayListSpinnerAdapter(internal var mContext: Context) : BaseAdapter() {
    private val mItems = ArrayList<String>()
    internal var mInflater: LayoutInflater

    init {
        this.mInflater = mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun clear() {
        mItems.clear()
    }

    fun addItem(yourObject: String) {
        mItems.add(yourObject)
    }

    fun addItems(yourObjectList: ArrayList<String>) {
        mItems.addAll(yourObjectList)
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun getItem(position: Int): Any {
        return mItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null || view.tag.toString() != "DROPDOWN") {
            view = mInflater.inflate(R.layout.item_spinner_dropdown, parent, false)
            view!!.tag = "DROPDOWN"
        }
        val textView = view.findViewById(android.R.id.text1) as TextView
        textView.text = getString(position)
        return view
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null || view.tag.toString() != "NON_DROPDOWN") {
            view = mInflater.inflate(R.layout.item_spinner_dropdown, parent, false)
            view!!.tag = "NON_DROPDOWN"
        }
        val textView = view.findViewById(android.R.id.text1) as TextView
        textView.text = getString(position)
        return view
    }

    private fun getString(position: Int): String {
        return if (position >= 0 && position < mItems.size) mItems[position] else ""
    }
}
