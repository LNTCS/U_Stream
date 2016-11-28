package kr.edcan.u_stream.adpater

import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import io.karim.MaterialTabs
import kr.edcan.u_stream.R
import org.jetbrains.anko.find

/**
 * Created by LNTCS on 2016-03-11.
 */
class MainPagerAdapter(internal var mContext: android.content.Context) : android.support.v4.view.PagerAdapter() {
    private val mInflater: android.view.LayoutInflater
    private val analogPagerAdapter: AnalogPagerAdapter

    init {
        mInflater = android.view.LayoutInflater.from(mContext)
        analogPagerAdapter = AnalogPagerAdapter(mContext)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        var v: View? = null
        when (position) {
            0 -> v = mInflater.inflate(R.layout.content_main_space, null)
            1 -> v = mInflater.inflate(R.layout.content_main_playlist, null)
            2 -> {
                v = mInflater.inflate(R.layout.content_main_analog, null)
                val analogTab = v.find<MaterialTabs>(R.id.analogTab)
                val analogPager = v.find<ViewPager>(R.id.analogPager)
                analogPager.adapter = analogPagerAdapter
                analogTab.setViewPager(analogPager)
            }
        }
        container!!.addView(v, null)
        return v!!
    }

    override fun destroyItem(container: ViewGroup?, position: Int, view: Any?) {
        container!!.removeView(view as android.view.View?)
    }

    override fun isViewFromObject(v: android.view.View, obj: Any): Boolean {
        return v === obj
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }
}