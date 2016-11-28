package kr.edcan.u_stream.adpater

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.github.nitrico.lastadapter.LastAdapter
import kr.edcan.u_stream.BR
import kr.edcan.u_stream.R
import kr.edcan.u_stream.model.SearchData

/**
 * Created by LNTCS on 2016-03-11.
 */
class SearchResultPagerAdapter(internal var mContext: android.content.Context, musicData: java.util.ArrayList<kr.edcan.u_stream.model.SearchData>, listData: java.util.ArrayList<kr.edcan.u_stream.model.SearchData>) : android.support.v4.view.PagerAdapter() {

    internal var titles = arrayOf("음악", "재생목록")
    internal var musicObject = java.util.ArrayList<kr.edcan.u_stream.model.SearchData>()
    internal var listObject = java.util.ArrayList<kr.edcan.u_stream.model.SearchData>()

    internal var searchResultMusicAdapter: LastAdapter
    internal var searchResultListAdapter: LastAdapter

    init {
        this.musicObject = musicData
        this.listObject = listData
        this.searchResultMusicAdapter = LastAdapter.with(musicObject, BR.item)
                .map<SearchData>(R.layout.item_search_result)
        this.searchResultListAdapter = LastAdapter.with(listObject, BR.item)
                .map<SearchData>(R.layout.item_search_result)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val recyclerView = RecyclerView(mContext)

        LinearLayoutManager(mContext).let {
            it.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = it
        }

        when (position) {
            0 -> searchResultMusicAdapter.into(recyclerView)
            1 -> searchResultListAdapter.into(recyclerView)
        }

        container!!.addView(recyclerView, null)
        return recyclerView
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

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        searchResultMusicAdapter.notifyDataSetChanged()
        searchResultListAdapter.notifyDataSetChanged()
    }
}
