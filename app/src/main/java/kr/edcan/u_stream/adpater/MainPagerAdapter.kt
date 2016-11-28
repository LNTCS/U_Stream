package kr.edcan.u_stream.adpater

import android.view.View
import android.widget.LinearLayout

/**
 * Created by LNTCS on 2016-03-11.
 */
class MainPagerAdapter(internal var mContext: android.content.Context) : android.support.v4.view.PagerAdapter() {
    private val mInflater: android.view.LayoutInflater

    init {
        mInflater = android.view.LayoutInflater.from(mContext)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun instantiateItem(pager: android.view.View?, position: Int): Any {
        var v: View? = null
        when (position) {
//            0 -> v = mInflater.inflate(R.layout., null)
//            1 -> v = mInflater.inflate(R.layout., null)
//            2 -> v = mInflater.inflate(R.layout., null)
        }
        v = LinearLayout(mContext)
        (pager as android.support.v4.view.ViewPager).addView(v, null)
        return v!!
    }

    override fun destroyItem(pager: android.view.View?, position: Int, view: Any?) {
        (pager as android.support.v4.view.ViewPager).removeView(view as android.view.View?)
    }

    override fun isViewFromObject(v: android.view.View, obj: Any): Boolean {
        return v === obj
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }
}