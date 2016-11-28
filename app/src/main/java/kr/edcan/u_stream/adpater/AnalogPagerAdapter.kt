package kr.edcan.u_stream.adpater

import android.view.ViewGroup
import kr.edcan.u_stream.R

/**
 * Created by LNTCS on 2016-03-11.
 */
class AnalogPagerAdapter(internal var mContext: android.content.Context) : android.support.v4.view.PagerAdapter() {

    internal var titles = arrayOf("재생목록", "음악목록")
    private val mInflater: android.view.LayoutInflater

    init {
        mInflater = android.view.LayoutInflater.from(mContext)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val v = mInflater.inflate(R.layout.item_analog_list, null)
        when (position) {
            0 -> {
            }
            1 -> {
            }
        }
        container!!.addView(v, null)
        return v
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    override fun destroyItem(container: ViewGroup?, position: Int, view: Any?) {
        container!!.removeView(view as android.view.View?)
    }

    override fun isViewFromObject(v: android.view.View, obj: Any): Boolean {
        return v === obj
    }
}
