package kr.edcan.u_stream

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kr.edcan.u_stream.adpater.MainPagerAdapter
import org.jetbrains.anko.textColor
import java.util.*

class MainActivity : AppCompatActivity() {

    var pAdapter: MainPagerAdapter? = null
    var mainTabs = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbarTitle.text = "플레이보드"

        mainTabs = arrayListOf(tabSpace, tabPlaylist, tabAnalog)

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
                override fun onPageScrollStateChanged(state: Int) { }
                override fun onPageSelected(position: Int) { }
            })
        }
    }


    override fun onResume() {
        super.onResume()
        pAdapter?.let (MainPagerAdapter::notifyDataSetChanged)
    }
}
