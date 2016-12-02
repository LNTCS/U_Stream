package kr.edcan.u_stream.adpater

import android.databinding.ObservableArrayList
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.nitrico.lastadapter.LastAdapter
import io.karim.MaterialTabs
import kr.edcan.u_stream.Application.Companion.realm
import kr.edcan.u_stream.BR
import kr.edcan.u_stream.PlaylistActivity
import kr.edcan.u_stream.R
import kr.edcan.u_stream.databinding.ItemPlaylistGridBinding
import kr.edcan.u_stream.model.PlaylistData
import kr.edcan.u_stream.model.RM_MusicData
import kr.edcan.u_stream.model.RM_PlayListData
import kr.edcan.u_stream.utils.DialogUtil
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity


/**
 * Created by LNTCS on 2016-03-11.
 */
class MainPagerAdapter(internal var mContext: android.content.Context) : android.support.v4.view.PagerAdapter() {

    private val mInflater: android.view.LayoutInflater
    private val analogPagerAdapter: AnalogPagerAdapter
    private var playlistAdapter: LastAdapter
    private var playlists = ObservableArrayList<PlaylistData>()

    init {
        mInflater = android.view.LayoutInflater.from(mContext)
        analogPagerAdapter = AnalogPagerAdapter(mContext)
        playlistAdapter = LastAdapter.with(playlists, BR.item)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        var v: View? = null
        when (position) {
            0 -> v = mInflater.inflate(R.layout.content_main_space, null)
            1 -> {
                v = mInflater.inflate(R.layout.content_main_playlist, null)
                var recycler = v.find<RecyclerView>(R.id.playlistRecycler)
                GridLayoutManager(mContext, 2).let {
                    recycler.layoutManager = it
                }
                playlistAdapter
                        .map<PlaylistData, ItemPlaylistGridBinding>(R.layout.item_playlist_grid) {
                            onClick { mContext.startActivity<PlaylistActivity>("title" to binding.item.title, "id" to binding.item.id) }
                            onLongClick {
                                DialogUtil.editPlayListDialog(mContext, binding.item)
                            }
                        }
                        .into(recycler)
                var results = realm.where(RM_PlayListData::class.java).findAll()

                for (res in results) {
                    var musics = realm.where(RM_MusicData::class.java).equalTo("playListId", res.id).findAll()
                    PlaylistData(res.id, res.title, musics.size, musics[(Math.random() * musics.size).toInt()].thumbnail).let {
                        playlists.add(it)
                    }
                }
            }
            2 -> {
                v = mInflater.inflate(R.layout.content_main_analog, null)
                val analogTab = v.find<MaterialTabs>(R.id.analogTab)
                val analogPager = v.find<ViewPager>(R.id.analogPager)
                v.find<TextView>(R.id.analogTitle)
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
