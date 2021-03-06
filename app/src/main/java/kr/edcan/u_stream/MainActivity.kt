package kr.edcan.u_stream

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kr.edcan.u_stream.adpater.MainPagerAdapter
import kr.edcan.u_stream.model.MusicData
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*

class MainActivity : BaseActivity() {
    override var viewId: Int = R.layout.activity_main
    override var toolbarId: Int = 0

    var pAdapter: MainPagerAdapter? = null
    var mainTabs = ArrayList<TextView>()

    override fun onCreate() {
        toolbarTitle.text = "플레이보드"

        mainTabs = arrayListOf(tabSpace, tabPlaylist, tabAnalog)

        PlayService.btmPlaying = playingBtn
        playingBtn.onClick {
            PlayService.playORpause()
        }

        pAdapter = MainPagerAdapter(this)
        mainPager.run {
            adapter = pAdapter
            offscreenPageLimit = 5
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    val param = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, position + positionOffset)
                    tabMargin.layoutParams = param
                    mainTabs.forEach {
                        it.setTextColor(ContextCompat.getColorStateList(this@MainActivity, R.color.selector_primary_color))
                    }
                    mainTabs[Math.round(position + positionOffset)].textColor =
                            ContextCompat.getColor(this@MainActivity, R.color.colorPrimary)
                }

                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageSelected(position: Int) {}
            })
        }
        PlayService.addTitleView(playingTitle)
        PlayService.addUploaderView(playingSubtitle)

        if(PlayService.nowPlaying.isNull) {
            val latest = Gson().fromJson<MusicData>(Pref.getString("latestPlay"), MusicData::class.java)
            if (latest != null && !latest.isNull) {
                PlayService.nowPlaying = latest
                PlayUtil.setPlayingList(PlayService.nowPlaying)
                PlayUtil.startService(this, PlayService.ACTION_INIT)
            }
        }

        mainBtmTab.onClick { startActivity<PlayerActivity>() }
        toolbarSearch.onClick { startActivity<SearchActivity>() }
        mainTabs.forEachIndexed { i, it -> it.onClick { mainPager.setCurrentItem(i, true) } }
    }

    override fun onResume() {
        super.onResume()
        playingBtn.setImageResource(if (PlayService.mediaPlayer.isPlaying) R.drawable.ic_btm_pause else R.drawable.ic_btm_play)
        pAdapter?.run { notifyDataSetChanged() }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base))
    }
}
