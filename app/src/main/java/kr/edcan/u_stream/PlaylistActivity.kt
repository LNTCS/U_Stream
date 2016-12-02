package kr.edcan.u_stream

import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.github.nitrico.lastadapter.LastAdapter
import kotlinx.android.synthetic.main.activity_playlist.*
import kotlinx.android.synthetic.main.toolbar_playlist.*
import kr.edcan.u_stream.Application.Companion.realm
import kr.edcan.u_stream.model.MusicData
import kr.edcan.u_stream.model.RM_MusicData
import org.jetbrains.anko.onClick


class PlaylistActivity : AppCompatActivity() {

    var musicList = ObservableArrayList<MusicData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        toolbarTitle.text = intent.getStringExtra("title")

        LinearLayoutManager(this).let {
            it.orientation = LinearLayoutManager.VERTICAL
            playlistRecycler.layoutManager = it
        }

        LastAdapter.with(musicList, BR.item)
                .map<MusicData>(R.layout.item_playlist_list)
                .into(playlistRecycler)

        for (data in realm.where(RM_MusicData::class.java).equalTo("playListId", intent.getIntExtra("id", 0)).findAll()) {
            musicList.add(MusicData(data))
        }

        toolbarBack.onClick { onBackPressed() }
    }
}
